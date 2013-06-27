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

/*
* OCA CONTRIBUTION ACKNOWLEDGEMENT - NOT PART OF LEGAL BOILERPLATE
* DO NOT DUPLICATE THIS COMMENT BLOCK WHEN CREATING NEW FILES!
*
* This file was contributed to the OpenNMS(R) project under the
* terms of the OpenNMS Contributor Agreement (OCA).  For details on
* the OCA, see http://www.opennms.org/index.php/Contributor_Agreement
*
* Contributed under the terms of the OCA by:
*
* Bobby Krupczak <rdk@krupczak.org>
* THE KRUPCZAK ORGANIZATION, LLC
* http://www.krupczak.org/
*/

/**
 *
 *   OpenNMS Xmp collection/config factory for kicking off parsing of
 *   the xmp-datacollection config file and returning an XmpCollection
 *   object.
 *   @author Bobby Krupczak, rdk@krupczak.org
 *   @version $Id: XmpCollectionFactory.java 38 2008-07-24 13:39:32Z rdk $
 */

package org.opennms.netmgt.protocols.xmp.collector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ConfigFileConstants;

import org.opennms.netmgt.config.xmpDataCollection.XmpCollection;
import org.opennms.netmgt.config.xmpDataCollection.XmpDatacollectionConfig;
import org.opennms.netmgt.model.RrdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class XmpCollectionFactory {

    /* class variables and methods *********************** */
    private static XmpCollectionFactory instance;

    private static XmpDatacollectionConfig config;
    
	private static final Logger LOG = LoggerFactory.getLogger(XmpCollectionFactory.class);


    // initialize our class for the creation of instances
    /**
     * <p>init</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static void init() throws IOException, FileNotFoundException, MarshalException, ValidationException 
    {

        if (instance == null) {
            File dataCfgFile = ConfigFileConstants.getFile(ConfigFileConstants.XMP_COLLECTION_CONFIG_FILE_NAME);
            instance = new XmpCollectionFactory(dataCfgFile.getPath());
        }

    }

    /**
     * <p>Getter for the field <code>instance</code>.</p>
     *
     * @return a {@link org.opennms.netmgt.protocols.xmp.collector.XmpCollectionFactory} object.
     */
    public static XmpCollectionFactory getInstance() { return instance; }

    /* instance variables ******************************** */
    private String rrdPath;

    /* constructors  ************************************* */
    /**
     * <p>Constructor for XmpCollectionFactory.</p>
     *
     * @param configFile a {@link java.lang.String} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public XmpCollectionFactory(String configFile) 
    throws MarshalException, ValidationException, IOException { 

        InputStream cfgIn = new FileInputStream(configFile);

        config = (XmpDatacollectionConfig)Unmarshaller.unmarshal(XmpDatacollectionConfig.class,new InputStreamReader(cfgIn, "UTF-8"));

        cfgIn.close();

        rrdPath = null;

        // list out the collections I've found
        if (LOG.isDebugEnabled()) {
            XmpCollection[] collections = config.getXmpCollection();
            for (XmpCollection coll: collections) {
                LOG.debug("XmpCollectionFactory: found collection {}", coll.getName());
            }
        }

        return; 
    }

    /**
     * <p>Constructor for XmpCollectionFactory.</p>
     *
     * @param rdr a {@link java.io.Reader} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public XmpCollectionFactory(Reader rdr)
    throws MarshalException, ValidationException, IOException { 

        config = (XmpDatacollectionConfig)Unmarshaller.unmarshal(XmpDatacollectionConfig.class,rdr);

        rrdPath = null;

        return;
    }

    /* private methods *********************************** */
   

    /* public methods ************************************ */

    // get our rrdPath from our CollectionConfig object
    /**
     * <p>Getter for the field <code>rrdPath</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRrdPath()
    {
        rrdPath = config.getRrdRepository();
        if (rrdPath == null) { return null; }

        if (rrdPath.endsWith(File.separator)) {
            rrdPath = rrdPath.substring(0,(rrdPath.length() - File.separator.length()));
        }

        return rrdPath;
    }

    /**
     * given a collection name, fetch its RRD info from the config file via
     *       the XmpDatacollectionConfig class and return an new repository *
     *
     * @param collectionName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.RrdRepository} object.
     */
    public RrdRepository getRrdRepository(String collectionName) 
    { 
        RrdRepository repo = new RrdRepository();

        //log().debug("XmpCollectionFactory: getting rrd for "+collectionName);

        XmpCollection collection = getXmpCollection(collectionName);

        // rrdPath not specific to a collection but specified for all of 
        // data collection
        if (rrdPath == null) getRrdPath();

        repo.setRrdBaseDir(new File(rrdPath));

        if (collection != null) {
            repo.setRraList(collection.getRrd().getRraCollection());
            repo.setStep(collection.getRrd().getStep());
            repo.setHeartBeat(2 * repo.getStep());
        }
        else {
            repo.setRraList(null);
            repo.setStep(-1);
            repo.setStep(-2);
        }

        return repo;
    }

    /**
     * given a collection name, fetch it from the config file via
     *       the XmpDatacollectionConfig class *
     *
     * @param collectionName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.xmpDataCollection.XmpCollection} object.
     */
    public XmpCollection getXmpCollection(String collectionName) 
    {
        XmpCollection[] collections = config.getXmpCollection();
        XmpCollection theCollection = null;

        //log().debug("XmpCollectionFactory: getting collection for "+collectionName);

        for (XmpCollection coll: collections) {

            //log().debug("XmpCollectionFactory: checking collection "+
            //           coll.getName());

            if (coll.getName().equalsIgnoreCase(collectionName)) {
                theCollection = coll;
                break;
            }
        }
        return theCollection;
    }

} /* class XmpCollectionFactory */
