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
package org.opennms.netmgt.model;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class SnmpInterfaceIdAdapter extends XmlAdapter<ArrayList<Integer>, Set<OnmsIpInterface>> {

    @Override
    public ArrayList<Integer> marshal(final Set<OnmsIpInterface> ifaces) throws Exception {
        final ArrayList<Integer> ret = new ArrayList<>();
        for (final OnmsIpInterface iface : ifaces) {
            ret.add(iface.getId());
        }
        return ret;
    }

    @Override
    public Set<OnmsIpInterface> unmarshal(final ArrayList<Integer> ids) throws Exception {
        final Set<OnmsIpInterface> ret = new TreeSet<>();
        for (final Integer id : ids) {
            final OnmsIpInterface iface = new OnmsIpInterface();
            iface.setId(id);
            ret.add(iface);
        }
        return ret;
    }

}
