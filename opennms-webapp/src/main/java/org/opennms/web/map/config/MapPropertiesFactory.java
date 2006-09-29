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
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.web.map.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Category;
import org.opennms.core.resource.Vault;
import org.opennms.core.utils.BundleLists;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;


public class MapPropertiesFactory extends Object{

	private static boolean m_loaded = false;

    /**
     * The singleton instance of this factory
     */
    private static MapPropertiesFactory m_singleton = null;

    /**
     * Descriptive information about this factory.
     */
    protected static final String info = "org.opennms.web.map.MapPropertiesFactory";

    /**
     * Descriptive information about this factory.
     */
    protected static final String name = "MapPropertiesFactory";


    /**
     * The map.properties file that is read for the list of severities and statuses settings
     * for map view.
     */
    protected static File mapPropertiesFile;

    protected static String home = null;

    /**
     * The Log4J category for logging web authentication messages.
     */
    protected static Category log = null;

    protected static Map[] propertiesMaps = null;

    protected static Map statusesMap = null;

    protected static Status[] orderedStatuses = null;

    protected static Map severitiesMap = null;

    protected static Severity[] orderedSeverities = null;

    protected static Map availsMap = null;

 	protected static Avail[] orderedAvails = null;

	protected static List assetFields = null;

	protected static Map iconsMap = null;

    protected static Map bgImagesMap = null;

    /**
     * Create a new instance.
     */
    private MapPropertiesFactory() {
        ThreadCategory.setPrefix("OpenNMS.Map");
        log = ThreadCategory.getInstance(this.getClass());
        home = Vault.getHomeDir();
//      configure the files to the given home dir
        MapPropertiesFactory.mapPropertiesFile = new File(home + File.separator + "etc" + File.separator + ConfigFileConstants.getFileName(ConfigFileConstants.MAP_PROPERTIES_FILE_NAME));
    }

    public static synchronized void init()throws FileNotFoundException,IOException{
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }


