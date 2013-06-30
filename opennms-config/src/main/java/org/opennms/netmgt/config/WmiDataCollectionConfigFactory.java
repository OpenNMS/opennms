/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ConfigFileConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.wmi.WmiCollection;
import org.opennms.netmgt.config.wmi.WmiDatacollectionConfig;
import org.opennms.netmgt.model.RrdRepository;

/**
 * <P>
 * This class loads and presents the WMI data collection configuration file
 * for use with the WMI performance collection classes.
 * </P>
 *
 * @author <a href="mailto:matt.raykowski@gmail.com">Matt Raykowski</a>
 * @author <a href="http://www.opennms.org">OpenNMS</a>
 */
public class WmiDataCollectionConfigFactory {
    private static final Logger LOG = LoggerFactory.getLogger(WmiDataCollectionConfigFactory.class);

     /** The singleton instance. */
     private static WmiDataCollectionConfigFactory m_instance;

     private static boolean m_loadedFromFile = false;

     /** Boolean indicating if the init() method has been called. */
     protected boolean initialized = false;

     /** Timestamp of the WMI collection config, used to know when to reload from disk. */
     protected static long m_lastModified;

     private static WmiDatacollectionConfig m_config;

     /**
      * <p>Constructor for WmiDataCollectionConfigFactory.</p>
      *
      * @param configFile a {@link java.lang.String} object.
      * @throws org.exolab.castor.xml.MarshalException if any.
      * @throws org.exolab.castor.xml.ValidationException if any.
      * @throws java.io.IOException if any.
      */
     public WmiDataCollectionConfigFactory(String configFile) throws MarshalException, ValidationException, IOException {
         InputStream is = null;

         try {
             is = new FileInputStream(configFile);
             initialize(is);
         } finally {
             if (is != null) {
                 IOUtils.closeQuietly(is);
             }
         }
     }

     /**
      * <p>Constructor for WmiDataCollectionConfigFactory.</p>
      *
      * @param is a {@link java.io.InputStream} object.
      * @throws org.exolab.castor.xml.MarshalException if any.
      * @throws org.exolab.castor.xml.ValidationException if any.
      */
     public WmiDataCollectionConfigFactory(InputStream is) throws MarshalException, ValidationException {
         initialize(is);
     }

     private void initialize(InputStream stream) throws MarshalException, ValidationException {
         LOG.debug("initialize: initializing WMI collection config factory.");
         m_config = CastorUtils.unmarshal(WmiDatacollectionConfig.class, stream);
     }
     
