package org.opennms.netmgt.endpoints.grafana.rest;

import org.junit.Test;
import org.opennms.netmgt.endpoints.grafana.api.Dashboard;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaClient;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaEndpoint;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaEndpointService;
import org.opennms.netmgt.endpoints.grafana.rest.internal.GrafanaEndpointRestServiceImpl;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class GrafanaEndpointRestServiceIT {


    private GrafanaEndpointService grafanaEndpointService = mock(GrafanaEndpointService.class);
    private GrafanaEndpointRestServiceImpl grafanaEndpointRestService =new GrafanaEndpointRestServiceImpl(grafanaEndpointService);

    @Test
    public void testVerifyEndpoint_WithNullUid() throws IOException {

        // Arrange
        GrafanaEndpoint endpoint = new GrafanaEndpoint();
        endpoint.setApiKey("testApiKey");
        endpoint.setUrl("http://localhost:3000");
        endpoint.setConnectTimeout(5000);
        endpoint.setReadTimeout(5000);


        GrafanaClient mockClient = mock(GrafanaClient.class);
        when(grafanaEndpointService.getClient(any(GrafanaEndpoint.class))).thenReturn(mockClient);

        // Create mock dashboards
        List<Dashboard> mockDashboards = new ArrayList<>();
        Dashboard dashboard = new Dashboard();
        dashboard.setUid("dashboard1");
        dashboard.setTitle("Test Dashboard");
        dashboard.setUrl("http://localhost:3000/d/test-dashboard");
        mockDashboards.add(dashboard);

        // Mock the getDashboards method
        when(mockClient.getDashboards()).thenReturn(mockDashboards);

        // Verify End Point
        Response response = grafanaEndpointRestService.verifyEndpoint(endpoint);

        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    }

}
