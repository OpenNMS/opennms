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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONObject;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigMeta;
import org.opennms.features.config.dao.api.ConfigStoreDao;
import org.opennms.features.config.dao.api.XmlConfigConverter;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.util.ValidateUsingConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Component
public class ConfigurationManagerServiceImpl implements ConfigurationManagerService<JSONObject> {

    private final static Logger LOG = LoggerFactory.getLogger(ConfigurationManagerServiceImpl.class);
    private final ConfigStoreDao<JSONObject> configStoreDao;

    public ConfigurationManagerServiceImpl(final ConfigStoreDao<JSONObject> configStoreDao) {
        this.configStoreDao = configStoreDao;
    }

    @Override
    public <ENTITY> void registerSchema(final String serviceName, final int majorVersion, final int minorVersion,
                                        final int patchVersion, Class<ENTITY> entityClass)
            throws IOException, ClassNotFoundException {
        ValidateUsingConverter<ENTITY> converter = new ValidateUsingConverter<>(entityClass);
        this.registerSchema(serviceName, majorVersion, minorVersion, patchVersion, converter);
    }

    @Override
    public void registerSchema(final String serviceName, final int majorVersion, final int minorVersion,
                               final int patchVersion, final XmlConfigConverter converter)
            throws IOException, ClassNotFoundException {
        Objects.requireNonNull(serviceName);
        Objects.requireNonNull(converter);
        if (this.getRegisteredSchema(serviceName).isPresent()) {
            throw new IllegalArgumentException(String.format("Schema with id=%s is already registered.", serviceName));
        }

        final ConfigMeta configMeta = new ConfigMeta(serviceName, majorVersion, minorVersion, patchVersion,
                converter.getClass(), converter);
        configStoreDao.register(configMeta);
    }

    @Override
    public Optional<ConfigMeta<?>> getRegisteredSchema(String serviceName) throws IOException, ClassNotFoundException {
        Objects.requireNonNull(serviceName);
        return configStoreDao.getConfigMeta(serviceName);
    }

    private String readFile(final String xmlPath) throws IOException {
        Path path = Path.of(xmlPath);
        return Files.readString(path);
    }

    @Override
    public void registerConfiguration(final String serviceName, final String configId, final String xmlPath)
            throws IOException, ClassNotFoundException {
        Objects.requireNonNull(configId);
        Objects.requireNonNull(serviceName);
        Objects.requireNonNull(xmlPath);

        Optional<ConfigMeta<?>> meta = this.getRegisteredSchema(serviceName);
        if (meta.isEmpty()) {
            throw new IllegalArgumentException(String.format("Unknown service with id=%s.", serviceName));
        }
        if (this.getConfiguration(serviceName, configId).isPresent()) {
            throw new IllegalArgumentException(String.format("Configuration with service=%s, id=%s is already registered, update instead.", serviceName, configId));
        }
        try {
            final String xmlStr = this.readFile(xmlPath);
            String jsonStr = meta.get().getConverter().xmlTOJson(xmlStr);
            JSONObject configJson = new JSONObject(jsonStr);
            this.registerConfiguration(serviceName, configId, configJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void registerConfiguration(String serviceName, String configId, JSONObject jsonObj) throws IOException {
        Objects.requireNonNull(configId);
        Objects.requireNonNull(serviceName);
        Objects.requireNonNull(jsonObj);
        //TODO: validation logic here
        configStoreDao.addConfig(serviceName, configId, jsonObj);
        LOG.info("ConfigurationManager.registeredConfiguration(service={}, id={}, config={});", serviceName, configId, jsonObj);
    }

    @Override
    public void unregisterConfiguration(final String serviceName, final String configId) throws IOException {
        this.configStoreDao.deleteConfig(serviceName, configId);
    }

    @Override
    public boolean updateConfiguration(String serviceName, String configId, JSONObject object) throws IOException {
        return configStoreDao.updateConfig(serviceName, configId, object);
    }

    @Override
    public Optional<JSONObject> getConfiguration(final String serviceName, final String configId) throws IOException {
        return configStoreDao.getConfig(serviceName, configId);
    }

    @Override
    public Set<String> getServiceIds() {
        return configStoreDao.getServiceIds().get();
    }

    @Override
    public void unregisterSchema(String serviceName) throws IOException {
        configStoreDao.unregister(serviceName);
    }

    @Override
    public Optional<ConfigData<JSONObject>> getConfigData(String serviceName) throws IOException {
        return configStoreDao.getConfigData(serviceName);
    }

//    //TODO: CHECK WHAT IS THAT FOR
//    @Override
//    public Optional<ConfigData<JSONObject>> getConfigurationMetaData(String serviceName) {
//        Objects.requireNonNull(serviceId);
//        return configStoreDao.getConfigData(serviceName).get();
//    }
/*
    @Override
    public ConfigData getSchemaForConfiguration(String serviceName) {
        return configStoreDao.getConfigMeta()
    }
*/
//    //TODO: CHECK later
//    @Override
//    public ConfigData<JSONObject> getSchemaForConfiguration(String serviceName) {
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
