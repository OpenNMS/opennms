/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.api.ThresholdsConfigModifiable;
import org.opennms.netmgt.config.threshd.Basethresholddef;
import org.opennms.netmgt.config.threshd.Group;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;
import org.opennms.netmgt.config.threshd.ThresholdingConfig;
import org.opennms.netmgt.dao.api.EffectiveConfigurationDao;
import org.opennms.netmgt.model.EffectiveConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

/**
 * This class is the main repository for thresholding configuration information
 * used by the thresholding daemon.. When this class is loaded it reads the
 * thresholding configuration into memory.
 *
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @version $Id: $
 */
public final class ThresholdsConfigFactory implements ThresholdsConfigModifiable {
    private static final Logger LOG = LoggerFactory.getLogger(ThresholdsConfigFactory.class);

    private ThresholdingConfig config;

    private Map<String, Group> groupByName;

    private File configFile;

    @Autowired
    private EffectiveConfigurationDao configDao;

    // @PostConstruct
    public void init() throws IOException {
        configFile = ConfigFileConstants.getFile(ConfigFileConstants.THRESHOLDING_CONF_FILE_NAME);
        loadConfigFile(configFile);
    }

    private void parseXML(InputStream stream) throws IOException {
        try (Reader reader = new InputStreamReader(stream)) {
            config = JaxbUtils.unmarshal(ThresholdingConfig.class, reader);
        }
        initGroupMap();
    }

    private void initGroupMap() {
        groupByName = config.getGroups().stream().collect(Collectors.toMap(Group::getName, Function.identity()));
    }


    @Override
    public void reload() {
        try {
            loadConfigFile(configFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void loadConfigFile(File cfgFile) throws IOException {
        LOG.debug("init: config file path: {}", cfgFile.getPath());

        try (InputStream stream = new FileInputStream(cfgFile.getPath());) {

            parseXML(stream);

            for (Group group : config.getGroups()) {
                for (org.opennms.netmgt.config.threshd.Threshold threshold : group.getThresholds()) {
                    if (threshold.getDsName().length() > ConfigFileConstants.RRD_DS_MAX_SIZE) {
                        throw new IllegalStateException(String.format("ds-name '%s' in group '%s' is greater than %d characters", threshold.getDsName(), group.getName(),
                                                                      ConfigFileConstants.RRD_DS_MAX_SIZE));
                    }
                }
            }
        }
        saveEffective();
    }

    public synchronized void loadThresholds(File thresholdsFile) throws IOException {
        loadConfigFile(thresholdsFile);
    }

    // injection of EffectiveConfigurationDao
    public void setEffectiveConfigurationDao(EffectiveConfigurationDao effectiveConfigurationDao) {
        configDao = effectiveConfigurationDao;
    }

    /**
     * <p>getGroup</p>
     *
     * @param groupName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.threshd.Group} object.
     */
    public Group getGroup(String groupName) {
        Group group = groupByName.get(groupName);
        if (group == null) {
            throw new IllegalArgumentException("Thresholding group " + groupName + " does not exist.");
        }
        return group;
    }

    /**
     * <p>getGroupNames</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<String> getGroupNames() {
        return Collections.unmodifiableCollection(groupByName.keySet());
    }
    
    /**
     * Saves the current in-memory configuration to disk and reloads
     *
     * @throws java.io.IOException if any.
     */
    public synchronized void saveCurrent() throws IOException {
        // Marshal to a string first, then write the string to the file. This
        // way the original config isn't lost if the XML from the marshal is hosed.
        final String xmlString = JaxbUtils.marshal(config);
        if (xmlString != null) {
            File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.THRESHOLDING_CONF_FILE_NAME);

            Writer fileWriter = new OutputStreamWriter(new FileOutputStream(cfgFile), StandardCharsets.UTF_8);
            fileWriter.write(xmlString);
            fileWriter.flush();
            fileWriter.close();

            update();

            saveEffective();
        }
    }

    private synchronized void saveEffective() {
        EffectiveConfiguration effective = new EffectiveConfiguration();
        effective.setKey(ConfigFileConstants.getFileName(ConfigFileConstants.THRESHOLDING_CONF_FILE_NAME));
        effective.setConfiguration(getJsonConfig());
        effective.setHashCode(config.hashCode());
        effective.setLastUpdated(new Date());
        configDao.save(effective);
    }

    private String getJsonConfig() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector(objectMapper.getTypeFactory()));
            return objectMapper.writeValueAsString(config);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
    }

    private void update() throws IOException {
        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.THRESHOLDING_CONF_FILE_NAME);

        InputStream stream = null;
        try {
            stream = new FileInputStream(cfgFile);
            parseXML(stream);
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    @Override
    public ThresholdingConfig getConfig() {
        return config;
    }

    @Override
    public String getRrdRepository(String groupName) {
        return getGroup(groupName).getRrdRepository();
    }

    @Override
    public Collection<Basethresholddef> getThresholds(String groupName) {
        Group group = getGroup(groupName);
        Collection<Basethresholddef> result = new ArrayList<>();
        result.addAll(group.getThresholds());
        result.addAll(group.getExpressions());
        return result;
    }

    @Override
    public void setConfigFile(File file) {
        configFile = file;
        reload();
    }
}
