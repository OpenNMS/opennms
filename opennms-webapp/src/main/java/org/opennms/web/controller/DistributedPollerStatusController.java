package org.opennms.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.svclayer.DistributedPollerService;
import org.opennms.web.svclayer.SimpleWebTable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class DistributedPollerStatusController extends AbstractController {
    private DistributedPollerService m_distributedPollerService;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SimpleWebTable webTable =
            m_distributedPollerService.createStatusTable();
        return new ModelAndView("distributedPollerStatus",
                                "webTable", webTable);
    }

    public DistributedPollerService getDistributedPollerService() {
        return m_distributedPollerService;
    }

    public void setDistributedPollerService(
            DistributedPollerService distributedPollerService) {
        m_distributedPollerService = distributedPollerService;
    }

}
