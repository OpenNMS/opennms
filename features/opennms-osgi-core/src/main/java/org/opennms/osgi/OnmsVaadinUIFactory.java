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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.opennms.vaadin.extender.ApplicationFactory;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.blueprint.reflect.BeanMetadata;
import org.osgi.service.blueprint.reflect.ComponentMetadata;

import com.vaadin.ui.UI;

/**
 * Creates an instance of a Vaadin UI object from the underlying OSGI container.
 * It is necessary that a bean-configuration with a id which matches the {@link #m_uiBeanId} exists inside the blueprint.xml represented by {@link #m_blueprintContainer}.
 *
 * @author Markus von Rüden
 */
public class OnmsVaadinUIFactory implements ApplicationFactory {

    private final BlueprintContainer m_blueprintContainer;
    private final String m_uiBeanId;
    private final Class<? extends UI> m_uiClass;

    public OnmsVaadinUIFactory(Class<? extends UI> uiClass, BlueprintContainer blueprintContainer, String uiBeanId) {
        m_blueprintContainer = Objects.requireNonNull(blueprintContainer);
        m_uiClass = Objects.requireNonNull(uiClass);
        m_uiBeanId = Objects.requireNonNull(uiBeanId);
        validate();
    }

    @Override
    public Map<String, String> getAdditionalHeaders() {
        return Collections.emptyMap();
    }

    @Override
    public UI createUI() {
        UI ui = (UI) m_blueprintContainer.getComponentInstance(m_uiBeanId);
        return ui;
    }

    @Override
    public Class<? extends UI> getUIClass() {
        return m_uiClass;
    }

    /**
     * Verify that the current UIFactory is set up correctly.
     */
    private void validate() {
        // Verify that the uiBean is a subclass of UI
        final Object instance = m_blueprintContainer.getComponentInstance(m_uiBeanId);
        if (!(instance instanceof UI)) {
            throw new IllegalStateException("The bean with id " + m_uiBeanId + " must be of type " + com.vaadin.ui.UI.class);
        }

        // Verify that the scope is prototype and NOT singleton
        final ComponentMetadata componentMetadata = Objects.requireNonNull(m_blueprintContainer.getComponentMetadata(m_uiBeanId));
        if (!(componentMetadata instanceof BeanMetadata)) {
            throw new IllegalStateException("The referenced id is not a bean");
        }

        if (!BeanMetadata.SCOPE_PROTOTYPE.equals(((BeanMetadata) componentMetadata).getScope())) {
            throw new IllegalStateException("The scope of the defined bean with id " + m_uiBeanId + " must be " + BeanMetadata.SCOPE_PROTOTYPE + " but is " + BeanMetadata.SCOPE_SINGLETON);
        }
    }
}
