/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.netmgt.config.kscReports.ReportsList;
import org.opennms.netmgt.model.ResourceId;
import org.springframework.core.io.FileSystemResource;

/** This test verifies the issue NMS-10309 **/
public class ResourceIdDecodeTest {

    private ReportsList configForLegacyReport;
    private ReportsList config;

    @Test
    public void testResourceRetrieval() throws UnsupportedEncodingException {
        configForLegacyReport = JaxbUtils.unmarshal(ReportsList.class, new FileSystemResource(this.getClass().getResource("/ksc-performance-reports-legacy.xml").getPath()));
        config = JaxbUtils.unmarshal(ReportsList.class, new FileSystemResource(this.getClass().getResource("/ksc-performance-reports-test.xml").getPath()));
        assertNotNull(configForLegacyReport);
        assertNotNull(config);
        Report reportWithEncodedResource = configForLegacyReport.getReports().get(0);
        Report report = config.getReports().get(0);
        assertNotNull(reportWithEncodedResource);
        assertNotNull(report);
        // Check that legacy reportId can be decoded to new reportId
        for(int i= 0; i < report.getGraphs().size(); i++) {
            ResourceId resourceId = ResourceId.fromString(report.getGraphs().get(i).getResourceId().get());
            String decodedResourceId = URLDecoder.decode(reportWithEncodedResource.getGraphs().get(i).getResourceId().get(), StandardCharsets.UTF_8.name());
            assertEquals(resourceId.toString(), decodedResourceId);
        }
        // Check that decoding non-encoded reportId will yield same reportId
        for(int i= 0; i < report.getGraphs().size(); i++) {
            ResourceId resourceId = ResourceId.fromString(report.getGraphs().get(i).getResourceId().get());
            String decodedResourceId = URLDecoder.decode(report.getGraphs().get(i).getResourceId().get(), StandardCharsets.UTF_8.name());
            assertEquals(resourceId.toString(), decodedResourceId);
        }
    }
}
