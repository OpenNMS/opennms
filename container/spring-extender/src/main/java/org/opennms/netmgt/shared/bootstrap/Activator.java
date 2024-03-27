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
package org.opennms.netmgt.shared.bootstrap;

import org.eclipse.gemini.blueprint.extender.internal.activator.ContextLoaderListener;
import org.eclipse.gemini.blueprint.extender.internal.activator.NamespaceHandlerActivator;
import org.eclipse.gemini.blueprint.extender.internal.support.ExtenderConfiguration;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;


// Original the Activator simply loaded the ApplicationContext from the bundle.
// However it lacked the functionality to load the spring.handlers and other spring internals.
// Therefore we re-use the functionality from eclipse-blueprint to do so.
// See https://github.com/eclipse/gemini.blueprint/blob/6976b50e76a17aaf800a60f836c6b7af46fbf483/extender/src/main/java/org/eclipse/gemini/blueprint/extender/internal/boot/ChainActivator.java
public class Activator implements BundleActivator {

    private final BundleActivator[] activators;

    public Activator() {
        final NamespaceHandlerActivator activateCustomNamespaceHandling = new NamespaceHandlerActivator();
        final ExtenderConfiguration initializeExtenderConfiguration = new ExtenderConfiguration();
        final ContextLoaderListener listenForSpringDmBundles = new ContextLoaderListener(initializeExtenderConfiguration);

        activators = new BundleActivator[] {
                activateCustomNamespaceHandling,
                initializeExtenderConfiguration,
                listenForSpringDmBundles
        };
    }

    @Override
    public void start(BundleContext context) throws Exception {
        for (int i = 0; i < activators.length; i++) {
            activators[i].start(context);
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        for (int i = activators.length - 1; i >= 0; i--) {
            activators[i].stop(context);
        }
    }
}