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
package org.opennms.features.topology.plugins.topo.linkd.internal;

import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.simple.SimpleConnector;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;

public class LinkdEdge extends AbstractEdge implements Edge {

    public static LinkdEdge create(String id,
            LinkdPort sourceport, LinkdPort targetport,
            ProtocolSupported discoveredBy, String namespace) {
        
        SimpleConnector source = new SimpleConnector(namespace, sourceport.getVertex().getId()+"-"+id+"-connector", sourceport.getVertex());
        SimpleConnector target = new SimpleConnector(namespace, targetport.getVertex().getId()+"-"+id+"-connector", targetport.getVertex());

        return new LinkdEdge(id, sourceport, targetport, source, target, discoveredBy, namespace);
    }
    
    private final LinkdPort m_sourcePort;
    private final LinkdPort m_targetPort;
    private final ProtocolSupported m_discoveredBy;

    public LinkdEdge(String id, LinkdPort sourcePort, LinkdPort targetPort, SimpleConnector source,
            SimpleConnector target, ProtocolSupported discoveredBy, String namespace) {
        super(namespace, id, source, target);
        m_sourcePort = sourcePort;
        m_targetPort = targetPort;
        m_discoveredBy = discoveredBy;
    }

    // Constructor to make cloneable easier for sub classes
    private LinkdEdge(LinkdEdge edgeToClone) {
            this(edgeToClone.getId(), 
                 edgeToClone.getSourcePort().clone(), 
                 edgeToClone.getTargetPort().clone(),
                 edgeToClone.getSource().clone(),
                 edgeToClone.getTarget().clone(),
                 edgeToClone.getDiscoveredBy(), edgeToClone.getNamespace());

            setLabel(edgeToClone.getLabel());
            setStyleName(edgeToClone.getStyleName());
            setTooltipText(edgeToClone.getTooltipText());
    }

    @Override
    public LinkdEdge clone() {
            return new LinkdEdge(this);
    }

    @Override
    public String  getTooltipText() {       
        final StringBuilder tooltipText = new StringBuilder();
        tooltipText.append("<p>");
        tooltipText.append("discovery by: ");
        tooltipText.append(m_discoveredBy.toString());
        tooltipText.append("</p>");
    
        tooltipText.append("<p>");
        tooltipText.append(m_sourcePort.getToolTipText());
        tooltipText.append("</p>");
        
        tooltipText.append("<p>");
        tooltipText.append(m_targetPort.getToolTipText());
        tooltipText.append("</p>");
        return tooltipText.toString();
    }

    
    public ProtocolSupported getDiscoveredBy() {
        return m_discoveredBy;
    }

    public LinkdPort getSourcePort() {
        return m_sourcePort;
    }

    public LinkdPort getTargetPort() {
        return m_targetPort;
    }
    
}
