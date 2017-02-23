/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.jmxconfiggenerator.log.Slf4jLogAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class GraphConfigGeneratorTest {

    // Verify that the default snmp-graph.properties template is not deleted
    @Test
    public void verifySnmpGraphTemplate() throws IOException {
        final String templatePath = GraphConfigGenerator.INTERN_TEMPLATE_NAME;
        InputStream templateInputStream = this.getClass().getClassLoader().getResourceAsStream(templatePath);
        if (templateInputStream == null) {
            throw new IOException(String.format("Template file '%s' doesn't exist.", GraphConfigGenerator.INTERN_TEMPLATE_NAME));
        }
    }

    @Test
    public void verifyGraphGeneration() {
        JmxConfigReader jmxConfigReader = new JmxConfigReader(new Slf4jLogAdapter(JmxConfigReader.class));
        Collection<Report> reports = jmxConfigReader.generateReportsByJmxDatacollectionConfig(getClass().getResourceAsStream("/cassandra21x-datacollection-config.xml"));
        GraphConfigGenerator graphConfigGenerator = new GraphConfigGenerator(new Slf4jLogAdapter(GraphConfigGenerator.class));
        String snmpGraph = graphConfigGenerator.generateSnmpGraph(reports);
        Assert.assertNotNull(snmpGraph);
    }
}
