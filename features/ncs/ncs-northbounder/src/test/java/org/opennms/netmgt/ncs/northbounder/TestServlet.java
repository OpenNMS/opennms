package org.opennms.netmgt.ncs.northbounder;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

@SuppressWarnings("serial")
public class TestServlet extends HttpServlet {
	
	private static String m_posted = null;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)	throws ServletException, IOException {
		m_posted = IOUtils.toString(req.getReader());
	}

	public static void reset() {
		m_posted = null;
	}
	
	public static String getPosted() {
		return m_posted;
	}

	
}