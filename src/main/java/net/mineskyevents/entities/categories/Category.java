package net.mineskyevents.entities.categories;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Category {
    private final ConfigurationSection categoriesSection;

    private final File file;
    private YamlConfiguration config;

    private final String id;
    private final String name;
    private final Material displayItem;
    private final Map<String, CategoryOption> optionalOptions;

    private final List<String> eventList = new ArrayList<>();

    public Category(File file, ConfigurationSection categoriesSection, String id) {
        this.categoriesSection = categoriesSection;
        this.id = id;

        this.file = file;
        this.config = YamlConfiguration.loadConfiguration(file);


        this.name = categoriesSection.getString("name", id.toLowerCase());
        String materialName = categoriesSection.getString("display-item", "STONE");
        Material resolvedMaterial = Material.getMaterial(materialName.toUpperCase());

        if (resolvedMaterial == null) {
            resolvedMaterial = Material.STONE;
        }

        this.displayItem = resolvedMaterial;

        this.optionalOptions = new HashMap<>();
        ConfigurationSection optionsSection = this.categoriesSection.getConfigurationSection("optional-options");
        if (optionsSection != null) {
            for (String optionId : optionsSection.getKeys(false)) {
                ConfigurationSection optionSection = optionsSection.getConfigurationSection(optionId);
                if (optionSection == null) continue;

                String typeString = optionSection.getString("type");
                CategoryOptionType type = CategoryOptionType.fromString(typeString);
                if (type == null) continue;

                boolean defaultEnabled = optionSection.getBoolean("default-enabled", false);
                String displayName = optionSection.getString("display-name");
                List<String> description = optionSection.getStringList("description");

                this.optionalOptions.put(optionId, new CategoryOption(optionId, type, defaultEnabled, displayName, description));
            }
        }
    }



   /* public void removeItem() {
        eventList.remove("public on EVENT");
    }

    public void forceAddItem(Item item) {
        itemList.add(item);
    }

    public List<Item> getAllItems() {
        return itemList;
    }*/

    public void reloadFile() {
        final File file = getFile();
        try {
            config.save(file);
        } catch (IOException ex) {
            ex.fillInStackTrace();
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

 /*   private void registerItemsInsideCategory() {
        for(String itemId : getConfig().getKeys(false)) {
            final ConfigurationSection section = getConfig().getConfigurationSection(itemId);

            assert section != null;
            try {
                Item item = new Item(this, itemId, section);
                //MineSkyItems.l.info("  | Item carregado: "+item.getMetadata().displayName());

                itemList.add(item);
            } catch (Exception exception) {
                exception.fillInStackTrace();
            }
        }
        reloadFile();
    }*/

    public void reloadCategory() {
        reloadFile();
        eventList.clear();
        //registerItemsInsideCategory();
    }

    public Material getDisplayItem() {
        return displayItem;
    }

    public String getName() {
        return name;
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public File getFile() {
        return file;
    }

    public String getId() {
        return id;
    }

    public Map<String, CategoryOption> getOptionalOptions() {
        return Collections.unmodifiableMap(optionalOptions);
    }

    public CategoryOption getOption(String optionId) {
        return optionalOptions.get(optionId);
    }

    public CategoryOption geCategoryOption() {
        if (!optionalOptions.isEmpty()) {
            return optionalOptions.values().iterator().next();
        }
        return null;
    }

    public ConfigurationSection getCategoriesFileSection() {
        return categoriesSection;
    }
}
