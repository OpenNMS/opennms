//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.threshd.Basethresholddef;
import org.opennms.netmgt.config.threshd.Group;
import org.opennms.netmgt.config.threshd.ThresholdingConfig;
import org.opennms.netmgt.dao.castor.CastorUtils;

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
 * 
 */
public final class ThresholdingConfigFactory {
    /**
     * The singleton instance of this factory
     */
    private static ThresholdingConfigFactory m_singleton = null;

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

    /**
     * Private constructor
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    private ThresholdingConfigFactory(String configFile) throws IOException, MarshalException, ValidationException {
        FileReader cfgIn = new FileReader(configFile);

        try {
            parseXML(cfgIn);
        } finally {
            try {
                cfgIn.close();
            } catch (IOException e) {
                // do nothing
            }
        }

    }
    
    public ThresholdingConfigFactory(Reader reader) throws MarshalException, ValidationException {
        parseXML(reader);
    }

    private void parseXML(Reader cfgIn) throws MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(ThresholdingConfig.class, cfgIn);
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

        for (Group g : m_config.getGroupCollection()) {
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
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.THRESHOLDING_CONF_FILE_NAME);

        if (log().isDebugEnabled()) {
            log().debug("init: config file path: " + cfgFile.getPath());
        }

        ThresholdingConfigFactory tcf = new ThresholdingConfigFactory(cfgFile.getPath());

        for (String groupName : tcf.getGroupNames()) {
            Group g = tcf.getGroup(groupName);
            for (org.opennms.netmgt.config.threshd.Threshold threshold :  g.getThresholdCollection()) {
                if (threshold.getDsName().length() > ConfigFileConstants.RRD_DS_MAX_SIZE) {
                    throw new ValidationException(
                        String.format("ds-name '%s' in group '%s' is greater than %d characters",
                            threshold.getDsName(), groupName, ConfigFileConstants.RRD_DS_MAX_SIZE)
                    );
                }
            }
        }
        m_singleton = tcf;
        m_loaded = true;
    }


    /**
     * Reload the config from the default config file
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    public static synchronized void reload() throws IOException, MarshalException, ValidationException {
        m_singleton = null;
        m_loaded = false;

        init();
    }

    /**
     * Return the singleton instance of this factory.
     * 
     * @return The current factory instance.
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized ThresholdingConfigFactory getInstance() {
        if (!m_loaded) {
            throw new IllegalStateException("The factory has not been initialized");
        }

        return m_singleton;
    }

    public static synchronized void setInstance(ThresholdingConfigFactory instance) {
        m_loaded = true;
        m_singleton = instance;
    }
    /**
     * Retrieves the configured path to the RRD file repository for the
     * specified thresholding group.
     * 
     * @param groupName
     *            Group name to lookup
     * 
     * @return RRD repository path.
     * 
     * @throws IllegalArgumentException
     *             if group name does not exist in the group map.
     */
    public String getRrdRepository(String groupName) {
        return getGroup(groupName).getRrdRepository();
    }

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
     * 
     * @return Collection consisting of all the Threshold objects for the
     *         specified group..
     * 
     * @throws IllegalArgumentException
     *             if group name does not exist in the group map.
     */
    public Collection<Basethresholddef> getThresholds(String groupName) {
        Group group=getGroup(groupName);
        Collection<Basethresholddef> result=new ArrayList<Basethresholddef>();
        result.addAll(group.getThresholdCollection());
        result.addAll(group.getExpressionCollection());
        return result;
    }
    
    public Collection<String> getGroupNames() {
        return m_groupMap.keySet();
    }
    
    /**
     * Saves the current in-memory configuration to disk and reloads
     */
    public synchronized void saveCurrent() throws MarshalException, IOException, ValidationException {
        // Marshal to a string first, then write the string to the file. This
        // way the original config
        // isn't lost if the XML from the marshal is hosed.
        StringWriter stringWriter = new StringWriter();
        Marshaller.marshal(m_config, stringWriter);

        String xmlString = stringWriter.toString();
        if (xmlString != null) {
            File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.THRESHOLDING_CONF_FILE_NAME);

            FileWriter fileWriter = new FileWriter(cfgFile);
            fileWriter.write(xmlString);
            fileWriter.flush();
            fileWriter.close();
        }
        
        update();

    }
    public void update() throws IOException, MarshalException, ValidationException {
        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.THRESHOLDING_CONF_FILE_NAME);

        Reader r = new FileReader(cfgFile);
        parseXML(r);

        r.close();
    }
    
    private static Category log() {
        return ThreadCategory.getInstance(ThresholdingConfigFactory.class);
    }
}
