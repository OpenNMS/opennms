/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.surveillanceviews.service;

import org.opennms.features.vaadin.surveillanceviews.model.Category;
import org.opennms.features.vaadin.surveillanceviews.model.View;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.SurveillanceStatus;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for the surveillance view service.
 */
public interface SurveillanceViewService {
    List<OnmsCategory> getOnmsCategories();

    SurveillanceStatus[][] calculateCellStatus(final View view);

    Set<OnmsCategory> getOnmsCategoriesFromViewCategories(final Collection<Category> viewCats);

    List<OnmsAlarm> getAlarmsForCategories(Set<OnmsCategory> rowCategories, Set<OnmsCategory> colCategories);

    List<OnmsNotification> getNotificationsForCategories(Set<OnmsCategory> rowCategories, Set<OnmsCategory> colCategories, Map<OnmsNotification, String> customSeverity);

    List<OnmsNode> getNodesForCategories(Set<OnmsCategory> rowCategories, Set<OnmsCategory> colCategories);

    List<NodeRtc> getNoteRtcsForCategories(Set<OnmsCategory> rowCategories, Set<OnmsCategory> colCategories);

    Map<OnmsResourceType, List<OnmsResource>> getResourceTypeMapForNodeId(final String nodeId);

    Map<String, String> getGraphResultsForResourceId(final String resourceId);

    Map<OnmsResourceType, List<OnmsResource>> getResourceTypeMapForNodeId(int nodeId);

    String imageUrlForGraph(String query, int width, int height);

    View selectDefaultViewForUsername(String username);

    class NodeRtc {
        private static final DecimalFormat AVAILABILITY_FORMAT = new DecimalFormat("0.000%");

        static {
            AVAILABILITY_FORMAT.setMultiplier(100);
        }

        private OnmsNode m_node;
        private int m_serviceCount;
        private int m_downServiceCount;
        private double m_availability;

        public NodeRtc(OnmsNode node, int serviceCount, int downServiceCount, double availability) {
            m_node = node;
            m_serviceCount = serviceCount;
            m_downServiceCount = downServiceCount;
            m_availability = availability;
        }

        public double getAvailability() {
            return m_availability;
        }

        public String getAvailabilityAsString() {
            return AVAILABILITY_FORMAT.format(m_availability);
        }

        public int getDownServiceCount() {
            return m_downServiceCount;
        }

        public OnmsNode getNode() {
            return m_node;
        }

        public int getServiceCount() {
            return m_serviceCount;
        }

        @Override
        public String toString() {
            return m_node.getLabel() + ": " + m_downServiceCount + " of " + m_serviceCount + ": " + getAvailabilityAsString();
        }
    }
}
