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
package org.opennms.features.jmxconfiggenerator.graphs;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.opennms.features.jmxconfiggenerator.log.LogAdapter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Collection;

/**
 * @author Simon Walter <simon.walter@hp-factory.de>
 * @author Markus Neumann <markus@opennms.com>
 */
public class GraphConfigGenerator {

    protected static final String INTERN_TEMPLATE_NAME = "graphTemplate.vm";

    private final LogAdapter logger;

    public GraphConfigGenerator(LogAdapter logger) {
        this.logger = logger;
    }

    public String generateSnmpGraph(Collection<Report> reports) {
        return generateSnmpGraph(reports, null);
    }

    public String generateSnmpGraph(Collection<Report> reports, String graphTemplate) {
        if (graphTemplate != null) {
            return generateSnmpGraphInternal(reports, graphTemplate);
        }
        return generateSnmpGraphInternal(reports);
    }

    private String generateSnmpGraphInternal(Collection<Report> reports, String graphTemplate) {
        Velocity.init();

        VelocityContext context = new VelocityContext();
        context.put("reportsList", reports.iterator());
        context.put("reportsBody", reports.iterator());

        Template template = Velocity.getTemplate(graphTemplate);
        StringWriter sw = new StringWriter();
        if (template != null) {
            template.merge(context, sw);
        }

        return sw.toString();
    }

    private String generateSnmpGraphInternal(Collection<Report> reports) {
        // init VelocityEngine
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();
        
        // create reader and writer for template extraction from jar
        InputStream templateInputStream = this.getClass().getClassLoader().getResourceAsStream(INTERN_TEMPLATE_NAME);
        StringWriter templateWriter = new StringWriter();
        Reader templateReader = new InputStreamReader(templateInputStream); 

        // create context
        VelocityContext context = new VelocityContext();
        context.put("reportsList", reports.iterator());
        context.put("reportsBody", reports.iterator());
        
        // get template
        Velocity.evaluate(context, templateWriter, INTERN_TEMPLATE_NAME, templateReader);
        return templateWriter.toString();
    }
}
