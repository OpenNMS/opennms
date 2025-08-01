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
package org.opennms.netmgt.events.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    public static List<Parm> normalizePreserveOrder(final List<Parm> parmList) {
        Objects.requireNonNull(parmList);
        Set<String> existingNames = new HashSet<>();
        LinkedList<Parm> filteredList = new LinkedList<>();
        // go through the list backwards since we want to keep the last element of duplicates names
        // (existing behaviour)
        for(int i = parmList.size() -1 ; i >= 0; i--) {
            Parm parm = parmList.get(i);
            if(!existingNames.contains(parm.getParmName())){
                filteredList.addFirst(parm);
                existingNames.add(parm.getParmName());
            }
        }
        return filteredList;
    }
}
