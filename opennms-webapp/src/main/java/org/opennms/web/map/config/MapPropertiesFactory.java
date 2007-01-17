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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Category;
import org.opennms.core.resource.Vault;
import org.opennms.core.utils.BundleLists;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.web.map.MapsException;

public class MapPropertiesFactory extends Object {

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
	 * The map.properties file that is read for the list of severities and
	 * statuses settings for map view.
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

	protected static Map iconsMap = null;

	protected static Map bgImagesMap = null;

	protected static Map sourcesMap = null;
	
	protected static Map nodesPerSource = null;
	
	protected static Map factoriesMap = null;
	
	protected static String defaultFactory = null; 
	
	protected static String severityMapAs = "avg"; 

	/**
	 * Create a new instance.
	 */
	private MapPropertiesFactory() {
		ThreadCategory.setPrefix("OpenNMS.Map");
		log = ThreadCategory.getInstance(this.getClass());
		home = Vault.getHomeDir();
		// configure the files to the given home dir
		MapPropertiesFactory.mapPropertiesFile = new File(
				home
						+ File.separator
						+ "etc"
						+ File.separator
						+ ConfigFileConstants
								.getFileName(ConfigFileConstants.MAP_PROPERTIES_FILE_NAME));
	}

	public static synchronized void init() throws FileNotFoundException,
			IOException {
		if (m_loaded) {
			// init already called - return
			// to reload, reload() will need to be called
			return;
		}

		m_singleton = new MapPropertiesFactory();
		parseMapProperties();
		m_loaded = true;
	}

	/**
	 * Every time called, reload the properties file and the nodes per data source defined in the properties file.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static synchronized void reload(boolean reloadPropertiesFile) throws FileNotFoundException,
			IOException {
		if(reloadPropertiesFile){
			m_singleton = null;
			m_loaded = false;
		}
		//reloadNodesPerSource();
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
			throw new IllegalStateException(
					"The factory has not been initialized");

		return m_singleton;
	}

	/**
	 * gets an Array of java.util.Map: 
	 *  -severitiesMap: severity label (String) to Severity
	 *	-statusesMap: status (String) uei to Status
	 * 	-availsMap: min (String) of avail to Avail
	 *	-iconsMap: icon (String) label to String (icon filename)
	 *	-bgImagesMap: background (String) image label to String (background image filename)
	 *	-sourcesMap: source label (String) to DataSource 
	 *	-factoriesMap: factory label (String) to MapsFactory
	 */
	public Map[] getMapProperties() throws IOException, FileNotFoundException {
		return propertiesMaps;
	}

