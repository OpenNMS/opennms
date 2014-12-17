/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.osgi;

import com.vaadin.ui.UI;
import org.opennms.vaadin.extender.ApplicationFactory;
import org.osgi.service.blueprint.container.BlueprintContainer;

import java.util.Collections;
import java.util.Map;

/**
 * Creates an instance of a Vaadin UI object from the underlying OSGI container.
 * It is necessary that a bean-configuration of {@link #m_uiBeanName} exists inside the blueprint.xml.
 *
 * @author Markus von Rüden
 */
public class OnmsVaadinUIFactory implements ApplicationFactory {

    private final BlueprintContainer m_blueprintContainer;
    private final String m_uiBeanName;
    private final Class<? extends UI> m_uiClass;

    public OnmsVaadinUIFactory(Class<? extends UI> uiClass, BlueprintContainer blueprintContainer, String uiBeanName) {
        m_blueprintContainer = blueprintContainer;
        m_uiClass = uiClass;
        m_uiBeanName = uiBeanName;
    }

    @Override
    public Map<String, String> getAdditionalHeaders() {
        return Collections.emptyMap();
    }

    @Override
    public UI createUI() {
        UI ui = (UI) m_blueprintContainer.getComponentInstance(m_uiBeanName);
        return ui;
    }

    @Override
    public Class<? extends UI> getUIClass() {
        return m_uiClass;
    }
}
