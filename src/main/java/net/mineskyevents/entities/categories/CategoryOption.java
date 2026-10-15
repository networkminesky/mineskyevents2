package net.mineskyevents.entities.categories;

import java.util.Collections;
import java.util.List;

public class CategoryOption {
    private final String id;
    private final CategoryOptionType type;
    private final boolean defaultEnabled;
    private final String displayName;
    private final List<String> description;

    public CategoryOption(String id, CategoryOptionType type, boolean defaultEnabled, String displayName, List<String> description) {
        this.id = id;
        this.type = type;
        this.defaultEnabled = defaultEnabled;
        this.displayName = displayName;
        this.description = description != null ? Collections.unmodifiableList(description) : Collections.emptyList();
    }

    public String getId() {
        return id;
    }

    public CategoryOptionType getType() {
        return type;
    }

    public boolean isDefaultEnabled() {
        return defaultEnabled;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getDescription() {
        return description;
    }
}
