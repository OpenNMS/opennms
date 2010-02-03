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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Category;
import org.opennms.core.utils.BundleLists;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.map.MapsConstants;
import org.opennms.web.map.MapsException;

public class MapPropertiesFactory extends Object {

	private boolean m_loaded = false;

	/**
	 * Descriptive information about this factory.
	 */
	protected static final String info = "org.opennms.web.map.config.MapPropertiesFactory";

	/**
	 * Descriptive information about this factory.
	 */
	protected static final String name = "MapPropertiesFactory";

	/**
	 * The map.properties file that is read for the list of severities and
	 * statuses settings for map view.
	 */
	protected File mapPropertiesFile;

	protected String mapPropertiesFileString;

	/**
	 * The Log4J category for logging web authentication messages.
	 */
	protected Category log = null;

//	protected Map[] propertiesMaps = null;

	protected  Map<String,Status> statusesMap = null;

	protected  Status[] orderedStatuses = null;

	protected  Map<String,Severity> severitiesMap = null;

	protected  Severity[] orderedSeverities = null;

	protected  Map<String,Avail> availsMap = null;

	protected  Avail[] orderedAvails = null;

	protected  Map<String,Icon> iconsMap = null;

	protected Map<String,String> iconsBySysoidMap = null;
	
	protected  Map<String,String> bgImagesMap = null;

	protected  Map<Integer,Link> linksMap = null;
	
	protected  Map<Integer,Set<Link>> linksBySnmpTypeMap = null;
	
	protected  Map<String,LinkStatus> linkStatusesMap = null;

	protected  String defaultNodeIcon = null;
	
	protected  String defaultMapIcon = null;
	
    protected  int defaultMapElementDimension = 25;

	public static final  String MULTILINK_BEST_STATUS ="best"; 
	
	public static final  String MULTILINK_WORST_STATUS ="worst";
	
	protected  String multilinkStatus = MULTILINK_BEST_STATUS;
	
	protected  int defaultLink = -1;

	protected  Severity defaultSeverity;
	
	protected  Severity indeterminateSeverity;
	
	protected  Status unknownStatus;
	
	protected  Status defaultStatus;
	
	protected  Avail undefinedAvail;
	
	protected  Avail disabledAvail;
	
	protected  boolean availEnabled=true;
	
	protected  boolean doubleClickEnabled=true;
	
	protected  boolean contextMenuEnabled=true;
	
	protected  boolean reload=false;

	protected  String severityMapAs = "avg"; 

	protected  ContextMenu cmenu;
	
	protected String mapScale ="disabled"; 
	
	
	public String getMapPropertiesFileString() {
		return mapPropertiesFileString;
	}

	public void setMapPropertiesFileString(String mapPropertiesFileString) {
		this.mapPropertiesFileString = mapPropertiesFileString;
	}

	public String getSeverityMapAs() {
		return severityMapAs;
	}

	public ContextMenu getContextMenu() {
		return cmenu;
	}

	public void setContextMenu(ContextMenu cmenu) {
		this.cmenu = cmenu;
	}

	public boolean isContextMenuEnabled() {
		return contextMenuEnabled;
	}

	public boolean isDoubleClickEnabled() {
		return doubleClickEnabled;
	}

	public boolean isReload() {
		return reload;
	}

	
	public MapPropertiesFactory(String mapPropertiesFileString) {
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());

		this.mapPropertiesFileString = mapPropertiesFileString;

		if (log.isDebugEnabled())
			log.debug("Map Properties Configuration file: " + mapPropertiesFileString);

