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
import java.util.Iterator;
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
import org.opennms.features.vaadin.mibcompiler.api.MibParser;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.netmgt.dao.support.IndexStorageStrategy;
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
/*
 * TODO Pending features:
 * 
 * - Better error output (more human readable).
 * - Fix HTML encoding for Description and LogMsg
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

    /** The errors. */
    private String errors;

    /** The missing dependencies. */
    private List<String> missingDependencies;

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
    public void setMibDirectory(File mibDirectory) {
        this.mibDirectory = mibDirectory;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.MibParser#parseMib(java.io.File)
     */
    public boolean parseMib(File mibFile) {
        // Validate MIB Directory
        if (mibDirectory == null) {
            errors = "MIB directory has not been set.";
            return false;
        }

        // Reset error handler
        errorHandler.reset();
        errors = null;

        // Add MIB to be parsed
        List<URL> inputUrls = new ArrayList<URL>();
        try {
            inputUrls.add(mibFile.toURI().toURL());
        } catch (Exception e) {
            errors = e.getMessage();
            return false;
        }
        parser.getFileParserPhase().setInputUrls(inputUrls);

        // Parse MIB
        LogUtils.debugf(this, "Parsing %s", mibFile.getAbsolutePath());
        SmiMib mib = parser.parse();
        if (parser.getProblemEventHandler().isNotOk()) {
            LogUtils.infof(this, "Some errors has been found when processing %s", mibFile.getAbsolutePath());
            // Check for dependencies and update URLs if the MIBs exists on the MIB directory
            missingDependencies = errorHandler.getDependencies();
            for (Iterator<String> it = missingDependencies.iterator(); it.hasNext();) {
                String dependency = it.next();
                for (String suffix : MIB_SUFFIXES) {
                    File f = new File(mibDirectory, dependency + suffix);
                    if (f.exists()) {
                        LogUtils.infof(this, "Adding dependency file %s", f.getAbsolutePath());
                        try {
                            inputUrls.add(f.toURI().toURL());
                        } catch (Exception e) {
                            errors = e.getMessage();
                            return false;
                        }
                        it.remove();
                    }
                }
            }
            if (missingDependencies.isEmpty()) {
                LogUtils.infof(this, "Reparsing all files %s", inputUrls);
                // All dependencies found, trying again.
                errorHandler.reset();
                mib = parser.parse();
                if (parser.getProblemEventHandler().isNotOk()) {
                    LogUtils.errorf(this, "Found errors when processing %s: %s", mibFile, errorHandler.getMessages());
                    return false;
                }
            } else {
                // There are still unsatisfied dependencies.
                LogUtils.warnf(this, "There are unsatisfied dependencies remaining: " + missingDependencies);
                return false;
            }
        }

        module = getModule(mib, mibFile);
        return module != null;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.MibParser#getFormattedErrors()
     */
    public String getFormattedErrors() {
        return errorHandler.getMessages();
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.MibParser#getMissingDependencies()
     */
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
    public Events getEvents(String ueibase) {
        if (module == null) {
            return null;
        }
        LogUtils.infof(this, "Generating events for %s using the following UEI Base: %s", module.getId(), ueibase);
        try {
            return convertMibToEvents(module, ueibase);
        } catch (Throwable e) {
            errors = e.getMessage();
            if (errors == null || errors.trim().equals(""))
                errors = "An unknown error accured when generating events objects from the MIB " + module.getId();
            LogUtils.errorf(this, e, "Event parsing error: %s", errors);
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.api.MibParser#getDataCollection()
     */
    public DatacollectionGroup getDataCollection() {
        if (module == null) {
            return null;
        }
        LogUtils.infof(this, "Generating data collection configuration for %s", module.getId());
        DatacollectionGroup dcGroup = new DatacollectionGroup();
        dcGroup.setName(module.getId());
        try {
            for (SmiVariable v : module.getVariables()) {
                String groupName = null, resourceType = null;
                if (v.getNode().getParent().getSingleValue() instanceof SmiRow) {
                    groupName = v.getNode().getParent().getParent().getSingleValue().getId();
                    resourceType = v.getNode().getParent().getSingleValue().getId();
                } else {
                    groupName = v.getNode().getParent().getSingleValue().getId();
                }
                Group group = getGroup(dcGroup, groupName, resourceType);
                String typeName = getType(v.getType().getPrimitiveType());
                if (typeName != null) {
                    MibObj mibObj = new MibObj();
                    mibObj.setOid('.' + v.getOidStr());
                    mibObj.setInstance(resourceType == null ? "0" : resourceType);
                    mibObj.setAlias(v.getId());
                    mibObj.setType(typeName);
                    group.addMibObj(mibObj);
                }
            }
        } catch (Throwable e) {
            errors = e.getMessage();
            if (errors == null || errors.trim().equals(""))
                errors = "An unknown error accured when generating data collection objects from the MIB " + module.getId();
            LogUtils.errorf(this, e, "Data Collection parsing error: %s", errors);
            return null;
        }
        return dcGroup;
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
        errors = "Can't find the MIB module for " + mibFile;
        return null;
    }

    /*
     * Data Collection processing methods
     */

    /**
     * Gets the type.
     *
     * @param type the type
     * @return the type
     */
    private String getType(SmiPrimitiveType type) {
        if (type.equals(SmiPrimitiveType.ENUM)) // ENUM are just informational elements.
            return "string";
        if (type.equals(SmiPrimitiveType.TIME_TICKS)) // TimeTicks will be treated as strings.
            return "string";
        if (type.equals(SmiPrimitiveType.OBJECT_IDENTIFIER)) // ObjectIdentifier will be treated as strings.
            return "string";
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
     * FIXME: This works for notifications (SmiNotificationType) not with SmiTrapType (which is different)
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
        Event evt = new Event();
        // Set the event's UEI, event-label, logmsg, severity, and descr
        evt.setUei(getTrapEventUEI(trap, ueibase));
        evt.setEventLabel(getTrapEventLabel(trap));
        evt.setLogmsg(getTrapEventLogmsg(trap));
        evt.setSeverity("Indeterminate");
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
        return getMatcherForOid(getTrapOid(trap)).group(1);
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
