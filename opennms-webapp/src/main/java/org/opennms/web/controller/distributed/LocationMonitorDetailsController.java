package org.opennms.web.controller.distributed;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.command.LocationMonitorDetailsCommand;
import org.opennms.web.svclayer.DistributedPollerService;
import org.opennms.web.svclayer.support.LocationMonitorDetailsModel;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

public class LocationMonitorDetailsController extends AbstractCommandController {
    
    private DistributedPollerService m_distributedPollerService;
    private String m_successView;

    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        LocationMonitorDetailsCommand cmd = (LocationMonitorDetailsCommand) command;
        LocationMonitorDetailsModel model = getDistributedPollerService().getLocationMonitorDetails(cmd, errors);
        return new ModelAndView(getSuccessView(), "model", model);
    }
    
    public DistributedPollerService getDistributedPollerService() {
        return m_distributedPollerService;
    }

    public void setDistributedPollerService(DistributedPollerService distributedPollerService) {
        m_distributedPollerService = distributedPollerService;
    }

    public String getSuccessView() {
        return m_successView;
    }

    public void setSuccessView(String successView) {
        m_successView = successView;
    }
}
