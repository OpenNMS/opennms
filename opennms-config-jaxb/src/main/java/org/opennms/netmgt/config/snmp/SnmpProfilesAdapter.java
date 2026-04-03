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
package org.opennms.netmgt.config.snmp;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Suppresses marshalling of an empty {@code <profiles/>} element.
 * <p>
 * JAXB skips a field when its adapter's {@link #marshal} returns {@code null},
 * so returning {@code null} here prevents the element from appearing in the
 * output when there are no profiles.
 */
public class SnmpProfilesAdapter extends XmlAdapter<SnmpProfiles, SnmpProfiles> {

    @Override
    public SnmpProfiles marshal(final SnmpProfiles profiles) {
        if (profiles == null || profiles.getSnmpProfiles().isEmpty()) {
            return null;
        }
        return profiles;
    }

    @Override
    public SnmpProfiles unmarshal(final SnmpProfiles profiles) {
        return profiles != null ? profiles : new SnmpProfiles();
    }
}
