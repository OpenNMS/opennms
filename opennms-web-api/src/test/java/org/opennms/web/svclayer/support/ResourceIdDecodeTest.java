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
