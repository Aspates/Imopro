package com.imopro.ui;

import java.util.prefs.Preferences;

public class PropertyDefaultsStore {
    private static final String KEY_ADDRESS = "property.default.address";
    private static final String KEY_CITY = "property.default.city";
    private static final String KEY_POSTAL = "property.default.postal";
    private static final String KEY_TYPE = "property.default.type";

    private final Preferences prefs = Preferences.userNodeForPackage(PropertyDefaultsStore.class);

    public PropertyDefaults load() {
        return new PropertyDefaults(
                prefs.get(KEY_ADDRESS, ""),
                prefs.get(KEY_CITY, ""),
                prefs.get(KEY_POSTAL, ""),
                prefs.get(KEY_TYPE, "")
        );
    }

    public void save(PropertyDefaults defaults) {
        prefs.put(KEY_ADDRESS, defaults.address());
        prefs.put(KEY_CITY, defaults.city());
        prefs.put(KEY_POSTAL, defaults.postalCode());
        prefs.put(KEY_TYPE, defaults.propertyType());
    }

    public record PropertyDefaults(String address, String city, String postalCode, String propertyType) {
    }
}
