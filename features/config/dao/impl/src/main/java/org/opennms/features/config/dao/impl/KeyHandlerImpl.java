/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.config.dao.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigItem;
import org.opennms.features.config.dao.api.ConfigSchema;
import org.opennms.features.config.dao.api.KeyHandler;

import com.google.common.base.Strings;

public class KeyHandlerImpl implements KeyHandler {

    private final JsonConfigStoreDaoImpl configStoreDao;

    public KeyHandlerImpl(JsonConfigStoreDaoImpl configStoreDao) {
        this.configStoreDao = Objects.requireNonNull(configStoreDao);
    }

    @Override
    public Set<String> getKeysMatchingPrefix(String prefix) {
        Optional<Set<String>> configNames = configStoreDao.getConfigNames();

        final List<String> matchingConfigNames = configNames.orElse(Collections.emptySet()).stream()
                .filter(s -> s.toLowerCase(Locale.ROOT).startsWith(prefix))
                .sorted()
                .collect(Collectors.toList());
        if (matchingConfigNames.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> matches = new LinkedHashSet<>();
        for (String configName : matchingConfigNames) {
            try {
                final Optional<ConfigSchema<?>> maybeSchema = configStoreDao.getConfigSchema(configName);
                if (maybeSchema.isEmpty()) {
                    // silently skip configs for which we don't have a schema
                    continue;
                }
                final ConfigSchema<?> schema = maybeSchema.get();

                KeyGenerator keyGenerator = new KeyGenerator(configName, schema);
                keyGenerator.getKeys().stream().filter(path -> path.startsWith(prefix)).forEach(matches::add);
            } catch (IOException e) {
                throw new RuntimeException("Failed to get schema for: " + configName, e);
            }
        }

        return matches;
    }

    @Override
    public <T> void testValue(String key, T value) {
        if (Strings.isNullOrEmpty(key)) {
            throw new IllegalArgumentException("Invalid key: " + key);
        }

        final String[] parts = key.split("/");
        if (parts.length < 2) {
            throw new RuntimeException("Invalid key: " + key + ". Not enough parts!");
        }

        // First part is the "configName", find the corresponding schema
        final String configName = parts[1];

        final ConfigSchema<?> schema;
        try {
            final Optional<ConfigSchema<?>> maybeSchema = configStoreDao.getConfigSchema(configName);
            if (maybeSchema.isEmpty()) {
                throw new RuntimeException("No schema for: " + configName);
            }
            schema = maybeSchema.get();
        } catch (IOException e) {
            throw new RuntimeException("Failed to get schema for: " + configName, e);
        }

        // Grab the configItem for the corresponding key and validate the path
        KeyWalker keyWalker = new KeyWalker(configName, schema.getConverter().getValidationSchema().getConfigItem());
        ConfigItem item = keyWalker.getItemForKey(parts);
        if (item == null) {
            throw new RuntimeException("No known element for key: " + key);
        }

        // FIXME: This doesn't belong here
        if (ConfigItem.Type.NUMBER.equals(item.getType())) {
            if (value instanceof Number) {
                // GOOD!
                return;
            } else if (value instanceof String) {
                try {
                    Double.parseDouble((String)value);
                    // GOOD!
                    return;
                } catch (NumberFormatException nfe) {
                    throw new RuntimeException("String is not a valid number: " + value, nfe);
                }
            } else {
                throw new RuntimeException("Unsupported object type for Number" + value.getClass());
            }
        } else if (ConfigItem.Type.BOOLEAN.equals(item.getType())) {
            if (value instanceof Boolean) {
                // GOOD!
                return;
            } else if (value instanceof String) {
                if(((String)value).trim().equalsIgnoreCase("true") || ((String)value).trim().equalsIgnoreCase("false")) {
                    // GOOD!
                    return;
                } else {
                    throw new RuntimeException("String is not a valid boolean: " + value);
                }
            } else {
                throw new RuntimeException("Unsupported object type for Number" + value.getClass());
            }
        } else {
            throw new RuntimeException("Unsupported type: " + item.getType());
        }
    }

    @Override
    public <T> void setValue(String key, T value) {
        if (Strings.isNullOrEmpty(key)) {
            throw new IllegalArgumentException("Invalid key: " + key);
        }
        final String[] parts = key.split("/");
        if (parts.length < 2) {
            throw new RuntimeException("Invalid key: " + key + ". Not enough parts!");
        }
        // First part is the "configName", find the corresponding schema
        final String configName = parts[1];
        // Take the remaining parts
        final String[] pathParts = Arrays.copyOfRange(parts, 2, parts.length);
        if (!addServiceConfig(configName, value, pathParts)) {
            throw new RuntimeException("Failed to set value at path: " + Arrays.toString(pathParts));
        }
    }

    public boolean addServiceConfig(String configName, Object value, String... pathParts) {
        throw new RuntimeException("See https://github.com/opennms-forge/notconfd/blob/0d2bdf2cc8bc7d26da591c239128766b399905c5/service/src/main/java/org/opennnms/notconfd/service/JSONStoreSvc.java#L53");
    }

    @Override
    public Optional<String> getValue(String key) {
        if (Strings.isNullOrEmpty(key)) {
            throw new IllegalArgumentException("Invalid key: " + key);
        }
        final String[] parts = key.split("/");
        if (parts.length < 2) {
            throw new RuntimeException("Invalid key: " + key + ". Not enough parts!");
        }
        // First part is the "configName", find the corresponding schema
        final String configName = parts[1];
        // Take the remaining parts
        final String[] pathParts = Arrays.copyOfRange(parts, 2, parts.length);

        try {
            ConfigData<JSONObject> configData = configStoreDao.getConfigData(configName).get();
            return Optional.ofNullable(getWithPath(configData.getConfigs().get("default"), pathParts));
        } catch (IOException e) {
            throw new RuntimeException("No config data for config with name: " + configName);
        }
    }

    public String getWithPath(JSONObject obj, String[] pathParts) {
        if ((pathParts.length == 0) || (pathParts.length == 1 && pathParts[0].isEmpty())) {
            if (obj != null) {
                return obj.toString();
            }
            return null;
        }

        Object currentLevel =  obj;
        // Go down the 'parts' path to the correct level/node.
        // Need to update the second-last since it points to the one we need to change
        int endLevel = pathParts.length;
        currentLevel = traversePath(pathParts, currentLevel, endLevel).orElse(null);
        if (currentLevel == null) {
            return null;
        }

        if (currentLevel instanceof JSONObject || currentLevel instanceof JSONArray || currentLevel instanceof Integer) {
            return currentLevel.toString();
        }
        return null;
    }

    private Optional<Object> traversePath(String[] levelKeys, Object jsonObj, int numberOfLevels)  {
        for (int i = 0; i < numberOfLevels; i++) {
            String part = levelKeys[i];
            if (! part.isEmpty()) {
                if (jsonObj instanceof JSONObject) {
                    JSONObject obj = (JSONObject) jsonObj;
                    if (obj.has(part)) {
                        jsonObj = obj.get(part);
                    } else {
                        return Optional.empty();
                    }
                } else if (jsonObj instanceof JSONArray) {
                    // We're going over an array, the url should be an index
                    int index = Integer.parseInt(part);
                    JSONArray jsonarray = (JSONArray) jsonObj;
                    if (index < jsonarray.length()) {
                        jsonObj = jsonarray.get(index);
                    } else {
                        return Optional.empty();
                    }
                }
            }
        }
        return Optional.of(jsonObj);
    }

    @Override
    public void setKeys(Map<String, String> keys) {
        keys.forEach(this::setValue);
    }

    public static void walk(ConfigItem parent, ConfigItem item, BiConsumer<ConfigItem, ConfigItem> consumer) {
        consumer.accept(parent, item);
        for (ConfigItem childItem : item.getChildren()) {
            walk(item, childItem, consumer);
        }
    }

    private static class KeyWalker {
        private final ConfigItem schema;

        public KeyWalker(String configName, ConfigItem schema) {
            this.schema = Objects.requireNonNull(schema);
        }

        public ConfigItem getItemForKey(String[] parts) {
            ConfigItem parent = schema;
            for (String part : Arrays.stream(parts).skip(2).collect(Collectors.toList())) {
                if (ConfigItem.Type.ARRAY.equals(parent.getType())) {
                    // Validate the index
                    try {
                        Integer.parseInt(part);
                    } catch (NumberFormatException nfe) {
                        throw new IllegalArgumentException("Invalid index value: " + part);
                    }

                    // Take the first child
                    parent = parent.getChildren().get(0);
                    continue;
                }

                boolean found = false;
                for (ConfigItem item : parent.getChildren()) {
                    if (Objects.equals(part, item.getName())) {
                        parent = item;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return null;
                }
            }

            return parent;
        }

    }

    private static class KeyGenerator {

        private final String root;
        private final Map<ConfigItem, String> pathsByItem = new LinkedHashMap<>();
        private final Map<ConfigItem, String> pathsToLeafs = new LinkedHashMap<>();
        private final Map<String, ConfigItem> itemsByPath = new LinkedHashMap<>();

        public KeyGenerator(String configName, ConfigSchema<?> schema) {
            this.root = "/" + configName;
            walk(null, schema.getConverter().getValidationSchema().getConfigItem(), this::generatePathsForItems);
        }

        private void generatePathsForItems(ConfigItem parent, ConfigItem item) {
            // Build the path to this element
            String path;
            if (parent != null) {
                path = pathsByItem.get(parent);
                if (ConfigItem.Type.ARRAY.equals(parent.getType())) {
                    path += "/{" + item.getName() + "Index}";
                } else {
                    path += "/" + item.getName();
                }
            } else {
                path = root;
            }

            // Index the path for future reference
            pathsByItem.put(item, path);

            // Copy the leafs
            if (item.getType().isSimple()) {
                pathsToLeafs.put(item, path);
            }

            // Reverse-indexing
            itemsByPath.put(path, item);
        }

        public ConfigItem getItemForKey(String key) {
            return itemsByPath.get(key);
        }

        public List<String> getKeys() {
            return pathsToLeafs.values().stream().sorted().collect(Collectors.toList());
        }


    }
}