		try {
			init();
		} catch (FileNotFoundException e) {
			log.error("Cannot found configuration file",e);
		} catch (IOException e) {
			log.error("Cannot load configuration file",e);
		}
		if(log.isDebugEnabled())
			log.debug("Instantiating MapPropertiesFactory with properties file: "+mapPropertiesFileString);
	}

	/**
	 * Create a new instance.
	 */
	public MapPropertiesFactory() {
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());
		
		try {
			init();
		} catch (FileNotFoundException e) {
			log.error("Cannot found configuration file",e);
		} catch (IOException e) {
			log.error("Cannot load configuration file",e);
		}
		if(log.isDebugEnabled())
			log.debug("Instantiating MapPropertiesFactory");
	}
	


	public synchronized void init() throws FileNotFoundException,
			IOException {
		
		log.info("Init");
		if (mapPropertiesFileString == null) {
			mapPropertiesFile = ConfigFileConstants.getFile(ConfigFileConstants.MAP_PROPERTIES_FILE_NAME);
			log.info("Using default map properties file: "+mapPropertiesFile.getPath());
		}else{ 		
			mapPropertiesFile = new File(mapPropertiesFileString);
			log.info("Using map properties file: "+mapPropertiesFile.getPath());
		}

		if (m_loaded) {
			// init already called - return
			// to reload, reload() will need to be called
			return;
		}

		parseMapProperties();
		m_loaded = true;
	}

	/**
	 * Every time called, reload the properties file and the nodes per data source defined in the properties file.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public synchronized void reload(boolean reloadPropertiesFile) throws FileNotFoundException,
			IOException {
		if(reloadPropertiesFile){
			m_loaded = false;
		}
		init();
	}

	/**
	 * Gets the java.util.Map with key = severity label and value the Severity
	 * corresponding to the label
	 * 
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public Map<String,Severity> getSeveritiesMap() {
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
	public Map<String, Avail> getAvailabilitiesMap() {
		return availsMap;
	}

	public Avail getAvail(double avail) {
		if (avail < 0)
			avail = -1;
		Avail rightAv = null;
		int bestfound = -1;
		Iterator<Avail> ite = availsMap.values().iterator();
		while (ite.hasNext()) {
			Avail av = ite.next();
			if (avail > av.getMin() && avail >= bestfound) {
				rightAv = av;
				bestfound = av.getMin();
			}
		}
		return rightAv;
	}

	public Avail getDisabledAvail() {
		return disabledAvail;
	}

	public boolean isAvailEnabled(){
		return availEnabled;
	}

	/**
	 * Gets the default status in map.properties. default status is a required
	 * parameter.
	 * 
	 * @return default status
	 */
	public Status getDefaultStatus() {
		return defaultStatus;
	}

	/**
	 * Gets the unknown status in map.properties. unknown status is a
	 * required parameter.
	 * 
	 * @return unknown status
	 */
	public Status getUnknownStatus()  {
		return unknownStatus;
	}

	/**
	 * Gets the default severity in map.properties. default severity is a
	 * required parameter.
	 * 
	 * @return default severity
	 */
	public Severity getDefaultSeverity() {
		return defaultSeverity;
	}

	/**
	 * Gets the indeterminate severity in map.properties. Indeterminate
	 * severity is a required parameter.
	 * 
	 * @return Indeterminate Severity
	 */
	public Severity getIndeterminateSeverity() {
		return indeterminateSeverity;
	}

	/**
	 * Gets the undefined Avilability in map.properties. Undefined
	 * Availability is a required parameter.
	 * 
	 * @return Undefined Availability
	 */
	public Avail getUndefinedAvail() {
		return undefinedAvail;
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
	public Map<String, Status> getStatusesMap() {
		return statusesMap;
	}

	public String getInfo() {
		return (info);
	}

	public String getProperty(String key)throws FileNotFoundException,IOException{
		// read the file
		Properties props = new Properties();
		props.load(new FileInputStream(mapPropertiesFile));
		return props.getProperty(key);
	}
	
	/**
	 * Parses the map.properties file into some mappings: 
	 *  -severitiesMap: severity label (String) to Severity
	 *	-statusesMap: status (String) uei to Status
	 * 	-availsMap: min (String) of avail to Avail
	 *	-iconsMap: icon (String) label to String (icon filename)
	 *	-iconsBySysoidMap: sysoid (String) to icon label (String)
	 *	-bgImagesMap: background (String) image label to String (background image filename)
	 *	-sourcesMap: source label (String) to DataSource 
	 *	-factoriesMap: factory label (String) to MapsFactory
	 */
	protected void parseMapProperties() throws FileNotFoundException,
			IOException {
		log.debug("Parsing map.properties...");
		severitiesMap = new HashMap<String,Severity>();
		statusesMap = new HashMap<String,Status>();
		availsMap = new HashMap<String,Avail>();
		iconsMap = new HashMap<String,Icon>();
		iconsBySysoidMap = new HashMap<String,String>();
		bgImagesMap = new HashMap<String,String>();
		linksMap = new HashMap<Integer,Link>();
		linksBySnmpTypeMap = new HashMap<Integer,Set<Link>>();
		linkStatusesMap = new HashMap<String,LinkStatus>();

		// read the file
		Properties props = new Properties();
		props.load(new FileInputStream(mapPropertiesFile));
		
		//load context menu flag
		String cntxtmenu = props.getProperty("enable.contextmenu");
		if(cntxtmenu!=null && cntxtmenu.equalsIgnoreCase("false"))
			contextMenuEnabled=false;
		if (log.isDebugEnabled())
			log.debug("enable.contextmenu=" + cntxtmenu);			
		// load context menu object only if context menu is enabled
		
		cmenu = new ContextMenu();
		if (contextMenuEnabled) {
			String commandList = props.getProperty("cmenu.commands");
			if(commandList!=null){
				String[] commands = BundleLists.parseBundleList(commandList);
				if(commands!=null){
					for (int j = 0; j < commands.length;j++) {
						String link = "-";
						String params = "-";
						if(!commands[j].equals("-")){
							//load the link to open for the command
							link = props.getProperty("cmenu."+commands[j]+".link");
							if (log.isDebugEnabled())
							log.debug("cmenu."+commands[j]+".link="+link);
							if(link==null){
								log.warn("link is null! skipping..");
								continue;
							}
							params = props.getProperty("cmenu."+commands[j]+".params");						
							if (log.isDebugEnabled())
								log.debug("cmenu."+commands[j]+".params="+params);
							if(params==null) params="";
						}
						cmenu.addEntry(commands[j], link, params);
					}
				}
			}else{
				log.warn("Context Menu enabled but No command found!");
			}

		}
		
		//load double click flag
		String doubleclick = props.getProperty("enable.doubleclick");
		if(doubleclick!=null && doubleclick.equalsIgnoreCase("false"))
			doubleClickEnabled=false;
		if (log.isDebugEnabled())
			log.debug("enable.doubleclick=" + doubleclick);			
		
		// load reload flag
		String reloadStr = props.getProperty("enable.reload");
		if(reloadStr!=null && reloadStr.equalsIgnoreCase("true"))
			reload=true;
		
		if (log.isDebugEnabled())
			log.debug("enable.reload=" + reloadStr);			
		
		// look up severities and their properties
		severityMapAs=props.getProperty("severity.map", "avg");

		// look up severities and their properties
		
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
			Severity sev = new Severity(WebSecurityUtils.safeParseInt(id), label, color);
			if (flash != null && flash.equalsIgnoreCase("true"))
				sev.setFlash(true);
			log.debug("found severity " + severities[i] + " with id=" + id
					+ ", label=" + label + ", color=" + color + ". Adding it.");
			severitiesMap.put(label, sev);
		}
		orderedSeverities = new Severity[severitiesMap.size()];
		Iterator<Severity> it_sev = severitiesMap.values().iterator();
		int k = 0;
		while (it_sev.hasNext()) {
			orderedSeverities[k++] = it_sev.next();
		}
		Arrays.sort(orderedSeverities);

		String defaultid = props.getProperty("severity.default");
		if (defaultid == null) {
			throw new IllegalStateException(
					"Required Default Severity not found.");
		}

		String sevid = props.getProperty("severity." + defaultid + ".id");
		String sevlabel = props.getProperty("severity." + defaultid + ".label");
		String sevcolor = props.getProperty("severity." + defaultid + ".color");
		defaultSeverity = new Severity(WebSecurityUtils.safeParseInt(sevid), sevlabel, sevcolor);
		
		
		String indeterminateId = props.getProperty("severity.indeterminate");
		if (indeterminateId == null) {
			throw new IllegalStateException(
					"Required Indeterminate Severity not found.");
		}

		String indsevid = props.getProperty("severity." + indeterminateId + ".id");
		String indsevlabel = props.getProperty("severity." + indeterminateId
				+ ".label");
		String indsevcolor = props.getProperty("severity." + indeterminateId
				+ ".color");
		indeterminateSeverity = new Severity(WebSecurityUtils.safeParseInt(indsevid), indsevlabel, indsevcolor);
		
		//Links
		String[] links = BundleLists.parseBundleList(props
				.getProperty("links"));
		
		String defaultLinkStr = props.getProperty("link.default");
		if(defaultLinkStr==null){
			log.error("Mandatory property 'link.default' not found!");
			throw new IllegalStateException("The property 'link.default' is mandatory");
		}
		defaultLink = WebSecurityUtils.safeParseInt(defaultLinkStr);

		for (int i = 0; i < links.length; i++) {
			String id = props.getProperty("link." + links[i] + ".id");
			String text = props.getProperty("link." + links[i]+ ".text");
			String speed = props.getProperty("link." + links[i]+ ".speed");
			String width = props.getProperty("link." + links[i]+ ".width");
			String dasharray = props.getProperty("link." + links[i]+ ".dash-array");			
			String snmptype = props.getProperty("link." + links[i]+ ".snmptype");			
			if(id==null){
				log.error("param id for link cannot be null in map.properties: skipping link...");
				continue;
			}
			if(text==null){
				log.error("param text for link cannot be null in map.properties: skipping link...");
				continue;
			}
			if(speed==null){
				log.error("param speed for link cannot be null in map.properties: skipping link...");
				continue;
			}
			if(width==null){
				log.error("param width for link cannot be null in map.properties: skipping link...");
				continue;
			}
				
			int dash_arr=-1;
			if(dasharray!=null)
				dash_arr=WebSecurityUtils.safeParseInt(dasharray);
			
			int snmp_type=-1;
			if(snmptype!=null)
				snmp_type=WebSecurityUtils.safeParseInt(snmptype);

			Link lnk = new Link(WebSecurityUtils.safeParseInt(id), speed,text,width,dash_arr,snmp_type);
			
			log.debug("found link " + links[i] + " with id=" + id
					+ ", text=" + text+ ", speed=" + speed+ ", width=" + width+ ", dash-array=" + dasharray+ "snmp-type=" + snmp_type+". Adding it.");
			linksMap.put(new Integer(id), lnk);
			Set<Link> linkbysnmptypeSet = linksBySnmpTypeMap.get(new Integer(snmp_type));
			if(linkbysnmptypeSet==null)
				linkbysnmptypeSet=new HashSet<Link>();
			linkbysnmptypeSet.add(lnk);
			linksBySnmpTypeMap.put(new Integer(snmp_type), linkbysnmptypeSet);
		}
		
		
		
		//Links Statuses
		String[] linkStatuses = BundleLists.parseBundleList(props
				.getProperty("linkstatuses"));
		for (int i = 0; i < linkStatuses.length; i++) {
			String color = props.getProperty("linkstatus." + linkStatuses[i] + ".color");
			String flash = props.getProperty("linkstatus." + linkStatuses[i]+ ".flash");
			if(color==null){
				log.error("param color for linkstatus cannot be null in map.properties: skipping linkstatus...");
				continue;
			}
			boolean flashBool = false;
			if(flash!=null && flash.equalsIgnoreCase("false"))
				flashBool=false;			
			log.debug("found linkstatus " + linkStatuses[i] + " with color=" + color
					+ ", flash=" + flashBool+ ". Adding it.");
			LinkStatus ls = new LinkStatus(linkStatuses[i],color,flashBool);
			linkStatusesMap.put(linkStatuses[i], ls);
		}		
		
		
		if(props.getProperty("multilink.status")!=null){
			multilinkStatus = props.getProperty("multilink.status"); 	
		}
		if(!multilinkStatus.equals("best") && !multilinkStatus.equals("worst")){
			log.error("multilink.status property must be 'best' or 'worst'... using default ('best')");
			multilinkStatus=MULTILINK_BEST_STATUS;
		}
		log.debug("found multilink.status:"+multilinkStatus);
				
			
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
			Status status = new Status(WebSecurityUtils.safeParseInt(id), uei, color, text);
			statusesMap.put(uei, status);
		}

		orderedStatuses = new Status[statusesMap.size()];
		Iterator<Status> it_status = statusesMap.values().iterator();
		k = 0;
		while (it_status.hasNext()) {
			orderedStatuses[k++] = it_status.next();
		}
		Arrays.sort(orderedStatuses);

		String unknownid = props.getProperty("status.unknown.uei");
		if (unknownid == null) {
			throw new IllegalStateException(
					"Required Unknown Uei Status not found.");
		}
		String stid = props.getProperty("status." + unknownid + ".id");
		String stuei = props.getProperty("status." + unknownid + ".uei");
		String stcolor = props.getProperty("status." + unknownid + ".color");
		String sttext = props.getProperty("status." + unknownid + ".text");
		unknownStatus = new Status(WebSecurityUtils.safeParseInt(stid), stuei, stcolor, sttext);
		
		String defaultstid = props.getProperty("status.default");
		if (defaultstid == null) {
			throw new IllegalStateException(
					"Required Default Status not found.");
		}
		String staid = props.getProperty("status." + defaultstid + ".id");
		String stauei = props.getProperty("status." + defaultstid + ".uei");
		String stacolor = props.getProperty("status." + defaultstid + ".color");
		String statext = props.getProperty("status." + defaultstid + ".text");
		defaultStatus = new Status(WebSecurityUtils.safeParseInt(staid), stauei, stacolor, statext);
		
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
			Avail avail = new Avail(WebSecurityUtils.safeParseInt(id),
					WebSecurityUtils.safeParseInt(min), color);
			if (flash != null && flash.equalsIgnoreCase("true"))
				avail.setFlash(true);
			availsMap.put(min, avail);
		}

		orderedAvails = new Avail[availsMap.size()];
		Iterator<Avail> it_avail = availsMap.values().iterator();
		k = 0;
		while (it_avail.hasNext()) {
			orderedAvails[k++] = it_avail.next();
		}
		Arrays.sort(orderedAvails);
		
		String avid = props.getProperty("avail.undefined.id");
		String avmin = props.getProperty("avail.undefined.min");
		String avcolor = props.getProperty("avail.undefined.color");
		if (avid == null || avmin == null || avcolor == null) {
			throw new IllegalStateException(
					"Required avail.undefined properties not found.");
		}
		undefinedAvail = new Avail(WebSecurityUtils.safeParseInt(avid), WebSecurityUtils.safeParseInt(avmin), avcolor);

		
		String enableAvail = props.getProperty("avail.enable");
		if (enableAvail != null && enableAvail.equalsIgnoreCase("false"))
			availEnabled=false;
		else availEnabled = true;
		
		String disableAvailId = props.getProperty("avail.enable.false.id");
		if (disableAvailId == null) {
			throw new IllegalStateException(
					"Required Default Status not found.");
		}
		Iterator<Avail> ite = availsMap.values().iterator();
		while (ite.hasNext()) {
			Avail av = ite.next();
			if (av.getId() == WebSecurityUtils.safeParseInt(disableAvailId)){
				disabledAvail=av;
				break;
			}
		}

		// look up icons filenames
		
		String[] icons = BundleLists
				.parseBundleList(props.getProperty("icons"));

		for (int i = 0; i < icons.length; i++) {
			
			String baseProperty = "icon." + icons[i] + ".";
			
			Icon icon = new Icon(props.getProperty(baseProperty + "filename"),
			                     props.getProperty(baseProperty + "width"),
			                     props.getProperty(baseProperty + "height"),
			                     props.getProperty(baseProperty + "semaphore.radius"),
			                     props.getProperty(baseProperty + "semaphore.x"),
			                     props.getProperty(baseProperty + "semaphore.y"),
			                     props.getProperty(baseProperty + "label.x"),
			                     props.getProperty(baseProperty + "label.y"),
			                     props.getProperty(baseProperty + "label.size"),
			                     props.getProperty(baseProperty + "label.align")
			                    );
			
			log.debug("found icon " + icons[i] + " with filename=" + icon.getFileName()
					+ ". Adding it.");
			iconsMap.put(icons[i], icon);
		}
		
		// look up sysoid icons
		if (props.getProperty("sysoids") != null && props.getProperty("sysoids") != "") {
		    String[] sysoids = BundleLists.parseBundleList(props.getProperty("sysoids"));
		
		    for (int i = 0; i < sysoids.length; i++) {
		        String iconName = props.getProperty("sysoid." + sysoids[i] + ".iconName");
		        log.debug("found sysoid " + sysoids[i] + " with iconName=" + iconName
	                        + ". Adding it.");
	            iconsBySysoidMap.put(sysoids[i], iconName);
		    }
		}
		
		defaultMapIcon = props.getProperty("icon.default.map");
		log.debug("default map icon: "+defaultMapIcon);
		if (defaultMapIcon == null) {
			throw new IllegalStateException(
					"Required Default Map Icon not found.");
		}
		defaultNodeIcon = props.getProperty("icon.default.node");
		if (defaultNodeIcon == null) {
			throw new IllegalStateException(
					"Required Default Icon Node not found.");
		}
		log.debug("default node icon: "+defaultNodeIcon);
		
		mapScale = props.getProperty("icon.default.scale");
		
		
		String defaultMapElementDimensionString = props.getProperty("icon.default.mapelementdimension");
        if (defaultMapElementDimensionString != null) {
            defaultMapElementDimension = WebSecurityUtils.safeParseInt(defaultMapElementDimensionString);
        }
        log.debug("default map element dimension: "+defaultMapElementDimension);

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

