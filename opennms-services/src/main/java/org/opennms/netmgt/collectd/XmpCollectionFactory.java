/* XmpCollectionFactory.java

   COPYRIGHT 2008 KRUPCZAK.ORG, LLC.

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License as
   published by the Free Software Foundation; either version 2 of the
   License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
   General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
   USA
 
   For more information, visit:
   http://www.krupczak.org/
   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License as
   published by the Free Software Foundation; either version 2 of the
   License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
   General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
   USA
 
   For more information, visit:
   http://www.krupczak.org/
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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.config.xmpDataCollection.XmpCollection;
import org.opennms.netmgt.config.xmpDataCollection.XmpDatacollectionConfig;

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

       config = (XmpDatacollectionConfig)Unmarshaller.unmarshal(XmpDatacollectionConfig.class,new InputStreamReader(cfgIn));
       
       cfgIn.close();

       rrdPath = null;

       return; 
   }

   public XmpCollectionFactory(Reader rdr)
                throws MarshalException, ValidationException, IOException { 

       config = (XmpDatacollectionConfig)Unmarshaller.unmarshal(XmpDatacollectionConfig.class,rdr);

       rrdPath = null;

       return;
   }

   /* private methods *********************************** */

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

       for (XmpCollection coll: collections) {

           if (coll.getName().equalsIgnoreCase(collectionName)) {
	      theCollection = coll;
              break;
	   }
       }
       return theCollection;
   }

} /* class XmpCollectionFactory */
