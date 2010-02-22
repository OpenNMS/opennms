package org.opennms.core.test.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MonkeyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
//		super.doGet(req, resp);
		
		String responseText = "You are reading this from a servlet!\n";
		ServletOutputStream os = resp.getOutputStream();
		os.print(responseText);
		os.close();
		resp.setContentType("text/plain");
		resp.setContentLength(responseText.length());
	}

}
