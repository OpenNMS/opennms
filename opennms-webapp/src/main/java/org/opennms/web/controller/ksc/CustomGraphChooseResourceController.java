package org.opennms.web.controller.ksc;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.model.OnmsResource;
import org.opennms.web.MissingParameterException;
import org.opennms.web.svclayer.ResourceService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class CustomGraphChooseResourceController extends AbstractController implements InitializingBean {
    private ResourceService m_resourceService;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String resourceId = request.getParameter("resourceId");
        if (resourceId == null) {
            throw new MissingParameterException("resourceId");
        }
        
        OnmsResource resource = getResourceService().getResourceById(resourceId);

//        List<Resource> childResources = getResourceService().findChildResources(resource, "nodeSnmp", "interfaceSnmp");
        List<OnmsResource> childResources = getResourceService().findChildResources(resource);
        return new ModelAndView("KSC/customGraphChooseResource", "resources", childResources);
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