	/**
	 * Gets the java.util.Map with key = severity label and value the Severity
	 * corresponding to the label
	 * 
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public Map getSeveritiesMap() throws IOException, FileNotFoundException {
		return severitiesMap;
	}

	/**
	 * Gets the java.util.Map with key = availability label and value the Avail
	 * corresponding to the label
	 * 
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public Map getAvailabilitiesMap() throws IOException, FileNotFoundException {
		return availsMap;
	}

	public Avail getAvail(double avail) {
		if (avail < 0)
			avail = -1;
		Avail rightAv = null;
		int bestfound = -1;
		Iterator ite = availsMap.values().iterator();
		while (ite.hasNext()) {
			Avail av = (Avail) ite.next();
			if (avail > av.getMin() && avail >= bestfound) {
				rightAv = av;
				bestfound = av.getMin();
			}
		}
		return rightAv;
	}

	public Avail getDisabledAvail() throws IOException, FileNotFoundException {

		Properties props = new Properties();
		props.load(new FileInputStream(MapPropertiesFactory.mapPropertiesFile));
		String disableAvailId = props.getProperty("avail.enable.false.id");
		if (disableAvailId == null) {
			throw new IllegalStateException(
					"Required Default Status not found.");
		}
		Iterator ite = availsMap.values().iterator();
		while (ite.hasNext()) {
			Avail av = (Avail) ite.next();
			if (av.getId() == Integer.parseInt(disableAvailId))
				return av;
		}
		return null;
	}

	public boolean enableAvail() throws IOException, FileNotFoundException {
		Properties props = new Properties();
		props.load(new FileInputStream(MapPropertiesFactory.mapPropertiesFile));
		String enableAvail = props.getProperty("avail.enable");
		if (enableAvail != null && enableAvail.equalsIgnoreCase("false"))
			return false;
		return true;
	}

	/**
	 * Gets the 'nodeup' status in map.properties. nodeup status is a required
	 * parameter.
	 * 
	 * @return nodeup status
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public Status getDefaultStatus() throws IOException, FileNotFoundException {
		Properties props = new Properties();
		props.load(new FileInputStream(MapPropertiesFactory.mapPropertiesFile));
		String defaultid = props.getProperty("status.default");
		if (defaultid == null) {
			throw new IllegalStateException(
					"Required Default Status not found.");
		}
		String id = props.getProperty("status." + defaultid + ".id");
		String uei = props.getProperty("status." + defaultid + ".uei");
		String color = props.getProperty("status." + defaultid + ".color");
		String text = props.getProperty("status." + defaultid + ".text");
		Status st = new Status(Integer.parseInt(id), uei, color, text);
		return st;
	}

	/**
	 * Gets the 'undefined' status in map.properties. nodeup status is a
	 * required parameter.
	 * 
	 * @return nodeup status
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public Status getUnknownUeiStatus() throws IOException,
			FileNotFoundException {
		Properties props = new Properties();
		props.load(new FileInputStream(MapPropertiesFactory.mapPropertiesFile));
		String defaultid = props.getProperty("status.unknown.uei");
		if (defaultid == null) {
			throw new IllegalStateException(
					"Required Unknown Uei Status not found.");
		}
		String id = props.getProperty("status." + defaultid + ".id");
		String uei = props.getProperty("status." + defaultid + ".uei");
		String color = props.getProperty("status." + defaultid + ".color");
		String text = props.getProperty("status." + defaultid + ".text");
		Status st = new Status(Integer.parseInt(id), uei, color, text);
		return st;
	}

	/**
	 * Gets the 'normal' severity in map.properties. Normal severity is a
	 * required parameter.
	 * 
	 * @return Normal severity
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public Severity getDefaultSeverity() throws IOException,
			FileNotFoundException {
		Properties props = new Properties();
		props.load(new FileInputStream(MapPropertiesFactory.mapPropertiesFile));
		String defaultid = props.getProperty("severity.default");
		if (defaultid == null) {
			throw new IllegalStateException(
					"Required Default Severity not found.");
		}

		String id = props.getProperty("severity." + defaultid + ".id");
		String label = props.getProperty("severity." + defaultid + ".label");
		String color = props.getProperty("severity." + defaultid + ".color");
		Severity se = new Severity(Integer.parseInt(id), label, color);
		return se;
	}

	/**
	 * Gets the 'indeterminate' severity in map.properties. Indeterminate
	 * severity is a required parameter.
	 * 
	 * @return Indeterminate Severity
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public Severity getIndeterminateSeverity() throws IOException,
			FileNotFoundException {
		Properties props = new Properties();
		props.load(new FileInputStream(MapPropertiesFactory.mapPropertiesFile));
		String indeterminateId = props.getProperty("severity.indeterminate");
		if (indeterminateId == null) {
			throw new IllegalStateException(
					"Required Indeterminate Severity not found.");
		}

		String id = props.getProperty("severity." + indeterminateId + ".id");
		String label = props.getProperty("severity." + indeterminateId
				+ ".label");
		String color = props.getProperty("severity." + indeterminateId
				+ ".color");
		Severity se = new Severity(Integer.parseInt(id), label, color);
		return se;
	}

	/**
	 * Gets the 'undefined' Avilability in map.properties. Undefined
	 * Availability is a required parameter.
	 * 
	 * @return Undefined Availability
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public Avail getUndefinedAvail() throws IOException, FileNotFoundException {
		Properties props = new Properties();
		props.load(new FileInputStream(MapPropertiesFactory.mapPropertiesFile));

		String id = props.getProperty("avail.undefined.id");
		String min = props.getProperty("avail.undefined.min");
		String color = props.getProperty("avail.undefined.color");
		if (id == null || min == null || color == null) {
			throw new IllegalStateException(
					"Required avail.undefined properties not found.");
		}
		Avail av = new Avail(Integer.parseInt(id), Integer.parseInt(min), color);
		return av;
	}

	/**
	 * Gets the java.util.Map with key = uei and value the status corresponding
	 * to the uei
	 * 
	 * @return java.util.Map with key = uei and value the status corresponding
	 *         to the uei
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public Map getStatusesMap() throws IOException, FileNotFoundException {
		return statusesMap;
	}

	public String getInfo() {
		return (MapPropertiesFactory.info);
	}

	
	public String getSeverityMapAs() {
		return severityMapAs;
	}

	public static String getProperty(String key)throws FileNotFoundException,IOException{
		// read the file
		Properties props = new Properties();
		props.load(new FileInputStream(MapPropertiesFactory.mapPropertiesFile));
		return props.getProperty(key);
	}
	
	/**
	 * Parses the map.properties file into some mappings: 
	 *  -severitiesMap: severity label (String) to Severity
	 *	-statusesMap: status (String) uei to Status
	 * 	-availsMap: min (String) of avail to Avail
	 *	-iconsMap: icon (String) label to String (icon filename)
	 *	-bgImagesMap: background (String) image label to String (background image filename)
	 *	-sourcesMap: source label (String) to DataSource 
	 *	-factoriesMap: factory label (String) to MapsFactory
	 */
	protected static Map[] parseMapProperties() throws FileNotFoundException,
			IOException {
		log.debug("Parsing map.properties...");
		severitiesMap = new HashMap();
		statusesMap = new HashMap();
		availsMap = new HashMap();
		iconsMap = new HashMap();
		bgImagesMap = new HashMap();
		sourcesMap = new HashMap();
		factoriesMap = new HashMap();

		// read the file
		Properties props = new Properties();
		props.load(new FileInputStream(MapPropertiesFactory.mapPropertiesFile));

		// looks up for sources and their properties
		if(props.containsKey("sources")){
			String[] sources = BundleLists.parseBundleList(props
					.getProperty("sources"));
			for (int i = 0; i < sources.length; i++) {
				log.debug("---found source " + sources[i] + "---");
				// load nodes' filters
				List filterList = new ArrayList();
				if(props.containsKey("source." + sources[i] + ".filters")){
					String[] filters = BundleLists.parseBundleList(props
							.getProperty("source." + sources[i] + ".filters"));
					for (int filterCounter = 0; filterCounter < filters.length; filterCounter++) {
						String table = props.getProperty("source." + sources[i]
								+ ".filter." + filters[filterCounter] + ".table");
						String condition = props.getProperty("source." + sources[i]
								+ ".filter." + filters[filterCounter] + ".condition");
						log.debug("source." + sources[i] + ".filter."
								+ filters[filterCounter] + ".table=" + table);
						log.debug("source." + sources[i] + ".filter."
								+ filters[filterCounter] + ".field=" + condition);
						Filter f = new Filter(table, condition);
						filterList.add(f);
					}
				}
	
				// load the datasource class to get data
				String implClass = props.getProperty("source." + sources[i]
						+ ".class");
				if(implClass==null){
					log.error("The property 'class' is mandatory for each source! skipping source "+sources[i]);
					continue;
				}
				log.debug("source." + sources[i] + ".class=" + implClass);
	
				// load datasource params
				HashMap paramsMap = new HashMap();
				if(props.containsKey("source." + sources[i] + ".params")){
					String[] params = BundleLists.parseBundleList(props
							.getProperty("source." + sources[i] + ".params"));
					for (int paramCounter = 0; paramCounter < params.length; paramCounter++) {
						String param = props.getProperty("source." + sources[i]
								+ ".param." + params[paramCounter]);
						paramsMap.put(params[paramCounter], param);
						log.debug("source." + sources[i] + ".param."
								+ params[paramCounter] + "=" + param);
					}
				}
				
				DataSource ds = new DataSource(sources[i], implClass, paramsMap,
						(Filter[]) filterList.toArray(new Filter[0]));
				sourcesMap.put(sources[i], ds);
				log.debug("---end of source " + sources[i] + "---");
			}
		}
		// look up factories and their properties
		defaultFactory = props.getProperty("default.factory");
		if(defaultFactory==null){
			log.fatal("The property 'default.factory' is mandatory");
			throw new IllegalStateException("The property 'default.factory' is mandatory");
		}
		
		String[] factories = BundleLists.parseBundleList(props
				.getProperty("factories"));
		for (int i = 0; i < factories.length; i++) {
			log.debug("---found map factory " + factories[i] + "---");
			
			// load the map manager class to delete/save maps and elements
			String managerClass = props.getProperty("factory." + factories[i]
					+ ".managerclass");
			if(managerClass==null){
				log.error("The property 'managerclass' is mandatory for each map factory! skipping factory "+factories[i]);
				continue;
			}
			log.debug("factory." + factories[i] + ".managerclass=" + managerClass);
			
			// load the map manager class to delete/save maps and elements
			String dataSource = props.getProperty("factory." + factories[i]
					+ ".source");
			
			log.debug("factory." + factories[i] + ".source=" + dataSource);
			// load map factory params
			HashMap paramsMap = new HashMap();
			if(props.containsKey("factory." + factories[i] + ".params")){
				String[] params = BundleLists.parseBundleList(props
						.getProperty("factory." + factories[i] + ".params"));
				for (int paramCounter = 0; paramCounter < params.length; paramCounter++) {
					String param = props.getProperty("factory." + factories[i]
							+ ".param." + params[paramCounter]);
					paramsMap.put(params[paramCounter], param);
					log.debug("factory." + factories[i] + ".param."
							+ params[paramCounter] + "=" + param);
				}
			}
			// load the modifymaps flag
			String adminmodify = props.getProperty("factory." + factories[i]
					+ ".adminmodify");
			boolean adminMod = true;
			if(adminmodify!=null && adminmodify.equalsIgnoreCase("false"))
				adminMod=false;
			log.debug("factory." + factories[i] + ".adminmodify=" + adminMod);
			
			//load the modifymaps flag
			String allmodify = props.getProperty("factory." + factories[i]
					+ ".allmodify");
			boolean allMod = false;
			if(allmodify!=null && allmodify.equalsIgnoreCase("true"))
				allMod=true;
			log.debug("factory." + factories[i] + ".allmodify=" + allMod);

			//load reload flag
			String reload = props.getProperty("factory." + factories[i]
					+ ".reload");
			boolean rel = false;
			if(reload!=null && reload.equalsIgnoreCase("true"))
				rel=true;
			log.debug("factory." + factories[i] + ".reload=" + rel);
			
			//load context menu flag
			String cntxtmenu = props.getProperty("factory." + factories[i]
					+ ".contextmenu");
			boolean cntxt = true;
			if(cntxtmenu!=null && cntxtmenu.equalsIgnoreCase("false"))
				cntxt=false;
			log.debug("factory." + factories[i] + ".contextmenu=" + cntxt);			

			//load double click flag
			String doubleclick = props.getProperty("factory." + factories[i]
					+ ".doubleclick");
			boolean dbclick = true;
			if(doubleclick!=null && doubleclick.equalsIgnoreCase("false"))
				dbclick=false;
			log.debug("factory." + factories[i] + ".doubleclick=" + dbclick);			
			
			ContextMenu contMenu = null;
			if(cntxt){
				String commandList = props.getProperty("factory."+factories[i]+".cmenu.commands");
				if(commandList!=null){
					String[] commands = BundleLists.parseBundleList(commandList);
					if(commands!=null){
						contMenu = new ContextMenu();
						for (int j = 0; j < commands.length;j++) {
							String link = "-";
							String params = "-";
							if(!commands[j].equals("-")){
								//load the link to open for the command
								link = props.getProperty("factory." + factories[i]+ ".cmenu."+commands[j]+".link");
								log.debug("factory." + factories[i]+ ".cmenu."+commands[j]+".link="+link);
								if(link==null){
									log.warn("link is null! skipping..");
									continue;
								}
								params = props.getProperty("factory." + factories[i]+ ".cmenu."+commands[j]+".params");						
								log.debug("factory." + factories[i]+ ".cmenu."+commands[j]+".params="+params);
								if(params==null) params="";
							}
							contMenu.addEntry(commands[j], link, params);
						}
					}
				}else{
					log.warn("Context Menu enabled and No command found for factory "+factories[i]);
				}
			}else{
				log.debug("contextMenu disabled for the factory "+factories[i] +": skipping...");
			}
			MapsFactory df = new MapsFactory( managerClass, adminMod, allMod, rel, cntxt, contMenu, dbclick , dataSource, paramsMap, factories[i]);
			factoriesMap.put(factories[i], df);
			log.debug("---end of map factory " + factories[i] + "---");
		}
		// look up severities and their properties
		severityMapAs=props.getProperty("severity.map", "avg");
		
		String[] severities = BundleLists.parseBundleList(props
				.getProperty("severities"));

		for (int i = 0; i < severities.length; i++) {
			String id = props.getProperty("severity." + severities[i] + ".id");
			String label = props.getProperty("severity." + severities[i]
					+ ".label");
			String color = props.getProperty("severity." + severities[i]
					+ ".color");
			String flash = props.getProperty("severity." + severities[i]
					+ ".flash");
			Severity sev = new Severity(Integer.parseInt(id), label, color);
			if (flash != null && flash.equalsIgnoreCase("true"))
				sev.setFlash(true);
			log.debug("found severity " + severities[i] + " with id=" + id
					+ ", label=" + label + ", color=" + color + ". Adding it.");
			severitiesMap.put(label, sev);
		}
		orderedSeverities = new Severity[severitiesMap.size()];
		Iterator it = severitiesMap.values().iterator();
		int k = 0;
		while (it.hasNext()) {
			orderedSeverities[k++] = (Severity) it.next();
		}
		Arrays.sort(orderedSeverities);

		// look up statuses and their properties
		String[] statuses = BundleLists.parseBundleList(props
				.getProperty("statuses"));

		for (int i = 0; i < statuses.length; i++) {
			String id = props.getProperty("status." + statuses[i] + ".id");
			String uei = props.getProperty("status." + statuses[i] + ".uei");
			String color = props
					.getProperty("status." + statuses[i] + ".color");
			String text = props.getProperty("status." + statuses[i] + ".text");
			log.debug("found status " + statuses[i] + " with id=" + id
					+ ", uei=" + uei + ", color=" + color + ", text=" + text
					+ ". Adding it.");
			Status status = new Status(Integer.parseInt(id), uei, color, text);
			statusesMap.put(uei, status);
		}

		orderedStatuses = new Status[statusesMap.size()];
		it = statusesMap.values().iterator();
		k = 0;
		while (it.hasNext()) {
			orderedStatuses[k++] = (Status) it.next();
		}
		Arrays.sort(orderedStatuses);

		// look up statuses and their properties
		String[] availes = BundleLists.parseBundleList(props
				.getProperty("availabilities"));

		for (int i = 0; i < availes.length; i++) {
			String id = props.getProperty("avail." + availes[i] + ".id");
			String min = props.getProperty("avail." + availes[i] + ".min");
			String color = props.getProperty("avail." + availes[i] + ".color");
			String flash = props.getProperty("avail." + availes[i] + ".flash");
			log.debug("found avail " + availes[i] + " with id=" + id + ", min="
					+ min + ", color=" + color + ". Adding it.");
			Avail avail = new Avail(Integer.parseInt(id),
					Integer.parseInt(min), color);
			if (flash != null && flash.equalsIgnoreCase("true"))
				avail.setFlash(true);
			availsMap.put(min, avail);
		}

		orderedAvails = new Avail[availsMap.size()];
		it = availsMap.values().iterator();
		k = 0;
		while (it.hasNext()) {
			orderedAvails[k++] = (Avail) it.next();
		}
		Arrays.sort(orderedAvails);

		// look up icons filenames
		String[] icons = BundleLists
				.parseBundleList(props.getProperty("icons"));

		for (int i = 0; i < icons.length; i++) {
			String filename = props.getProperty("icon." + icons[i]
					+ ".filename");
			log.debug("found icon " + icons[i] + " with filename=" + filename
					+ ". Adding it.");
			iconsMap.put(icons[i], filename);
		}

		// look up background filenames
		String[] bg = BundleLists
				.parseBundleList(props.getProperty("bgimages"));

		for (int i = 0; i < bg.length; i++) {
			String filename = props.getProperty("bgimage." + bg[i]
					+ ".filename");
			log.debug("found bgimage " + bg[i] + " with filename=" + filename
					+ ". Adding it.");
			bgImagesMap.put(bg[i], filename);
		}
		propertiesMaps = new Map[] { severitiesMap, statusesMap, availsMap,
				iconsMap, bgImagesMap, sourcesMap , factoriesMap};

		return (propertiesMaps);
	}

