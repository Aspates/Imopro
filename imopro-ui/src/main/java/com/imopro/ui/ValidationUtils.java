package com.imopro.ui;

import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class ValidationUtils {
    private ValidationUtils() {
    }

    public static HBox attachRegexValidation(TextInputControl input,
                                             Pattern pattern,
                                             boolean allowBlank,
                                             String message) {
        return attachValidation(input, value -> pattern.matcher(value).matches(), allowBlank, message);
    }

    public static HBox attachValidation(TextInputControl input,
                                        Predicate<String> validator,
                                        boolean allowBlank,
                                        String message) {
        Label error = new Label();
        error.setStyle("-fx-text-fill:#dc2626; -fx-font-size:11px;");

        Runnable apply = () -> {
            String value = input.getText() == null ? "" : input.getText();
            boolean valid = (allowBlank && value.isBlank()) || validator.test(value);
            error.setText(valid ? "" : message);
        };

        input.textProperty().addListener((obs, oldVal, newVal) -> apply.run());
        apply.run();

        HBox row = new HBox(8, input, error);
        HBox.setHgrow(input, Priority.ALWAYS);
        return row;
    }
}
