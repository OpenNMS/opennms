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
package org.opennms.features.topology.api;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.LoggerFactory;

import com.vaadin.server.Page;
import com.vaadin.server.VaadinServlet;

public abstract class AbstractOperation implements Operation {

    @Override
    public boolean display(final List<VertexRef> targets, final OperationContext operationContext) {
        return true;
    }

    @Override
    public boolean enabled(final List<VertexRef> targets, final OperationContext operationContext) {
        if (targets != null && targets.size() == 1) {
            for (final VertexRef target : targets) {
                final Integer nodeValue = getNodeIdValue(operationContext, target);
                if (nodeValue != null && nodeValue > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getId() {
        return getClass().getSimpleName();
    }

    protected static String getLabelValue(final OperationContext operationContext, final VertexRef target) {
        Vertex vertex = getVertexItem(operationContext, target);
        return vertex == null ? null : vertex.getLabel();
    }

    protected static String getIpAddrValue(final OperationContext operationContext, final VertexRef target) {
        Vertex vertex = getVertexItem(operationContext, target);
        return vertex == null ? null : vertex.getIpAddress();
    }

    protected static Integer getNodeIdValue(final OperationContext operationContext, final VertexRef target) {
        Vertex vertex = getVertexItem(operationContext, target);
        return vertex == null ? null : vertex.getNodeID();
    }

    protected static Vertex getVertexItem(final OperationContext operationContext, final VertexRef target) {
        Vertex vertex = operationContext.getGraphContainer().getTopologyServiceClient().getVertex(target, operationContext.getGraphContainer().getCriteria());
        if (vertex == null) {
            LoggerFactory.getLogger(AbstractOperation.class).debug("Null vertex found for vertex reference: {}:{}", target.getNamespace(), target.getId());
            return null;
        } else {
            return vertex;
        }
    }
    
    protected String getFullUrl(final String urlFragment) {
        final URI currentLocation = Page.getCurrent().getLocation();
        final String contextRoot = VaadinServlet.getCurrent().getServletContext().getContextPath();
        try {
            return new URL(currentLocation.toURL(), contextRoot + "/" + urlFragment).toString();
        } catch (final MalformedURLException e) {
            throw new RuntimeException("Failed to create full URL from current location: " + currentLocation + ", context root: " + contextRoot + ", url: " + urlFragment);
        }
    }
}
