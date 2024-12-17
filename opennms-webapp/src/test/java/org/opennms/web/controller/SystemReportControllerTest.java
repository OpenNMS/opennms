package org.opennms.web.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opennms.systemreport.SystemReport;
import org.opennms.systemreport.SystemReportFormatter;
import org.opennms.systemreport.SystemReportPlugin;
import org.opennms.web.controller.admin.support.SystemReportController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class SystemReportControllerTest extends SystemReportController{




    private SystemReport mockSystemReport;

    private SystemReportController systemReportController;

    @Before
    public void setUp() {

        systemReportController = new SystemReportController();
        mockSystemReport = new SystemReport();
    }


    @Test
    public void testMockMvcHandleRequest() throws Exception {

        // Create a mock request
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");

        // Set the form parameters (simulating the request payload)
        request.addParameter("operation", "run");
        request.addParameter("output", "abc.txt");
        request.addParameter("formatter", "text");
        request.addParameter("plugins", "Java");

        // Set the request URI (for routing)
        request.setRequestURI("/admin/support/systemReport.htm");

        // Create a mock response
        MockHttpServletResponse response = new MockHttpServletResponse();

        systemReportController.setSystemReport(mockSystemReport);
        ModelAndView modelAndView = systemReportController.handleRequest(request, response);

        // Check the results or response
        assertNotNull(modelAndView);
        // Extracting the 'Content-Disposition' header
        String contentDisposition = response.getHeader("Content-Disposition");

        // Assert that the Content-Disposition header contains the expected file name
        assertNotNull(contentDisposition);
        assertTrue(contentDisposition.contains("attachment; filename=\"abc.txt\""));

    }
}
