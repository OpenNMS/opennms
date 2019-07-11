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

import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.threshd.Basethresholddef;
import org.opennms.netmgt.config.threshd.Group;
import org.opennms.netmgt.config.threshd.ThresholdingConfig;
import org.opennms.netmgt.dao.api.EffectiveConfigurationDao;
import org.opennms.netmgt.model.EffectiveConfiguration;
import org.opennms.netmgt.model.OnmsJsonDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

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
public final class ThresholdsConfigFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ThresholdsConfigFactory.class);

    /**
     * The singleton instance of this factory
     */
    private static ThresholdsConfigFactory m_singleton = null;

    /**
     * The config class loaded from the config file
     */
    private ThresholdingConfig m_config;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    /**
     * Map of org.opennms.netmgt.config.threshd.Group objects indexed by group
     * name.
     */
    private Map<String, Group> m_groupMap;

    @Autowired
    private EffectiveConfigurationDao m_configDao;

    private Gson gson = new Gson();

    /**
     * <p>Constructor for ThresholdingConfigFactory.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws IOException 
     */
    public ThresholdsConfigFactory(InputStream stream) throws IOException {
        parseXML(stream);
    }

    private void parseXML(InputStream stream) throws IOException {
        try (Reader reader = new InputStreamReader(stream)) {
            m_config = JaxbUtils.unmarshal(ThresholdingConfig.class, reader);
        }
        initGroupMap();
    }

    /**
     * Build map of org.opennms.netmgt.config.threshd.Group objects
     * indexed by group name.
     *
     * This is parsed and built at initialization for
     * faster processing at run-timne.
     */ 
    private void initGroupMap() {
        Map<String, Group> groupMap = new HashMap<String, Group>();

        for (Group g : m_config.getGroups()) {
            groupMap.put(g.getName(), g);
        }
        
        m_groupMap = groupMap;
    }

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @throws java.io.IOException if any.
     */
    public static synchronized void init() throws IOException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.THRESHOLDING_CONF_FILE_NAME);

        loadConfigFile(cfgFile);
    }


    /**
     * Reload the config from the default config file
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @throws java.io.IOException if any.
     */
    public static synchronized void reload() throws IOException {
        m_singleton = null;
        m_loaded = false;

        init();
    }

    /**
     * Return the singleton instance of this factory.
     *
     * @return The current factory instance.
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized ThresholdsConfigFactory getInstance() {
        if (!m_loaded) {
            throw new IllegalStateException("The factory has not been initialized");
        }

        return m_singleton;
    }

    @Deprecated // use @loadThresholds()
    public static synchronized void setInstance(ThresholdsConfigFactory instance) {
        m_loaded = true;
        m_singleton = instance;
    }

    private static void loadConfigFile(File cfgFile) throws IOException {
        LOG.debug("init: config file path: {}", cfgFile.getPath());

        try (InputStream stream = new FileInputStream(cfgFile.getPath());) {

            ThresholdsConfigFactory tcf = new ThresholdsConfigFactory(stream);

            for (String groupName : tcf.getGroupNames()) {
                Group g = tcf.getGroup(groupName);
                for (org.opennms.netmgt.config.threshd.Threshold threshold : g.getThresholds()) {
                    if (threshold.getDsName().length() > ConfigFileConstants.RRD_DS_MAX_SIZE) {
                        throw new IllegalStateException(String.format("ds-name '%s' in group '%s' is greater than %d characters", threshold.getDsName(), groupName,
                                                                      ConfigFileConstants.RRD_DS_MAX_SIZE));
                    }
                }
            }

            m_singleton = tcf;
            m_loaded = true;
        }
    }

    public synchronized void loadThresholds(File thresholdsFile) throws IOException {
        m_loaded = true;
        m_singleton = null;
        loadConfigFile(thresholdsFile);
    }

    public void setEffectiveConfigurationDao(EffectiveConfigurationDao effectiveConfigurationDao) {
        m_configDao = effectiveConfigurationDao;
    }

    public EffectiveConfigurationDao getEffectiveConfigurationDao() {
        return m_configDao;
    }
    /**
     * Retrieves the configured path to the RRD file repository for the
     * specified thresholding group.
     *
     * @param groupName
     *            Group name to lookup
     * @return RRD repository path.
     * @throws java.lang.IllegalArgumentException
     *             if group name does not exist in the group map.
     */
    public String getRrdRepository(String groupName) {
        return getGroup(groupName).getRrdRepository();
    }

    /**
     * <p>getGroup</p>
     *
     * @param groupName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.threshd.Group} object.
     */
    public Group getGroup(String groupName) {
        Group group = m_groupMap.get(groupName);
        if (group == null) {
            throw new IllegalArgumentException("Thresholding group " + groupName + " does not exist.");
        }
        return group;
    }

    /**
     * Retrieves a Collection object consisting of all the
     * org.opennms.netmgt.config.Threshold objects which make up the specified
     * thresholding group.
     *
     * @param groupName
     *            Group name to lookup
     * @return Collection consisting of all the Threshold objects for the
     *         specified group..
     * @throws java.lang.IllegalArgumentException
     *             if group name does not exist in the group map.
     */
    public Collection<Basethresholddef> getThresholds(String groupName) {
        Group group=getGroup(groupName);
        Collection<Basethresholddef> result=new ArrayList<>();
        result.addAll(group.getThresholds());
        result.addAll(group.getExpressions());
        return result;
    }
    
    /**
     * <p>getGroupNames</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<String> getGroupNames() {
        return Collections.unmodifiableCollection(m_groupMap.keySet());
    }
    
    /**
     * Saves the current in-memory configuration to disk and reloads
     *
     * @throws java.io.IOException if any.
     */
    public synchronized void saveCurrent() throws IOException {
        // Marshal to a string first, then write the string to the file. This
        // way the original config isn't lost if the XML from the marshal is hosed.
        final String xmlString = JaxbUtils.marshal(m_config);
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
        effective.setLastUpdated(new Date());
        m_configDao.save(effective);

    }

    private OnmsJsonDocument getJsonConfig() {
        JsonObject document = new JsonObject();
        document.addProperty("groups", gson.toJson(m_groupMap));
        OnmsJsonDocument onmsJson = new OnmsJsonDocument();
        onmsJson.setDocument(document);
        return onmsJson;
    }

    /**
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     */
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
}
