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
package org.opennms.netmgt.rrd.model;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * The Class LongAdapter.
 * <p>The null representation of some long values inside the XML version of an RRD is expressed as 'U'</p>
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class LongAdapter extends XmlAdapter<String, Long> {

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(Long value) throws Exception {
        if (value == null) {
            return null;
        }
        if (value == Long.MIN_VALUE) {
            return "-inf";
        }
        if (value == Long.MAX_VALUE) {
            return "inf";
        }
        return value.toString();
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public Long unmarshal(String value) throws Exception {
        final String v = value.trim();
        if (v.equalsIgnoreCase("u")) {
            return null;
        }
        if (v.equalsIgnoreCase("-inf")) {
            return Long.MIN_VALUE;
        }
        if (v.equalsIgnoreCase("inf")) {
            return Long.MAX_VALUE;
        }
        return new Long(v);
    }

}
