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
