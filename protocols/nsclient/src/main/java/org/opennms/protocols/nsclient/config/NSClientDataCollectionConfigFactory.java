/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.protocols.nsclient.config;


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
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.nsclient.NsclientCollection;
import org.opennms.netmgt.config.nsclient.NsclientDatacollectionConfig;
import org.opennms.netmgt.model.RrdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>NSClientDataCollectionConfigFactory class.</p>
 *
 * @author <a href="mailto:cmiskell@opennms.org">Craig Miskell</a>
 */
public class NSClientDataCollectionConfigFactory {
	
	private static final Logger LOG = LoggerFactory.getLogger(NSClientDataCollectionConfigFactory.class);

    private final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_globalLock.readLock();
    private final Lock m_writeLock = m_globalLock.writeLock();
    
     /** The singleton instance. */
     private static NSClientDataCollectionConfigFactory m_instance;

     private static boolean m_loadedFromFile = false;

     /** Timestamp of the nsclient collection config, used to know when to reload from disk. */
     protected static long m_lastModified;

     private NsclientDatacollectionConfig m_config;

     /**
      * <p>Constructor for NSClientDataCollectionConfigFactory.</p>
      *
      * @param configFile a {@link java.lang.String} object.
      * @throws org.exolab.castor.xml.MarshalException if any.
      * @throws org.exolab.castor.xml.ValidationException if any.
      * @throws java.io.IOException if any.
      */
     public NSClientDataCollectionConfigFactory(final String configFile) throws MarshalException, ValidationException, IOException {
         InputStream is = null;
         
         try {
             is = new FileInputStream(configFile);
             initialize(is);
         } finally {
             IOUtils.closeQuietly(is);
         }
     }

     public Lock getReadLock() {
         return m_readLock;
     }
     
     public Lock getWriteLock() {
         return m_writeLock;
     }

     private void initialize(final InputStream stream) throws MarshalException, ValidationException {
         LOG.debug("initialize: initializing NSCLient collection config factory.");
         m_config = CastorUtils.unmarshal(NsclientDatacollectionConfig.class, stream);
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
             final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.NSCLIENT_COLLECTION_CONFIG_FILE_NAME);
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
      * @param instance a {@link org.opennms.protocols.nsclient.config.NSClientDataCollectionConfigFactory} object.
      */
     public static synchronized void setInstance(final NSClientDataCollectionConfigFactory instance) {
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
      * <p>getNSClientCollection</p>
      *
      * @param collectionName a {@link java.lang.String} object.
      * @return a {@link org.opennms.netmgt.config.nsclient.NsclientCollection} object.
      */
     public NsclientCollection getNSClientCollection(final String collectionName) {
         getReadLock().lock();
         try {
             NsclientCollection collection = null;
             for (final NsclientCollection coll : m_config.getNsclientCollection()) {
                 if (coll.getName().equalsIgnoreCase(collectionName)) {
                	 collection = coll;
                     break;
                 }
             }
             if (collection == null) {
                 throw new IllegalArgumentException("getNSClientCollection: collection name: "
                         +collectionName+" specified in collectd configuration not found in nsclient collection configuration.");
             }
             return collection;
         } finally {
             getReadLock().unlock();
         }
     }

     /**
      * <p>getRrdRepository</p>
      *
      * @param collectionName a {@link java.lang.String} object.
      * @return a {@link org.opennms.netmgt.model.RrdRepository} object.
      */
     public RrdRepository getRrdRepository(final String collectionName) {
         getReadLock().lock();
         try {
             final RrdRepository repo = new RrdRepository();
             repo.setRrdBaseDir(new File(getRrdPath()));
             repo.setRraList(getRRAList(collectionName));
             repo.setStep(getStep(collectionName));
             repo.setHeartBeat((2 * getStep(collectionName)));
             return repo;
         } finally {
             getReadLock().unlock();
         }
     }
     
     /**
      * <p>getStep</p>
      *
      * @param cName a {@link java.lang.String} object.
      * @return a int.
      */
     public int getStep(final String cName) {
         getReadLock().lock();
         try {
             final NsclientCollection collection = getNSClientCollection(cName);
             if (collection != null) {
                 return collection.getRrd().getStep();
             } else {
                 return -1;
             }
         } finally {
             getReadLock().unlock();
         }
     }
     
     /**
      * <p>getRRAList</p>
      *
      * @param cName a {@link java.lang.String} object.
      * @return a {@link java.util.List} object.
      */
     public List<String> getRRAList(final String cName) {
         getReadLock().lock();
         try {
             final NsclientCollection collection = getNSClientCollection(cName);
             if (collection != null) {
                 return collection.getRrd().getRraCollection();
             } else {
                 return null;
             }
         } finally {
             getReadLock().unlock();
         }
     }
     
     /**
      * <p>getRrdPath</p>
      *
      * @return a {@link java.lang.String} object.
      */
     public String getRrdPath() {
         getReadLock().lock();
         try {
             String rrdPath = m_config.getRrdRepository();
             if (rrdPath == null) {
                 throw new RuntimeException("Configuration error, failed to retrieve path to RRD repository.");
             }
         
             /*
              * TODO: make a path utils class that has the below in it strip the
              * File.separator char off of the end of the path.
              */
             if (rrdPath.endsWith(File.separator)) {
                 rrdPath = rrdPath.substring(0, (rrdPath.length() - File.separator.length()));
             }
             
             return rrdPath;
         } finally {
             getReadLock().unlock();
         }
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
             getWriteLock().lock();
             try {
                 File surveillanceViewsFile = ConfigFileConstants.getFile(ConfigFileConstants.NSCLIENT_COLLECTION_CONFIG_FILE_NAME);
                 if (m_lastModified != surveillanceViewsFile.lastModified()) {
                     this.reload();
                 }
             } finally {
                 getWriteLock().unlock();
             }
         }
     }

 }