//		propertiesMaps = new Map[] { severitiesMap, statusesMap, availsMap,
//				iconsMap, bgImagesMap};

//		return (propertiesMaps);
	}


	public Map<String,Icon> getIconsMap() {
		return iconsMap;
	}
	
	public Map<String,String> getIconsBySysoidMap() {
	    return iconsBySysoidMap;
	}

	public Map<String,String> getBackgroundImagesMap() {
		return bgImagesMap;
	}

	public String getDefaultMapIcon(){
		return defaultMapIcon;
	}

	public String getDefaultNodeIcon() {
		return defaultNodeIcon;
	}

	public String getMapScale() {
	    return mapScale;
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
        		return getUnknownStatus().getId();
    		} catch (Exception e) {
    			throw new RuntimeException("Exception while getting unknown status "+e);
			}
    	}
    	return status.getId();
    }
    
    /**
     * gets the config Link by snmpType defined in the map properties config file
     * @param linkTypologyId
     * @return 
     */
    public Set<Link> getLinkBySnmpType(int linkTypologyId){
    	return linksBySnmpTypeMap.get(new Integer(linkTypologyId));
    }
    
    /**
     * gets the id corresponding to the link defined in configuration file. The match is performed first by snmptype, 
     * then by speed (if more are defined). If there is no match, the default link id is returned. 
     * @param snmpiftype
     * @param snmpifspeed
     * @return the id corresponding to the link defined in configuration file. If there is no match, the default link id is returned.
     */
    public int getLinkTypeId(int snmpiftype, long snmpifspeed) {
    	Link link=null;
    	Set<Link> linkSet = getLinkBySnmpType(snmpiftype);
    	if(linkSet==null)
    		link=getDefaultLink();
    	else{
    		if(linkSet.size()>1){
	    		Iterator<Link> it = linkSet.iterator();
	    		while(it.hasNext()){
	    			Link next = it.next();
	    			if(WebSecurityUtils.safeParseLong(next.getSpeed())==snmpifspeed){
	    				link=next;
	    				break;
	    			}
	    		}
    		}else{
    			Iterator<Link> it=linkSet.iterator();
    			if(it.hasNext()){
	    			link = it.next();
	    		}
    		}
    	}
    	if(link==null)	
    		link=getDefaultLink();
    	return link.getId();
    }    

    public Link getLink(int id){
    	return (Link)linksMap.get(new Integer(id));
    }
    
    /**
     * gets the config LinkStatus by label defined in the map properties config file
     * @param linkStatusLabel
     * @return
     */
    public LinkStatus getLinkStatus(String linkStatusLabel){
    	return (LinkStatus)linkStatusesMap.get(linkStatusLabel);
    }
    
    public Link getDefaultLink(){
    	return (Link) linksMap.get(defaultLink);
    }

	public Map<Integer,Link> getLinksMap() {
		return linksMap;
	}
	
	public Map<String, LinkStatus> getLinkStatusesMap() {
		return linkStatusesMap;
	}
    
	public  String getMultilinkStatus() {
		return multilinkStatus;
	}

    public List<Avail> getAvails() throws MapsException {
    	List<Avail> avails = new ArrayList<Avail>();
    	avails.addAll(Arrays.asList(getOrderedAvails()));
    	return avails;
    }
    
    public List<Link> getLinks() throws MapsException {
    	List<Link> links = new ArrayList<Link>();
    	links.addAll((getLinksMap().values()));
    	return links;
    }
    
    public List<LinkStatus> getLinkStatuses() throws MapsException {
    	List<LinkStatus> linkstatutes = new ArrayList<LinkStatus>();
    	linkstatutes.addAll((getLinkStatusesMap().values()));
    	return linkstatutes;
    }
    
    public List<Status> getStatuses() throws MapsException {
    	List<Status> statutes = new ArrayList<Status>();
    	statutes.addAll(getStatusesMap().values());
    	return statutes;
    }
    
    public List<Severity> getSeverities() throws MapsException {
    	List<Severity> sevs = new ArrayList<Severity>(); 
    	sevs.addAll(getSeveritiesMap().values());
    	return sevs;
    }
    
    public java.util.Map<String,Icon> getIcons() throws MapsException{
    	return getIconsMap();
    }
    
    public java.util.Map<String,String> getIconsBySysoid() throws MapsException{
        return getIconsBySysoidMap();
    }
    
    public java.util.Map<String, String> getBackgroundImages() throws MapsException {
    	return getBackgroundImagesMap();
    }
    
    public String getDefaultBackgroundColor() {
    	return "ffffff";
    }
    
    public int getDefaultStatusId() {
    	return getDefaultStatus().getId();
    }
    
    public String getDefaultSemaphoreColorBy() {
    	return MapsConstants.COLOR_SEMAPHORE_BY_SEVERITY;
    }

    public java.util.Map<String, String> getMapElementDimensions() {
    	// TODO To be implemented (via map.properties-MapPropertiesFactory)
    	java.util.Map<String, String> dims = new TreeMap<String, String>();
    	
    	dims.put("06","smallest");
    	dims.put("10","very small");
    	dims.put("15","small");
    	dims.put("20","normal");
    	dims.put("25","firefox");
    	dims.put("30","bigger");
        dims.put("35","biggest");
    	
    	return dims;
    }
    
    public int getDefaultMapElementDimension() {
    	return defaultMapElementDimension;
    }
    



}
