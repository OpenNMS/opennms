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

package org.opennms.features.jmxconfiggenerator.graphs;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Collection;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simon Walter <simon.walter@hp-factory.de>
 * @author Markus Neumann <markus@opennms.com>
 */
public class GraphConfigGenerator {

    private static Logger logger = LoggerFactory.getLogger(GraphConfigGenerator.class);

    public String generateSnmpGraph(Collection<Report> reports, String graphTemplate) {
        Velocity.init();
        VelocityContext context = new VelocityContext();

        context.put("reportsList", reports.iterator());
        context.put("reportsBody", reports.iterator());
        Template template = null;

        try {
            template = Velocity.getTemplate(graphTemplate);
        } catch (ResourceNotFoundException rnfe) {
            logger.debug("couldn't find the template:'{}'", rnfe.getMessage());
        } catch (ParseErrorException pee) {
            logger.debug("syntax error: problem parsing the template:'{}'", pee.getMessage());
        } catch (MethodInvocationException mie) {
            logger.debug("something invoked in the template threw an exception:'{}'", mie.getMessage());
        } catch (Exception e) {
            logger.debug("undefined exception:'{}'", e.getMessage());
        }

        StringWriter sw = new StringWriter();

        if (template != null) {
            template.merge(context, sw);
        }

        return sw.toString();
    }

    public String generateSnmpGraph(Collection<Report> reports) throws IOException {
        String jarInternTemplate = "graphTemplate.vm";

        // init VelocityEngine
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();

        // check if tmeplate exists in jar
        final String templatePath = jarInternTemplate;
        InputStream templateInputStream = this.getClass().getClassLoader().getResourceAsStream(templatePath);
        if (templateInputStream == null) {
            throw new IOException(String.format("Template file '%s' doesn't exist.", jarInternTemplate));
        }
        
        // create reader and writer for template extraction from jar
        StringWriter templateWriter = new StringWriter();
        Reader templateReader = new InputStreamReader(templateInputStream); 

        // create context
        VelocityContext context = new VelocityContext();
        context.put("reportsList", reports.iterator());
        context.put("reportsBody", reports.iterator());
        
        // get template
        Velocity.evaluate(context, templateWriter, jarInternTemplate, templateReader);
        return templateWriter.toString();
    }
}
