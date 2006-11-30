package org.opennms.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.command.DistributedStatusDetailsCommand;
import org.opennms.web.svclayer.DistributedStatusService;
import org.opennms.web.svclayer.SimpleWebTable;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

public class DistributedStatusDetailsController extends AbstractCommandController {
    
    private DistributedStatusService m_distributedStatusService;
    private String m_successView;

    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        DistributedStatusDetailsCommand cmd = (DistributedStatusDetailsCommand) command;
        SimpleWebTable table = m_distributedStatusService.createStatusTable(cmd, errors);
        return new ModelAndView(getSuccessView(), "webTable", table);
    }

    public DistributedStatusService getDistributedStatusService() {
        return m_distributedStatusService;
    }

    public void setDistributedStatusService(DistributedStatusService statusService) {
        m_distributedStatusService = statusService;
    }

    public String getSuccessView() {
        return m_successView;
    }

    public void setSuccessView(String successView) {
        m_successView = successView;
    }
}
