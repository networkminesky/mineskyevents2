package net.mineskyevents.entities;

import net.mineskyevents.entities.categories.Category;

import java.text.Normalizer;
import java.util.UUID;

public class EventBuilder {
    private final Category category;
    private String displayName;

    public EventBuilder(Category category) {
        this.category = category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String name) {
        this.displayName = name;
    }

    public String generateId() {
        if (displayName == null || displayName.isEmpty()) return "";

        String baseId = Normalizer.normalize(displayName + "-" + category.getName(), Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("[^a-zA-Z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "")
                .toLowerCase();

        String uniqueId = UUID.randomUUID().toString().split("-")[0];
        return baseId + "-" + uniqueId;
    }
}
