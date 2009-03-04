package org.opennms.web.inventory;

import java.io.IOException;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.log4j.Category;

import org.opennms.web.inventory.InventoryLayer;

public class RancidStatusServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        HttpSession userSession = request.getSession(false);

        log().debug("RancidStatusServlet invoked");
        
        if (userSession != null) {
            String status = request.getParameter("status");
            String device = request.getParameter("deviceName");
            String group = request.getParameter("groupName");
            
            log().debug("RancidStatusServlet setting state to "+ device + " " + group + " " + status);

            int ret = InventoryLayer.updateStatus(device, group);
            redirect(request, response);
        }
    }
        
    private void redirect(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String redirectURL = request.getHeader("Referer");
        response.sendRedirect(redirectURL);
    }
    private static Category log() {
        return Logger.getLogger("Rancid");
    }
}