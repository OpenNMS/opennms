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

package org.opennms.netmgt.eventd.datablock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <pre>
 * The key for an event - it extends the {@link LinkedHashMap} and basically is a
 *  map of name/value pairs of the 'maskelements' block in the event.
 *  While the names are maskelement names,
 *  - if the event is a 'org.opennms.netmgt.xml.eventconf.Event',
 *    the maskvalue list is taken as the value
 *  - if the event is an 'org.opennms.netmgt.xml.event.Event',
 *    the value in the event for the mask element is used as the value.
 *
 *  This hashtable is pretty much constant once constructed - so the hashcode
 *  is evaluated once at construction and reused(if new values are added or
 *  values changed, hashcode is re-evaluated)
 * </pre>
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
public class EventKey extends LinkedHashMap<String, Object> implements Serializable, Comparable<EventKey> {
    /**
     * 
     */
    private static final long serialVersionUID = 3498941419429346315L;

    /**
     * The UEI xml tag
     */
    public static final String TAG_UEI = "uei";

    /**
     * The event source xml tag
     */
    public static final String TAG_SOURCE = "source";

    /**
     * The event nodeid xml tag
     */
    public static final String TAG_NODEID = "nodeid";

    /**
     * The event host xml tag
     */
    public static final String TAG_HOST = "host";

    /**
     * The event interface xml tag
     */
    public static final String TAG_INTERFACE = "interface";

    /**
     * The event snmp host xml tag
     */
    public static final String TAG_SNMPHOST = "snmphost";

    /**
     * The event service xml tag
     */
    public static final String TAG_SERVICE = "service";

    /**
     * The SNMP EID xml tag
     */
    public static final String TAG_SNMP_EID = "id";

    /**
     * The SNMP specific xml tag
     */
    public static final String TAG_SNMP_SPECIFIC = "specific";

    /**
     * The SNMP generic xml tag
     */
    public static final String TAG_SNMP_GENERIC = "generic";

    /**
     * The SNMP community xml tag
     */
    public static final String TAG_SNMP_COMMUNITY = "community";

    /**
     * The hash code calculated from the elements
     */
    private int m_hashCode;

    /**
     * Default constructor for this class
     */
    public EventKey() {
        super();
        m_hashCode = -1111;
    }

    /**
     * Constructor for this class
     *
     * @see java.util.HashMap#HashMap(int)
     * @param initCapacity a int.
     */
    public EventKey(int initCapacity) {
        super(initCapacity);
        m_hashCode = -1111;
    }

    /**
     * Constructor for this class
     *
     * @see java.util.HashMap#HashMap(int, float)
     * @param initCapacity a int.
     * @param loadFactor a float.
     */
    public EventKey(int initCapacity, float loadFactor) {
        super(initCapacity, loadFactor);
        m_hashCode = -1111;
    }

    /**
     * Constructor for this class
     *
     * @param maskelements
     *            the maskelements that should form this key
     */
    public EventKey(Map<String, Object> maskelements) {
        super(maskelements);

        m_hashCode = 1;

        // evaluate hash code
        evaluateHashCode();
    }

    /**
     * Constructor for this class
     *
     * @param event
     *            the config event that this will be the key for
     */
    public EventKey(org.opennms.netmgt.xml.eventconf.Event event) {
        super();

        m_hashCode = 1;

        org.opennms.netmgt.xml.eventconf.Mask mask = event.getMask();
        if ((mask == null) || (mask != null && mask.getMaskelementCount() == 0)) {
            String uei = event.getUei();
            if (uei != null) {
                put(TAG_UEI, new EventMaskValueList(uei));
            }
        } else {
            for (org.opennms.netmgt.xml.eventconf.Maskelement maskelement : mask.getMaskelementCollection()) {
                String name = maskelement.getMename();

                EventMaskValueList value = new EventMaskValueList();
                String[] mevalues = maskelement.getMevalue();
                for (int index = 0; index < mevalues.length; index++) {
                    value.add(mevalues[index]);
                }

                put(name, value);
            }
            if (mask != null && mask.getVarbindCount() != 0) {
                for (org.opennms.netmgt.xml.eventconf.Varbind varbind : mask.getVarbindCollection()) {
                    EventMaskValueList vbvalues = new EventMaskValueList();
                    int vbint = varbind.getVbnumber();
                    String vbnumber = Integer.toString(vbint);
                    String[] vbvaluelist = varbind.getVbvalue();
                    for (int index = 0; index < vbvaluelist.length; index++) {
                        vbvalues.add(vbvaluelist[index]);
                    }

                    put(vbnumber, vbvalues);
                }
            }
        }

    }

    /**
     * Constructor for this class
     *
     * @param event
     *            the event that this will be the key for
     */
    public EventKey(org.opennms.netmgt.xml.event.Event event) {
        super();

        m_hashCode = 1;

        org.opennms.netmgt.xml.event.Mask mask = event.getMask();
        if ((mask == null) || (mask != null && mask.getMaskelementCount() == 0)) {
            String uei = event.getUei();
            if (uei != null) {
                put(TAG_UEI, uei);
            }
        } else {
            for (org.opennms.netmgt.xml.event.Maskelement maskelement : mask.getMaskelementCollection()) {
                String name = maskelement.getMename();
                String value = getMaskElementValue(event, name);

                put(name, value);
            }
        }
    }

    /*
     * Following methods are to ensure hashcode is not out of sync with elements
     */

    /**
     * Override to re-evaluate hashcode
     *
     * @see java.util.HashMap#clear()
     */
    public void clear() {
        super.clear();
        evaluateHashCode();
    }

