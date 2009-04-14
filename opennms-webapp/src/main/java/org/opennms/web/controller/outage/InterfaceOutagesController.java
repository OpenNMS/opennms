package org.opennms.web.controller.outage;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.element.ElementUtil;
import org.opennms.web.element.Interface;
import org.opennms.web.filter.Filter;
import org.opennms.web.outage.Outage;
import org.opennms.web.outage.WebOutageRepository;
import org.opennms.web.outage.filter.InterfaceFilter;
import org.opennms.web.outage.filter.NodeFilter;
import org.opennms.web.outage.filter.OutageCriteria;
import org.opennms.web.outage.filter.RecentOutagesFilter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class InterfaceOutagesController extends AbstractController implements InitializingBean {

    private String m_successView;
    private WebOutageRepository m_webOutageRepository;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Interface iface = ElementUtil.getInterfaceByParams(request);

        Outage[] outages = new Outage[0];

        if (iface.getNodeId() > 0 && iface.getIpAddress() != null) {
            List<Filter> filters = new ArrayList<Filter>();

            filters.add(new InterfaceFilter(iface.getIpAddress()));
            filters.add(new NodeFilter(iface.getNodeId()));
            filters.add(new RecentOutagesFilter());

            OutageCriteria criteria = new OutageCriteria(filters.toArray(new Filter[0]));
            outages = m_webOutageRepository.getMatchingOutages(criteria);
        }

        ModelAndView modelAndView = new ModelAndView(getSuccessView());
        modelAndView.addObject("nodeId", iface.getNodeId());
        modelAndView.addObject("outages", outages);
        return modelAndView;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_successView, "property successView must be set");
        Assert.notNull(m_webOutageRepository, "webOutageRepository must be set");
    }

    private String getSuccessView() {
        return m_successView;
    }

    public void setSuccessView(String successView) {
        m_successView = successView;
    }
    
    public void setWebOutageRepository(WebOutageRepository webOutageRepository) {
        m_webOutageRepository = webOutageRepository;
    }

}
