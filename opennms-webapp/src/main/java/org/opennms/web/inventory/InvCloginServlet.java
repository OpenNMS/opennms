package org.opennms.web.inventory;

import java.io.IOException;
import java.text.ChoiceFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.web.inventory.InventoryLayer;

public class InvCloginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        HttpSession userSession = request.getSession(false);

        System.out.println("InvCloginServlet");
        
        if (userSession != null) {
            String userId = request.getParameter("userID");
            String password = request.getParameter("pass");
            String loginM = request.getParameter("loginM");
            String device = request.getParameter("deviceName");
            String group = request.getParameter("groupName");
            String autoenable = request.getParameter("autoE");
            String enablepass = request.getParameter("enpass");
            
            //System.out.println("InvCloginServlet setting user and password "+ device +" "+ userId +" "+password + " enablep "+ enablepass);

            int ret = InventoryLayer.updateCloginInfo(device, userId, password, loginM, autoenable, enablepass);
            redirect(request, response);
        }
    }
        
        private void redirect(HttpServletRequest request,
                HttpServletResponse response) throws IOException {
            String redirectURL = request.getHeader("Referer");
            response.sendRedirect(redirectURL);
        }
}