package org.opennms.web.controller.rtc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.svclayer.RtcService;
import org.opennms.web.svclayer.support.RtcNodeModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class ListNodes extends AbstractController implements InitializingBean {
    private RtcService m_rtcService;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        RtcNodeModel model = m_rtcService.getNodeList();
        return new ModelAndView("rtc/category", "model", model);
    }
    
    public void afterPropertiesSet() {
        Assert.state(m_rtcService != null, "property rtcService must be set and non-null");
    }

    public RtcService getRtcService() {
        return m_rtcService;
    }

    public void setRtcService(RtcService rtcService) {
        m_rtcService = rtcService;
    }

}
