/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: March 4, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.config;


//
//  This file is part of the OpenNMS(R) Application.
//
//  OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
//  OpenNMS(R) is a derivative work, containing both original code, included code and modified
//  code that was published under the GNU General Public License. Copyrights for modified 
//  and included code are below.
//
//  OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.nsclient.NsclientCollection;
import org.opennms.netmgt.config.nsclient.NsclientDatacollectionConfig;
import org.opennms.netmgt.model.RrdRepository;

/**
 * <p>NSClientDataCollectionConfigFactory class.</p>
 *
 * @author <a href="mailto:cmiskell@opennms.org">Craig Miskell</a>
 * @version $Id: $
 */
public class NSClientDataCollectionConfigFactory {
     /** The singleton instance. */
     private static NSClientDataCollectionConfigFactory m_instance;

     private static boolean m_loadedFromFile = false;

     /** Boolean indicating if the init() method has been called. */
     protected boolean initialized = false;

     /** Timestamp of the nsclient collection config, used to know when to reload from disk. */
     protected static long m_lastModified;

     private static NsclientDatacollectionConfig m_config;

     /**
      * <p>Constructor for NSClientDataCollectionConfigFactory.</p>
      *
      * @param configFile a {@link java.lang.String} object.
      * @throws org.exolab.castor.xml.MarshalException if any.
      * @throws org.exolab.castor.xml.ValidationException if any.
      * @throws java.io.IOException if any.
      */
     public NSClientDataCollectionConfigFactory(String configFile) throws MarshalException, ValidationException, IOException {
         InputStreamReader rdr = new InputStreamReader(new FileInputStream(configFile));
         
         try {
             initialize(rdr);
         } finally {
             rdr.close();
         }
     }

     /**
      * <p>Constructor for NSClientDataCollectionConfigFactory.</p>
      *
      * @param rdr a {@link java.io.Reader} object.
      * @throws org.exolab.castor.xml.MarshalException if any.
      * @throws org.exolab.castor.xml.ValidationException if any.
      */
     public NSClientDataCollectionConfigFactory(Reader rdr) throws MarshalException, ValidationException {
         initialize(rdr);
     }

     private void initialize(Reader rdr) throws MarshalException, ValidationException {
         log().debug("initialize: initializing NSCLient collection config factory.");
         m_config = (NsclientDatacollectionConfig) Unmarshaller.unmarshal(NsclientDatacollectionConfig.class, rdr);
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
             File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.NSCLIENT_COLLECTION_CONFIG_FILE_NAME);
             m_instance = new NSClientDataCollectionConfigFactory(cfgFile.getPath());
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
     public static synchronized NSClientDataCollectionConfigFactory getInstance() {
         
         if (m_instance == null) {
             throw new IllegalStateException("You must call NSClientCollectionConfigFactory.init() before calling getInstance().");
         }
         return m_instance;
     }
     
     /**
      * <p>setInstance</p>
      *
      * @param instance a {@link org.opennms.netmgt.config.NSClientDataCollectionConfigFactory} object.
      */
     public static synchronized void setInstance(NSClientDataCollectionConfigFactory instance) {
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
      * Reload the nsclient-datacollection-config.xml file if it has been changed since we last
      * read it.
      *
      * @throws java.io.IOException if any.
      * @throws org.exolab.castor.xml.MarshalException if any.
      * @throws org.exolab.castor.xml.ValidationException if any.
      */
     protected void updateFromFile() throws IOException, MarshalException, ValidationException {
         if (m_loadedFromFile) {
             File surveillanceViewsFile = ConfigFileConstants.getFile(ConfigFileConstants.NSCLIENT_COLLECTION_CONFIG_FILE_NAME);
             if (m_lastModified != surveillanceViewsFile.lastModified()) {
                 this.reload();
             }
         }
     }

     /**
      * <p>getConfig</p>
      *
      * @return a {@link org.opennms.netmgt.config.nsclient.NsclientDatacollectionConfig} object.
      */
     public synchronized static NsclientDatacollectionConfig getConfig() {
         return m_config;
     }

     /**
      * <p>setConfig</p>
      *
      * @param m_config a {@link org.opennms.netmgt.config.nsclient.NsclientDatacollectionConfig} object.
      */
     public synchronized static void setConfig(NsclientDatacollectionConfig m_config) {
         NSClientDataCollectionConfigFactory.m_config = m_config;
     }

     private Category log() {
         return ThreadCategory.getInstance();
     }

      /**
       * <p>getNSClientCollection</p>
       *
       * @param collectionName a {@link java.lang.String} object.
       * @return a {@link org.opennms.netmgt.config.nsclient.NsclientCollection} object.
       */
      @SuppressWarnings("unchecked")
     public NsclientCollection getNSClientCollection(String collectionName) {
        NsclientCollection[] collections = m_config.getNsclientCollection();
         NsclientCollection collection = null;
         for (NsclientCollection coll : collections) {
             if (coll.getName().equalsIgnoreCase(collectionName)) collection = coll;
             break;
         }
         if (collection == null) {
             throw new IllegalArgumentException("getNSClientCollection: collection name: "
                     +collectionName+" specified in collectd configuration not found in nsclient collection configuration.");
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
         NsclientCollection collection = getNSClientCollection(cName);
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
     public List getRRAList(String cName) {
         NsclientCollection collection = (NsclientCollection) getNSClientCollection(cName);
         if (collection != null)
             return (List) collection.getRrd().getRraCollection();
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

