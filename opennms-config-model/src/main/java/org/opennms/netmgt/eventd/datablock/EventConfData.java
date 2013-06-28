/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd.datablock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 * The information read from the eventconf.xml is stored here. It maintains
 *  a map,  keyed with 'EventKey's.
 *
 *  It also has an UEI to 'EventKey'list map - this mapping fastens the lookup
 *  for OpenNMS internal events when different masks are configured for the
 *  same UEI.
 *
 *  When a lookup is to be done for an 'Event',
 *  - its 'key' is used to get a lookup,
 *  - if no match is found for the key, UEI is used to lookup the keys that got added for that UEI
 *    and the first best fit in the event map for any of the UEI keys are used
 *  - if there is still no match at this point, all keys in the eventconf are iterated through to
 *    find a match
 *
 * </pre>
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * @version $Id: $
 */
public class EventConfData extends Object {
    private static final Logger LOG = LoggerFactory.getLogger(EventConfData.class);
    /**
     * The map keyed with 'EventKey's
     */
    private LinkedHashMap<EventKey, org.opennms.netmgt.xml.eventconf.Event> m_eventMap;

    /**
     * The map of UEI to 'EventKey's list - used mainly to find matches for the
     * OpenNMS internal events faster(in cases where there are multiple masks
     * for the same UEI)
     */
    private LinkedHashMap<String, List<EventKey>> m_ueiToKeyListMap;

    /**
     * Check whether the event matches the passed key
     * 
     * @return true if the event matches the passed key
     */
    private boolean eventMatchesKey(EventKey eventKey, org.opennms.netmgt.xml.event.Event event) {
        // go through the key elements and see if this event will match
        boolean maskMatch = true;

        Iterator<String> keysetIter = eventKey.keySet().iterator();
        while (keysetIter.hasNext() && maskMatch) {
            String key = keysetIter.next();

            @SuppressWarnings("unchecked")
            List<String> maskValues = (List<String>) eventKey.get(key);

            // get the event value for this key
            String eventvalue = EventKey.getMaskElementValue(event, key);
            maskMatch = eventValuePassesMaskValue(eventvalue, maskValues);
            if (!maskMatch) {
                return maskMatch;
            }
        }

        return maskMatch;
    }

    /**
     * Check whether the eventvalue passes any of the mask values Mask values
     * ending with a '%' only need to be a substring of the eventvalue for the
     * eventvalue to pass the mask
     *
     * Enhanced 2005/08/31 to allow regular expression in eventconf.
     *
     * @return true if the values passes the mask
     * @param eventvalue a {@link java.lang.String} object.
     * @param maskValues a {@link java.util.List} object.
     */
    protected static boolean eventValuePassesMaskValue(String eventvalue, List<String> maskValues) {
        boolean maskMatch = false;

        Iterator<String> valiter = maskValues.iterator();
        while (valiter.hasNext() && !maskMatch) {
            String keyvalue = valiter.next();
            if (keyvalue != null && eventvalue != null) {
                int len = keyvalue.length();
                if (keyvalue.equals(eventvalue)) {
                    maskMatch = true;
                } else if (keyvalue.charAt(0) == '~'){
                    if (eventvalue.matches(keyvalue.substring(1))) {
                        maskMatch = true;
                    }
                } else if (keyvalue.charAt(len - 1) == '%') {
                    if (eventvalue.startsWith(keyvalue.substring(0, len - 1))) {
                        maskMatch = true;
                    }
                }
            }
        }

        return maskMatch;
    }

    /**
     * Update the uei to keylist map
     */
    private void updateUeiToKeyListMap(EventKey eventKey, org.opennms.netmgt.xml.eventconf.Event event) {
        String eventUei = event.getUei();
        List<EventKey> keylist = m_ueiToKeyListMap.get(eventUei);
        if (keylist == null) {
            keylist = new ArrayList<EventKey>();
            keylist.add(eventKey);

            m_ueiToKeyListMap.put(eventUei, keylist);
        } else {
            if (!keylist.contains(eventKey)) {
                keylist.add(eventKey);
            }
        }
    }

    /**
     * Default constructor - allocate the maps
     */
    public EventConfData() {
        m_eventMap = new LinkedHashMap<EventKey, org.opennms.netmgt.xml.eventconf.Event>();

        m_ueiToKeyListMap = new LinkedHashMap<String, List<EventKey>>();
    }

