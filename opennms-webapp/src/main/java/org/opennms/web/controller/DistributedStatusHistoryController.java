package org.opennms.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.svclayer.DistributedStatusService;
import org.opennms.web.svclayer.support.DistributedStatusHistoryModel;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class DistributedStatusHistoryController extends AbstractController {
    private DistributedStatusService m_distributedStatusService;

    public DistributedStatusService getDistributedStatusService() {
        return m_distributedStatusService;
    }

    public void setDistributedStatusService(DistributedStatusService statusService) {
        m_distributedStatusService = statusService;
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String locationName = request.getParameter("location");
        String monitorId = request.getParameter("monitorId");
        String applicationName = request.getParameter("application");
        String timeSpan = request.getParameter("timeSpan");
        String previousLocation = request.getParameter("previousLocation");
        DistributedStatusHistoryModel model =
            m_distributedStatusService.createHistoryModel(locationName,
                                                          monitorId,
                                                          applicationName,
                                                          timeSpan,
                                                          previousLocation);
        return new ModelAndView("distributedStatusHistory", "historyModel", model);

    }

}
