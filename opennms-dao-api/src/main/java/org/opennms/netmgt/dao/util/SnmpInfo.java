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
package org.opennms.netmgt.dao.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;

import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.events.api.EventDatabaseConstants;
import org.opennms.netmgt.xml.event.Snmp;

import com.google.common.base.Strings;

/**
 * This class is used to format the Snmp block into an appropiate string for
 * storage in the event data storage.
 *
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public abstract class SnmpInfo {
    /**
     * <P>
     * Converts the SNMP information from the event into a string that can be
     * stored into the database. The information is formatted in by separating
     * the of the textual fields with a delimiter character (a comma ',').
     * </P>
     *
     * <P>
     * If the enterprise id text information is not present then the string will
     * have only two commas. An extra comma is not added to signify the missing
     * field.
     * </P>
     *
     * @see EventDatabaseConstants#DB_ATTRIB_DELIM
     * @see EventDatabaseConstants#escape
     * @see EventDatabaseConstants#DB_ATTRIB_DELIM
     * @see EventDatabaseConstants#escape
     * @return The smnpblock as a string
     * @param info a {@link org.opennms.netmgt.xml.event.Snmp} object.
     * @param maxlen a int.
     */
    public static String format(Snmp info, int maxlen) {
        if (info == null) {
            return null;
        }

        // id
        final StringBuilder snmpStr = new StringBuilder(info.getId());

        // id text
        if (info.getIdtext() != null) {
            snmpStr.append(EventDatabaseConstants.DB_ATTRIB_DELIM).append(EventDatabaseConstants.escape(info.getIdtext(), EventDatabaseConstants.DB_ATTRIB_DELIM));
        } else {
            snmpStr.append(EventDatabaseConstants.DB_ATTRIB_DELIM).append("undefined");
        }

        // version
        snmpStr.append(EventDatabaseConstants.DB_ATTRIB_DELIM).append(info.getVersion());

        // specific
        if (info.hasSpecific()) {
            snmpStr.append(EventDatabaseConstants.DB_ATTRIB_DELIM).append(Integer.toString(info.getSpecific()));
        } else {
            snmpStr.append(EventDatabaseConstants.DB_ATTRIB_DELIM).append("undefined");
        }

        // generic
        if (info.hasGeneric()) {
            snmpStr.append(EventDatabaseConstants.DB_ATTRIB_DELIM).append(Integer.toString(info.getGeneric()));
        } else {
            snmpStr.append(EventDatabaseConstants.DB_ATTRIB_DELIM).append("undefined");
        }

        // community
        if (info.getCommunity() != null) {
            snmpStr.append(EventDatabaseConstants.DB_ATTRIB_DELIM).append(info.getCommunity());
        } else {
            snmpStr.append(EventDatabaseConstants.DB_ATTRIB_DELIM).append("undefined");
        }

        return EventDatabaseConstants.format(snmpStr.toString(), maxlen);
    }

    /**
     *  Tries to create Snmp Object from formatted eventSnmp String.
     * @param eventSnmp formatted snmp string that has all the @Snmp fields.
     * @return snmp Snmp object created or null if it can't be created.
     */
    public static Snmp createSnmp(String eventSnmp) {
        if (Strings.isNullOrEmpty(eventSnmp)) {
            return null;
        }
        String[] snmpFields = eventSnmp.split(String.valueOf(EventDatabaseConstants.DB_ATTRIB_DELIM));
        if (snmpFields.length == 0) {
            return null;
        }
        Iterator<String> fields = Arrays.stream(snmpFields).iterator();
        Snmp snmp = new Snmp();
        snmp.setId(fields.next());
        setFieldText(fields, snmp::setIdtext);
        setFieldText(fields, snmp::setVersion);
        setFieldValue(fields, snmp::setSpecific);
        setFieldValue(fields, snmp::setGeneric);
        setFieldText(fields, snmp::setCommunity);
        return snmp;
    }

    private static void setFieldText(Iterator<String> fields, Consumer<String> setter) {
        if (fields.hasNext()) {
            String fieldValue = fields.next();
            if (!Strings.isNullOrEmpty(fieldValue) && !fieldValue.equals("undefined")) {
                setter.accept(fieldValue);
            }
        }
    }

    private static void setFieldValue(Iterator<String> fields, Consumer<Integer> setter) {
        if (fields.hasNext()) {
            String fieldValue = fields.next();
            if (!Strings.isNullOrEmpty(fieldValue) && !fieldValue.equals("undefined")) {
                setter.accept(StringUtils.parseInt(fieldValue, null));
            }
        }
    }
}
