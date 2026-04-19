package com.imopro.ui;

import com.imopro.application.PipelineService;
import com.imopro.application.PropertyService;
import com.imopro.application.RentService;
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
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

public class PropertyViewModel {
    private final PropertyService propertyService;
    private final RentService rentService;
    private final PropertyDefaultsStore defaultsStore;
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
    private final StringProperty status = new SimpleStringProperty("Prospect locataire");
    private final StringProperty estimatedRentLabel = new SimpleStringProperty("");
    private final ObservableList<String> availableStatuses = FXCollections.observableArrayList();

    public PropertyViewModel(PropertyService propertyService, PipelineService pipelineService, PropertyDefaultsStore defaultsStore, RentService rentService) {
        this.propertyService = propertyService;
        this.rentService = rentService;
        this.defaultsStore = defaultsStore;
        availableStatuses.setAll(pipelineService.listStages().stream().map(stage -> stage.name()).collect(Collectors.toList()));
        if (!availableStatuses.isEmpty()) {
            status.set(availableStatuses.get(0));
        }
        loadProperties();
        selectedProperty.addListener((obs, oldValue, newValue) -> populateFields(newValue));
        searchQuery.addListener((obs, oldValue, newValue) -> applyFilter(newValue));
        surface.addListener((obs, oldValue, newValue) -> recomputeEstimatedRentLabel());
        recomputeEstimatedRentLabel();
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
    public StringProperty estimatedRentLabelProperty() { return estimatedRentLabel; }
    public ObservableList<String> availableStatuses() { return availableStatuses; }

    public void loadProperties() {
        List<Property> items = propertyService.listProperties();
        properties.setAll(items);
    }

    public void createProperty() {
        Property p = Property.newProperty();
        PropertyDefaultsStore.PropertyDefaults defaults = defaultsStore.load();
        p.setAddress(defaults.address());
        p.setCity(defaults.city());
        p.setPostalCode(defaults.postalCode());
        p.setPropertyType(defaults.propertyType());
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

    public void selectById(UUID id) {
        if (id == null) return;
        properties.stream().filter(p -> p.getId().equals(id)).findFirst().ifPresent(selectedProperty::set);
    }

    public UUID selectedPropertyRentId() {
        Property p = selectedProperty.get();
        if (p == null) return null;
        return rentService.listRents().stream()
                .filter(r -> p.getId().equals(r.getPropertyId()))
                .map(r -> r.getId())
                .findFirst()
                .orElse(null);
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
            status.set(availableStatuses.isEmpty() ? "" : availableStatuses.get(0));
            recomputeEstimatedRentLabel();
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
        String currentStatus = nullToEmpty(p.getStatus());
        status.set(currentStatus);
        if (!currentStatus.isBlank() && !availableStatuses.contains(currentStatus)) {
            availableStatuses.add(currentStatus);
        }
        recomputeEstimatedRentLabel();
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

    private void recomputeEstimatedRentLabel() {
        String rentPerSquareMeterRaw = defaultsStore.load().rentPerSquareMeter();
        BigDecimal rentPerSquareMeter = parseBigDecimal(rentPerSquareMeterRaw);
        if (rentPerSquareMeter == null) {
            estimatedRentLabel.set("Renseignez \"Loyer au m\u00B2\" dans Param\u00E8tres > Valeurs par d\u00E9faut.");
            return;
        }

        BigDecimal habitableSurface = parseBigDecimal(surface.get());
        if (habitableSurface == null) {
            estimatedRentLabel.set("Loyer estim\u00E9 : -");
            return;
        }

        BigDecimal estimatedRent = habitableSurface.multiply(rentPerSquareMeter).setScale(2, RoundingMode.HALF_UP);
        estimatedRentLabel.set("Loyer estim\u00E9 : " + formatFrenchDecimal(estimatedRent) + " \u20AC");
    }

    private String formatFrenchDecimal(BigDecimal value) {
        DecimalFormat formatter = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.FRANCE);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return formatter.format(value);
    }

    private String nullToEmpty(String v) { return v == null ? "" : v; }

    private Double parseDouble(String v) {
        try {
            if (v == null || v.isBlank()) return null;
            return Double.parseDouble(v.trim().replace(',', '.'));
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseInteger(String v) { try { return (v == null || v.isBlank()) ? null : Integer.parseInt(v); } catch (Exception e) { return null; } }

    private BigDecimal parseBigDecimal(String v) {
        try {
            if (v == null || v.isBlank()) return null;
            return new BigDecimal(v.trim().replace(',', '.'));
        } catch (Exception e) {
            return null;
        }
    }
}
