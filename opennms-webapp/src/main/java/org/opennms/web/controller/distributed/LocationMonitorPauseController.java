package org.opennms.web.controller.distributed;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.command.LocationMonitorIdCommand;
import org.opennms.web.svclayer.DistributedPollerService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

public class LocationMonitorPauseController extends AbstractCommandController implements InitializingBean {
    
    private DistributedPollerService m_distributedPollerService;
    private String m_successView;
    private String m_errorView;

    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        LocationMonitorIdCommand cmd = (LocationMonitorIdCommand) command;
        if (!errors.hasErrors()) {
            getDistributedPollerService().pauseLocationMonitor(cmd, errors);
        }
        
        if (errors.hasErrors()) {
            return new ModelAndView(getErrorView(), "errors", errors);
        } else {
            return new ModelAndView(getSuccessView());
        }
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

    public String getErrorView() {
        return m_errorView;
    }

    public void setErrorView(String errorView) {
        m_errorView = errorView;
    }

    public void afterPropertiesSet() throws Exception {
        if (m_distributedPollerService == null) {
            throw new IllegalStateException("distributedPollerService property cannot be null");
        }
        
        if (m_successView == null) {
            throw new IllegalStateException("successView property cannot be null");
        }
        
        if (m_errorView == null) {
            throw new IllegalStateException("errorView property cannot be null");
        }
    }

}
