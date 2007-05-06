package org.opennms.web.controller.alarm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.svclayer.TroubleTicketProxy;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

public class AlarmTicketController extends MultiActionController {
    private TroubleTicketProxy m_troubleTicketProxy;
    
    public AlarmTicketController() {
        super();
        log().debug("AlarmTicketController created");
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log().debug("AlarmTicketController handleRequestInternal called");
        return super.handleRequestInternal(request, response);
    }
    
    static class CommandBean {
        Integer alarm;
        String redirect;
        public Integer getAlarm() {
            return alarm;
        }
        public void setAlarm(Integer alarm) {
            this.alarm = alarm;
        }
        public String getRedirect() {
            return redirect;
        }
        public void setRedirect(String redirect) {
            this.redirect = redirect;
        }
        
        
    }
    
    public ModelAndView create(HttpServletRequest request, HttpServletResponse response, CommandBean bean) throws Exception {
        m_troubleTicketProxy.createTicket(bean.getAlarm());
        return new ModelAndView("redirect:"+bean.getRedirect());
    }
    public ModelAndView update(HttpServletRequest request, HttpServletResponse response, CommandBean bean) {
        m_troubleTicketProxy.updateTicket(bean.getAlarm());
        return new ModelAndView("redirect:"+bean.getRedirect());
    }
    public ModelAndView close(HttpServletRequest request, HttpServletResponse response, CommandBean bean) {
        m_troubleTicketProxy.closeTicket(bean.getAlarm());
        return new ModelAndView("redirect:"+bean.getRedirect());
    }

    public void setTroubleTicketProxy(TroubleTicketProxy troubleTicketProxy) {
        m_troubleTicketProxy = troubleTicketProxy;
    }

    /**
     * @return logger for this class
     */
    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
}
