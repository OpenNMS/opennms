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

package org.opennms.features.config.service.impl;

import org.json.JSONObject;
import org.opennms.features.config.dao.api.ConfigConverter;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigSchema;
import org.opennms.features.config.dao.api.ConfigStoreDao;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.util.ValidateUsingConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Component
public class ConfigurationManagerServiceImpl implements ConfigurationManagerService {
    private final static Logger LOG = LoggerFactory.getLogger(ConfigurationManagerServiceImpl.class);
    private final ConfigStoreDao<JSONObject> configStoreDao;

    public ConfigurationManagerServiceImpl(final ConfigStoreDao<JSONObject> configStoreDao) {
        this.configStoreDao = configStoreDao;
    }

    @Override
    public <ENTITY> void registerSchema(final String configName, final int majorVersion, final int minorVersion,
                                        final int patchVersion, Class<ENTITY> entityClass)
            throws IOException, ClassNotFoundException {
        ValidateUsingConverter<ENTITY> converter = new ValidateUsingConverter<>(entityClass);
        this.registerSchema(configName, majorVersion, minorVersion, patchVersion, converter);
    }

    @Override
    public void registerSchema(final String configName, final int majorVersion, final int minorVersion,
                               final int patchVersion, final ConfigConverter converter)
            throws IOException, ClassNotFoundException {
        Objects.requireNonNull(configName);
        Objects.requireNonNull(converter);
        if (this.getRegisteredSchema(configName).isPresent()) {
            throw new IllegalArgumentException(String.format("Schema with id=%s is already registered.", configName));
        }

        final ConfigSchema configSchema = new ConfigSchema(configName, majorVersion, minorVersion, patchVersion,
                converter.getClass(), converter);
        configStoreDao.register(configSchema);
    }

    @Override
    public Optional<ConfigSchema<?>> getRegisteredSchema(final String configName) throws IOException, ClassNotFoundException {
        Objects.requireNonNull(configName);
        return configStoreDao.getConfigSchema(configName);
    }

    @Override
    public void registerConfiguration(final String configName, final String configId, Object configEntity)
            throws IOException, ClassNotFoundException {
        Objects.requireNonNull(configId);
        Objects.requireNonNull(configName);
        Objects.requireNonNull(configEntity);
        //TODO: validation logic here
        Optional<ConfigSchema<?>> schema = configStoreDao.getConfigSchema(configName);
        if (schema.isEmpty()) {
            throw new IllegalArgumentException("ConfigName not found: " + configName);
        }
        String jsonStr = schema.get().getConverter().jaxbObjectToJson(configEntity);

        configStoreDao.addConfig(configName, configId, new JSONObject(jsonStr));
        LOG.info("ConfigurationManager.registeredConfiguration(service={}, id={}, config={});", configName, configId, jsonStr);
    }

    @Override
    public void unregisterConfiguration(final String configName, final String configId) throws IOException {
        this.configStoreDao.deleteConfig(configName, configId);
    }

    @Override
    public void updateConfiguration(String configName, String configId, JSONObject object) throws IOException {
        configStoreDao.updateConfig(configName, configId, object);
    }

    @Override
    public <ENTITY> Optional<ENTITY> getConfiguration(String configName, String configId, Class<ENTITY> entityClass)
            throws IOException {
        Optional<ConfigSchema<?>> configSchema = configStoreDao.getConfigSchema(configName);
        if (configSchema.isEmpty()) {
            LOG.error("Fail to get schema for configName: " + configName + " configId: " + configId);
            return Optional.empty();
        }
        Optional<JSONObject> config = configStoreDao.getConfig(configName, configId);
        if (config.isEmpty()) {
            LOG.error("Fail to get config for configName: " + configName + " configId: " + configId);
            return Optional.empty();
        }
        JSONObject json = config.get();
        return (Optional<ENTITY>) Optional.of(configSchema.get().getConverter().jsonToJaxbObject(json.toString()));
    }

    @Override
    public Optional<JSONObject> getJSONConfiguration(final String configName, final String configId) throws IOException {
        return configStoreDao.getConfig(configName, configId);
    }

    @Override
    public Optional<String> getXmlConfiguration(String configName, String configId) throws IOException {
        Optional<ConfigSchema<?>> configSchema = configStoreDao.getConfigSchema(configName);
        if (configSchema.isEmpty()) {
            LOG.error("Fail to get schema for configName: " + configName + " configId: " + configId);
            return Optional.empty();
        }
        Optional<JSONObject> config = configStoreDao.getConfig(configName, configId);
        if (config.isEmpty()) {
            LOG.error("Fail to get config for configName: " + configName + " configId: " + configId);
            return Optional.empty();
        }
        JSONObject json = config.get();
        return (Optional<String>) Optional.of(configSchema.get().getConverter().jsonToXml(json.toString()));
    }

    @Override
    public Set<String> getServiceIds() {
        return configStoreDao.getServiceIds().get();
    }

