/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc. OpenNMS(R) is Copyright (C)
 * 1999-2013 The OpenNMS Group, Inc.
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
 ******************************************************************************
 */
package org.opennms.features.jmxconfiggenerator.graphs;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.bind.JAXB;
import org.apache.commons.lang.StringUtils;
import org.opennms.features.jmxconfiggenerator.helper.Colors;
import org.opennms.xmlns.xsd.config.jmx_datacollection.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simon Walter <simon.walter@hp-factory.de>
 * @author Markus Neumann <markus@opennms.com>
 */
public class JmxConfigReader {

    private Logger logger = LoggerFactory.getLogger(JmxConfigReader.class);
    private final String ATTRIBUTEREPORT = "AttributeReport";
    private final String MBEANREPORT = "MBeanReport";
    private final String COMPOSITEREPORT = "CompositeReport";
    private final String COMPOSITATTRIBEREPORT = "CompositeAttributeReport";

    public Collection<Report> generateReportsByJmxDatacollectionConfig(String inputConfigFileName) {
        return generateReportsByJmxDatacollectionConfig(
                JAXB.unmarshal(
                    new File(inputConfigFileName), 
                    JmxDatacollectionConfig.class));
    }

    public Collection<Report> generateReportsByJmxDatacollectionConfig(JmxDatacollectionConfig inputConfig) {
        Collection<Report> reports = new ArrayList<Report>();
        for (JmxCollection jmxCollection : inputConfig.getJmxCollection()) {
            logger.debug("jmxCollection: '{}'", jmxCollection.getName());
            for (Mbean mbean : jmxCollection.getMbeans().getMbean()) {
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

            String reportId = StringUtils.deleteWhitespace(mbean.getName()) + "." + attrib.getAlias() + "." + ATTRIBUTEREPORT;
            Report report = new Report(reportId, attrib.getName(), attrib.getName(), "verticalLabel");
            report.addGraph(new Graph(attrib.getAlias(), attrib.getName(), attrib.getAlias(), Colors.getNextColor(), Colors.getNextColor(), Colors.getNextColor()));
            reports.add(report);
            Colors.restetColor();
        }
        return reports;
    }

    private Collection<Report> generateMbeanReportsByMBean(Mbean mbean) {
        Collection<Report> reports = new ArrayList<Report>();
        if (!mbean.getAttrib().isEmpty()) {

            String reportId = StringUtils.deleteWhitespace(mbean.getName()) + "." + MBEANREPORT;
            Report report = new Report(reportId, mbean.getName(), mbean.getName(), "verticalLabel");
            for (Attrib attrib : mbean.getAttrib()) {
                report.addGraph(new Graph(attrib.getAlias(), attrib.getName(), attrib.getAlias(), Colors.getNextColor(), Colors.getNextColor(), Colors.getNextColor()));
            }
            reports.add(report);
            Colors.restetColor();
        }
        return reports;
    }

    private Collection<Report> generateCompositeReportsByMBean(Mbean mbean) {
        Collection<Report> reports = new ArrayList<Report>();

        for (CompAttrib compAttrib : mbean.getCompAttrib()) {

            String reportId = StringUtils.deleteWhitespace(mbean.getName()) + "." + compAttrib.getName() + "." + COMPOSITEREPORT;

            Report report = new Report(reportId, reportId, reportId, "verticalLabel");
            for (CompMember compMember : compAttrib.getCompMember()) {
                report.addGraph(new Graph(compMember.getAlias(), compMember.getName(), compMember.getAlias(), Colors.getNextColor(), Colors.getNextColor(), Colors.getNextColor()));
            }
            reports.add(report);
            Colors.restetColor();
        }
        return reports;
    }

    private Collection<Report> generateCompositeMemberReportsByMBean(Mbean mbean) {
        Collection<Report> reports = new ArrayList<Report>();

        for (CompAttrib compAttrib : mbean.getCompAttrib()) {
            for (CompMember compMember : compAttrib.getCompMember()) {

                String reportId = StringUtils.deleteWhitespace(mbean.getName()) + "." + compAttrib.getName() + "." + compMember.getName() + "." + COMPOSITATTRIBEREPORT;

                Report report = new Report(reportId, reportId, reportId, "verticalLabel");
                report.addGraph(new Graph(compMember.getAlias(), compMember.getName(), compMember.getAlias(), Colors.getNextColor(), Colors.getNextColor(), Colors.getNextColor()));
                reports.add(report);
                Colors.restetColor();
            }
        }
        return reports;
    }
}
