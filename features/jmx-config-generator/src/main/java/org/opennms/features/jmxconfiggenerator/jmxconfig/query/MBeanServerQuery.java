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

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Enables the user to make queries against a given {@link MBeanServerConnection}.
 */
public class MBeanServerQuery {

    private List<FilterCriteria> ignoreFilterList = new ArrayList<>();
    private List<FilterCriteria> filterCriteriaList = new ArrayList<>();
    private boolean sort;
    private boolean fetchValues;
    private boolean showEmptyMbeans;

    public MBeanServerQuery withFilters(Collection<String> filterList) {
        if (filterList != null) {
            for (String eachFilter : filterList) {
                filterCriteriaList.add(FilterCriteria.parse(eachFilter));
            }
        }
        return this;
    }

    public MBeanServerQuery sort(boolean sortingFlag) {
        sort = sortingFlag;
        return this;
    }

    public MBeanServerQuery fetchValues(boolean fetchValuesFlag) {
        this.fetchValues = fetchValuesFlag;
        return this;
    }

    public MBeanServerQuery showMBeansWithoutAttributes(boolean all) {
        showEmptyMbeans = all;
        return this;
    }

    public MBeanServerQuery withIgnoresFilter(Collection<String> ignoreFilter) {
        if (ignoreFilter != null) {
            for (String eachFilter : ignoreFilter) {
                ignoreFilterList.add(FilterCriteria.parse(eachFilter));
            }
        }
        return this;
    }

    public QueryResult execute(MBeanServerConnection mbeanServerConnection) throws MBeanServerQueryException {
        try {
            if (filterCriteriaList.isEmpty()) {
                filterCriteriaList.add(new FilterCriteria());
            }

            // retrieve all Mbeans
            QueryResult queryResult = executeQuery(filterCriteriaList, mbeanServerConnection);
            queryResult.setTotalMBeanCount(mbeanServerConnection.getMBeanCount());

            // retrieve all ignoring Mbeans
            QueryResult ignoreResult = executeQuery(ignoreFilterList, mbeanServerConnection);

            // filter out all ignored attributes
            for (QueryResult.MBeanResult eachResult : ignoreResult.getMBeanResults()) {
                for (MBeanAttributeInfo eachAttribute : eachResult.attributeResult.attributes) {
                    queryResult.remove(eachResult.objectName, eachAttribute);
                }
            }

            // now build the result
            if (!showEmptyMbeans) {
                queryResult.removeEmptyMBeanResults();
            }
            if (sort) {
                queryResult.sort();
            }

            if (fetchValues) {
                for (QueryResult.MBeanResult eachMbeanResult : queryResult.getMBeanResults()) {
                    QueryResult.AttributeResult attributeResult = eachMbeanResult.attributeResult;
                    for (MBeanAttributeInfo eachAttribute : attributeResult.attributes) {
                        if (eachAttribute.isReadable()) {
                            try {
                                Object value = mbeanServerConnection.getAttribute(eachMbeanResult.objectName, eachAttribute.getName());
                                attributeResult.setValue(eachAttribute, value);
                            } catch (Exception uoe) {
                                // while receiving the value an exception could occur, we mark
                                // the values as such.
                                attributeResult.setValue(eachAttribute, "ERROR: " + uoe.getMessage());
                            }
                        }
                    }
                }
            }
            return queryResult;
        } catch (IOException  | JMException e) {
            throw new MBeanServerQueryException(e);
        }
    }

    private static QueryResult executeQuery(List<FilterCriteria> filterCriteriaList, MBeanServerConnection mbeanServerConnection) throws IOException, JMException {
        QueryResult result = new QueryResult();
        for (FilterCriteria eachFilterCriteria : filterCriteriaList) {
            ObjectName query = eachFilterCriteria.objectName != null ? new ObjectName(eachFilterCriteria.objectName) : null;
            Set<ObjectName> tmpObjectNames = mbeanServerConnection.queryNames(query, null);

            // Filter out all Attributes
            // Now we filter the MBeans attributes (Default: show all)
            for (ObjectName eachObjectName : tmpObjectNames) {
                MBeanInfo mbeanInfo = mbeanServerConnection.getMBeanInfo(eachObjectName);
                result.put(eachObjectName, mbeanInfo);
                result.setAttributeTotalCount(eachObjectName, mbeanInfo.getAttributes().length);
                for (MBeanAttributeInfo eachAttribute : mbeanInfo.getAttributes()) {
                    if (eachFilterCriteria.matches(eachAttribute)) {
                        result.put(eachObjectName, eachAttribute);
                    }
                }
            }
        }
        return result;
    }
}
