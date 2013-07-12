/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.web.map.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.logging.Logging;
import org.opennms.core.utils.BundleLists;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.web.map.MapsConstants;
import org.opennms.web.map.MapsException;

/**
 * <p>MapPropertiesFactory class.</p>
 * @since 1.8.1
 */
public class MapPropertiesFactory {
    
    private static final Logger LOG = LoggerFactory.getLogger(MapPropertiesFactory.class);

    private boolean m_loaded = false;

    /**
     * Descriptive information about this factory.
     */
    protected static final String info = MapPropertiesFactory.class.getName();

    /**
     * Descriptive information about this factory.
     */
    protected static final String name = MapPropertiesFactory.class.getSimpleName();

    /**
     * The map.properties file that is read for the list of severities and
     * statuses settings for map view.
     */
    protected File mapPropertiesFile;

    protected String mapPropertiesFileString;

    //	protected Map[] propertiesMaps = null;

    protected  Map<String,Status> statusesMap = null;

    protected  Status[] orderedStatuses = null;

    protected  Map<String,Severity> severitiesMap = null;

    protected  Severity[] orderedSeverities = null;

    protected  Map<String,Avail> availsMap = null;

    protected  Avail[] orderedAvails = null;

    protected  Map<String,String> iconsMap = null;

    protected Map<String,String> iconsBySysoidMap = null;

    protected  Map<String,String> bgImagesMap = null;

    protected  Map<Integer,Link> linksMap = null;

    protected  Map<Integer,Set<Link>> linksBySnmpTypeMap = null;

    protected  Map<String,LinkStatus> linkStatusesMap = null;

    protected  String defaultNodeIcon = null;

    protected  String defaultMapIcon = null;

    protected  int defaultMapElementDimension = 25;

    protected int maxLinks = 3;

    protected int summaryLink = -1;

    protected String summaryLinkColor = "yellow";

    /** Constant <code>MULTILINK_BEST_STATUS="best"</code> */
    public static final  String MULTILINK_BEST_STATUS ="best"; 

    /** Constant <code>MULTILINK_WORST_STATUS="worst"</code> */
    public static final  String MULTILINK_WORST_STATUS ="worst";

    /** Constant <code>MULTILINK_IGNORE_STATUS="ignore"</code> */
    public static final  String MULTILINK_IGNORE_STATUS ="ignore";

    protected  String multilinkStatus = MULTILINK_BEST_STATUS;

    protected String multilinkIgnoreColor = "yellow";

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

    protected boolean useSemaphore=true;

    protected  boolean reload=false;

    protected  String severityMapAs = "avg"; 

    protected  ContextMenu cmenu;

    /**
     * <p>Getter for the field <code>mapPropertiesFileString</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMapPropertiesFileString() {
        return mapPropertiesFileString;
    }

    /**
     * <p>Setter for the field <code>mapPropertiesFileString</code>.</p>
     *
     * @param mapPropertiesFileString a {@link java.lang.String} object.
     */
    public void setMapPropertiesFileString(String mapPropertiesFileString) {
        this.mapPropertiesFileString = mapPropertiesFileString;
    }

    /**
     * <p>Getter for the field <code>severityMapAs</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSeverityMapAs() {
        return severityMapAs;
    }

    /**
     * <p>getContextMenu</p>
     *
     * @return a {@link org.opennms.web.map.config.ContextMenu} object.
     */
    public ContextMenu getContextMenu() {
        return cmenu;
    }

    /**
     * <p>setContextMenu</p>
     *
     * @param cmenu a {@link org.opennms.web.map.config.ContextMenu} object.
     */
    public void setContextMenu(ContextMenu cmenu) {
        this.cmenu = cmenu;
    }

    /**
     * <p>isContextMenuEnabled</p>
     *
     * @return a boolean.
     */
    public boolean isContextMenuEnabled() {
        return contextMenuEnabled;
    }