    /**
     * Add an event - add to the 'EventKey' map using the event mask by default.
     * If the event has snmp information, add using the snmp EID
     *
     * @param event
     *            the org.opennms.netmgt.xml.eventconf.Event
     */
    public synchronized void put(org.opennms.netmgt.xml.eventconf.Event event) {

        // the event key
        EventKey eventKey = new EventKey(event);

        // add to the configevent map first
        m_eventMap.put(eventKey, event);

        // add to the uei to key list map
        updateUeiToKeyListMap(eventKey, event);

        // if event has snmp information, add to the snmp map
        org.opennms.netmgt.xml.eventconf.Snmp eventSnmp = event.getSnmp();
        if (eventSnmp != null) {
            String eventEID = eventSnmp.getId();
            if (eventEID != null) {
                EventKey snmpKey = new EventKey();
                snmpKey.put(EventKey.TAG_SNMP_EID, new EventMaskValueList(eventEID));

                m_eventMap.put(snmpKey, event);

                // add to the uei to key list map
                updateUeiToKeyListMap(snmpKey, event);
            }
        }
    }

    /**
     * Add an event with the specified key
     *
     * @param key
     *            the EventKey for this event
     * @param event
     *            the org.opennms.netmgt.xml.eventconf.Event
     */
    public synchronized void put(EventKey key, org.opennms.netmgt.xml.eventconf.Event event) {
        m_eventMap.put(key, event);

        // add to the uei to key list map
        updateUeiToKeyListMap(key, event);
    }

    /**
     * <pre>
     * Get the right configuration for the event - the eventkey is used first.
     *  If no match is found, the event's uei to keylist is iterated through, and these keys
     *  used to lookup the event map. if still no match is found, all eventconf
     *  keys are iterated through to find a match. The first successful match is returned.
     *
     *
     * <EM>
     * NOTE:
     * </EM>
     * The first right config event that the event matches is returned.
     *  The ordering of the configurations is the responsibility of the user
     * </pre>
     *
     * @param event
     *            the event which is to be looked up
     * @return a {@link org.opennms.netmgt.xml.eventconf.Event} object.
     */
    public synchronized org.opennms.netmgt.xml.eventconf.Event getEvent(org.opennms.netmgt.xml.event.Event event) {
        org.opennms.netmgt.xml.eventconf.Event matchedEvent = null;

        //
        // use the eventkey and see if there is a match
        //
	/*
        EventKey key = new EventKey(event);
        matchedEvent = m_eventMap.get(key);
        if (matchedEvent != null) {
            LOG.debug("Match found using key: {}", key);

            return matchedEvent;
        }
        */

        //
        // get the UEI and see if the UEI keys get a match - this step is here
        // to make the matching process faster in case of usual internal events
        // of OpenNMS, using the UEI shortens the search as against going
        // through
        // the entire eventconf for each event
        //
	/*
        String uei = event.getUei();
        if (uei != null) {
            // Go through the uei to keylist map
            List<EventKey> keylist = m_ueiToKeyListMap.get(uei);
            if (keylist != null) {
                // check the event keys known for this uei
                matchedEvent = getMatchInKeyList(keylist, event);
            }
        }
        */

        //
        // if still no match, no option but to go through all known keys
        //
        if (matchedEvent == null) {
            Iterator<Entry<EventKey, org.opennms.netmgt.xml.eventconf.Event>> entryIterator = m_eventMap.entrySet().iterator();
            while (entryIterator.hasNext() && (matchedEvent == null)) {
                Entry<EventKey, org.opennms.netmgt.xml.eventconf.Event> entry = entryIterator.next();
                EventKey iterKey = entry.getKey();

                boolean keyMatchFound = eventMatchesKey(iterKey, event);

                // if a match was found, return the config
                if (keyMatchFound) {
                    LOG.debug("Match found using key: {}", iterKey);

                    matchedEvent = entry.getValue();
                }
            }
        }

        return matchedEvent;
    }

    /**
     * Get the event with the specified uei
     *
     * @param uei
     *            the uei
     * @return a {@link org.opennms.netmgt.xml.eventconf.Event} object.
     */
    public synchronized org.opennms.netmgt.xml.eventconf.Event getEventByUEI(String uei) {
        EventKey key = new EventKey();
        key.put(EventKey.TAG_UEI, new EventMaskValueList(uei));

        return m_eventMap.get(key);
    }

    /**
     * Clear out the data
     */
    public synchronized void clear() {
        m_eventMap.clear();
        m_ueiToKeyListMap.clear();
    }
}
