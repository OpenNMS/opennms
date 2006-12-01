package org.opennms.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.HttpJspPage;

public abstract class CustomJspBase extends HttpServlet implements HttpJspPage {
    
    @Override
    public void init() throws ServletException {
        customInit();
        jspInit();
    }

    @Override
    final protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        _jspService(request, response);
    }

    @Override
    final public void destroy() {
        jspDestroy();
        super.destroy();
    }
    
    public void customInit() throws ServletException {
    }

    public void jspDestroy() {
    }

    public void jspInit() {
    }

    public void _jspService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }



}