    /**
     * <p>isDoubleClickEnabled</p>
     *
     * @return a boolean.
     */
    public boolean isDoubleClickEnabled() {
        return doubleClickEnabled;
    }

    /**
     * <p>isReload</p>
     *
     * @return a boolean.
     */
    public boolean isReload() {
        return reload;
    }


    /**
     * <p>Constructor for MapPropertiesFactory.</p>
     *
     * @param mapPropertiesFileString a {@link java.lang.String} object.
     */
    public MapPropertiesFactory(final String mapPropertiesFileString) {
        Logging.withPrefix(MapsConstants.LOG4J_CATEGORY, new Runnable() {
            @Override
            public void run() {
                LOG.debug("Map Properties Configuration file: {}", mapPropertiesFileString);

                try {
                    init();
                } catch (FileNotFoundException e) {
                    LOG.error("Cannot found configuration file", e);
                } catch (IOException e) {
                    LOG.error("Cannot load configuration file", e);
                }
                LOG.debug("Instantiating MapPropertiesFactory with properties file: {}", mapPropertiesFileString);
            }
        });
    }

    /**
     * Create a new instance.
     */
    public MapPropertiesFactory() {
        Logging.withPrefix(MapsConstants.LOG4J_CATEGORY, new Runnable() {
            @Override
            public void run() {
                try {
                    init();
                } catch (FileNotFoundException e) {
                    LOG.error("Cannot found configuration file",e);
                } catch (IOException e) {
                    LOG.error("Cannot load configuration file",e);
                }
                LOG.debug("Instantiating MapPropertiesFactory");
            }
        });
    }



