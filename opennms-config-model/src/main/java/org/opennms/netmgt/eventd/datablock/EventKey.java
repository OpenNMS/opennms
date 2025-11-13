/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.eventd.datablock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.opennms.netmgt.events.api.EventConstants;

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
public class EventKey extends LinkedHashMap<EventKey.Entity, Object> implements Serializable, Comparable<EventKey> {

    enum Type {
        TAG,
        VBNUMBER,
        VBOID;
    }

    public static class Entity {
        private final Type type;
        private final String key;

        public Entity(final Type type, final String key) {
            this.type = type;
            this.key = key;
        }
    }

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


    public static final String TAG_SNMP_TRAPOID = "trapoid";

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
    public EventKey(Map<Entity, Object> maskelements) {
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

        final org.opennms.netmgt.xml.eventconf.Mask mask = event.getMask();
        if ((mask == null) || mask.getMaskelements().size() == 0) {
            String uei = event.getUei();
            if (uei != null) {
                put(new Entity(Type.TAG, TAG_UEI), new EventMaskValueList(uei));
            }
        } else {
            for (org.opennms.netmgt.xml.eventconf.Maskelement maskelement : mask.getMaskelements()) {
                String name = maskelement.getMename();

                EventMaskValueList value = new EventMaskValueList();
                for (final String mevalue : maskelement.getMevalues()) {
                    value.add(mevalue);
                }

                put(new Entity(Type.TAG, name), value);
            }
            if (mask != null && mask.getVarbinds().size() != 0) {
                for (org.opennms.netmgt.xml.eventconf.Varbind varbind : mask.getVarbinds()) {
                    final EventMaskValueList vbvalues = new EventMaskValueList();
                    vbvalues.addAll(varbind.getVbvalues());

                    if (varbind.getVbnumber() != null) {
                        put(new Entity(Type.VBNUMBER, varbind.getVbnumber().toString()), vbvalues);
                    } else {
                        put(new Entity(Type.VBOID, varbind.getVboid()), vbvalues);
                    }
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
        if ((mask == null) || mask.getMaskelementCount() == 0) {
            String uei = event.getUei();
            if (uei != null) {
                put(new Entity(Type.TAG, TAG_UEI), uei);
            }
        } else {
            for (org.opennms.netmgt.xml.event.Maskelement maskelement : mask.getMaskelementCollection()) {
                String name = maskelement.getMename();
                String value = getMaskElementValue(event, new Entity(Type.TAG, name));

                put(new Entity(Type.TAG, name), value);
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
    @Override
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
    @Override
    public Object put(Entity key, Object value) {
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
    @Override
    public void putAll(Map<? extends Entity, ? extends Object> m) {
        super.putAll(m);
        evaluateHashCode();
    }

    /**
     * {@inheritDoc}
     *
     * Override to re-evaluate hashcode
     * @see java.util.HashMap#remove(Object)
     */
    @Override
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

        for (final Map.Entry<Entity, Object> entry : entrySet()) {
            final Entity key = entry.getKey();
            // m_hashCode = 31 * m_hashCode;

            // value
            final Object value = entry.getValue();

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
    @Override
    public int compareTo(EventKey obj) {
        return (hashCode() - obj.hashCode());
    }

    /**
     * Overrides the 'hashCode()' method in the superclass
     *
     * @return a hash code for this object
     */
    @Override
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
    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder("EventKey\n[\n\t");

        for (Map.Entry<Entity, Object> e : entrySet()) {
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
    public static String getMaskElementValue(org.opennms.netmgt.xml.event.Event event, Entity mename) {
        String retParmVal = null;

        if (mename.type == Type.TAG) {
            switch (mename.key) {
                case TAG_UEI:
                    retParmVal = event.getUei();
                    break;
                case TAG_SOURCE:
                    retParmVal = event.getSource();
                    break;
                case TAG_NODEID:
                    retParmVal = Long.toString(event.getNodeid());
                    break;
                case TAG_HOST:
                    retParmVal = event.getHost();
                    break;
                case TAG_INTERFACE:
                    retParmVal = event.getInterface();
                    break;
                case TAG_SNMPHOST:
                    retParmVal = event.getSnmphost();
                    break;
                case TAG_SERVICE:
                    retParmVal = event.getService();
                    break;
                case TAG_SNMP_EID:
                    if (event.getSnmp() != null) {
                        retParmVal = event.getSnmp().getId();
                    }
                    break;
                case TAG_SNMP_TRAPOID:
                    if (event.getSnmp() != null && event.getSnmp().hasTrapOID()) {
                        retParmVal = event.getSnmp().getTrapOID();
                    }

                    break;
                case TAG_SNMP_SPECIFIC: {
                        org.opennms.netmgt.xml.event.Snmp eventSnmpInfo = event.getSnmp();
                        if (eventSnmpInfo != null) {
                            if (eventSnmpInfo.hasSpecific()) {
                                retParmVal = Integer.toString(eventSnmpInfo.getSpecific());
                            }
                        }
                    }
                    break;
                case TAG_SNMP_GENERIC: {
                        org.opennms.netmgt.xml.event.Snmp eventSnmpInfo = event.getSnmp();
                        if (eventSnmpInfo != null) {
                            if (eventSnmpInfo.hasGeneric()) {
                                retParmVal = Integer.toString(eventSnmpInfo.getGeneric());
                            }
                        }
                    }
                    break;
                case TAG_SNMP_COMMUNITY: {
                        org.opennms.netmgt.xml.event.Snmp eventSnmpInfo = event.getSnmp();
                        if (eventSnmpInfo != null) {
                            retParmVal = eventSnmpInfo.getCommunity();
                        }
                    }
                default:
                    break;
            }
        } else {
            if (!event.getParmCollection().isEmpty()) {
                if (mename.type == Type.VBNUMBER) {
                    ArrayList<String> eventparms = new ArrayList<>();
                    for (org.opennms.netmgt.xml.event.Parm evParm : event.getParmCollection()) {
                        eventparms.add(EventConstants.getValueAsString(evParm.getValue()));
                    }
                    int vbnumber = Integer.parseInt(mename.key);
                    if (vbnumber > 0 && vbnumber <= eventparms.size()) {
                        retParmVal = (String) eventparms.get(vbnumber - 1);
                    }
                } else {
                    final ArrayList<String> eventParms = new ArrayList<>();
                    for (org.opennms.netmgt.xml.event.Parm eventParameter : event.getParmCollection()) {
                        final String oid = eventParameter.getParmName();
                        if (oid != null && oid.equals(mename.key)) {
                            retParmVal = EventConstants.getValueAsString(eventParameter.getValue());
                            break;
                        }
                    }
                }
            }
        }

        return retParmVal;
    }
}
