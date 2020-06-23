/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.datacollection;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataCollectionGroups  implements Serializable {

    private Map<String, List<DatacollectionGroup>> dataCollectionGroupByName = new HashMap<>();

    public void addDataCollectionGroup(String snmpCollectionName, List<DatacollectionGroup> datacollectionGroups) {
        if(dataCollectionGroupByName.containsKey(snmpCollectionName)) {
            dataCollectionGroupByName.get(snmpCollectionName).addAll(datacollectionGroups);
        } else {
            dataCollectionGroupByName.put(snmpCollectionName, datacollectionGroups);
        }
    }

    public Set<String> getSnmpCollectionNames() {
          return dataCollectionGroupByName.keySet();
    }

    public List<DatacollectionGroup> getDataCollectionGroup(String snmpCollectionName) {
        return dataCollectionGroupByName.get(snmpCollectionName);
    }

    public Map<String, List<DatacollectionGroup>> getDataCollectionGroupByName() {
        return dataCollectionGroupByName;
    }
}
