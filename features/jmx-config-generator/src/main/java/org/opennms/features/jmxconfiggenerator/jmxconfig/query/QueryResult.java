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
package org.opennms.features.jmxconfiggenerator.jmxconfig.query;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the result of a query when using the {@link MBeanServerQuery}.
 */
public class QueryResult {

    public static class MBeanResult {
        public ObjectName objectName;
        public MBeanInfo mbeanInfo;
        public AttributeResult attributeResult;
    }

    public static class AttributeResult {
        private Map<MBeanAttributeInfo, Object> valueMap = new HashMap<>();
        public int totalCount;
        public Collection<MBeanAttributeInfo> attributes = new ArrayList<>();
        public MBeanInfo mbeanInfo;

        public Object getValue(MBeanAttributeInfo eachAttribute) {
            return valueMap.get(eachAttribute);
        }

        public void setValue(MBeanAttributeInfo eachAttribute, Object value) {
            valueMap.put(eachAttribute, value);
        }
    }

    private Map<ObjectName, MBeanResult> mbeanResultMap = new HashMap<>();
    private int totalMBeanCount;
    private Comparator<MBeanResult> comparator;

    public void setAttributeTotalCount(ObjectName objectName, int attributeTotalCount) {
        mbeanResultMap.get(objectName).attributeResult.totalCount = attributeTotalCount;
    }

    public void put(ObjectName objectName, MBeanAttributeInfo attributeInfo) {
        AttributeResult attributeResult = mbeanResultMap.get(objectName).attributeResult;
        if (!attributeResult.attributes.contains(attributeInfo)) {
            attributeResult.attributes.add(attributeInfo);
        }
    }

    public void put(ObjectName objectName, MBeanInfo mbeanInfo) {
        AttributeResult attributeResult = new AttributeResult();
        attributeResult.mbeanInfo = mbeanInfo;

        MBeanResult mbeanResult = new MBeanResult();
        mbeanResult.objectName = objectName;
        mbeanResult.mbeanInfo = mbeanInfo;
        mbeanResult.attributeResult = attributeResult;

        mbeanResultMap.put(objectName, mbeanResult);
    }

    public int getTotalMBeanCount() {
        return totalMBeanCount;
    }

    public void setTotalMBeanCount(int totalCount) {
        this.totalMBeanCount = totalCount;
    }

    public List<MBeanResult> getMBeanResults() {
        List<MBeanResult> result = new ArrayList<>(mbeanResultMap.values());
        if (comparator != null) {
            Collections.sort(result, comparator);
        }
        return result;
    }

    public void removeEmptyMBeanResults() {
        List<ObjectName> markForDeletion = new ArrayList<>();
        for (Map.Entry<ObjectName, MBeanResult> eachEntry : mbeanResultMap.entrySet()) {
            if (eachEntry.getValue().attributeResult.attributes.isEmpty()) {
                markForDeletion.add(eachEntry.getKey());
            }
        }
        for (ObjectName eachObjectName : markForDeletion) {
            mbeanResultMap.remove(eachObjectName);
        }
    }

    public void remove(ObjectName objectName, MBeanAttributeInfo eachAttribute) {
        mbeanResultMap.get(objectName).attributeResult.attributes.remove(eachAttribute);
    }

    public void sort() {
        comparator = new Comparator<MBeanResult>() {

            @Override
            public int compare(QueryResult.MBeanResult o1, QueryResult.MBeanResult o2) {
                return o1.objectName.compareTo(o2.objectName);
            }
        };
    }
}