	public Map getSourcesMap() {
		return sourcesMap;
	}
	
	public Map getFactoriesMap() {
		return factoriesMap;
	}

	public Map getIconsMap() {
		return iconsMap;
	}

	public Map getBackgroundImagesMap() {
		return bgImagesMap;
	}

	public String getDefaultMapIcon() throws IOException, FileNotFoundException {
		Properties props = new Properties();
		props.load(new FileInputStream(MapPropertiesFactory.mapPropertiesFile));
		String defaultMapIcon = props.getProperty("icon.default.map");
		if (defaultMapIcon == null) {
			throw new IllegalStateException(
					"Required Default Icon Map not found.");
		}
		return defaultMapIcon;
	}

	public String getDefaultNodeIcon() throws IOException,
			FileNotFoundException {
		Properties props = new Properties();
		props.load(new FileInputStream(MapPropertiesFactory.mapPropertiesFile));
		String defaultNodeIcon = props.getProperty("icon.default.node");
		if (defaultNodeIcon == null) {
			throw new IllegalStateException(
					"Required Default Icon Node not found.");
		}
		return defaultNodeIcon;
	}

	/**
	 * Gets the array of ordered Severity by id.
	 * 
	 * @return
	 */
	public Severity[] getOrderedSeverities() {
		return orderedSeverities;
	}

