/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.mibcompiler.services;

import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsmiparser.parser.SmiDefaultParser;
import org.jsmiparser.smi.Notification;
import org.jsmiparser.smi.SmiMib;
import org.jsmiparser.smi.SmiModule;
import org.jsmiparser.smi.SmiNamedNumber;
import org.jsmiparser.smi.SmiNotificationType;
import org.jsmiparser.smi.SmiPrimitiveType;
import org.jsmiparser.smi.SmiRow;
import org.jsmiparser.smi.SmiTrapType;
import org.jsmiparser.smi.SmiVariable;

import org.opennms.core.utils.LogUtils;
import org.opennms.features.namecutter.NameCutter;
import org.opennms.features.vaadin.mibcompiler.api.MibParser;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.netmgt.dao.support.IndexStorageStrategy;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.xml.eventconf.Decode;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.netmgt.xml.eventconf.Logmsg;
import org.opennms.netmgt.xml.eventconf.Mask;
import org.opennms.netmgt.xml.eventconf.Maskelement;
import org.opennms.netmgt.xml.eventconf.Varbindsdecode;

/**
 * JSMIParser implementation of the interface MibParser.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class JsmiMibParser implements MibParser, Serializable {

    /** The Constant MIB_SUFFIXES. */
    private static final String[] MIB_SUFFIXES = new String[] { "", ".txt", ".mib", ".my" };

    /** The Constant TRAP_OID_PATTERN. */
    private static final Pattern TRAP_OID_PATTERN = Pattern.compile("(.*)\\.(\\d+)$");

    /** The MIB directory. */
    private File mibDirectory;

    /** The parser. */
    private SmiDefaultParser parser;

    /** The module. */
    private SmiModule module;

    /** The error handler. */
    private OnmsProblemEventHandler errorHandler;

    /** The missing dependencies. */
    private List<String> missingDependencies = new ArrayList<String>();

    /**
     * Instantiates a new JLIBSMI MIB parser.
     */
    public JsmiMibParser() {
        parser = new SmiDefaultParser();
        errorHandler = new OnmsProblemEventHandler(parser);
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.MibParser#setMibDirectory(java.io.File)
     */
    @Override
    public void setMibDirectory(File mibDirectory) {
        this.mibDirectory = mibDirectory;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.MibParser#parseMib(java.io.File)
     */
    @Override
    public boolean parseMib(File mibFile) {
        // Validate MIB Directory
        if (mibDirectory == null) {
            errorHandler.addError("MIB directory has not been set.");
            return false;
        }

        // Reset error handler and dependencies tracker
        missingDependencies.clear();

        // Set UP the MIB Queue MIB to be parsed
        List<URL> queue = new ArrayList<URL>();
        parser.getFileParserPhase().setInputUrls(queue);

        // Create a cache of filenames to do case-insensitive lookups
        final Map<String,File> mibDirectoryFiles = new HashMap<String,File>();
        for (final File file : mibDirectory.listFiles()) {
            mibDirectoryFiles.put(file.getName().toLowerCase(), file);
        }

        // Parse MIB
        LogUtils.debugf(this, "Parsing %s", mibFile.getAbsolutePath());
        SmiMib mib = null;
        addFileToQueue(queue, mibFile);
        while (true) {
            errorHandler.reset();
            try {
                mib = parser.parse();
            } catch (Exception e) {
                LogUtils.errorf(this, e, "Can't compile %s", mibFile);
                errorHandler.addError(e.getMessage());
                return false;
            }
            if (errorHandler.isOk()) {
                break;
            } else {
                List<String> dependencies = errorHandler.getDependencies();
                if (dependencies.isEmpty()) // No dependencies, everything is fine.
                    break;
                missingDependencies.addAll(dependencies);
                if (!addDependencyToQueue(queue, mibDirectoryFiles))
                    break;
            }
        }
        if (errorHandler.isNotOk()) // There are still non-dependency related problems.
            return false;

        // Extracting the module from compiled MIB.
        LogUtils.infof(this, "The MIB %s has been parsed successfully.", mibFile.getAbsolutePath());
        module = getModule(mib, mibFile);
        return module != null;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.MibParser#getFormattedErrors()
     */
    @Override
    public String getFormattedErrors() {
        return errorHandler.getMessages();
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.MibParser#getMissingDependencies()
     */
    @Override
    public List<String> getMissingDependencies() {
        return missingDependencies;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.services.MibParser#getMibName()
     */
    @Override
    public String getMibName() {
        return module.getId();
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.services.MibParser#getEvents(java.lang.String)
     */
    @Override
    public Events getEvents(String ueibase) {
        if (module == null) {
            return null;
        }
        LogUtils.infof(this, "Generating events for %s using the following UEI Base: %s", module.getId(), ueibase);
        try {
            return convertMibToEvents(module, ueibase);
        } catch (Throwable e) {
            String errors = e.getMessage();
            if (errors == null || errors.trim().equals(""))
                errors = "An unknown error accured when generating events objects from the MIB " + module.getId();
            LogUtils.errorf(this, e, "Event parsing error: %s", errors);
            errorHandler.addError(errors);
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.api.MibParser#getDataCollection()
     */
    @Override
    public DatacollectionGroup getDataCollection() {
        if (module == null) {
            return null;
        }
        LogUtils.infof(this, "Generating data collection configuration for %s", module.getId());
        DatacollectionGroup dcGroup = new DatacollectionGroup();
        dcGroup.setName(module.getId());
        NameCutter cutter = new NameCutter();
        try {
            for (SmiVariable v : module.getVariables()) {
                String groupName = getGroupName(v);
                String resourceType = getResourceType(v);
                Group group = getGroup(dcGroup, groupName, resourceType);
                String typeName = getMetricType(v.getType().getPrimitiveType());
                if (typeName != null) {
                    String alias = cutter.trimByCamelCase(v.getId(), 19); // RRDtool/JRobin DS size restriction.
                    MibObj mibObj = new MibObj();
                    mibObj.setOid('.' + v.getOidStr());
                    mibObj.setInstance(resourceType == null ? "0" : resourceType);
                    mibObj.setAlias(alias);
                    mibObj.setType(typeName);
                    group.addMibObj(mibObj);
                    if (typeName.equals("string") && resourceType != null) {
                        for (ResourceType rs : dcGroup.getResourceTypeCollection()) {
                            if (rs.getName().equals(resourceType) && rs.getResourceLabel().equals("${index}")) {
                                rs.setResourceLabel("${" + v.getId() + "} (${index})");
                            }
                        }
                    }
                }
            }
        } catch (Throwable e) {
            String errors = e.getMessage();
            if (errors == null || errors.trim().equals(""))
                errors = "An unknown error accured when generating data collection objects from the MIB " + module.getId();
            LogUtils.errorf(this, e, "Data Collection parsing error: %s", errors);
            errorHandler.addError(errors);
            return null;
        }
        return dcGroup;
    }


    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.api.MibParser#getPrefabGraphs()
     */
    @Override
    public List<PrefabGraph> getPrefabGraphs() {
        if (module == null) {
            return null;
        }
        List<PrefabGraph> graphs = new ArrayList<PrefabGraph>();
        LogUtils.infof(this, "Generating graph templates for %s", module.getId());
        NameCutter cutter = new NameCutter();
        try {
            for (SmiVariable v : module.getVariables()) {
                String groupName = getGroupName(v);
                String resourceType = getResourceType(v);
                if (resourceType == null)
                    resourceType = "nodeSnmp";
                String typeName = getMetricType(v.getType().getPrimitiveType());
                if (v.getId().contains("Index")) { // Treat SNMP Indexes as strings.
                    typeName = "string";
                }
                int order = 1;
                if (typeName != null && !typeName.toLowerCase().contains("string")) {
                    String name = groupName + '.' + v.getId();
                    String alias = cutter.trimByCamelCase(v.getId(), 19); // RRDtool/JRobin DS size restriction.
                    String descr = v.getDescription().replaceAll("[\n\r]", "").replaceAll("\\s+", " ");
                    StringBuffer sb = new StringBuffer();
                    sb.append("--title=\"").append(v.getId()).append("\" \\\n");
                    sb.append(" DEF:var={rrd1}:").append(alias).append(":AVERAGE \\\n");
                    sb.append(" LINE1:var#0000ff:\"").append(v.getId()).append("\" \\\n");
                    sb.append(" GPRINT:var:AVERAGE:\"Avg\\\\: %8.2lf %s\" \\\n");
                    sb.append(" GPRINT:var:MIN:\"Min\\\\: %8.2lf %s\" \\\n");
                    sb.append(" GPRINT:var:MAX:\"Max\\\\: %8.2lf %s\\n\"");
                    sb.append("\n\n");
                    PrefabGraph graph = new PrefabGraph(name, descr, new String[] { alias }, sb.toString(), new String[0], new String[0], order++, new String[] { resourceType }, descr, null, null, new String[0]);
                    graphs.add(graph);
                }
            }
        } catch (Throwable e) {
            String errors = e.getMessage();
            if (errors == null || errors.trim().equals(""))
                errors = "An unknown error accured when generating graph templates from the MIB " + module.getId();
            LogUtils.errorf(this, e, "Graph templates parsing error: %s", errors);
            errorHandler.addError(errors);
            return null;
        }
        return graphs;
    }

    /**
     * Gets the group name.
     *
     * @param var the SMI Variable
     * @return the group name
     */
    private String getGroupName(SmiVariable var) {
        if (var.getNode().getParent().getSingleValue() instanceof SmiRow) {
            return var.getNode().getParent().getParent().getSingleValue().getId();
        }
        return var.getNode().getParent().getSingleValue().getId();
    }

    /**
     * Gets the resource type.
     *
     * @param var the SMI Variable
     * @return the resource type
     */
    private String getResourceType(SmiVariable var) {
        if (var.getNode().getParent().getSingleValue() instanceof SmiRow) {
            return var.getNode().getParent().getSingleValue().getId();
        }
        return null;
    }

    /**
     * Adds a file to the queue.
     *
     * @param queue the queue
     * @param mibFile the MIB file
     */
    private void addFileToQueue(List<URL> queue, File mibFile) {
        try {
            URL url = mibFile.toURI().toURL();
            if (!queue.contains(url)) {
                LogUtils.debugf(this, "Adding %s to queue ", url);
                queue.add(url);
            }
        } catch (Exception e) {
            LogUtils.warnf(this, "Can't generate URL from %s", mibFile.getAbsolutePath());
        }
    }

    /**
     * Adds the dependency to the queue.
     *
     * @param queue the queue
     * @param mibDirectoryFiles
     * @return true, if successful
     */
    private boolean addDependencyToQueue(final List<URL> queue, final Map<String, File> mibDirectoryFiles) {
        final List<String> dependencies = new ArrayList<String>(missingDependencies);
        boolean ok = true;
        for (String dependency : dependencies) {
            boolean found = false;
            for (String suffix : MIB_SUFFIXES) {
                final String fileName = (dependency+suffix).toLowerCase();
                if (mibDirectoryFiles.containsKey(fileName)) {
                    File f = mibDirectoryFiles.get(fileName);
                    LogUtils.debugf(this, "Checking dependency file %s", f.getAbsolutePath());
                    if (f.exists()) {
                        LogUtils.infof(this, "Adding dependency file %s", f.getAbsolutePath());
                        addFileToQueue(queue, f);
                        missingDependencies.remove(dependency);
                        found = true;
                        break;
                    }
                }
                LogUtils.debugf(this, "Dependency file %s doesn't exist", fileName);
            }
            if (!found) {
                LogUtils.warnf(this, "Couldn't find dependency %s on %s", dependency, mibDirectory);
                ok = false;
            }
        }
        return ok;
    }

    /**
     * Gets the module.
     *
     * @param mibObject the MIB object
     * @param mibFile the MIB file
     * @return the module
     */
    private SmiModule getModule(SmiMib mibObject, File mibFile) {
        for (SmiModule m : mibObject.getModules()) {
            if (m.getIdToken().getLocation().getSource().contains(mibFile.getAbsolutePath())) {
                return m;
            }
        }
        errorHandler.addError("Can't find the MIB module for " + mibFile);
        return null;
    }

    /*
     * Data Collection processing methods
     */

    /**
     * Gets the metric type.
     * <p>This should be consistent with NumericAttributeType and StringAttributeType.</p>
     * <p>For this reason the valid types are: counter, gauge, timeticks, integer, octetstring, string.</p>
     * <p>Any derivative is also valid, for example: Counter32, Integer64, etc...</p>
     * 
     * @param type the type
     * @return the type
     */
    private String getMetricType(SmiPrimitiveType type) {
        if (type.equals(SmiPrimitiveType.ENUM)) // ENUM are just informational elements.
            return "string";
        if (type.equals(SmiPrimitiveType.OBJECT_IDENTIFIER)) // ObjectIdentifier will be treated as strings.
            return "string";
        if (type.equals(SmiPrimitiveType.UNSIGNED_32)) // Unsigned32 will be treated as integer.
            return "integer";
        return type.toString().replaceAll("_", "").toLowerCase();
    }

    /**
     * Gets the group.
     *
     * @param data the data collection group object
     * @param groupName the group name
     * @param resourceType the resource type
     * @return the group
     */
    protected Group getGroup(DatacollectionGroup data, String groupName, String resourceType) {
        for (Group group : data.getGroupCollection()) {
            if (group.getName().equals(groupName))
                return group;
        }
        Group group = new Group();
        group.setName(groupName);
        group.setIfType(resourceType == null ? "ignore" : "all");
        if (resourceType != null) {
            ResourceType type = new ResourceType();
            type.setName(resourceType);
            type.setLabel(resourceType);
            type.setResourceLabel("${index}");
            type.setPersistenceSelectorStrategy(new PersistenceSelectorStrategy("org.opennms.netmgt.collectd.PersistAllSelectorStrategy")); // To avoid requires opennms-services
            type.setStorageStrategy(new StorageStrategy(IndexStorageStrategy.class.getName()));
            data.addResourceType(type);
        }
        data.addGroup(group);
        return group;
    }

    /*
     * Event processing methods
     * 
     */

    /**
     * Convert MIB to events.
     *
     * @param module the module object
     * @param ueibase the UEI base
     * @return the events
     */
    protected Events convertMibToEvents(SmiModule module, String ueibase) {
        Events events = new Events();
        for (SmiNotificationType trap : module.getNotificationTypes()) {
            events.addEvent(getTrapEvent(trap, ueibase));
        }
        for (SmiTrapType trap : module.getTrapTypes()) {
            events.addEvent(getTrapEvent(trap, ueibase));
        }
        return events;
    }

    /**
     * Gets the trap event.
     *
     * @param trap the trap object
     * @param ueibase the UEI base
     * @return the trap event
     */
    protected Event getTrapEvent(Notification trap, String ueibase) {
        // Build default severity
        String severity = OnmsSeverity.INDETERMINATE.toString();
        severity = severity.substring(0, 1).toUpperCase() + severity.substring(1).toLowerCase();
        // Set the event's UEI, event-label, logmsg, severity, and descr
        Event evt = new Event();
        evt.setUei(getTrapEventUEI(trap, ueibase));
        evt.setEventLabel(getTrapEventLabel(trap));
        evt.setLogmsg(getTrapEventLogmsg(trap));
        evt.setSeverity(severity);
        evt.setDescr(getTrapEventDescr(trap));
        List<Varbindsdecode> decode = getTrapVarbindsDecode(trap);
        if (!decode.isEmpty()) {
            evt.setVarbindsdecode(decode);
        }
        evt.setMask(new Mask());
        // The "ID" mask element (trap enterprise)
        addMaskElement(evt, "id", getTrapEnterprise(trap));
        // The "generic" mask element: hard-wired to enterprise-specific(6)
        addMaskElement(evt, "generic", "6");
        // The "specific" mask element (trap specific-type)
        addMaskElement(evt, "specific", getTrapSpecificType(trap));
        return evt;
    }

    /**
     * Gets the trap event UEI.
     *
     * @param trap the trap object
     * @param ueibase the UEI base
     * @return the trap event UEI
     */
    protected String getTrapEventUEI(Notification trap, String ueibase) {
        StringBuffer buf = new StringBuffer(ueibase);
        if (! ueibase.endsWith("/")) {
            buf.append("/");
        }
        buf.append(trap.getId());
        return buf.toString();
    }

    /**
     * Gets the trap event label.
     *
     * @param trap the trap object
     * @return the trap event label
     */
    protected String getTrapEventLabel(Notification trap) {
        StringBuffer buf = new StringBuffer();
        buf.append(trap.getModule().getId());
        buf.append(" defined trap event: ");
        buf.append(trap.getId());
        return buf.toString();
    }

    /**
     * Gets the trap event LogMsg.
     *
     * @param trap the trap object
     * @return the trap event LogMsg
     */
    protected Logmsg getTrapEventLogmsg(Notification trap) {
        Logmsg msg = new Logmsg();
        msg.setDest("logndisplay");
        final StringBuffer dbuf = new StringBuffer();
        dbuf.append("<p>");
        dbuf.append("\n");
        dbuf.append("\t").append(trap.getId()).append(" trap received\n");
        int vbNum = 1;
        for (SmiVariable var : trap.getObjects()) {
            dbuf.append("\t").append(var.getId()).append("=%parm[#").append(vbNum).append("]%\n");
            vbNum++;
        }
        if (dbuf.charAt(dbuf.length() - 1) == '\n') {
            dbuf.deleteCharAt(dbuf.length() - 1); // delete the \n at the end
        }
        dbuf.append("</p>\n\t");
        msg.setContent(dbuf.toString());
        return msg;
    }

    /**
     * Gets the trap event description.
     *
     * @param trap the trap object
     * @return the trap event description
     */
    protected String getTrapEventDescr(Notification trap) {
        String description = trap.getDescription();
        // FIXME There a lot of detail here (like removing the last \n) that can go away when we don't need to match mib2opennms exactly
        final String descrStartingNewlines = description.replaceAll("^", "\n<p>");
        final String descrEndingNewlines = descrStartingNewlines.replaceAll("$", "</p>\n");
        final StringBuffer dbuf = new StringBuffer(descrEndingNewlines);
        if (dbuf.charAt(dbuf.length() - 1) == '\n') {
            dbuf.deleteCharAt(dbuf.length() - 1); // delete the \n at the end
        }
        dbuf.append("<table>");
        dbuf.append("\n");
        int vbNum = 1;
        for (SmiVariable var : trap.getObjects()) {
            dbuf.append("\t<tr><td><b>\n\n\t").append(var.getId());
            dbuf.append("</b></td><td>\n\t%parm[#").append(vbNum).append("]%;</td><td><p>");
            SmiPrimitiveType type = var.getType().getPrimitiveType();
            if (type.equals(SmiPrimitiveType.ENUM)) {
                SortedMap<BigInteger, String> map = new TreeMap<BigInteger, String>();
                for (SmiNamedNumber v : var.getType().getEnumValues()) {
                    map.put(v.getValue(), v.getId());
                }
                dbuf.append("\n");
                for (Entry<BigInteger, String> entry : map.entrySet()) {
                    dbuf.append("\t\t").append(entry.getValue()).append("(").append(entry.getKey()).append(")\n");
                }
                dbuf.append("\t");
            }
            dbuf.append("</p></td></tr>\n");
            vbNum++;
        }
        if (dbuf.charAt(dbuf.length() - 1) == '\n') {
            dbuf.deleteCharAt(dbuf.length() - 1); // delete the \n at the end
        }
        dbuf.append("</table>\n\t");
        return dbuf.toString();
    }

    /**
     * Gets the trap varbinds decode.
     *
     * @param trap the trap object
     * @return the trap varbinds decode
     */
    protected List<Varbindsdecode> getTrapVarbindsDecode(Notification trap) {
        Map<String, Varbindsdecode> decode = new LinkedHashMap<String, Varbindsdecode>();
        int vbNum = 1;
        for (SmiVariable var : trap.getObjects()) {
            String parmName = "parm[#" + vbNum + "]";
            SmiPrimitiveType type = var.getType().getPrimitiveType();
            if (type.equals(SmiPrimitiveType.ENUM)) {
                SortedMap<BigInteger, String> map = new TreeMap<BigInteger, String>();
                for (SmiNamedNumber v : var.getType().getEnumValues()) {
                    map.put(v.getValue(), v.getId());
                }
                for (Entry<BigInteger, String> entry : map.entrySet()) {
                    if (!decode.containsKey(parmName)) {
                        Varbindsdecode newVarbind = new Varbindsdecode();
                        newVarbind.setParmid(parmName);
                        decode.put(newVarbind.getParmid(), newVarbind);
                    }
                    Decode d = new Decode();
                    d.setVarbinddecodedstring(entry.getValue());
                    d.setVarbindvalue(entry.getKey().toString());
                    decode.get(parmName).addDecode(d);
                }
            }
            vbNum++;
        }
        return new ArrayList<Varbindsdecode>(decode.values());
    }

    /**
     * Gets the trap enterprise.
     *
     * @param trap the trap object
     * @return the trap enterprise
     */
    private String getTrapEnterprise(Notification trap) {
        String trapOid = getMatcherForOid(getTrapOid(trap)).group(1);
        
        /* RFC3584 sec 3.2 (1) bullet 2 sub-bullet 1 states:
         * 
         * "If the next-to-last sub-identifier of the snmpTrapOID value
         * is zero, then the SNMPv1 enterprise SHALL be the SNMPv2
         * snmpTrapOID value with the last 2 sub-identifiers removed..."
         * 
         * Issue SPC-592 boils down to the fact that we were not doing the above.
         * 
         */
        
        if (trapOid.endsWith(".0")) {
            trapOid = trapOid.substring(0, trapOid.length() - 2);
        }
        return trapOid;
    }

    /**
     * Gets the trap specific type.
     *
     * @param trap the trap object
     * @return the trap specific type
     */
    private String getTrapSpecificType(Notification trap) {
        return getMatcherForOid(getTrapOid(trap)).group(2);
    }

    /**
     * Gets the matcher for OID.
     *
     * @param trapOid the trap OID
     * @return the matcher for OID
     */
    private Matcher getMatcherForOid(String trapOid) {
        Matcher m = TRAP_OID_PATTERN.matcher(trapOid);
        if (!m.matches()) {
            throw new IllegalStateException("Could not match the trap OID '" + trapOid + "' against '" + m.pattern().pattern() + "'");
        }
        return m;
    }

    /**
     * Gets the trap OID.
     *
     * @param trap the trap object
     * @return the trap OID
     */
    private String getTrapOid(Notification trap) {
        return '.' + trap.getOidStr();
    }

    /**
     * Adds the mask element.
     *
     * @param event the event object
     * @param name the name
     * @param value the value
     */
    private void addMaskElement(Event event, String name, String value) {
        if (event.getMask() == null) {
            throw new IllegalStateException("Event mask is null, must have been set before this method was called");
        }
        Maskelement me = new Maskelement();
        me.setMename(name);
        me.addMevalue(value);
        event.getMask().addMaskelement(me);
    }
}
