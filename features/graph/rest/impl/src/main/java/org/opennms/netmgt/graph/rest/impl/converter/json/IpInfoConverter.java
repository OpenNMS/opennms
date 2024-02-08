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
package org.opennms.netmgt.graph.rest.impl.converter.json;

import org.json.JSONObject;
import org.opennms.netmgt.graph.api.info.IpInfo;
import org.opennms.netmgt.graph.rest.api.PropertyConverter;

public class IpInfoConverter implements PropertyConverter<IpInfo, JSONObject> {
    @Override
    public boolean canConvert(Class<IpInfo> type) {
        return IpInfo.class.isAssignableFrom(type);
    }

    @Override
    public JSONObject convert(IpInfo input) {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("address", input.getIpAddress());
        jsonObject.put("managed", input.isManaged());
        jsonObject.put("primary", input.isPrimary());
        return jsonObject;
    }
}
