/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc. OpenNMS(R) is Copyright (C)
 * 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 *
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 * *****************************************************************************
 */
package org.opennms.tools.jmxconfiggenerator.graphs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.JAXB;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.opennms.xmlns.xsd.config.jmx_datacollection.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simon Walter <simon.walter@hp-factory.de>
 * @author Markus Neumann <markus@opennms.com>
 */
public class JmxToSnmpGraphConfigGenerator {

    private static Logger logger = LoggerFactory.getLogger(JmxToSnmpGraphConfigGenerator.class);
    private static final String ATTRIBUTEREPORT = "AttributeReport";
    private static final String MBEANREPORT = "MBeanReport";
    private static final String COMPOSITEREPORT = "CompositeReport";
    private static final String COMPOSITATTRIBEREPORT = "CompositeAttributeReport";
    private static List<String> tangoColors = new ArrayList<String>();
    private static int colorIndex = 0;

    static {
        tangoColors.add("c4a000");
        tangoColors.add("edd400");
        tangoColors.add("fce94f");

        tangoColors.add("ce5c00");
        tangoColors.add("f57900");
        tangoColors.add("fcaf3e");

        tangoColors.add("8f5902");
        tangoColors.add("c17d11");
        tangoColors.add("e9b96e");

        tangoColors.add("4e9a06");
        tangoColors.add("73d216");
        tangoColors.add("8ae234");

        tangoColors.add("204a87");
        tangoColors.add("3465a4");
        tangoColors.add("729fcf");

        tangoColors.add("5c3566");
        tangoColors.add("75507b");
        tangoColors.add("ad7fa8");

        tangoColors.add("a40000");
        tangoColors.add("cc0000");
        tangoColors.add("ef2929");

        tangoColors.add("babdb6");
        tangoColors.add("d3d7cf");
        tangoColors.add("eeeeec");

        tangoColors.add("2e3436");
        tangoColors.add("555753");
        tangoColors.add("888a85");
    }

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

    public String generateSnmpGraph(Collection<Report> reports) throws IOException, Exception {
        String jarInternTemplate = "graphTemplate.vm";
        
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());

        velocityEngine.init();

        final String templatePath = jarInternTemplate;
        InputStream input = this.getClass().getClassLoader().getResourceAsStream(templatePath);
        if (input == null) {
            throw new IOException("Template file doesn't exist " + jarInternTemplate);
        }

        VelocityContext context = new VelocityContext();

        context.put("reportsList", reports.iterator());
        context.put("reportsBody", reports.iterator());

        Template template = null;

        try {
            template = velocityEngine.getTemplate(templatePath, "UTF-8");
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

    public Collection<Report> generateReportsByJmxDatacollectionConfig(String jmxDatacollectionConfig) {
        Collection<Report> reports = new ArrayList<Report>();
        JmxDatacollectionConfig inputConfig = JAXB.unmarshal(new File(jmxDatacollectionConfig), JmxDatacollectionConfig.class);
        
        for (JmxCollection jmxCollection : inputConfig.getJmxCollection()) {
            logger.debug("jmxCollection: '{}'", jmxCollection.getName());
            Mbeans mbeans = jmxCollection.getMbeans();

            for (Mbean mbean : mbeans.getMbean()) {
                reports.addAll(generateMbeanReportsByMBean(mbean));
                reports.addAll(generateAttributeReporsByMBean(mbean));

                reports.addAll(generateCompositeReportsByMBean(mbean));
                reports.addAll(generateCompositeMemberReportsByMBean(mbean));
            }
        }
        return reports;
    }

    private Collection<Report> generateAttributeReporsByMBean(Mbean mbean) {
        Collection<Report> reports = new ArrayList<Report>();
        for (Attrib attrib : mbean.getAttrib()) {
            Report report = new Report(mbean.getName() + "." + attrib.getAlias() + "." + ATTRIBUTEREPORT, attrib.getName(), attrib.getName(), "verticalLabel");
            report.addGraph(new Graph(attrib.getAlias(), attrib.getName(), attrib.getAlias(), getNextColor(), getNextColor(), getNextColor()));
            reports.add(report);
            restetColor();
        }
        return reports;
    }

    private Collection<Report> generateMbeanReportsByMBean(Mbean mbean) {
        Collection<Report> reports = new ArrayList<Report>();
        if (!mbean.getAttrib().isEmpty()) {
            Report report = new Report(mbean.getName() + "." + MBEANREPORT, mbean.getName(), mbean.getName(), "verticalLabel");
            for (Attrib attrib : mbean.getAttrib()) {
                report.addGraph(new Graph(attrib.getAlias(), attrib.getName(), attrib.getAlias(), getNextColor(), getNextColor(), getNextColor()));
            }
            reports.add(report);
            restetColor();
        }
        return reports;
    }

    private Collection<Report> generateCompositeReportsByMBean(Mbean mbean) {
        Collection<Report> reports = new ArrayList<Report>();

        for (CompAttrib compAttrib : mbean.getCompAttrib()) {
            Report report = new Report(mbean.getName() + "." + compAttrib.getName() + "." + COMPOSITEREPORT, compAttrib.getName(), compAttrib.getName(), "verticalLabel");
            for (CompMember compMember : compAttrib.getCompMember()) {
                report.addGraph(new Graph(compMember.getAlias(), compMember.getName(), compMember.getAlias(), getNextColor(), getNextColor(), getNextColor()));
            }
            reports.add(report);
            restetColor();
        }
        return reports;
    }

    private Collection<Report> generateCompositeMemberReportsByMBean(Mbean mbean) {
        Collection<Report> reports = new ArrayList<Report>();

        for (CompAttrib compAttrib : mbean.getCompAttrib()) {
            for (CompMember compMember : compAttrib.getCompMember()) {
                Report report = new Report(mbean.getName() + "." + compAttrib.getName() + "." + compMember.getName() + "." + COMPOSITATTRIBEREPORT, "name", "title", "verticalLabel");
                report.addGraph(new Graph(compMember.getAlias(), compMember.getName(), compMember.getAlias(), getNextColor(), getNextColor(), getNextColor()));
                reports.add(report);
                restetColor();
            }
        }
        return reports;
    }

    private static String getNextColor() {
        String color = tangoColors.get(colorIndex);
        colorIndex = (colorIndex + 1) % tangoColors.size();
        return color;
    }

    private static void restetColor() {
        colorIndex = 0;
    }
}
