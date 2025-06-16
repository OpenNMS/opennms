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
package org.opennms.netmgt.jetty;

import java.util.ArrayList;
import java.util.List;

import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.providers.WebAppProvider;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.webapp.WebAppContext;

@ManagedObject("Provider for start-up deployement of webapps based on presence in directory")
public class OpenNMSWebAppProvider extends WebAppProvider {

    @Override
    public ContextHandler createContextHandler(final App app) throws Exception {
        final ContextHandler handler = super.createContextHandler(app);

        /*
         * Add an alias check that accepts double slashes in our resource paths.
         */
        handler.addAliasCheck(new ApproveAbsolutePathAliases());

        /*
         * Pulled from: http://bengreen.eu/fancyhtml/quickreference/jettyjsp9error.html
         *
         * Configure the application to support the compilation of JSP files.
         * We need a new class loader and some stuff so that Jetty can call the
         * onStartup() methods as required.
         */
        if (handler instanceof WebAppContext) {
            WebAppContext context = (WebAppContext)handler;
            context.setAttribute(AnnotationConfiguration.CONTAINER_INITIALIZERS, jspInitializers());
            context.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
            context.addBean(new ServletContainerInitializersStarter(context), true);
            context.setThrowUnavailableOnStartupException(true);
        }

        return handler;
    }

    private static List<ContainerInitializer> jspInitializers() {
        final List<ContainerInitializer> initializers = new ArrayList<>();
        JettyJasperInitializer sci = new JettyJasperInitializer();
        ContainerInitializer initializer = new ContainerInitializer(sci, null);
        initializers.add(initializer);
        return initializers;
    }
}
