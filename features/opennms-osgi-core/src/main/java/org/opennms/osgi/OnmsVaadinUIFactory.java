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
