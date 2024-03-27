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
package org.opennms.features.datachoices.web.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.opennms.features.datachoices.internal.StateManager;
import org.opennms.features.datachoices.internal.usagestatistics.UsageStatisticsMetadataDTO;
import org.opennms.features.datachoices.internal.usagestatistics.UsageStatisticsReportDTO;
import org.opennms.features.datachoices.internal.usagestatistics.UsageStatisticsReporter;
import org.opennms.features.datachoices.internal.usagestatistics.UsageStatisticsStatusDTO;
import org.opennms.features.datachoices.internal.productupdateenrollment.ProductUpdateEnrollmentFormData;
import org.opennms.features.datachoices.internal.productupdateenrollment.ProductUpdateEnrollmentService;
import org.opennms.features.datachoices.internal.productupdateenrollment.ProductUpdateEnrollmentStatusDTO;
import org.opennms.features.datachoices.web.DataChoiceRestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/** 
 * Rest-Endpoint mounted at /datachoices. Supported paths are:
 *
 * GET /opennms/rest/datachoices
 * GET /opennms/rest/datachoices/status
 * POST /opennms/rest/datachoices/status
 * GET /opennms/rest/datachoices/meta
 * GET /opennms/rest/datachoices/productupdate/status
 * POST /opennms/rest/datachoices/productupdate/status
 * POST /opennms/rest/datachoices/productupdate/submit
 *
 * These are no longer supported:
 * POST /opennms/rest/datachoices?action=enable
 * POST /opennms/rest/datachoices?action=disable
 *
 * @author jwhite
 * @author mvrueden
 */
public class DataChoiceRestServiceImpl implements DataChoiceRestService {
    private static final Logger LOG = LoggerFactory.getLogger(DataChoiceRestServiceImpl.class);
    private StateManager m_stateManager;
    private UsageStatisticsReporter m_usageStatisticsReporter;

    private ProductUpdateEnrollmentService productUpdateEnrollmentService;

    private static final String METADATA_RESOURCE_PATH = "web/datachoicesMetadata.json";

    @Override
    public UsageStatisticsReportDTO getUsageStatistics() throws ServletException, IOException {
        return m_usageStatisticsReporter.generateReport();
    }

    @Override
    public Response getStatus() throws ServletException, IOException {
        UsageStatisticsStatusDTO dto = new UsageStatisticsStatusDTO();

        try {
            dto.setEnabled(m_stateManager.isEnabled());
            dto.setInitialNoticeAcknowledged(m_stateManager.isInitialNoticeAcknowledged());
        } catch (Exception e) {
            return getExceptionResponse("Error getting Usage Statistics status.", e);
        }

        return Response.ok(dto).build();
    }

    @Override
    public Response setStatus(HttpServletRequest request, UsageStatisticsStatusDTO dto) throws ServletException, IOException {
        try {
            final String remoteUser = request.getRemoteUser();

            if (dto.getEnabled() != null) {
                m_stateManager.setEnabled(dto.getEnabled().booleanValue(), remoteUser);
            }

            if (dto.getInitialNoticeAcknowledged() != null) {
                m_stateManager.setInitialNoticeAcknowledged(dto.getInitialNoticeAcknowledged().booleanValue(), remoteUser);
            }
        } catch (Exception e) {
            return getExceptionResponse("Error setting Usage Statistics status.", e);
        }

        return Response.accepted().build();
    }

    @Override
    public Response getProductUpdateEnrollmentStatus() throws ServletException, IOException {
        ProductUpdateEnrollmentStatusDTO dto = new ProductUpdateEnrollmentStatusDTO();

        try {
            dto.setNoticeAcknowledged(m_stateManager.isProductUpdateEnrollmentNoticeAcknowledged());
            dto.setOptedIn(m_stateManager.isProductUpdateEnrollmentOptedIn());
        } catch (Exception e) {
            return getExceptionResponse("Error getting Product Update Enrollment status.", e);
        }

        return Response.ok(dto).build();
    }

    @Override
    public Response setProductUpdateEnrollmentStatu(HttpServletRequest request, ProductUpdateEnrollmentStatusDTO dto) throws ServletException, IOException {
        try {
            final String remoteUser = request.getRemoteUser();

            if (dto.getOptedIn() != null) {
                m_stateManager.setProductUpdateEnrollmentOptedIn(dto.getOptedIn());
            }

            if (dto.getNoticeAcknowledged() != null) {
                m_stateManager.setProductUpdateEnrollmentNoticeAcknowledged(dto.getNoticeAcknowledged(), remoteUser);
            }
        } catch (Exception e) {
            return getExceptionResponse("Error setting Product Update Enrollment status.", e);
        }

        return Response.accepted().build();
    }

    @Override
    public Response getMetadata() {
        UsageStatisticsMetadataDTO dto = null;

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(METADATA_RESOURCE_PATH)) {
            ObjectMapper mapper = new ObjectMapper();
            dto = mapper.readValue(inputStream, UsageStatisticsMetadataDTO.class);
        } catch (Exception e) {
            return getExceptionResponse("Error getting Usage Statistics metadata.", e);
        }

        return Response.ok(dto).build();
    }

    @Override
    public Response submitProductUpdateEnrollmentData(HttpServletRequest request, ProductUpdateEnrollmentFormData data) throws ServletException, IOException {
        String show = System.getProperty("opennms.productUpdateEnrollment.show", "true");

        // don't process Product Update Enrollment if disabled by configuration
        if (show != null && show.equalsIgnoreCase("false")) {
            String msg = "Product Update Enrollment has been disabled by the 'opennms.productUpdateEnrollment.show' configuration point.";
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }

        try {
            productUpdateEnrollmentService.submit(data);
        } catch (Exception e) {
            return getExceptionResponse("Error submitting Product Update Enrollment form data.", e);
        }

        return Response.accepted().build();
    }

    public void setStateManager(StateManager stateManager) {
        m_stateManager = stateManager;
    }

    public void setUsageStatisticsReporter(UsageStatisticsReporter usageStatisticsReporter) {
        m_usageStatisticsReporter = Objects.requireNonNull(usageStatisticsReporter);
    }

    public void setProductUpdateEnrollmentService(ProductUpdateEnrollmentService service) {
        this.productUpdateEnrollmentService = service;
    }

    private Response getExceptionResponse(String msg, Throwable e) {
        LOG.error(msg, e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
    }
}
