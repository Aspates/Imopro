package com.imopro.ui;

import com.imopro.application.PropertyService;
import com.imopro.domain.Property;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.math.BigDecimal;
import java.util.List;

public class PropertyViewModel {
    private final PropertyService propertyService;
    private final ObservableList<Property> properties = FXCollections.observableArrayList();
    private final FilteredList<Property> filteredProperties = new FilteredList<>(properties, p -> true);
    private final ObjectProperty<Property> selectedProperty = new SimpleObjectProperty<>();

    private final StringProperty searchQuery = new SimpleStringProperty("");
    private final StringProperty title = new SimpleStringProperty("");
    private final StringProperty address = new SimpleStringProperty("");
    private final StringProperty city = new SimpleStringProperty("");
    private final StringProperty postalCode = new SimpleStringProperty("");
    private final StringProperty propertyType = new SimpleStringProperty("");
    private final StringProperty surface = new SimpleStringProperty("");
    private final StringProperty rooms = new SimpleStringProperty("");
    private final StringProperty price = new SimpleStringProperty("");
    private final StringProperty status = new SimpleStringProperty("Lead");

    public PropertyViewModel(PropertyService propertyService) {
        this.propertyService = propertyService;
        loadProperties();
        selectedProperty.addListener((obs, oldValue, newValue) -> populateFields(newValue));
        searchQuery.addListener((obs, oldValue, newValue) -> applyFilter(newValue));
    }

    public ObservableList<Property> getProperties() { return filteredProperties; }
    public ObjectProperty<Property> selectedPropertyProperty() { return selectedProperty; }
    public StringProperty searchQueryProperty() { return searchQuery; }
    public StringProperty titleProperty() { return title; }
    public StringProperty addressProperty() { return address; }
    public StringProperty cityProperty() { return city; }
    public StringProperty postalCodeProperty() { return postalCode; }
    public StringProperty propertyTypeProperty() { return propertyType; }
    public StringProperty surfaceProperty() { return surface; }
    public StringProperty roomsProperty() { return rooms; }
    public StringProperty priceProperty() { return price; }
    public StringProperty statusProperty() { return status; }

    public void loadProperties() {
        List<Property> items = propertyService.listProperties();
        properties.setAll(items);
    }

    public void createProperty() {
        Property p = Property.newProperty();
        properties.add(0, p);
        selectedProperty.set(p);
    }

    public void saveSelected() {
        Property p = selectedProperty.get();
        if (p == null) return;

        p.setTitle(title.get());
        p.setAddress(address.get());
        p.setCity(city.get());
        p.setPostalCode(postalCode.get());
        p.setPropertyType(propertyType.get());
        p.setSurface(parseDouble(surface.get()));
        p.setRooms(parseInteger(rooms.get()));
        p.setPrice(parseBigDecimal(price.get()));
        p.setStatus(status.get());
        p.touchUpdatedAt();

        propertyService.save(p);
        loadProperties();
        selectedProperty.set(p);
    }

    public void deleteSelected() {
        Property p = selectedProperty.get();
        if (p == null) return;
        propertyService.delete(p.getId());
        properties.remove(p);
        selectedProperty.set(null);
    }

    public String display(Property p) {
        return p.displayTitle();
    }

    public javafx.beans.binding.BooleanBinding canSaveBinding() {
        return Bindings.createBooleanBinding(() -> selectedProperty.get() != null, selectedProperty);
    }

    private void populateFields(Property p) {
        if (p == null) {
            title.set("");
            address.set("");
            city.set("");
            postalCode.set("");
            propertyType.set("");
            surface.set("");
            rooms.set("");
            price.set("");
            status.set("Lead");
            return;
        }

        title.set(nullToEmpty(p.getTitle()));
        address.set(nullToEmpty(p.getAddress()));
        city.set(nullToEmpty(p.getCity()));
        postalCode.set(nullToEmpty(p.getPostalCode()));
        propertyType.set(nullToEmpty(p.getPropertyType()));
        surface.set(p.getSurface() == null ? "" : p.getSurface().toString());
        rooms.set(p.getRooms() == null ? "" : p.getRooms().toString());
        price.set(p.getPrice() == null ? "" : p.getPrice().toPlainString());
        status.set(nullToEmpty(p.getStatus()));
    }

    private void applyFilter(String query) {
        String q = query == null ? "" : query.toLowerCase();
        filteredProperties.setPredicate(p -> {
            if (q.isBlank()) return true;
            return contains(p.getTitle(), q)
                    || contains(p.getAddress(), q)
                    || contains(p.getCity(), q)
                    || contains(p.getStatus(), q);
        });
    }

    private boolean contains(String source, String q) {
        return source != null && source.toLowerCase().contains(q);
    }

    private String nullToEmpty(String v) { return v == null ? "" : v; }
    private Double parseDouble(String v) { try { return (v == null || v.isBlank()) ? null : Double.parseDouble(v); } catch (Exception e) { return null; } }
    private Integer parseInteger(String v) { try { return (v == null || v.isBlank()) ? null : Integer.parseInt(v); } catch (Exception e) { return null; } }
    private BigDecimal parseBigDecimal(String v) { try { return (v == null || v.isBlank()) ? null : new BigDecimal(v); } catch (Exception e) { return null; } }
}
