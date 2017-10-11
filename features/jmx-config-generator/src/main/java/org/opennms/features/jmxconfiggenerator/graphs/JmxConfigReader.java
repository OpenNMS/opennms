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

package org.opennms.features.jmxconfiggenerator.graphs;

import org.apache.commons.lang.StringUtils;
import org.opennms.features.jmxconfiggenerator.log.LogAdapter;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Attrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompAttrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompMember;
import org.opennms.xmlns.xsd.config.jmx_datacollection.JmxCollection;
import org.opennms.xmlns.xsd.config.jmx_datacollection.JmxDatacollectionConfig;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Mbean;

import javax.xml.bind.JAXB;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Simon Walter <simon.walter@hp-factory.de>
 * @author Markus Neumann <markus@opennms.com>
 */
public class JmxConfigReader {

    private static final String ATTRIBUTEREPORT = "AttributeReport";
    private static final String MBEANREPORT = "MBeanReport";
    private static final String COMPOSITEREPORT = "CompositeReport";
    private static final String COMPOSITATTRIBEREPORT = "CompositeAttributeReport";

    private final LogAdapter logger;

    public JmxConfigReader(LogAdapter logger) {
        this.logger = logger;
    }

    protected Collection<Report> generateReportsByJmxDatacollectionConfig(InputStream inputConfigStream) {
        return generateReportsByJmxDatacollectionConfig(
                JAXB.unmarshal(
                        inputConfigStream,
                        JmxDatacollectionConfig.class));
    }

    public Collection<Report> generateReportsByJmxDatacollectionConfig(String inputConfigFileName) {
        return generateReportsByJmxDatacollectionConfig(
                JAXB.unmarshal(
                    new File(inputConfigFileName), 
                    JmxDatacollectionConfig.class));
    }

    public Collection<Report> generateReportsByJmxDatacollectionConfig(JmxDatacollectionConfig inputConfig) {
        Collection<Report> reports = new ArrayList<>();
        for (JmxCollection jmxCollection : inputConfig.getJmxCollection()) {
            logger.debug("jmxCollection: '{}'", jmxCollection.getName());
            for (Mbean mbean : jmxCollection.getMbeans().getMbean()) {
                reports.addAll(generateMbeanReportsByMBean(mbean));
                reports.addAll(generateAttributeReportsByMBean(mbean));

                reports.addAll(generateCompositeReportsByMBean(mbean));
                reports.addAll(generateCompositeMemberReportsByMBean(mbean));
            }
        }
        return reports;
    }

    private Collection<Report> generateAttributeReportsByMBean(Mbean mbean) {
        Collection<Report> reports = new ArrayList<>();
        for (Attrib attrib : mbean.getAttrib()) {
            final String title = String.format("%s[%s]", mbean.getObjectname().toString() , attrib.getName());
            final String reportId = StringUtils.deleteWhitespace(mbean.getName()) + "." + attrib.getAlias() + "." + ATTRIBUTEREPORT;
            Report report = new Report(reportId, title, title, "verticalLabel");
            report.addGraph(new Graph(attrib.getAlias(), attrib.getName(), attrib.getAlias(), Colors.getNextColor(), Colors.getNextColor(), Colors.getNextColor()));
            reports.add(report);
            Colors.resetColor();
        }
        return reports;
    }

    private Collection<Report> generateMbeanReportsByMBean(Mbean mbean) {
        Collection<Report> reports = new ArrayList<>();
        if (!mbean.getAttrib().isEmpty()) {

            String reportId = StringUtils.deleteWhitespace(mbean.getName()) + "." + MBEANREPORT;
            Report report = new Report(reportId, mbean.getName(), mbean.getName(), "verticalLabel");
            for (Attrib attrib : mbean.getAttrib()) {
                report.addGraph(new Graph(attrib.getAlias(), attrib.getName(), attrib.getAlias(), Colors.getNextColor(), Colors.getNextColor(), Colors.getNextColor()));
            }
            reports.add(report);
            Colors.resetColor();
        }
        return reports;
    }

    private Collection<Report> generateCompositeReportsByMBean(Mbean mbean) {
        Collection<Report> reports = new ArrayList<>();

        for (CompAttrib compAttrib : mbean.getCompAttrib()) {

            String reportId = StringUtils.deleteWhitespace(mbean.getName()) + "." + compAttrib.getName() + "." + COMPOSITEREPORT;

            Report report = new Report(reportId, reportId, reportId, "verticalLabel");
            for (CompMember compMember : compAttrib.getCompMember()) {
                report.addGraph(new Graph(compMember.getAlias(), compMember.getName(), compMember.getAlias(), Colors.getNextColor(), Colors.getNextColor(), Colors.getNextColor()));
            }
            reports.add(report);
            Colors.resetColor();
        }
        return reports;
    }

    private Collection<Report> generateCompositeMemberReportsByMBean(Mbean mbean) {
        Collection<Report> reports = new ArrayList<>();

        for (CompAttrib compAttrib : mbean.getCompAttrib()) {
            for (CompMember compMember : compAttrib.getCompMember()) {

                String reportId = StringUtils.deleteWhitespace(mbean.getName()) + "." + compAttrib.getName() + "." + compMember.getName() + "." + COMPOSITATTRIBEREPORT;

                Report report = new Report(reportId, reportId, reportId, "verticalLabel");
                report.addGraph(new Graph(compMember.getAlias(), compMember.getName(), compMember.getAlias(), Colors.getNextColor(), Colors.getNextColor(), Colors.getNextColor()));
                reports.add(report);
                Colors.resetColor();
            }
        }
        return reports;
    }
}
