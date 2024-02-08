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
package org.opennms.netmgt.dao.mock;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.filter.api.FilterParseException;

public abstract class UnimplementedFilterDao implements FilterDao {
    @Override
    public SortedMap<Integer, String> getNodeMap(String rule) throws FilterParseException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Map<InetAddress, Set<String>> getIPAddressServiceMap(String rule) throws FilterParseException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Map<Integer, Map<InetAddress, Set<String>>> getNodeIPAddressServiceMap(String rule) throws FilterParseException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void flushActiveIpAddressListCache() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<InetAddress> getActiveIPAddressList(String rule) throws FilterParseException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<InetAddress> getIPAddressList(String rule) throws FilterParseException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public boolean isValid(String addr, String rule) throws FilterParseException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public boolean isRuleMatching(String rule) throws FilterParseException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void validateRule(String rule) throws FilterParseException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }
}
