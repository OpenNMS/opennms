package org.opennms.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.MissingParameterException;
import org.opennms.web.svclayer.ChooseResourceService;
import org.opennms.web.svclayer.support.ChooseResourceModel;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;


public class ChooseResourceController extends AbstractController {
    private ChooseResourceService m_chooseResourceService;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String[] requiredParameters = new String[] { "node or domain", "endUrl" };

        String nodeId = request.getParameter("node");
        String domain = request.getParameter("domain");
        String endUrl = request.getParameter("endUrl");
        
        if (request.getParameter("endUrl") == null) {
            throw new MissingParameterException("endUrl", requiredParameters);
        }
        
        String resourceType;
        String resource;
        if (nodeId != null) {
            resourceType = "node";
            resource = nodeId;
        } else if (domain != null) {
            resourceType = "domain";
            resource = domain;
        } else {
            throw new MissingParameterException("node or domain",
                                                requiredParameters);
        }

        ChooseResourceModel model = 
            m_chooseResourceService.findChildResources(resourceType,
                                                       resource,
                                                       endUrl);
        
        return new ModelAndView("/graph/chooseresource",
                                "model",
                                model);
    }

    public ChooseResourceService getChooseResourceService() {
        return m_chooseResourceService;
    }

    public void setChooseResourceService(
            ChooseResourceService chooseResourceService) {
        m_chooseResourceService = chooseResourceService;
    }
}