    /**
     * {@inheritDoc}
     *
     * Override to re-evaluate hashcode
     * @see java.util.HashMap#put(Object, Object)
     */
    public Object put(String key, Object value) {
        Object ret = super.put(key, value);
        evaluateHashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     *
     * Override to re-evaluate hashcode
     * @see java.util.HashMap#putAll(Map)
     */
    public void putAll(Map<? extends String, ? extends Object> m) {
        super.putAll(m);
        evaluateHashCode();
    }

    /**
     * {@inheritDoc}
     *
     * Override to re-evaluate hashcode
     * @see java.util.HashMap#remove(Object)
     */
    public Object remove(Object key) {
        Object ret = super.remove(key);
        evaluateHashCode();
        return ret;
    }

    /*
     * End methods to ensure hashcode is not out of sync with elements
     */

    /**
     * <pre>
     * Evaluate the hash code for this object
     * 
     *  This hashtable gets constructed once and does not really change -
     *  so hashcode is only evaluated at construction time. Also, while
     *  the superclass uses just the entry set to calculate the hashcode,
     *  this uses both the names and their values in calculating the hashcode
     * 
     */
    private void evaluateHashCode() {
        m_hashCode = 0;

        if (isEmpty()) {
            return;
        }

        for (String key : keySet()) {
            // m_hashCode = 31 * m_hashCode;

            // value
            Object value = get(key);

            // add key
            m_hashCode += (key == null ? 0 : key.hashCode());

            // add value
            m_hashCode += (value == null ? 0 : value.hashCode());
        }
    }

    /**
     * Implementation for the Comparable interface
     *
     * @see java.lang.Comparable#compareTo(Object)
     * @param obj a {@link org.opennms.netmgt.eventd.datablock.EventKey} object.
     * @return a int.
     */
    public int compareTo(EventKey obj) {
        return (hashCode() - obj.hashCode());
    }

    /**
     * Overrides the 'hashCode()' method in the superclass
     *
     * @return a hash code for this object
     */
    public int hashCode() {
        if (m_hashCode != -1111) {
            return m_hashCode;
        } else {
            return super.hashCode();
        }
    }

    /**
     * Returns a String equivalent of this object
     *
     * @return a String equivalent of this object
     */
    public String toString() {
        StringBuffer s = new StringBuffer("EventKey\n[\n\t");

        for (Map.Entry<String, Object> e : entrySet()) {
            s.append(e.getKey() + "    = " + e.getValue().toString() + "\n\t");
        }

        s.append("\n]\n");

        return s.toString();
    }

    /**
     * <pre>
     * Get the value of the mask element for this event.
     *
     * <em>
     * Note:
     * </em>
     *  The only event elements that can occur to
     *  uniquely identify an event are -
     *  uei, source, host, snmphost, nodeid, interface, service, id(SNMP EID), specific, generic, community
     *
     *  @return value of the event element
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @param mename a {@link java.lang.String} object.
     */
    public static String getMaskElementValue(org.opennms.netmgt.xml.event.Event event, String mename) {
        String retParmVal = null;

        if (mename.equals(TAG_UEI)) {
            retParmVal = event.getUei();
        } else if (mename.equals(TAG_SOURCE)) {
            retParmVal = event.getSource();
        } else if (mename.equals(TAG_NODEID)) {
            retParmVal = Long.toString(event.getNodeid());
        } else if (mename.equals(TAG_HOST)) {
            retParmVal = event.getHost();
        } else if (mename.equals(TAG_INTERFACE)) {
            retParmVal = event.getInterface();
        } else if (mename.equals(TAG_SNMPHOST)) {
            retParmVal = event.getSnmphost();
        } else if (mename.equals(TAG_SERVICE)) {
            retParmVal = event.getService();
        } else if (mename.equals(TAG_SNMP_EID)) {
            if (event.getSnmp() != null) {
                retParmVal = event.getSnmp().getId();
            }
        } else if (mename.equals(TAG_SNMP_SPECIFIC)) {
            org.opennms.netmgt.xml.event.Snmp eventSnmpInfo = event.getSnmp();
            if (eventSnmpInfo != null) {
                if (eventSnmpInfo.hasSpecific()) {
                    retParmVal = Integer.toString(eventSnmpInfo.getSpecific());
                }
            }
        } else if (mename.equals(TAG_SNMP_GENERIC)) {
            org.opennms.netmgt.xml.event.Snmp eventSnmpInfo = event.getSnmp();
            if (eventSnmpInfo != null) {
                if (eventSnmpInfo.hasGeneric()) {
                    retParmVal = Integer.toString(eventSnmpInfo.getGeneric());
                }
            }
        } else if (mename.equals(TAG_SNMP_COMMUNITY)) {
            org.opennms.netmgt.xml.event.Snmp eventSnmpInfo = event.getSnmp();
            if (eventSnmpInfo != null) {
                retParmVal = eventSnmpInfo.getCommunity();
            }
        } else if (event.getParmCollection().size() > 0) {
            ArrayList<String> eventparms = new ArrayList<String>();
            for (org.opennms.netmgt.xml.event.Parm evParm : event.getParmCollection()) {
                eventparms.add(EventUtil.getValueAsString(evParm.getValue()));
            }
            int vbnumber = Integer.parseInt(mename);
            if (vbnumber > 0 && vbnumber <= eventparms.size()) {
                retParmVal = (String) eventparms.get(vbnumber - 1);
            }
        }

        return retParmVal;
    }
}
