package org.opennms.nrtg.web.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.ModelAndView;

public class NrtServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	private NrtController m_controller;

	public void setController(NrtController controller) {
		m_controller = controller;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)	throws ServletException, IOException {
		
		HttpSession httpSession = req.getSession(true);
		
		if (req.getParameter("nrtCollectionTaskId") != null) {
			m_controller.nrtCollectionJobTrigger(req.getParameter("nrtCollectionTaskId"), httpSession);
		} else if (req.getParameter("resourceId") != null &&  req.getParameter("report") != null) {
			ModelAndView modelAndView = m_controller.nrtStart(req.getParameter("resourceId"), req.getParameter("report"), httpSession);
			
			String template = getTemplateAsString(modelAndView.getViewName()+".template");

			for(Entry<String, Object> entry : modelAndView.getModel().entrySet()) {
				template = template.replaceAll("\\$\\{"+entry.getKey()+"\\}", (entry.getValue() != null ? entry.getValue().toString() : "null"));
			}
				
			
			resp.getOutputStream().write(template.getBytes());
			
		} else {
			throw new ServletException("unrecognized servlet parameters");
		}
		

	}
	
	public String getTemplateAsString(String templateName) throws IOException {
		
		BufferedReader r = null;
		try {
			StringBuilder results = new StringBuilder();
			r = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/"+templateName)));
			
			String line = null;
			while((line = r.readLine()) != null) {
				results.append(line).append('\n');
			}
			
			return results.toString();
		} finally {
			if (r!=null) r.close();
		}
	}

}