    /**
     * <p>init</p>
     *
     * @throws java.io.FileNotFoundException if any.
     * @throws java.io.IOException if any.
     */
    public synchronized void init() throws FileNotFoundException,
    IOException {

        LOG.info("Init");
        if (mapPropertiesFileString == null) {
            mapPropertiesFile = ConfigFileConstants.getFile(ConfigFileConstants.MAP_PROPERTIES_FILE_NAME);
            LOG.info("Using default map properties file: {}", mapPropertiesFile.getPath());
        }else{ 		
            mapPropertiesFile = new File(mapPropertiesFileString);
            LOG.info("Using map properties file: {}", mapPropertiesFile.getPath());
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
     *
     * @throws java.io.FileNotFoundException if any.
     * @throws java.io.IOException if any.
     * @param reloadPropertiesFile a boolean.
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
     * @throws IOException if any.
     * @throws FileNotFoundException if any.
     * @return a {@link java.util.Map} object.
     */
    public Map<String,Severity> getSeveritiesMap() {
        return Collections.unmodifiableMap(severitiesMap);
    }

    /**
     * Gets the java.util.Map with key = availability label and value the Avail
     * corresponding to the label
     *
     * @throws IOException if any.
     * @throws FileNotFoundException if any.
     * @return a {@link java.util.Map} object.
     */
    public Map<String, Avail> getAvailabilitiesMap() {
        return Collections.unmodifiableMap(availsMap);
    }

    /**
     * <p>getAvail</p>
     *
     * @param avail a double.
     * @return a {@link org.opennms.web.map.config.Avail} object.
     */
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

    /**
     * <p>Getter for the field <code>disabledAvail</code>.</p>
     *
     * @return a {@link org.opennms.web.map.config.Avail} object.
     */
    public Avail getDisabledAvail() {
        return disabledAvail;
    }

    /**
     * <p>isAvailEnabled</p>
     *
     * @return a boolean.
     */
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
     * @throws IOException if any.
     * @throws FileNotFoundException if any.
     */
    public Map<String, Status> getStatusesMap() {
        return Collections.unmodifiableMap(statusesMap);
    }

    /**
     * <p>Getter for the field <code>info</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getInfo() {
        return (info);
    }

    /**
     * <p>getProperty</p>
     *
     * @param key a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws java.io.FileNotFoundException if any.
     * @throws java.io.IOException if any.
     */
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
     *
     * @throws java.io.FileNotFoundException if any.
     * @throws java.io.IOException if any.
     */
    protected void parseMapProperties() throws FileNotFoundException,
    IOException {
        LOG.debug("Parsing map.properties...");
        severitiesMap = new HashMap<String,Severity>();
        statusesMap = new HashMap<String,Status>();
        availsMap = new HashMap<String,Avail>();
        iconsMap = new HashMap<String,String>();
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
        LOG.debug("enable.contextmenu={}", cntxtmenu);			
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
                            LOG.debug("cmenu.{}.link={}", commands[j], link);
                            if(link==null){
                                LOG.warn("link is null! skipping..");
                                continue;
                            }
                            params = props.getProperty("cmenu."+commands[j]+".params");						
                            LOG.debug("cmenu.{}.params={}", commands[j], params);
                            if(params==null) params="";
                        }
                        cmenu.addEntry(commands[j], link, params);
                    }
                }
            }else{
                LOG.warn("Context Menu enabled but No command found!");
            }

        }

        //load double click flag
        String doubleclick = props.getProperty("enable.doubleclick");
        if(doubleclick!=null && doubleclick.equalsIgnoreCase("false"))
            doubleClickEnabled=false;
        LOG.debug("enable.doubleclick={}", doubleclick);			

        // load reload flag
        String reloadStr = props.getProperty("enable.reload");
        if(reloadStr!=null && reloadStr.equalsIgnoreCase("true"))
            reload=true;

        LOG.debug("enable.reload={}", reloadStr);			

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
            Severity sev = new Severity(Integer.parseInt(id), label, color);
            if (flash != null && flash.equalsIgnoreCase("true"))
                sev.setFlash(true);
            LOG.debug("found severity {} with id={}, label={}, color={}. Adding it.", severities[i], id, label, color);
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
        defaultSeverity = new Severity(Integer.parseInt(sevid), sevlabel, sevcolor);


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
        indeterminateSeverity = new Severity(Integer.parseInt(indsevid), indsevlabel, indsevcolor);

        //Links
        String[] links = BundleLists.parseBundleList(props
                                                     .getProperty("links"));

        String defaultLinkStr = props.getProperty("link.default");
        if(defaultLinkStr==null){
            LOG.error("Mandatory property 'link.default' not found!");
            throw new IllegalStateException("The property 'link.default' is mandatory");
        }
        defaultLink = Integer.parseInt(defaultLinkStr);

        for (int i = 0; i < links.length; i++) {
            String id = props.getProperty("link." + links[i] + ".id");
            String text = props.getProperty("link." + links[i]+ ".text");
            String speed = props.getProperty("link." + links[i]+ ".speed");
            String width = props.getProperty("link." + links[i]+ ".width");
            String dasharray = props.getProperty("link." + links[i]+ ".dash-array");			
            String snmptype = props.getProperty("link." + links[i]+ ".snmptype");			
            String multilinkwidth = props.getProperty("link." + links[i]+ ".multilink.width");
            String multilinkdasharray = props.getProperty("link." + links[i]+ ".multilink.dash-array");            
            if(id==null){
                LOG.error("param id for link cannot be null in map.properties: skipping link...");
                continue;
            }
            if(text==null){
                LOG.error("param text for link cannot be null in map.properties: skipping link...");
                continue;
            }
            if(width==null){
                LOG.error("param width for link cannot be null in map.properties: skipping link...");
                continue;
            }
            if(speed==null){
                LOG.info("param speed for link cannot be null in map.properties: skipping link...");
                speed="Unknown";
            }

            int dash_arr=-1;
            if(dasharray!=null)
                dash_arr=Integer.parseInt(dasharray);

            int snmp_type=-1;
            if(snmptype!=null)
                snmp_type=Integer.parseInt(snmptype);

            if (multilinkwidth==null) {
                multilinkwidth= width;
            }

            int multilink_dasharray=dash_arr;
            if (multilinkdasharray!=null) {
                multilink_dasharray=Integer.parseInt(multilinkdasharray);
            }

            Link lnk = new Link(Integer.parseInt(id), speed,text,width,dash_arr,snmp_type,multilinkwidth,multilink_dasharray);

            LOG.debug("found link {} with id={}, text={}, speed={}, width={}, dash-array={}, snmp-type={}. Adding it.", links[i], id, text, speed, width, dasharray, snmp_type);
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
                LOG.error("param color for linkstatus cannot be null in map.properties: skipping linkstatus...");
                continue;
            }
            boolean flashBool = false;
            if(flash!=null && flash.equalsIgnoreCase("false"))
                flashBool=false;			
            LOG.debug("found linkstatus {} with color={}, flash={}. Adding it.", linkStatuses[i], color, flashBool);
            LinkStatus ls = new LinkStatus(linkStatuses[i],color,flashBool);
            linkStatusesMap.put(linkStatuses[i], ls);
        }		

        if(props.getProperty("summarylink.id")!=null){
            summaryLink = Integer.parseInt(props.getProperty("summarylink.id"));    
        }
        LOG.debug("found summarylink.id: {}", summaryLink);

        if(props.getProperty("summarylink.color")!=null){
            summaryLinkColor = props.getProperty("summarylink.color");    
        }
        LOG.debug("found summarylink.color: {}", summaryLinkColor);

        if(props.getProperty("max.links")!=null){
            maxLinks = Integer.parseInt(props.getProperty("max.links"));    
        }
        LOG.debug("found max.links: {}", maxLinks);


        if(props.getProperty("multilink.status")!=null){
            multilinkStatus = props.getProperty("multilink.status"); 	
        }
        if(!multilinkStatus.equals(MULTILINK_BEST_STATUS) && !multilinkStatus.equals(MULTILINK_IGNORE_STATUS) && !multilinkStatus.equals(MULTILINK_WORST_STATUS)){
            LOG.error("multilink.status property must be 'best' or 'worst' or 'ignore' ... using default ('best')");
            multilinkStatus=MULTILINK_BEST_STATUS;
        }
        LOG.debug("found multilink.status:{}", multilinkStatus);

        if(props.getProperty("multilink.ignore.color")!=null){
            multilinkIgnoreColor = props.getProperty("multilink.ignore.color");    
        }
        LOG.debug("found multilink.ignore.color:{}", multilinkIgnoreColor);

        // look up statuses and their properties
        String[] statuses = BundleLists.parseBundleList(props
                                                        .getProperty("statuses"));

        for (int i = 0; i < statuses.length; i++) {
            String id = props.getProperty("status." + statuses[i] + ".id");
            String uei = props.getProperty("status." + statuses[i] + ".uei");
            String color = props
                    .getProperty("status." + statuses[i] + ".color");
            String text = props.getProperty("status." + statuses[i] + ".text");
            LOG.debug("found status {} with id={}, uei={}, color={}, text={}. Adding it.", statuses[i], id, uei, color, text);
            Status status = new Status(Integer.parseInt(id), uei, color, text);
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
        unknownStatus = new Status(Integer.parseInt(stid), stuei, stcolor, sttext);

        String defaultstid = props.getProperty("status.default");
        if (defaultstid == null) {
            throw new IllegalStateException(
                    "Required Default Status not found.");
        }
        String staid = props.getProperty("status." + defaultstid + ".id");
        String stauei = props.getProperty("status." + defaultstid + ".uei");
        String stacolor = props.getProperty("status." + defaultstid + ".color");
        String statext = props.getProperty("status." + defaultstid + ".text");
        defaultStatus = new Status(Integer.parseInt(staid), stauei, stacolor, statext);

        // look up statuses and their properties
        String[] availes = BundleLists.parseBundleList(props
                                                       .getProperty("availabilities"));

        for (int i = 0; i < availes.length; i++) {
            String id = props.getProperty("avail." + availes[i] + ".id");
            String min = props.getProperty("avail." + availes[i] + ".min");
            String color = props.getProperty("avail." + availes[i] + ".color");
            String flash = props.getProperty("avail." + availes[i] + ".flash");
            LOG.debug("found avail {} with id={}, min={}, color={}. Adding it.", availes[i], id, min, color);
            Avail avail = new Avail(Integer.parseInt(id),
                                    Integer.parseInt(min), color);
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
        undefinedAvail = new Avail(Integer.parseInt(avid), Integer.parseInt(avmin), avcolor);


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
            if (av.getId() == Integer.parseInt(disableAvailId)){
                disabledAvail=av;
                break;
            }
        }

        // look up icons filenames

        String[] icons = BundleLists
                .parseBundleList(props.getProperty("icons"));

        for (int i = 0; i < icons.length; i++) {

            String baseProperty = "icon." + icons[i] + ".";

            String filename =  props.getProperty(baseProperty + "filename");
            LOG.debug("found icon {} with filename={}. Adding it.", icons[i], filename);
            iconsMap.put(icons[i], filename);
        }

        // look up sysoid icons
        if (props.getProperty("sysoids") != null && props.getProperty("sysoids") != "") {
            String[] sysoids = BundleLists.parseBundleList(props.getProperty("sysoids"));

            for (int i = 0; i < sysoids.length; i++) {
                String iconName = props.getProperty("sysoid." + sysoids[i] + ".iconName");
                LOG.debug("found sysoid {} with iconName={}. Adding it.", sysoids[i], iconName);
                iconsBySysoidMap.put(sysoids[i], iconName);
            }
        }

        defaultMapIcon = props.getProperty("icon.default.map");
        LOG.debug("default map icon: {}", defaultMapIcon);
        if (defaultMapIcon == null) {
            throw new IllegalStateException(
                    "Required Default Map Icon not found.");
        }
        defaultNodeIcon = props.getProperty("icon.default.node");
        if (defaultNodeIcon == null) {
            throw new IllegalStateException(
                    "Required Default Icon Node not found.");
        }
        LOG.debug("default node icon: {}", defaultNodeIcon);

        String defaultMapElementDimensionString = props.getProperty("icon.default.mapelementdimension");
        if (defaultMapElementDimensionString != null) {
            defaultMapElementDimension = Integer.parseInt(defaultMapElementDimensionString);
        }
        LOG.debug("default map element dimension: {}", defaultMapElementDimension);

        String useSemaphoreString = props.getProperty("use.semaphore");
        if (useSemaphoreString != null && useSemaphoreString.equalsIgnoreCase("false"))
            useSemaphore=false;
        else useSemaphore = true;
        LOG.debug("use semaphore: {}", useSemaphoreString);

        // look up background filenames
        String[] bg = BundleLists
                .parseBundleList(props.getProperty("bgimages"));

        for (int i = 0; i < bg.length; i++) {
            String filename = props.getProperty("bgimage." + bg[i]
                    + ".filename");
            LOG.debug("found bgimage {} with filename={}. Adding it.", bg[i], filename);
            bgImagesMap.put(bg[i], filename);
        }
    }

    /**
     * <p>Getter for the field <code>summaryLinkColor</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSummaryLinkColor() {
        return summaryLinkColor;
    }

    /**
     * <p>Getter for the field <code>iconsBySysoidMap</code>.</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String,String> getIconsBySysoidMap() {
        return Collections.unmodifiableMap(iconsBySysoidMap);
    }

    /**
     * <p>getBackgroundImagesMap</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String,String> getBackgroundImagesMap() {
        return Collections.unmodifiableMap(bgImagesMap);
    }

    /**
     * <p>Getter for the field <code>defaultMapIcon</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDefaultMapIcon(){
        return defaultMapIcon;
    }

    /**
     * <p>Getter for the field <code>defaultNodeIcon</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDefaultNodeIcon() {
        return defaultNodeIcon;
    }

    /**
     * Gets the array of ordered Severity by id.
     *
     * @return an array of {@link org.opennms.web.map.config.Severity} objects.
     */
    public Severity[] getOrderedSeverities() {
        return orderedSeverities;
    }

    /**
     * Gets the array of ordered Avail by min.
     *
     * @return an array of {@link org.opennms.web.map.config.Avail} objects.
     */
    public Avail[] getOrderedAvails() {
        return orderedAvails;
    }

    /**
     * Gets the array of ordered Status by id.
     *
     * @return an array of {@link org.opennms.web.map.config.Status} objects.
     */
    public Status[] getOrderedStatuses() {
        return orderedStatuses;
    }

    /**
     * <p>getSeverity</p>
     *
     * @param severityLabel a {@link java.lang.String} object.
     * @return a int.
     */
    public int getSeverity(String severityLabel) {
        Severity sev = ((Severity)severitiesMap.get(severityLabel));
        if(sev==null){
            throw new IllegalStateException("Severity with label "+severityLabel+" not found.");
        }
        return sev.getId();
    }

    /**
     * <p>getStatus</p>
     *
     * @param uei a {@link java.lang.String} object.
     * @return a int.
     */
    public int getStatus(String uei) {

        Status status = (Status)statusesMap.get(uei);
        if(status==null){
            try {
                return getUnknownStatus().getId();
            } catch (Throwable e) {
                throw new RuntimeException("Exception while getting unknown status "+e);
            }
        }
        return status.getId();
    }

    /**
     * gets the config Link by snmpType defined in the map properties config file
     *
     * @param linkTypologyId a int.
     * @return a {@link java.util.Set} object.
     */
    public Set<Link> getLinkBySnmpType(int linkTypologyId){
        return linksBySnmpTypeMap.get(new Integer(linkTypologyId));
    }

    /**
     * gets the id corresponding to the link defined in configuration file. The match is performed first by snmptype,
     * then by speed (if more are defined). If there is no match, the default link id is returned.
     *
     * @param snmpiftype a int.
     * @param snmpifspeed a long.
     * @return the id corresponding to the link defined in configuration file. If there is no match, the default link id is returned.
     */
    public int getLinkTypeId(int snmpiftype, long snmpifspeed) {
        Link link = null;
        Set<Link> linkSet = getLinkBySnmpType(snmpiftype);
        if (linkSet == null) {
            link = getDefaultLink();
        } else {
            if (linkSet.size() > 1) {
                Iterator<Link> it = linkSet.iterator();
                while (it.hasNext()) {
                    Link next = it.next();
                    try {
                        if (Long.parseLong(next.getSpeed()) == snmpifspeed) {
                            link = next;
                            break;
                        }
                    } catch (NumberFormatException e) {
                        // Ignore NumberFormatException
                        continue;
                    }
                }
            } else {
                Iterator<Link> it = linkSet.iterator();
                if (it.hasNext()) {
                    link = it.next();
                }
            }
        }
        if (link == null) {
            link = getDefaultLink();
        }
        return link.getId();
    }

    /**
     * <p>getLink</p>
     *
     * @param id a int.
     * @return a {@link org.opennms.web.map.config.Link} object.
     */
    public Link getLink(int id){
        return (Link)linksMap.get(new Integer(id));
    }

    /**
     * gets the config LinkStatus by label defined in the map properties config file
     *
     * @param linkStatusLabel a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.map.config.LinkStatus} object.
     */
    public LinkStatus getLinkStatus(String linkStatusLabel){
        return (LinkStatus)linkStatusesMap.get(linkStatusLabel);
    }

    /**
     * <p>Getter for the field <code>defaultLink</code>.</p>
     *
     * @return a {@link org.opennms.web.map.config.Link} object.
     */
    public Link getDefaultLink(){
        return (Link) linksMap.get(defaultLink);
    }

    /**
     * <p>Getter for the field <code>linksMap</code>.</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<Integer,Link> getLinksMap() {
        return linksMap;
    }

    /**
     * <p>Getter for the field <code>linkStatusesMap</code>.</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, LinkStatus> getLinkStatusesMap() {
        return linkStatusesMap;
    }

    /**
     * <p>Getter for the field <code>multilinkStatus</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public  String getMultilinkStatus() {
        return multilinkStatus;
    }

    /**
     * <p>getAvails</p>
     *
     * @return a {@link java.util.List} object.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public List<Avail> getAvails() throws MapsException {
        List<Avail> avails = new ArrayList<Avail>();
        avails.addAll(Arrays.asList(getOrderedAvails()));
        return avails;
    }

    /**
     * <p>getLinks</p>
     *
     * @return a {@link java.util.List} object.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public List<Link> getLinks() throws MapsException {
        List<Link> links = new ArrayList<Link>();
        links.addAll((getLinksMap().values()));
        return links;
    }

    /**
     * <p>getLinkStatuses</p>
     *
     * @return a {@link java.util.List} object.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public List<LinkStatus> getLinkStatuses() throws MapsException {
        List<LinkStatus> linkstatutes = new ArrayList<LinkStatus>();
        linkstatutes.addAll((getLinkStatusesMap().values()));
        return linkstatutes;
    }

    /**
     * <p>getStatuses</p>
     *
     * @return a {@link java.util.List} object.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public List<Status> getStatuses() throws MapsException {
        List<Status> statutes = new ArrayList<Status>();
        statutes.addAll(getStatusesMap().values());
        return statutes;
    }

    /**
     * <p>getSeverities</p>
     *
     * @return a {@link java.util.List} object.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public List<Severity> getSeverities() throws MapsException {
        List<Severity> sevs = new ArrayList<Severity>(); 
        sevs.addAll(getSeveritiesMap().values());
        return sevs;
    }

    /**
     * <p>getIcons</p>
     *
     * @return a java$util$Map object.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public java.util.Map<String,String> getIcons() throws MapsException{
        return Collections.unmodifiableMap(iconsMap);
    }

    /**
     * <p>getIconsBySysoid</p>
     *
     * @return a java$util$Map object.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public java.util.Map<String,String> getIconsBySysoid() throws MapsException{
        return Collections.unmodifiableMap(getIconsBySysoidMap());
    }

    /**
     * <p>getBackgroundImages</p>
     *
     * @return a java$util$Map object.
     * @throws org.opennms.web.map.MapsException if any.
     */
    public java.util.Map<String, String> getBackgroundImages() throws MapsException {
        return Collections.unmodifiableMap(getBackgroundImagesMap());
    }

    /**
     * <p>getDefaultBackgroundColor</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDefaultBackgroundColor() {
        return "ffffff";
    }

    /**
     * <p>getDefaultStatusId</p>
     *
     * @return a int.
     */
    public int getDefaultStatusId() {
        return getDefaultStatus().getId();
    }

    /**
     * <p>getUnknownStatusId</p>
     *
     * @return a int.
     */
    public int getUnknownStatusId() {
        return getUnknownStatus().getId();
    }

    /**
     * <p>getMapElementDimensions</p>
     *
     * @return a java$util$Map object.
     */
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

    /**
     * <p>Getter for the field <code>defaultMapElementDimension</code>.</p>
     *
     * @return a int.
     */
    public int getDefaultMapElementDimension() {
        return defaultMapElementDimension;
    }

    /**
     * <p>Getter for the field <code>maxLinks</code>.</p>
     *
     * @return a int.
     */
    public int getMaxLinks() {
        return maxLinks;
    }

    /**
     * <p>Getter for the field <code>summaryLink</code>.</p>
     *
     * @return a int.
     */
    public int getSummaryLink() {
        return summaryLink;
    }

    /**
     * <p>Getter for the field <code>multilinkIgnoreColor</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMultilinkIgnoreColor() {
        return multilinkIgnoreColor;
    }

    /**
     * <p>isUseSemaphore</p>
     *
     * @return a boolean.
     */
    public boolean isUseSemaphore() {
        return useSemaphore;
    }
}
