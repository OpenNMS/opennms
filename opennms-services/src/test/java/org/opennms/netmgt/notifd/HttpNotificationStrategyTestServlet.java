package org.opennms.netmgt.notifd;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpNotificationStrategyTestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static Map<String,String> m_parameters = new HashMap<String,String>();

    @Override protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }

    @Override protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }

    @Override protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }

    public static Map<String,String> getRequestParameters() {
        return m_parameters;
    }

    public void handleRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        m_parameters.clear();

        @SuppressWarnings("unchecked")
        final Enumeration<String> e = req.getParameterNames();
        while (e.hasMoreElements()) {
            final String key = e.nextElement();
            m_parameters.put(key, req.getParameter(key));
        }

        final String responseText = "It worked!\n";
        final ServletOutputStream os = resp.getOutputStream();
        os.print(responseText);
        os.close();
        resp.setContentType("text/plain");
        resp.setContentLength(responseText.length());
    }
}