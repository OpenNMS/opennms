/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 24, 2008  - rdk@krupczak.org
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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
   OpenNMS Xmp collection/config factory for kicking off parsing of
   the xmp-datacollection config file and returning an XmpCollection 
   object.
   @author Bobby Krupczak, rdk@krupczak.org
   @version $Id: XmpCollectionFactory.java 38 2008-07-24 13:39:32Z rdk $
 */

package org.opennms.netmgt.collectd;

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
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.xmpDataCollection.XmpCollection;
import org.opennms.netmgt.config.xmpDataCollection.XmpDatacollectionConfig;
import org.opennms.netmgt.model.RrdRepository;

public class XmpCollectionFactory {

    /* class variables and methods *********************** */
    private static XmpCollectionFactory instance;
    private static boolean loadedFromFile = false;
    private static boolean initialized = false;

    private static XmpDatacollectionConfig config;

    // initialize our class for the creation of instances
    public static void init() throws IOException, FileNotFoundException, MarshalException, ValidationException 
    {

        if (instance == null) {
            File dataCfgFile = ConfigFileConstants.getFile(ConfigFileConstants.XMP_COLLECTION_CONFIG_FILE_NAME);
            instance = new XmpCollectionFactory(dataCfgFile.getPath());
            loadedFromFile = true;
            initialized = true;
        }

    }

    public static XmpCollectionFactory getInstance() { return instance; }

    /* instance variables ******************************** */
    private String rrdPath;

    /* constructors  ************************************* */
    public XmpCollectionFactory(String configFile) 
    throws MarshalException, ValidationException, IOException { 

        InputStream cfgIn = new FileInputStream(configFile);

        config = (XmpDatacollectionConfig)Unmarshaller.unmarshal(XmpDatacollectionConfig.class,new InputStreamReader(cfgIn, "UTF-8"));

        cfgIn.close();

        rrdPath = null;

        // list out the collections I've found
        XmpCollection[] collections = config.getXmpCollection();
        XmpCollection theCollection = null;
        for (XmpCollection coll: collections) {

            log().debug("XmpCollectionFactory: found collection "+
                        coll.getName());

            //System.out.println("XmpCollectionFactory: found collection "+
            //                    coll.getName());
        }

        return; 
    }

    public XmpCollectionFactory(Reader rdr)
    throws MarshalException, ValidationException, IOException { 

        config = (XmpDatacollectionConfig)Unmarshaller.unmarshal(XmpDatacollectionConfig.class,rdr);

        rrdPath = null;

        return;
    }

    /* private methods *********************************** */
    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    /* public methods ************************************ */

    // get our rrdPath from our CollectionConfig object
    public String getRrdPath()
    {
        rrdPath = config.getRrdRepository();
        if (rrdPath == null) { return null; }

        if (rrdPath.endsWith(File.separator)) {
            rrdPath = rrdPath.substring(0,(rrdPath.length() - File.separator.length()));
        }

        return rrdPath;
    }

    /** given a collection name, fetch its RRD info from the config file via
       the XmpDatacollectionConfig class and return an new repository **/
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

    /** given a collection name, fetch it from the config file via
       the XmpDatacollectionConfig class **/
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
