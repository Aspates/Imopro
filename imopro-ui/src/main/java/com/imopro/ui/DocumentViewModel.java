package com.imopro.ui;

import com.imopro.application.DocumentService;
import com.imopro.domain.DocumentItem;
import com.imopro.infra.LocalDocumentStorage;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class DocumentViewModel {
    private final DocumentService documentService;
    private final LocalDocumentStorage storage;
    private final ObservableList<DocumentItem> documents = FXCollections.observableArrayList();
    private final FilteredList<DocumentItem> filteredDocuments = new FilteredList<>(documents, d -> true);
    private final ObjectProperty<DocumentItem> selectedDocument = new SimpleObjectProperty<>();

    private final StringProperty searchQuery = new SimpleStringProperty("");
    private final StringProperty fileName = new SimpleStringProperty("");
    private final StringProperty relativePath = new SimpleStringProperty("");
    private final StringProperty mimeType = new SimpleStringProperty("");
    private final StringProperty sizeBytes = new SimpleStringProperty("");

    public DocumentViewModel(DocumentService documentService, LocalDocumentStorage storage) {
        this.documentService = documentService;
        this.storage = storage;
        loadDocuments();
        selectedDocument.addListener((obs, o, n) -> populateFields(n));
        searchQuery.addListener((obs, o, n) -> applyFilter());
    }

    public ObservableList<DocumentItem> getDocuments() { return filteredDocuments; }
    public ObjectProperty<DocumentItem> selectedDocumentProperty() { return selectedDocument; }
    public StringProperty searchQueryProperty() { return searchQuery; }
    public StringProperty fileNameProperty() { return fileName; }
    public StringProperty relativePathProperty() { return relativePath; }
    public StringProperty mimeTypeProperty() { return mimeType; }
    public StringProperty sizeBytesProperty() { return sizeBytes; }

    public void loadDocuments() {
        List<DocumentItem> items = documentService.listDocuments();
        documents.setAll(items);
        applyFilter();
    }

    public void importDocument(Path sourcePath) {
        if (sourcePath == null) return;
        try {
            Path rel = storage.importFile(sourcePath);
            DocumentItem item = DocumentItem.newDocument();
            item.setFileName(sourcePath.getFileName().toString());
            item.setFilePath(rel.toString());
            String detected = Files.probeContentType(sourcePath);
            item.setMimeType(detected == null ? "application/octet-stream" : detected);
            item.setSizeBytes(Files.size(sourcePath));
            documentService.save(item);
            loadDocuments();
            selectedDocument.set(item);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to import document", e);
        }
    }

    public void saveSelected() {
        DocumentItem item = selectedDocument.get();
        if (item == null) return;
        item.setFileName(fileName.get());
        item.setMimeType(mimeType.get());
        item.setSizeBytes(parseLong(sizeBytes.get()));
        documentService.save(item);
        loadDocuments();
        selectedDocument.set(item);
    }

    public void openSelected() {
        DocumentItem item = selectedDocument.get();
        if (item == null || item.getFilePath() == null || item.getFilePath().isBlank()) return;
        storage.open(item.getFilePath());
    }

    public void deleteSelected() {
        DocumentItem item = selectedDocument.get();
        if (item == null) return;
        if (item.getFilePath() != null && !item.getFilePath().isBlank()) {
            storage.deleteIfExists(item.getFilePath());
        }
        documentService.delete(item.getId());
        documents.remove(item);
        selectedDocument.set(null);
    }

    public String display(DocumentItem item) {
        String size = item.getSizeBytes() == null ? "?" : item.getSizeBytes().toString();
        return item.displayName() + " (" + size + " o)";
    }

    public javafx.beans.binding.BooleanBinding canActBinding() {
        return Bindings.createBooleanBinding(() -> selectedDocument.get() != null, selectedDocument);
    }

    private void applyFilter() {
        String q = searchQuery.get() == null ? "" : searchQuery.get().toLowerCase();
        filteredDocuments.setPredicate(d -> q.isBlank()
                || contains(d.getFileName(), q)
                || contains(d.getMimeType(), q)
                || contains(d.getFilePath(), q));
    }

    private boolean contains(String value, String q) {
        return value != null && value.toLowerCase().contains(q);
    }

    private Long parseLong(String raw) {
        try {
            return raw == null || raw.isBlank() ? null : Long.parseLong(raw);
        } catch (Exception e) {
            return null;
        }
    }

    private void populateFields(DocumentItem item) {
        if (item == null) {
            fileName.set("");
            relativePath.set("");
            mimeType.set("");
            sizeBytes.set("");
            return;
        }
        fileName.set(item.getFileName() == null ? "" : item.getFileName());
        relativePath.set(item.getFilePath() == null ? "" : item.getFilePath());
        mimeType.set(item.getMimeType() == null ? "" : item.getMimeType());
        sizeBytes.set(item.getSizeBytes() == null ? "" : item.getSizeBytes().toString());
    }
}
