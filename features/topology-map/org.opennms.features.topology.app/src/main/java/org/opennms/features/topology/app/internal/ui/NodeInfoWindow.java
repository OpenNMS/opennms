/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
