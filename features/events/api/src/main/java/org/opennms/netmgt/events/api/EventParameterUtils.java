/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.events.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;

/**
 * This is an utility class used to format the event parameters - to be inserted
 * into the 'events' table
 *
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver</A>
 */
public abstract class EventParameterUtils {
    /**
     * Format the list of event parameters
     * @param event TODO
     *
     * @return the formatted event parameters string
     */
    public static String format(final Event event) {
        if (event == null || event.getParmCollection() == null || event.getParmCollection().size() == 0) {
            return null;
        }

        boolean first = true;
        final StringBuilder parmbuf = new StringBuilder();

        for (final Parm parm : event.getParmCollection()) {
            if (parm.getParmName() != null && parm.getValue() != null && parm.getValue().getContent() != null) {
                if (!first) {
                    parmbuf.append(EventDatabaseConstants.MULTIPLE_VAL_DELIM);
                }
                parmbuf.append(format(parm));
                first = false;
            }
        }

        return parmbuf.toString();
    }

    /**
     * Format each parameter
     *
     * @param parm
     *            the parameter
     * @return the formatted event parameter string
     */
    public static String format(Parm parm) {
        Value pValue = parm.getValue();

        String type = pValue.getType();
        String encoding = pValue.getEncoding();

        String tmp = EventDatabaseConstants.escape(parm.getParmName(), EventDatabaseConstants.NAME_VAL_DELIM);
        String name = EventDatabaseConstants.escape(tmp, EventDatabaseConstants.MULTIPLE_VAL_DELIM);
        tmp = EventDatabaseConstants.escape(pValue.getContent(), EventDatabaseConstants.NAME_VAL_DELIM);
        String value = EventDatabaseConstants.escape(tmp, EventDatabaseConstants.MULTIPLE_VAL_DELIM);

        String empty = "";
        name = (name != null ? name.trim() : empty);
        value = (value != null ? value.trim() : empty);
        type = (type != null ? type.trim() : empty);
        encoding = (encoding != null ? encoding.trim() : empty);

        final StringBuilder buf = new StringBuilder();
        buf.append(name);
        buf.append(EventDatabaseConstants.NAME_VAL_DELIM);
        buf.append(value);
        buf.append('(');
        buf.append(type);
        buf.append(EventDatabaseConstants.DB_ATTRIB_DELIM);
        buf.append(encoding);
        buf.append(')');

        return buf.toString();
        // return name + EventDatabaseConstants.NAME_VAL_DELIM + value + "(" + type +
        // EventDatabaseConstants.DB_ATTRIB_DELIM + encoding + ")";
    }
    
    /**
     * <p>decode</p>
     *
     * @param eventparms an event parm string
     * @return a list of parameters
     */
    public static List<Parm> decode(final String eventparms) {
        if (eventparms == null ) return null;
        final List<Parm> parms = new ArrayList<>();
  
        String[] paramslistString = eventparms.split(Character.toString(EventDatabaseConstants.MULTIPLE_VAL_DELIM));
        if (paramslistString != null) {
                for (int i =0; i< paramslistString.length;i++) {
                    String[] paramEncoded = paramslistString[i].split(Character.toString(EventDatabaseConstants.NAME_VAL_DELIM));
                    if (paramEncoded != null && paramEncoded.length == 2) {
                        Parm parm = new Parm();
                        parm.setParmName(paramEncoded[0]);
                        Value value = new Value();
                        int startParamType = paramEncoded[1].lastIndexOf('(');
                        if (startParamType == -1 ) {
                            value.setContent(paramEncoded[1]);
                            value.setType("string");
                            value.setEncoding("text");
                        } else {
                            value.setContent(paramEncoded[1].substring(0,startParamType));
                            String paramType=paramEncoded[1].substring(startParamType+1);
                            String[] typeAndEncode = paramType.split(Character.toString(EventDatabaseConstants.DB_ATTRIB_DELIM));
                            if (typeAndEncode != null && typeAndEncode.length == 2) {
                                value.setType(typeAndEncode[0]);
                                value.setEncoding(typeAndEncode[1].split("\\)")[0]);
                            } else {
                                value.setType("string");
                                value.setEncoding("text");
                            }
                        }
                        parm.setValue(value);
                        parms.add(parm);
                    }
                }
        }
        return parms;
    }

    public static Map<String, Parm> normalize(final List<Parm> parmList) {
        return parmList.stream().collect(Collectors.toMap(Parm::getParmName, Function.identity(), (p1, p2) -> p2));
    }
}
