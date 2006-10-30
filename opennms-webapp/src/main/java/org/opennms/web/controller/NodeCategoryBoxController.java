package org.opennms.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.web.MissingParameterException;
import org.opennms.web.acegisecurity.Authentication;
import org.opennms.web.svclayer.AdminCategoryService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class NodeCategoryBoxController extends AbstractController {
    private AdminCategoryService m_adminCategoryService; 

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String nodeIdString = request.getParameter("node");

        if (nodeIdString == null) {
            throw new MissingParameterException("node");
        }

        int nodeId = Integer.parseInt(nodeIdString);

        List<OnmsCategory> categories = m_adminCategoryService.findByNode(nodeId);
        
        ModelAndView modelAndView =
            new ModelAndView("/includes/nodeCategory-box", "categories",
                             categories);
        if (request.isUserInRole(Authentication.ADMIN_ROLE)) {
            modelAndView.addObject("isAdmin", "true");
        }
        return modelAndView;
    }

    public AdminCategoryService getAdminCategoryService() {
        return m_adminCategoryService;
    }

    public void setAdminCategoryService(AdminCategoryService adminCategoryService) {
        m_adminCategoryService = adminCategoryService;
    }

}
