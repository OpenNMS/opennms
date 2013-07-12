/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.nodemaps.internal;

import org.ops4j.pax.vaadin.AbstractApplicationFactory;
import org.osgi.service.blueprint.container.BlueprintContainer;

import com.vaadin.ui.UI;

/**
 * A factory for creating NodeMapsApplication objects.
 * 
 * TODO: Refactor into a common class
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class NodeMapsApplicationFactory extends AbstractApplicationFactory {

	private final BlueprintContainer m_blueprintContainer;
	private final String m_beanName;
	
	public NodeMapsApplicationFactory(BlueprintContainer container, String beanName) {
		m_blueprintContainer = container;
		m_beanName = beanName;
	}

    @Override
    public Class<? extends UI> getUIClass() {
        return NodeMapsApplication.class;
    }

    @Override
    public UI getUI() {
        NodeMapsApplication application = (NodeMapsApplication) m_blueprintContainer.getComponentInstance(m_beanName);
        return application;
    }
}
