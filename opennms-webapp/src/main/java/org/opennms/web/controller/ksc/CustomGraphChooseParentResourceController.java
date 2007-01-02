package org.opennms.web.controller.ksc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.svclayer.ResourceService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class CustomGraphChooseParentResourceController extends AbstractController implements InitializingBean {
    
    private ResourceService m_resourceService;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("KSC/customGraphChooseParentResource");

        modelAndView.addObject("nodeResources", getResourceService().findNodeResources());
        modelAndView.addObject("domainResources", getResourceService().findDomainResources());
        
        return modelAndView;
    }

    public ResourceService getResourceService() {
        return m_resourceService;
    }

    public void setResourceService(ResourceService resourceService) {
        m_resourceService = resourceService;
    }

    public void afterPropertiesSet() throws Exception {
        if (m_resourceService == null) {
            throw new IllegalStateException("property resourceService must be set");
        }
    }

}