	/**
	 * Gets the array of ordered Avail by min.
	 * 
	 * @return
	 */
	public Avail[] getOrderedAvails() {
		return orderedAvails;
	}

	/**
	 * Gets the array of ordered Status by id.
	 * 
	 * @return
	 */
	public Status[] getOrderedStatuses() {
		return orderedStatuses;
	}

	/**
	 * return the Class of the first dataSource (of map.properties file)
	 * matching with the nodeid in input
	 * 
	 * @param nodeid
	 * @return the Class of the first dataSource (of map.properties file)
	 *         matching with the nodeid in input, null if there are no matches.
	 */
	public Class getDataSourceClass(int nodeid){
		Class ds = null;
		if(nodesPerSource!=null){
			Iterator it = nodesPerSource.entrySet().iterator();
			while(it.hasNext()){
				Entry entry = (Entry)it.next();
				String dsName = (String)entry.getKey();
				Set val = (Set)entry.getValue();
				if(val.contains(new Integer(nodeid))){
					DataSource dsource = (DataSource)sourcesMap.get(dsName);
					try{
						ds=Class.forName(dsource.getImplClass());
					}catch(ClassNotFoundException e){
						log.error(e);
					}
					break;
				}
			}
		}
		return ds;
	}

	/*
	 * return the first DataSource (of map.properties file)
	 * matching with the nodeid in input
	 * 
	 * @param nodeid
	 * @return the first DataSource (of map.properties file)
	 *         matching with the nodeid in input, null if there are no matches.
	 
	public DataSource getDataSource(int nodeid){
		DataSource ds = null;
		if(nodesPerSource!=null){
			Iterator it = nodesPerSource.entrySet().iterator();
			while(it.hasNext()){
				Entry entry = (Entry)it.next();
				String dsName = (String)entry.getKey();
				Set val = (Set)entry.getValue();
				if(val.contains(new Integer(nodeid))){
					ds = (DataSource)sourcesMap.get(dsName);
					break;
				}
			}
		}
		return ds;
	}*/
	