    @Override
    public void unregisterSchema(String configName) throws IOException {
        configStoreDao.unregister(configName);
    }

    @Override
    public Optional<ConfigData<JSONObject>> getConfigData(String configName) throws IOException {
        return configStoreDao.getConfigData(configName);
    }

//    //TODO: CHECK WHAT IS THAT FOR
//    @Override
//    public Optional<ConfigData<JSONObject>> getConfigurationMetaData(String configName) {
//        Objects.requireNonNull(serviceId);
//        return configStoreDao.getConfigData(configName).get();
//    }
/*
    @Override
    public ConfigData getSchemaForConfiguration(String configName) {
        return configStoreDao.getConfigSchema()
    }
*/
//    //TODO: CHECK later
//    @Override
//    public ConfigData<JSONObject> getSchemaForConfiguration(String configName) {
//        return null;
//    }


//    @Override
//    public void putConfiguration(final String configId, final JSONObject configContent) {
//        Objects.requireNonNull(configId);
//        Objects.requireNonNull(configContent);
//        if(!this.isConfigurationRegistered(configId)) {
//            throw new IllegalArgumentException(String.format("Unknown configuration with configId=%s. Please register first.", configId));
//        }
//        store.put(configId, configContent.toString(), STORE_CONTEXT_CONFIG);
//    }
//
//    private String getNamespaceForConfigId(final String configId) {
//        return getSchemaForConfiguration(configId)
//                .getXmlSchema()
//                .getNamespace();
//    }
//
//    @Override
//    public void putConfiguration(String configId, JSONObject object) {
//
//    }
//
//    @Override
//    public void removeConfiguration(String configId, String path) {
//
//    }


//    private void putConfigurationWithException(final String configId, final String path, final String content) throws DocumentException, IOException, SAXException {
//        Objects.requireNonNull(configId);
//        Objects.requireNonNull(path);
//        Document config = getConfigurationAsDocument(configId);
//        int index;
//        String namespace = getNamespaceForConfigId(configId);
//        final Element element = (Element) PathUtil.selectSingleNode(config, path, namespace);
//        Branch parent;
//        if (element == null) {
//            parent = (Branch) config.selectSingleNode(PathUtil.getParentPath(path));
//            index = Optional.ofNullable(parent).map(Branch::content).map(List::size).orElse(0);
//        } else {
//            // node exists, we need to replace it. Lets remove the old one
//            parent = element.getParent();
//            index = Optional.ofNullable(parent).map(Branch::content).map(list -> list.indexOf(element)).orElse(0);
//            if (parent == null) {
//                config.remove(element);
//            } else {
//                parent.remove(element);
//            }
//        }
//
//        String elementName = PathUtil.getElementName(PathUtil.getLastElement(path));
//        Element newElement = DocumentHelper.createElement(elementName);
//        setContentOfElement(newElement, content);
//        if (parent == null) {
//            config.add(newElement);
//        } else {
//            parent.content().add(index, newElement); // changes to list is reflected in parent
//        }
//        writeConfiguration(configId, config);
//    }

//    private void writeConfiguration(final String configId, final Document configDoc) throws IOException, SAXException, DocumentException {
//        Configuration config = this.getConfigurationMetaData(configId)
//                .orElseThrow(() -> new NullPointerException(String.format("Config with id=%s does not exist. Register first.", configId)));
//        XMLSchema schema = this.getSchemaForConfiguration(config.getSchemaId()).getXmlSchema();
//        String xml = configDoc.asXML();
//
//        // validate
//        XMLValidator.validate(xml, schema);
//
//        // write
//        String json = xmlMapper.xmlToJson(configId, xml);
//        putConfiguration(configId, new JSONObject(json));
//    }
//
//    private Document getConfigurationAsDocument(final String configId) throws DocumentException {
//        Objects.requireNonNull(configId);
//        Document config;
//        Optional<String> configAsString = this.getConfiguration(configId)
//                .map(json -> xmlMapper.jsonToXml(configId, json.toString()));
//        if (configAsString.isPresent()) {
//            SAXReader reader = new SAXReader();
//            config = reader.read(new StringReader(configAsString.get()));
//        } else {
//            config = DocumentHelper.createDocument();
//        }
//        return config;
//    }
//    private String getNamespaceForConfigId(final String configId) {
//        return getSchemaForConfiguration(configId)
//                .getXmlSchema()
//                .getNamespace();
//    }

//    private void removeConfigurationWithException(final String configId, final String path) throws DocumentException, IOException, SAXException {
//        Objects.requireNonNull(configId);
//        Objects.requireNonNull(path);
//        Document config = getConfigurationAsDocument(configId);
//
//        for (Object nodeObj : PathUtil.selectNodes(config, path, getNamespaceForConfigId(configId))) {
//            Node node = (Node) nodeObj;
//            node.getParent().remove(node);
//        }
//
//        writeConfiguration(configId, config);
//    }
}