        m_singleton = new MapPropertiesFactory();
        parseMapProperties();
        m_loaded = true;
    }

    public static synchronized void reload()throws FileNotFoundException,IOException{
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
    public static synchronized MapPropertiesFactory getInstance() {
        if (!m_loaded)
            throw new IllegalStateException("The factory has not been initialized");

        return m_singleton;
    }

    public Map[] getMapProperties()throws IOException,FileNotFoundException{
    	return propertiesMaps;
    }

    /**
     * Gets the java.util.Map with key = severity label and value the Severity corresponding to the label
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     */
    public Map getSeveritiesMap()throws IOException,FileNotFoundException{
    	return severitiesMap;
    }

    /**
     * Gets the java.util.Map with key = availability label and value the Avail corresponding to the label
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     */
    public Map getAvailabilitiesMap()throws IOException,FileNotFoundException{
    	return availsMap;
    }

    public Avail getAvail(double avail) {
    	if (avail < 0) avail = -1;
    	Avail rightAv= null;
    	int bestfound = -1;
    	Iterator ite = availsMap.values().iterator();
    	while (ite.hasNext()) {
    		Avail av = (Avail) ite.next();
    		if (avail > av.getMin() && avail >=bestfound) {
    			rightAv=av;
    			bestfound=av.getMin();
    		}
    	}
    	return rightAv;
    }

    public Avail getDisabledAvail() throws IOException,FileNotFoundException {

        Properties props = new Properties();
        props.load(new FileInputStream(MapPropertiesFactory.mapPropertiesFile));
        String disableAvailId = props.getProperty("avail.enable.false.id");
        if(disableAvailId ==null){
        	throw new IllegalStateException("Required Default Status not found.");
        }
    	Iterator ite = availsMap.values().iterator();
    	while (ite.hasNext()) {
    		Avail av = (Avail) ite.next();
    		if (av.getId() == Integer.parseInt(disableAvailId)) return av;
    	}
    	return null;
    }

    public boolean enableAvail() throws IOException, FileNotFoundException {
        Properties props = new Properties();
        props.load(new FileInputStream(MapPropertiesFactory.mapPropertiesFile));
        String enableAvail = props.getProperty("avail.enable");
        if (enableAvail != null && enableAvail.equalsIgnoreCase("false")) return false;
    	return true;
    }


    /**
     * Gets the 'nodeup' status in map.properties. nodeup status is a required parameter.
     * @return nodeup status
     * @throws IOException
     * @throws FileNotFoundException
     */
    public Status getDefaultStatus()throws IOException, FileNotFoundException{
        Properties props = new Properties();
        props.load(new FileInputStream(MapPropertiesFactory.mapPropertiesFile));
        String defaultid = props.getProperty("status.default");
        if(defaultid ==null){
        	throw new IllegalStateException("Required Default Status not found.");
        }
        String id = props.getProperty("status."+defaultid+".id");
        String uei = props.getProperty("status."+defaultid+".uei");
        String color = props.getProperty("status."+defaultid+".color");
        String text = props.getProperty("status."+defaultid+".text");
        Status st = new Status(Integer.parseInt(id),uei,color,text);
        return st;
    }

    /**
     * Gets the 'undefined' status in map.properties. nodeup status is a required parameter.
     * @return nodeup status
     * @throws IOException
     * @throws FileNotFoundException
     */
    public Status getUnknownUeiStatus()throws IOException, FileNotFoundException{
        Properties props = new Properties();
        props.load(new FileInputStream(MapPropertiesFactory.mapPropertiesFile));
        String defaultid = props.getProperty("status.unknown.uei");
        if(defaultid ==null){
        	throw new IllegalStateException("Required Unknown Uei Status not found.");
        }
        String id = props.getProperty("status."+defaultid+".id");
        String uei = props.getProperty("status."+defaultid+".uei");
        String color = props.getProperty("status."+defaultid+".color");
        String text = props.getProperty("status."+defaultid+".text");
        Status st = new Status(Integer.parseInt(id),uei,color,text);
        return st;
    }


    /**
     * Gets the 'normal' severity in map.properties. Normal severity is a required parameter.
     * @return Normal severity
     * @throws IOException
     * @throws FileNotFoundException
     */
    public Severity getDefaultSeverity()throws IOException, FileNotFoundException{
        Properties props = new Properties();
        props.load(new FileInputStream(MapPropertiesFactory.mapPropertiesFile));
        String defaultid = props.getProperty("severity.default");
        if(defaultid==null){
        	throw new IllegalStateException("Required Default Severity not found.");
        }

        String id = props.getProperty("severity."+defaultid+".id");
        String label = props.getProperty("severity."+defaultid+".label");
        String color = props.getProperty("severity."+defaultid+".color");
        Severity se = new Severity(Integer.parseInt(id),label,color);
        return se;
    }


    /**
     * Gets the java.util.Map with key = uei and value the status corresponding to the uei
     * @return java.util.Map with key = uei and value the status corresponding to the uei
     * @throws IOException
     * @throws FileNotFoundException
     */
    public Map getStatusesMap()throws IOException,FileNotFoundException{
    	return statusesMap;
    }



    public String getInfo() {
        return (MapPropertiesFactory.info);
    }








    /**
     * Parses the map.properties file into two mappings: from severity label to Severity and
     *  from status uei to Status.
     *
     */
    protected static Map[] parseMapProperties() throws FileNotFoundException, IOException {
        log.debug("Parsing map.properties...");
    	severitiesMap = new HashMap();
        statusesMap = new HashMap();
        availsMap = new HashMap();
        assetFields = new ArrayList();
        iconsMap = new HashMap();
        bgImagesMap = new HashMap();
        // read the file
        Properties props = new Properties();
        props.load(new FileInputStream(MapPropertiesFactory.mapPropertiesFile));

        // look up severities and their properties
        String[] severities = BundleLists.parseBundleList(props.getProperty("severities"));

        for (int i = 0; i < severities.length; i++) {
            String id = props.getProperty("severity." + severities[i] + ".id");
            String label = props.getProperty("severity." + severities[i] + ".label");
            String color = props.getProperty("severity." + severities[i] + ".color");
            String flash = props.getProperty("severity." + severities[i] + ".flash");
            Severity sev = new Severity(Integer.parseInt(id),label,color);
            if (flash != null && flash.equalsIgnoreCase("true")) sev.setFlash(true);
            log.debug("found severity "+severities[i]+" with id="+id+", label="+label+", color="+color+ ". Adding it.");
            severitiesMap.put(label, sev);
        }
        orderedSeverities = new Severity[severitiesMap.size()];
        Iterator it = severitiesMap.values().iterator();
        int k =0;
        while(it.hasNext()){
        	orderedSeverities[k++]=(Severity)it.next();
        }
        Arrays.sort(orderedSeverities);

        // look up statuses and their properties
        String[] statuses = BundleLists.parseBundleList(props.getProperty("statuses"));


        for (int i = 0; i < statuses.length; i++) {
            String id = props.getProperty("status." + statuses[i] + ".id");
            String uei = props.getProperty("status." + statuses[i] + ".uei");
            String color = props.getProperty("status." + statuses[i] + ".color");
            String text = props.getProperty("status." + statuses[i] + ".text");
            log.debug("found status "+statuses[i]+" with id="+id+", uei="+uei+", color="+color+ ", text="+text+ ". Adding it.");
            Status status = new Status(Integer.parseInt(id),uei,color,text);
            statusesMap.put(uei, status);
        }

        orderedStatuses = new Status[statusesMap.size()];
        it = statusesMap.values().iterator();
        k =0;
        while(it.hasNext()){
        	orderedStatuses[k++]=(Status)it.next();
        }
        Arrays.sort(orderedStatuses);

        // look up statuses and their properties
        String[] availes = BundleLists.parseBundleList(props.getProperty("availabilities"));

        for (int i = 0; i < availes.length; i++) {
            String id = props.getProperty("avail." + availes[i] + ".id");
            String min = props.getProperty("avail." + availes[i] + ".min");
            String color = props.getProperty("avail." + availes[i] + ".color");
            String flash = props.getProperty("avail." + availes[i] + ".flash");
            log.debug("found avail "+availes[i]+" with id="+id+", min="+min+", color="+color+ ". Adding it.");
            Avail avail = new Avail(Integer.parseInt(id),Integer.parseInt(min),color);
            if (flash != null && flash.equalsIgnoreCase("true")) avail.setFlash(true);
            availsMap.put(min, avail);
        }

        orderedAvails = new Avail[availsMap.size()];
        it = availsMap.values().iterator();
        k =0;
        while(it.hasNext()){
        	orderedAvails[k++]=(Avail)it.next();
        }
        Arrays.sort(orderedAvails);


        // look up asset fields
        String assets =props.getProperty("assets");
        if(assets!=null){
	        String[] assFields = BundleLists.parseBundleList(props.getProperty("assets"));
	        assetFields = Arrays.asList(assFields);
        }

//      look up icons filenames
        String[] icons = BundleLists.parseBundleList(props.getProperty("icons"));

        for (int i = 0; i < icons.length; i++) {
            String filename = props.getProperty("icon." + icons[i] + ".filename");
            log.debug("found icon "+icons[i]+" with filename="+filename+". Adding it.");
            iconsMap.put(icons[i], filename);
        }

//      look up background filenames
        String[] bg = BundleLists.parseBundleList(props.getProperty("bgimages"));

        for (int i = 0; i < bg.length; i++) {
            String filename = props.getProperty("bgimage." + bg[i] + ".filename");
            log.debug("found bgimage "+bg[i]+" with filename="+filename+". Adding it.");
            bgImagesMap.put(bg[i], filename);
        }
        propertiesMaps = new Map[] { severitiesMap, statusesMap, availsMap ,iconsMap, bgImagesMap};

        return (propertiesMaps);
    }


	public List getAssetFields() {
		return assetFields;
	}
	public static Map getIconsMap() {
		return iconsMap;
	}

	public static Map getBackgroundImagesMap() {
		return bgImagesMap;
	}

    public String getDefaultMapIcon()throws IOException, FileNotFoundException{
        Properties props = new Properties();
        props.load(new FileInputStream(MapPropertiesFactory.mapPropertiesFile));
        String defaultMapIcon = props.getProperty("icon.default.map");
        if(defaultMapIcon ==null){
        	throw new IllegalStateException("Required Default Icon Map not found.");
        }
       return defaultMapIcon;
    }

    public String getDefaultNodeIcon()throws IOException, FileNotFoundException{
        Properties props = new Properties();
        props.load(new FileInputStream(MapPropertiesFactory.mapPropertiesFile));
        String defaultNodeIcon = props.getProperty("icon.default.node");
        if(defaultNodeIcon ==null){
        	throw new IllegalStateException("Required Default Icon Node not found.");
        }
       return defaultNodeIcon;
    }

    /**
     * Gets the array of ordered Severity by id.
     * @return
     */
	public static Severity[] getOrderedSeverities() {
		return orderedSeverities;
	}

    /**
     * Gets the array of ordered Avail by min.
     * @return
     */
	public static Avail[] getOrderedAvails() {
		return orderedAvails;
	}

    /**
     * Gets the array of ordered Status by id.
     * @return
     */
	public static Status[] getOrderedStatuses() {
		return orderedStatuses;
	}
}
