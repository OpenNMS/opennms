package org.opennms.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.svclayer.DistributedStatusService;
import org.opennms.web.svclayer.SimpleWebTable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class DistributedStatusDetailsController extends AbstractController {
    
    private DistributedStatusService m_distributedStatusService;

    public DistributedStatusService getDistributedStatusService() {
        return m_distributedStatusService;
    }

    public void setDistributedStatusService(DistributedStatusService statusService) {
        m_distributedStatusService = statusService;
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String applicationLabel = request.getParameter("application");
        String locationName = request.getParameter("location");
        SimpleWebTable table = m_distributedStatusService.createStatusTable(locationName, applicationLabel);
        return new ModelAndView("distributedStatusDetails", "webTable", table);
    }

}
