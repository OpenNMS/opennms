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
package org.opennms.vaadin.extender.internal.servlet;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.opennms.vaadin.extender.ApplicationFactory;

import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.server.UIProviderEvent;
import com.vaadin.ui.UI;

/**
 * This class is responsible to dispatch any Vaadin UI creation (exposed via {@link ApplicationFactory}) accordingly.
 * This was required as a {@link UIProvider} should take care of all ui creation, and not just one, which was the case
 * originally, but broke with the Karaf 4.2.2 Upgrade.
 *
 * @author mvrueden
 */
public class OSGiUIProvider extends UIProvider {

    // cannonical class name -> application factory
    private Map<String, ApplicationFactory> factories = new HashMap<>();

    public OSGiUIProvider() {

    }

    public OSGiUIProvider(ApplicationFactory applicationFactory) {
        addApplicationFactory(applicationFactory);
    }

    @Override
    public synchronized Class<? extends UI> getUIClass(UIClassSelectionEvent event) {
        final String uiClassName = extractUIClassName(event);
        if (uiClassName != null) {
            final ApplicationFactory applicationFactory = factories.get(uiClassName);
            if (applicationFactory != null) {
                return applicationFactory.getUIClass();
            }
        }
        return null;
    }
    
    @Override
    public synchronized UI createInstance(final UICreateEvent event) {
        final String uiClassName = extractUIClassName(event);
        if (uiClassName != null) {
            return factories.get(uiClassName).createUI();
        }
        return null;
    }

    public synchronized void addApplicationFactory(ApplicationFactory applicationFactory) {
        Objects.requireNonNull(applicationFactory);
        Objects.requireNonNull(applicationFactory.getUIClass());
        final Class<? extends UI> uiClass = applicationFactory.getUIClass();
        factories.put(uiClass.getCanonicalName(), applicationFactory);
    }

    public synchronized void removeApplicationFactory(ApplicationFactory applicationFactory) {
        Objects.requireNonNull(applicationFactory);
        Objects.requireNonNull(applicationFactory.getUIClass());
        factories.remove(applicationFactory.getUIClass().getCanonicalName());
    }

    private String extractUIClassName(UIProviderEvent event) {
        return event.getService().getDeploymentConfiguration().getInitParameters().getProperty("ui.class");
    }
}