	public DataSource getDataSource(String dataSourceLabel){
		if(sourcesMap!=null){
			return (DataSource)sourcesMap.get(dataSourceLabel);
		}
		return null;
	}
	
	public Set getNodeIdsBySource(String sourceLabel)throws MapsException{
		DataSource dataSource = (DataSource) sourcesMap.get(sourceLabel);
		Filter[] filters = dataSource.getFilters();
		if(filters==null || filters.length==0){
			return getAllNodes();
		}
		Connection conn=null;
		Statement stmt = null;
		ResultSet rs = null;
		HashSet nodes = new HashSet();
		try {
			conn = Vault.getDbConnection();
			String sqlQuery = null;
			// contructs and execute the query
			
			sqlQuery = "select distinct " + filters[0].table + ".nodeid from ";
			for (int i = 0; i < filters.length; i++) {
				sqlQuery += filters[i].table;
				if (i < filters.length - 1) {
					sqlQuery += ",";
				}
			}
			sqlQuery += " where ";
			for (int i = 0; i < filters.length; i++) {
				sqlQuery += filters[i].condition;
				if (i < filters.length - 1) {
					sqlQuery += " AND ";
				}
			}
			log.debug("Applying filters for source "+sourceLabel+" '"+sqlQuery+"'");
			
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlQuery);
			// add all matching nodes (id) with the source to the Set.
			while (rs.next()) {
				nodes.add(new Integer(rs.getInt(1)));
			}
			rs.close();
			stmt.close();
			conn.close();
		}catch(Exception e){
			throw new MapsException("Exception while getting nodes by source label "+e);
		}	
		return nodes;
	}
	
	private Set getAllNodes()throws MapsException{
		Connection conn=null;
		Statement stmt = null;
		ResultSet rs = null;
		HashSet nodes = new HashSet();
		try {
			conn = Vault.getDbConnection();
			String sqlQuery = "select distinct nodeid from ipinterface";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlQuery);
			// add all matching nodes (id) with the source to the Set.
			while (rs.next()) {
				nodes.add(new Integer(rs.getInt(1)));
			}
			rs.close();
			stmt.close();
			conn.close();
		}catch(Exception e){
			throw new MapsException("Exception while getting all nodes "+e);
		}
		return nodes;
	}
	/*private static void reloadNodesPerSource() {
		nodesPerSource = new HashMap();
		Connection conn=null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = Vault.getDbConnection();
			Iterator it = sourcesMap.keySet().iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				DataSource dataSource = (DataSource) sourcesMap.get(key);
				Filter[] filters = dataSource.getFilters();
				String sqlQuery = null;
				// contructs and execute the query
				if (filters != null) {
					sqlQuery = "select distinct " + filters[0].table + ".nodeid from ";
					for (int i = 0; i < filters.length; i++) {
						sqlQuery += filters[i].table;
						if (i < filters.length - 1) {
							sqlQuery += ",";
						}
					}
					sqlQuery += " where ";
					for (int i = 0; i < filters.length; i++) {
						sqlQuery += filters[i].condition;
						if (i < filters.length - 1) {
							sqlQuery += " AND ";
						}
					}
					log.debug("Applying filters for source "+key+" '"+sqlQuery+"'");
					HashSet nodes = new HashSet();
					stmt = conn.createStatement();
					rs = stmt.executeQuery(sqlQuery);
					// add all matching nodes (id) with the source to the Set.
					while (rs.next()) {
						nodes.add(new Integer(rs.getInt(1)));
					}
					nodesPerSource.put(key, nodes);
				}
			}
		} catch (Exception s) {
			log.error(s);
			throw new RuntimeException(s);
		} finally {
			try {
				rs.close();
				stmt.close();
				Vault.releaseDbConnection(conn);
			} catch (Exception e) {
				log.error(e);
				throw new RuntimeException(e);
			}
		}
		Iterator it = nodesPerSource.keySet().iterator();
		while(it.hasNext()){
			String sourceName = (String)it.next();
			HashSet nodes = (HashSet) nodesPerSource.get(sourceName);
			log.debug("found association source/nodes -> "+sourceName+"/"+nodes.toString());
		}

	}*/

	/**
	 * gets the MapsFactory by the factoryLabel in input
	 * @param factoryLabel
	 * @return MapsFactory by the factoryLabel in input
	 */
	public MapsFactory getMapsFactory(String factoryLabel){
		MapsFactory result = null;
		if(factoriesMap!=null){
			result=(MapsFactory)factoriesMap.get(factoryLabel);
		}
		return result;
	}

	/**
	 * gets the default MapsFactory
	 * @return the default MapsFactory
	 */
	public MapsFactory getDefaultFactory() {
		MapsFactory mf=null;
		if(defaultFactory!=null && factoriesMap!=null){
			mf =(MapsFactory)factoriesMap.get(defaultFactory);
		}
		return mf;
	}

    public int getSeverity(String severityLabel) {
    	Severity sev = ((Severity)severitiesMap.get(severityLabel));
    	if(sev==null){
    		throw new IllegalStateException("Severity with label "+severityLabel+" not found.");
    	}
    	return sev.getId();
    }

    public int getStatus(String uei) {
    	
    	Status status = (Status)statusesMap.get(uei);
    	if(status==null){
    		try {
        		return getUnknownUeiStatus().getId();
    		} catch (Exception e) {
    			throw new RuntimeException("Exception while getting unknown status "+e);
			}
    	}
    	return status.getId();
    }

	
}
