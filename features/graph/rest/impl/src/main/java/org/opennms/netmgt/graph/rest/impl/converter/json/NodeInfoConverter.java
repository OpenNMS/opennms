/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.rest.impl.converter.json;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.netmgt.graph.api.info.IpInfo;
import org.opennms.netmgt.graph.api.info.NodeInfo;
import org.opennms.netmgt.graph.rest.api.PropertyConverter;

public class NodeInfoConverter implements PropertyConverter<NodeInfo, JSONObject> {

    @Override
    public boolean canConvert(Class<NodeInfo> type) {
        return NodeInfo.class.isAssignableFrom(type);
    }

    @Override
    public JSONObject convert(NodeInfo input) {
        final JSONObject jsonNodeInfo = new JSONObject();
        jsonNodeInfo.put("id", input.getId());
        jsonNodeInfo.put("label", input.getLabel());
        jsonNodeInfo.put("location", input.getLocation());
        jsonNodeInfo.put("foreignId", input.getForeignId());
        jsonNodeInfo.put("foreignSource", input.getForeignSource());
        jsonNodeInfo.put("categories", new JSONArray(input.getCategories()));
        jsonNodeInfo.put("ipInterfaces", new JSONArray());

        for (IpInfo eachInterface : input.getIpInterfaces()) {
            final JSONObject jsonInterfaceInfo = new IpInfoConverter().convert(eachInterface);
            jsonNodeInfo.getJSONArray("ipInterfaces").put(jsonInterfaceInfo);
        }
        return jsonNodeInfo;
    }
}
