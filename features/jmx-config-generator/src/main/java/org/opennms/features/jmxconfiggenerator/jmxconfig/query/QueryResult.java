/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
