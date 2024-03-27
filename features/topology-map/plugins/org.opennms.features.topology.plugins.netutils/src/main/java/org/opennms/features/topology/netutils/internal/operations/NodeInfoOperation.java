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
package org.opennms.features.topology.netutils.internal.operations;

import java.net.URL;
import java.util.List;

import org.opennms.features.topology.api.AbstractOperation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.OperationContext.DisplayLocation;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.netutils.internal.Node;
import org.opennms.features.topology.netutils.internal.NodeInfoWindow;

public class NodeInfoOperation extends AbstractOperation {
    private String m_nodePageURL;
    private String m_nodeListURL;

    @Override
    public void execute(final List<VertexRef> targets, final OperationContext operationContext) {
        try {
            String label = "";
            int nodeID = -1;

            if (targets != null) {
                for (final VertexRef target : targets) {
                    final String labelValue = getLabelValue(operationContext, target);
                    final Integer nodeValue = getNodeIdValue(operationContext, target);

                    if (nodeValue != null && nodeValue > 0) {
                        label = labelValue == null? "" : labelValue;
                        nodeID = nodeValue.intValue();
                        break;
                    }
                }
            }

            final Node node = new Node(nodeID, null, label);

            final String url;
            if (node.getNodeID() >= 0) {
                url = getNodePageURL() + node.getNodeID();
            } else {
                url = getNodeListURL();
            }

            final URL fullUrl = new URL(getFullUrl(url));
            operationContext.getMainWindow().addWindow(new NodeInfoWindow(node, fullUrl));
        } catch (final Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            } else {
                throw new RuntimeException("Failed to create node window.", e);
            }
        }
    }

    @Override
    public boolean display(final List<VertexRef> targets, final OperationContext operationContext) {
        if (operationContext.getDisplayLocation() == DisplayLocation.MENUBAR) {
            return true;
        }
        else if(targets != null && targets.size() > 0 && targets.get(0) != null) {
            return true;
        }else {
            return false;
        }

    }

    @Override
    public String getId() {
        return "contextNodeInfo";
    }

    public String getNodePageURL() {
        return m_nodePageURL;
    }

    public void setNodePageURL(final String nodePageURL) {
        m_nodePageURL = nodePageURL;
    }

    public String getNodeListURL() {
        return m_nodeListURL;
    }

    public void setNodeListURL(final String nodeListURL) {
        m_nodeListURL = nodeListURL;
    }

}
