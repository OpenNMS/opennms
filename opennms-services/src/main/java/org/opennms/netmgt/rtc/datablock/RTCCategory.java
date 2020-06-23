/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.rtc.datablock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.opennms.netmgt.config.categories.Category;

/**
 * This class is used to encapsulate a category in the categories XML file.
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
public class RTCCategory extends Category {

    private static final long serialVersionUID = -3599148594628072528L;

    /**
     * The 'effective' rule
     */
    private final String m_effectiveRule;

    /**
     * The nodes list - list of node IDs
     */
    private final List<Integer> m_nodes = Collections.synchronizedList(new ArrayList<Integer>());

    /**
     * The default constructor - initializes the values
     *
     * @param cat a {@link org.opennms.netmgt.config.categories.Category} object.
     * @param commonRule a {@link java.lang.String} object.
     */
    public RTCCategory(Category cat, String commonRule) {
        setLabel(cat.getLabel());
        setComment(cat.getComment().orElse(null));
        setRule(cat.getRule());
        setNormalThreshold(cat.getNormalThreshold());
        setWarningThreshold(cat.getWarningThreshold());
        setServices(cat.getServices());

        m_effectiveRule = "(" + commonRule + ") & (" + cat.getRule() + ")";
    }

    /**
     * Add to the nodes in this category
     *
     * @param node
     *            the node to add
     */
    public void addNode(RTCNode node) {
        Integer longnodeid = node.getNodeID();

        if (!m_nodes.contains(longnodeid))
            m_nodes.add(longnodeid);
    }

    /**
     * Add to the nodes in this category
     *
     * @param nodeid
     *            the node ID to add
     */
    public void addNode(int nodeid) {
        if (!m_nodes.contains(nodeid)) {
            m_nodes.add(nodeid);
        }
    }

    /**
     * Delete from the nodes in this category
     *
     * @param nodeid
     *            the node ID to delete
     */
    public void deleteNode(int nodeid) {
        m_nodes.remove(Integer.valueOf(nodeid));
    }

    /**
     * Delete all nodes in this category
     */
    public void clearNodes() {
        m_nodes.clear();
    }

    /**
     * Delete all nodes in this category
     */
    public void addAllNodes(Collection<Integer> nodes) {
        m_nodes.addAll(nodes);
    }

    /**
     * Returns true if the service is in the services list in this category or
     * if service list is null
     *
     * @return true if the service is in the services list in this category or
     *         if service list is null
     * @param svcname a {@link java.lang.String} object.
     */
    public boolean containsService(String svcname) {
        final List<String> services = getServices();

        // if there are no services listed, include all services
        if (services.size() == 0) {
            return true;
        }
        return services.contains(svcname);
    }

    /**
     * Return the 'effective' category rule
     *
     * @return the 'effective' category rule
     */
    public String getEffectiveRule() {
        return m_effectiveRule;
    }

    /**
     * Get the node IDs in this category
     *
     * @return the list of node IDs in this category
     */
    public List<Integer> getNodes() {
        return m_nodes;
    }
}