     /**
      * Be sure to call this method before calling getInstance().
      *
      * @throws java.io.IOException if any.
      * @throws java.io.FileNotFoundException if any.
      * @throws org.exolab.castor.xml.MarshalException if any.
      * @throws org.exolab.castor.xml.ValidationException if any.
      */
     public static synchronized void init() throws IOException, FileNotFoundException, MarshalException, ValidationException {

         if (m_instance == null) {
             File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.WMI_COLLECTION_CONFIG_FILE_NAME);
             m_instance = new WmiDataCollectionConfigFactory(cfgFile.getPath());
             m_lastModified = cfgFile.lastModified();
             m_loadedFromFile = true;
         }
     }

     /**
      * Singleton static call to get the only instance that should exist
      *
      * @return the single factory instance
      * @throws java.lang.IllegalStateException
      *             if init has not been called
      */
     public static synchronized WmiDataCollectionConfigFactory getInstance() {

         if (m_instance == null) {
             throw new IllegalStateException("You must call WmiCollectionConfigFactory.init() before calling getInstance().");
         }
         return m_instance;
     }

     /**
      * <p>setInstance</p>
      *
      * @param instance a {@link org.opennms.netmgt.config.WmiDataCollectionConfigFactory} object.
      */
     public static synchronized void setInstance(WmiDataCollectionConfigFactory instance) {
         m_instance = instance;
         m_loadedFromFile = false;
     }

     /**
      * <p>reload</p>
      *
      * @throws java.io.IOException if any.
      * @throws java.io.FileNotFoundException if any.
      * @throws org.exolab.castor.xml.MarshalException if any.
      * @throws org.exolab.castor.xml.ValidationException if any.
      */
     public synchronized void reload() throws IOException, FileNotFoundException, MarshalException, ValidationException {
         m_instance = null;
         init();
     }


     /**
      * Reload the wmi-datacollection-config.xml file if it has been changed since we last
      * read it.
      *
      * @throws java.io.IOException if any.
      * @throws org.exolab.castor.xml.MarshalException if any.
      * @throws org.exolab.castor.xml.ValidationException if any.
      */
     protected void updateFromFile() throws IOException, MarshalException, ValidationException {
         if (m_loadedFromFile) {
             File surveillanceViewsFile = ConfigFileConstants.getFile(ConfigFileConstants.WMI_COLLECTION_CONFIG_FILE_NAME);
             if (m_lastModified != surveillanceViewsFile.lastModified()) {
                 this.reload();
             }
         }
     }

     /**
      * <p>getConfig</p>
      *
      * @return a {@link org.opennms.netmgt.config.wmi.WmiDatacollectionConfig} object.
      */
     public synchronized static WmiDatacollectionConfig getConfig() {
         return m_config;
     }

     /**
      * <p>setConfig</p>
      *
      * @param m_config a {@link org.opennms.netmgt.config.wmi.WmiDatacollectionConfig} object.
      */
     public synchronized static void setConfig(WmiDatacollectionConfig m_config) {
         WmiDataCollectionConfigFactory.m_config = m_config;
     }

     /**
      * <p>getWmiCollection</p>
      *
      * @param collectionName a {@link java.lang.String} object.
      * @return a {@link org.opennms.netmgt.config.wmi.WmiCollection} object.
      */
     public WmiCollection getWmiCollection(String collectionName) {
        WmiCollection[] collections = m_config.getWmiCollection();
         WmiCollection collection = null;
         for (WmiCollection coll : collections) {
             if (coll.getName().equalsIgnoreCase(collectionName)) collection = coll;
             break;
         }
         if (collection == null) {
             throw new IllegalArgumentException("getWmiCollection: collection name: "
                     +collectionName+" specified in collectd configuration not found in WMI collection configuration.");
         }
         return collection;
     }

     /**
      * <p>getRrdRepository</p>
      *
      * @param collectionName a {@link java.lang.String} object.
      * @return a {@link org.opennms.netmgt.model.RrdRepository} object.
      */
     public RrdRepository getRrdRepository(String collectionName) {
         RrdRepository repo = new RrdRepository();
         repo.setRrdBaseDir(new File(getRrdPath()));
         repo.setRraList(getRRAList(collectionName));
         repo.setStep(getStep(collectionName));
         repo.setHeartBeat((2 * getStep(collectionName)));
         return repo;
     }

     /**
      * <p>getStep</p>
      *
      * @param cName a {@link java.lang.String} object.
      * @return a int.
      */
     public int getStep(String cName) {
         WmiCollection collection = getWmiCollection(cName);
         if (collection != null)
             return collection.getRrd().getStep();
         else
             return -1;
     }

     /**
      * <p>getRRAList</p>
      *
      * @param cName a {@link java.lang.String} object.
      * @return a {@link java.util.List} object.
      */
     public List<String> getRRAList(String cName) {
         WmiCollection collection = getWmiCollection(cName);
         if (collection != null)
             return collection.getRrd().getRraCollection();
         else
             return null;

     }

     /**
      * <p>getRrdPath</p>
      *
      * @return a {@link java.lang.String} object.
      */
     public String getRrdPath() {
         String rrdPath = m_config.getRrdRepository();
         if (rrdPath == null) {
             throw new RuntimeException("Configuration error, failed to "
                     + "retrieve path to RRD repository.");
         }

         /*
          * TODO: make a path utils class that has the below in it strip the
          * File.separator char off of the end of the path.
          */
         if (rrdPath.endsWith(File.separator)) {
             rrdPath = rrdPath.substring(0, (rrdPath.length() - File.separator.length()));
         }

         return rrdPath;
     }

 }

