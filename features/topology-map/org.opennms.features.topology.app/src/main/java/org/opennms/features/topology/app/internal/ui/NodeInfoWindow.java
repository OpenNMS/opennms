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
package org.opennms.features.topology.app.internal.ui;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.opennms.features.topology.api.support.InfoWindow;

import com.google.common.base.Throwables;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;

public class NodeInfoWindow extends InfoWindow {
    public NodeInfoWindow(int nodeId) {
        super(getURL(nodeId), () -> "Node Info " + nodeId);
    }

    private static URL getURL(int nodeId) {
        final URI currentLocation = Page.getCurrent().getLocation();
        final String contextRoot = VaadinServlet.getCurrent().getServletContext().getContextPath();
        final String redirectFragment = contextRoot + "/element/node.jsp?node=" + nodeId;
        try {
            return new URL(currentLocation.toURL(), redirectFragment);
        } catch (MalformedURLException e) {
            throw Throwables.propagate(e);
        }
    }

    public void open() {
        if (UI.getCurrent() != null) {
            UI.getCurrent().addWindow(this);
        }
    }
}
