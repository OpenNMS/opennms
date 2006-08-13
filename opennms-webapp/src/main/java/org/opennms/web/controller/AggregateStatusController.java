package org.opennms.web.controller;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.svclayer.AggregateStatus;
import org.opennms.web.svclayer.AggregateStatusService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class AggregateStatusController extends AbstractController {

    private static final int FIVE_MINUTES = 5*60;
    
    private static AggregateStatusService m_aggrSvc;

    public AggregateStatusController() {
        setSupportedMethods(new String[] {METHOD_GET});
        setCacheSeconds(FIVE_MINUTES);
    }
    
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        ModelAndView mav = new ModelAndView("aggregateStatuses");
        String statusView = req.getParameter("statusView");
        Collection<AggregateStatus> aggrStati = m_aggrSvc.createAggregateStatusView(statusView);
        mav.addObject("statuses", aggrStati);
        return mav;
    }

}
