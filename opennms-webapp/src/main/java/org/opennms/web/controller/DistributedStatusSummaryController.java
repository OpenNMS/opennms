package org.opennms.web.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.svclayer.DistributedStatusService;
import org.opennms.web.svclayer.SimpleWebTable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class DistributedStatusSummaryController extends AbstractController {
    
    private DistributedStatusService m_distributedStatusService;

    public DistributedStatusService getDistributedStatusService() {
        return m_distributedStatusService;
    }

    public void setDistributedStatusService(DistributedStatusService statusService) {
        m_distributedStatusService = statusService;
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        GregorianCalendar calendar = new GregorianCalendar();
        Date startDate = new Date(calendar.getTimeInMillis());
        
        calendar.set(Calendar.HOUR_OF_DAY, 0); 
        calendar.set(Calendar.MINUTE, 0); 
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0); 
        Date endDate = new Date(calendar.getTimeInMillis());
        
        SimpleWebTable table = m_distributedStatusService.createFacilityStatusTable(startDate, endDate);
        return new ModelAndView("distributedStatusSummary", "webTable", table);
    }

}
