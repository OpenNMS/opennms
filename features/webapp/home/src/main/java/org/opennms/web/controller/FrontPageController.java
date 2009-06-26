package org.opennms.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.springframework.security.Authentication;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class FrontPageController extends AbstractController {
    
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (request.isUserInRole(Authentication.DASHBOARD_ROLE)) {
            return new ModelAndView("redirect:/dashboard.jsp");
        } else {
            return new ModelAndView("redirect:/index.jsp");
        }
    }
}
