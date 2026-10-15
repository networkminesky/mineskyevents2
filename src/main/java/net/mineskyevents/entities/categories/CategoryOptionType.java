package net.mineskyevents.entities.categories;

public enum CategoryOptionType {
        BOOLEAN,
        INTEGER,
        STRING;

        public static CategoryOptionType fromString(String text) {
            for (CategoryOptionType type : CategoryOptionType.values()) {
                if (type.name().equalsIgnoreCase(text)) {
                    return type;
                }
            }
            return null;
        }
}
