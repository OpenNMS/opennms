package org.opennms.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.model.OnmsResource;
import org.opennms.web.MissingParameterException;
import org.opennms.web.svclayer.ChooseResourceService;
import org.opennms.web.svclayer.support.ChooseResourceModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;


public class ChooseResourceController extends AbstractController implements InitializingBean {
    private ChooseResourceService m_chooseResourceService;
    private String m_defaultEndUrl;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String[] requiredParameters = new String[] { "parentResourceId or", "parentResourceType and parentResource" };

        String endUrl = request.getParameter("endUrl");

        String resourceId = request.getParameter("parentResourceId");
        if (resourceId == null) {
            String resourceType = request.getParameter("parentResourceType");
            String resource = request.getParameter("parentResource");
            if (request.getParameter("parentResourceType") == null) {
                throw new MissingParameterException("parentResourceType", requiredParameters);
            }
            if (request.getParameter("parentResource") == null) {
                throw new MissingParameterException("parentResource", requiredParameters);
            }
            
            resourceId = OnmsResource.createResourceId(resourceType, resource);
        }
        
        if (endUrl == null || "".equals(endUrl)) {
            endUrl = m_defaultEndUrl;
        }

        ChooseResourceModel model = 
            m_chooseResourceService.findChildResources(resourceId,
                                                       endUrl);
        
        return new ModelAndView("/graph/chooseresource",
                                "model",
                                model);
    }
    
    public void afterPropertiesSet() {
        if (m_chooseResourceService == null) {
            throw new IllegalStateException("chooseResourceService property not set");
        }
        
        if (m_defaultEndUrl == null) {
            throw new IllegalStateException("defaultEndUrl property not set");
        }
    }

    public ChooseResourceService getChooseResourceService() {
        return m_chooseResourceService;
    }

    public void setChooseResourceService(
            ChooseResourceService chooseResourceService) {
        m_chooseResourceService = chooseResourceService;
    }

    public String getDefaultEndUrl() {
        return m_defaultEndUrl;
    }

    public void setDefaultEndUrl(String defaultEndUrl) {
        m_defaultEndUrl = defaultEndUrl;
    }
}
