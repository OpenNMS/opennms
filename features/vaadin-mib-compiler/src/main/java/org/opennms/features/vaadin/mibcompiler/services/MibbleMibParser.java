/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibLoader;
import net.percederberg.mibble.MibLoaderException;
import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibTypeTag;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.snmp.SnmpNotificationType;
import net.percederberg.mibble.snmp.SnmpObjectType;
import net.percederberg.mibble.snmp.SnmpTrapType;
import net.percederberg.mibble.snmp.SnmpType;
import net.percederberg.mibble.type.IntegerType;
import net.percederberg.mibble.value.ObjectIdentifierValue;

import org.opennms.features.vaadin.mibcompiler.api.MibParser;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.netmgt.dao.support.IndexStorageStrategy;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Decode;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.netmgt.xml.eventconf.Logmsg;
import org.opennms.netmgt.xml.eventconf.Mask;
import org.opennms.netmgt.xml.eventconf.Maskelement;
import org.opennms.netmgt.xml.eventconf.Varbindsdecode;

/**
 * Mibble implementation of the interface MibParser.
 * 
 * <p>Mibble is a GPL Library.</p>
 * <p>The event parsing code has been extracted from Mib2Events.</p>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class MibbleMibParser implements MibParser, Serializable {

    /** The errors. */
    private String errors;

    /** The loader. */
    private MibLoader loader;

    /** The MIB object. */
    private Mib mib;

    /** The pattern. */
    private static final Pattern DEPENDENCY_PATERN = Pattern.compile("couldn't find referenced MIB '(.+)'", Pattern.MULTILINE);

    /** The Constant TRAP_OID_PATTERN. */
    private static final Pattern TRAP_OID_PATTERN = Pattern.compile("(.*)\\.(\\d+)$");

    /**
     * Instantiates a new Mibble MIB parser.
     */
    public MibbleMibParser() {
        loader = new MibLoader();
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.MibParser#addMibDirectory(java.io.File)
     */
    public void addMibDirectory(File mibDirectory) {
        LogUtils.debugf(this, "Adding MIB directory to %s", mibDirectory);
        loader.addDir(mibDirectory);
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.MibParser#parseMib(java.io.File)
     */
    public boolean parseMib(File mibFile) {
        errors = null; // Cleaning existing errors.
        mib = null; // Cleaning existing MIB object.
        try {
            LogUtils.debugf(this, "Parsing MIB file %s", mibFile);
            mib = loader.load(mibFile);
            if (mib == null) {
                errors = "Can't load " + mibFile + ", please try again.";
                return false;
            }
            if (mib.getLog().warningCount() > 0) {
                LogUtils.infof(this, "Warning found when processing %s", mibFile);
                handleMibError(mib.getLog());
                return false;
            }
            return true;
        } catch (IOException e) {
            errors = e.getMessage();
            LogUtils.infof(this, "IO error: " + errors);
        } catch (MibLoaderException e) {
            handleMibError(e.getLog());
            LogUtils.infof(this, "MIB error: " + errors);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.MibParser#getFormattedErrors()
     */
    public String getFormattedErrors() {
        return errors;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.MibParser#getMissingDependencies()
     */
    public List<String> getMissingDependencies() {
        List<String> dependencies = new ArrayList<String>();
        if (errors == null)
            return dependencies;
        Matcher m = DEPENDENCY_PATERN.matcher(errors);
        while (m.find()) {
            final String dep = m.group(1);
            if (!dependencies.contains(dep))
                dependencies.add(dep);
        }
        return dependencies;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.services.MibParser#getMibName()
     */
    @Override
    public String getMibName() {
        return mib == null ? "Unknown" : mib.getName();
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.services.MibParser#getEvents(java.lang.String)
     */
    public Events getEvents(String ueibase) {
        if (mib == null) {
            return null;
        }
        LogUtils.infof(this, "Generating events for %s using the following UEI Base: %s", mib.getName(), ueibase);
        try {
            return convertMibToEvents(mib, ueibase);
        } catch (Throwable e) {
            errors = e.getMessage();
            if (errors == null || errors.trim().equals(""))
                errors = "An unknown error accured when generating events objects from the MIB " + mib.getName();
            LogUtils.errorf(this, e, "Mib2Events error: %s", errors);
            return null;
        }
    }

    // TODO This is an experimental implementation using Mibble
    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.api.MibParser#getDataCollection()
     */
    public DatacollectionGroup getDataCollection() {
        if (mib == null) {
            return null;
        }
        LogUtils.infof(this, "Generating data collection configuration for %s", mib.getName());
        DatacollectionGroup dcGroup = new DatacollectionGroup();
        dcGroup.setName(mib.getName());
        try {
            for (Object o : mib.getAllSymbols()) {
                if (o instanceof MibValueSymbol) {
                    MibValueSymbol node = (MibValueSymbol) o;
                    if (node.getType() instanceof SnmpObjectType && !node.isTable() && !node.isTableRow()) {
                        SnmpObjectType type = (SnmpObjectType) node.getType();
                        String groupName = node.isTableColumn() ? node.getParent().getParent().getName() : node.getParent().getName();
                        String resourceType = node.isTableColumn() ? node.getParent().getName() : null;
                        Group group = getGroup(dcGroup, groupName, resourceType);
                        String typeName = getType(type.getSyntax());
                        if (typeName != null) {
                            MibObj mibObj = new MibObj();
                            mibObj.setOid('.' + node.getValue().toString());
                            mibObj.setInstance(node.isTableColumn() ? resourceType : "0");
                            mibObj.setAlias(node.getName());
                            mibObj.setType(typeName);
                            group.addMibObj(mibObj);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            errors = e.getMessage();
            if (errors == null || errors.trim().equals(""))
                errors = "An unknown error accured when generating data collection objects from the MIB " + mib.getName();
            LogUtils.errorf(this, e, "Data Collection parsing error: %s", errors);
            return null;
        }
        return dcGroup;
    }

    /**
     * Gets the group.
     *
     * @param data the data
     * @param groupName the group name
     * @param resourceType the resource type
     * @return the group
     */
    public Group getGroup(DatacollectionGroup data, String groupName, String resourceType) {
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

    /**
     * Gets the type.
     *
     * @param type the type
     * @return the type
     */
    private String getType(MibType type) {
        if (type.hasTag(MibTypeTag.UNIVERSAL_CATEGORY, 2)) { // INTEGER / INTEGER32
            return "Integer32";
        } else if (type.hasTag(MibTypeTag.UNIVERSAL_CATEGORY, 4)) { // OCTET STRING
            return "OctetString";
        } else if (type.hasTag(MibTypeTag.UNIVERSAL_CATEGORY, 6)) { // OBJECT IDENTIFIER
            return "String"; // TODO Is this Correct?
        } else if (type.hasTag(MibTypeTag.APPLICATION_CATEGORY, 0)) { // IPADDRESS
            return "String"; // TODO Is this Correct?
        } else if (type.hasTag(MibTypeTag.APPLICATION_CATEGORY, 1)) { // COUNTER
            return "Counter";
        } else if (type.hasTag(MibTypeTag.APPLICATION_CATEGORY, 2)) { // GAUGE
            return "Gauge";
        } else if (type.hasTag(MibTypeTag.APPLICATION_CATEGORY, 3)) { // TIMETICKS
            return "String"; // TODO Is this Correct?
        } else if (type.hasTag(MibTypeTag.APPLICATION_CATEGORY, 4)) { // OPAQUE
            return "String"; // TODO Is this Correct?
        } else if (type.hasTag(MibTypeTag.APPLICATION_CATEGORY, 6)) { // COUNTER64
            return "Counter64";
        }
        return null;
    }

    /**
     * Handle MIB error.
     *
     * @param log the log
     */
    private void handleMibError(MibLoaderLog log) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        log.printTo(ps);
        errors = os.toString();
    }

    /*
     * The following code has been extracted from Mib2Events
     */

    /**
     * Convert mib to events.
     *
     * @param mib the mib
     * @param ueibase the ueibase
     * @return the events
     */
    protected Events convertMibToEvents(Mib mib, String ueibase) {
        Events events = new Events();
        for (Object sym : mib.getAllSymbols()) {
            if (!(sym instanceof MibValueSymbol)) {
                continue;
            }
            MibValueSymbol vsym = (MibValueSymbol) sym;
            if ((!(vsym.getType() instanceof SnmpNotificationType)) && (!(vsym.getType() instanceof SnmpTrapType))) {
                continue;
            }
            events.addEvent(getTrapEvent(vsym, ueibase));
        }
        return events;
    }

    /**
     * Gets the trap event.
     *
     * @param trapValueSymbol the trap value symbol
     * @param ueibase the ueibase
     * @return the trap event
     */
    protected Event getTrapEvent(MibValueSymbol trapValueSymbol, String ueibase) {
        Event evt = new Event();
        // Set the event's UEI, event-label, logmsg, severity, and descr
        evt.setUei(getTrapEventUEI(trapValueSymbol, ueibase));
        evt.setEventLabel(getTrapEventLabel(trapValueSymbol));
        evt.setLogmsg(getTrapEventLogmsg(trapValueSymbol));
        evt.setSeverity("Indeterminate");
        evt.setDescr(getTrapEventDescr(trapValueSymbol));
        List<Varbindsdecode> decode = getTrapVarbindsDecode(trapValueSymbol);
        if (!decode.isEmpty()) {
            evt.setVarbindsdecode(decode);
        }
        evt.setMask(new Mask());
        // The "ID" mask element (trap enterprise)
        addMaskElement(evt, "id", getTrapEnterprise(trapValueSymbol));
        // The "generic" mask element: hard-wired to enterprise-specific(6)
        addMaskElement(evt, "generic", "6");
        // The "specific" mask element (trap specific-type)
        addMaskElement(evt, "specific", getTrapSpecificType(trapValueSymbol));
        return evt;
    }

    /**
     * Gets the trap event uei.
     *
     * @param trapValueSymbol the trap value symbol
     * @param ueibase the ueibase
     * @return the trap event uei
     */
    protected  String getTrapEventUEI(MibValueSymbol trapValueSymbol, String ueibase) {
        StringBuffer buf = new StringBuffer(ueibase);
        if (! ueibase.endsWith("/")) {
            buf.append("/");
        }
        buf.append(trapValueSymbol.getName());
        return buf.toString();
    }

    /**
     * Gets the trap event label.
     *
     * @param trapValueSymbol the trap value symbol
     * @return the trap event label
     */
    protected String getTrapEventLabel(MibValueSymbol trapValueSymbol) {
        StringBuffer buf = new StringBuffer();
        buf.append(trapValueSymbol.getMib());
        buf.append(" defined trap event: ");
        buf.append(trapValueSymbol.getName());
        return buf.toString();
    }

    /**
     * Gets the trap event logmsg.
     *
     * @param trapValueSymbol the trap value symbol
     * @return the trap event logmsg
     */
    protected Logmsg getTrapEventLogmsg(MibValueSymbol trapValueSymbol) {
        Logmsg msg = new Logmsg();
        msg.setDest("logndisplay");
        final StringBuffer dbuf = new StringBuffer();
        dbuf.append("<p>");
        dbuf.append("\n");
        dbuf.append("\t").append(trapValueSymbol.getName()).append(" trap received\n");
        int vbNum = 1;
        for (MibValue vb : getTrapVars(trapValueSymbol)) {
            dbuf.append("\t").append(vb.getName()).append("=%parm[#").append(vbNum).append("]%\n");
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
     * Gets the trap vars.
     *
     * @param trapValueSymbol the trap value symbol
     * @return the trap vars
     */
    protected List<MibValue> getTrapVars(MibValueSymbol trapValueSymbol) {
        if (trapValueSymbol.getType() instanceof SnmpNotificationType) {
            SnmpNotificationType v2notif = (SnmpNotificationType) trapValueSymbol.getType();
            return getV2NotificationObjects(v2notif);
        } else if (trapValueSymbol.getType() instanceof SnmpTrapType) {
            SnmpTrapType v1trap = (SnmpTrapType) trapValueSymbol.getType();
            return getV1TrapVariables(v1trap);
        } else {
            throw new IllegalStateException("trap type is not an SNMP v1 Trap or v2 Notification");
        }
    }

    /**
     * Gets the v1 trap variables.
     *
     * @param v1trap the v1trap
     * @return the v1 trap variables
     */
    @SuppressWarnings("unchecked")
    protected List<MibValue> getV1TrapVariables(SnmpTrapType v1trap) {
        return v1trap.getVariables();
    }

    /**
     * Gets the v2 notification objects.
     *
     * @param v2notif the v2notif
     * @return the v2 notification objects
     */
    @SuppressWarnings("unchecked")
    protected List<MibValue> getV2NotificationObjects(SnmpNotificationType v2notif) {
        return v2notif.getObjects();
    }

    /**
     * Gets the trap event descr.
     *
     * @param trapValueSymbol the trap value symbol
     * @return the trap event descr
     */
    protected String getTrapEventDescr(MibValueSymbol trapValueSymbol) {
        String description = ((SnmpType) trapValueSymbol.getType()).getDescription();
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
        for (MibValue vb : getTrapVars(trapValueSymbol)) {
            dbuf.append("\t<tr><td><b>\n\n\t").append(vb.getName());
            dbuf.append("</b></td><td>\n\t%parm[#").append(vbNum).append("]%;</td><td><p>");
            SnmpObjectType snmpObjectType = ((SnmpObjectType) ((ObjectIdentifierValue) vb).getSymbol().getType());
            if (snmpObjectType.getSyntax().getClass().equals(IntegerType.class)) {
                IntegerType integerType = (IntegerType) snmpObjectType.getSyntax();
                if (integerType.getAllSymbols().length > 0) {
                    SortedMap<Integer, String> map = new TreeMap<Integer, String>();
                    for (MibValueSymbol sym : integerType.getAllSymbols()) {
                        map.put(new Integer(sym.getValue().toString()), sym.getName());
                    }
                    dbuf.append("\n");
                    for (Entry<Integer, String> entry : map.entrySet()) {
                        dbuf.append("\t\t").append(entry.getValue()).append("(").append(entry.getKey()).append(")\n");
                    }
                    dbuf.append("\t");
                }
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
     * @param trapValueSymbol the trap value symbol
     * @return the trap varbinds decode
     */
    protected List<Varbindsdecode> getTrapVarbindsDecode(MibValueSymbol trapValueSymbol) {
        Map<String, Varbindsdecode> decode = new LinkedHashMap<String, Varbindsdecode>();
        int vbNum = 1;
        for (MibValue vb : getTrapVars(trapValueSymbol)) {
            String parmName = "parm[#" + vbNum + "]";
            SnmpObjectType snmpObjectType = ((SnmpObjectType) ((ObjectIdentifierValue) vb).getSymbol().getType());
            if (snmpObjectType.getSyntax().getClass().equals(IntegerType.class)) {
                IntegerType integerType = (IntegerType) snmpObjectType.getSyntax();
                if (integerType.getAllSymbols().length > 0) {
                    SortedMap<Integer, String> map = new TreeMap<Integer, String>();
                    for (MibValueSymbol sym : integerType.getAllSymbols()) {
                        map.put(new Integer(sym.getValue().toString()), sym.getName());
                    }
                    for (Entry<Integer, String> entry : map.entrySet()) {
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
            }
            vbNum++;
        }
        return new ArrayList<Varbindsdecode>(decode.values());
    }

    /**
     * Gets the trap enterprise.
     *
     * @param trapValueSymbol the trap value symbol
     * @return the trap enterprise
     */
    private String getTrapEnterprise(MibValueSymbol trapValueSymbol) {
        return getMatcherForOid(getTrapOid(trapValueSymbol)).group(1);
    }

    /**
     * Gets the trap specific type.
     *
     * @param trapValueSymbol the trap value symbol
     * @return the trap specific type
     */
    private String getTrapSpecificType(MibValueSymbol trapValueSymbol) {
        return getMatcherForOid(getTrapOid(trapValueSymbol)).group(2);
    }

    /**
     * Gets the matcher for oid.
     *
     * @param trapOid the trap oid
     * @return the matcher for oid
     */
    private Matcher getMatcherForOid(String trapOid) {
        Matcher m = TRAP_OID_PATTERN.matcher(trapOid);
        if (!m.matches()) {
            throw new IllegalStateException("Could not match the trap OID '" + trapOid + "' against '" + m.pattern().pattern() + "'");
        }
        return m;
    }

    /**
     * Gets the trap oid.
     *
     * @param trapValueSymbol the trap value symbol
     * @return the trap oid
     */
    private String getTrapOid(MibValueSymbol trapValueSymbol) {
        if (trapValueSymbol.getType() instanceof SnmpNotificationType) {
            return "." + trapValueSymbol.getValue().toString();
        } else if (trapValueSymbol.getType() instanceof SnmpTrapType) {
            SnmpTrapType v1trap = (SnmpTrapType) trapValueSymbol.getType();
            return "." + v1trap.getEnterprise().toString() + "." + trapValueSymbol.getValue().toString();
        } else {
            throw new IllegalStateException("Trying to get trap information from an object that's not a trap and not a notification");
        }
    }

    /**
     * Adds the mask element.
     *
     * @param event the event
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
