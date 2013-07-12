/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.app.internal;

import java.util.HashMap;
import java.util.Map;

import org.ops4j.pax.vaadin.AbstractApplicationFactory;
import org.osgi.service.blueprint.container.BlueprintContainer;

import com.vaadin.ui.UI;

public class TopologyWidgetTestApplicationFactory extends AbstractApplicationFactory {
    
	private final BlueprintContainer m_blueprintContainer;
	private final String m_beanName;
	
	public TopologyWidgetTestApplicationFactory(BlueprintContainer container, String beanName) {
		m_blueprintContainer = container;
		m_beanName = beanName;
	}

    @Override
    public Class<? extends UI> getUIClass() {
        return TopologyWidgetTestApplication.class;
    }

    @Override
    public Map<String, String> getAdditionalHeaders() {
        final Map<String,String> headers = new HashMap<String,String>();
        headers.put("X-UA-Compatible", "chrome=1");
        //headers.put("X-Frame-Options", "ALLOW-FROM http://cdn.leafletjs.com/");
        //headers.put("X-Frame-Options", "ALLOW-FROM http://maps.google.com/");
        return headers;
    }

    @Override
    public UI getUI() {
        TopologyWidgetTestApplication application = (TopologyWidgetTestApplication) m_blueprintContainer.getComponentInstance(m_beanName);
        return application;
    }

}
