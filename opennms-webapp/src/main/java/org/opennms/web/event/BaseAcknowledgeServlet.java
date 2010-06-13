package org.opennms.web.event;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

public abstract class BaseAcknowledgeServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4059726823978789453L;
	public static final String ACKNOWLEDGE_ACTION = "1";
	public static final String UNACKNOWLEDGE_ACTION = "2";
	/** The URL to redirect the client to in case of success. */
	protected String redirectSuccess;

	public BaseAcknowledgeServlet() {
		super();
	}

	/**
	 * Looks up the <code>dispath.success</code> parameter in the servlet's
	 * config. If not present, this servlet will throw an exception so it will
	 * be marked unavailable.
	 */
	public void init() throws ServletException {
	    ServletConfig config = this.getServletConfig();
	
	    this.redirectSuccess = config.getInitParameter("redirect.success");
	
	    if (this.redirectSuccess == null) {
	        throw new UnavailableException("Require a redirect.success init parameter.");
	    }
	}

	/**
	 * Convenience method for dynamically creating the redirect URL if
	 * necessary.
	 */
	protected String getRedirectString(HttpServletRequest request) {
	    String redirectValue = request.getParameter("redirect");
	
	    if (redirectValue != null) {
	        return (redirectValue);
	    }
	
	    redirectValue = this.redirectSuccess;
	    String redirectParms = request.getParameter("redirectParms");
	
	    if (redirectParms != null) {
	        StringBuffer buffer = new StringBuffer(this.redirectSuccess);
	        buffer.append("?");
	        buffer.append(redirectParms);
	        redirectValue = buffer.toString();
	    }
	
	    return (redirectValue);
	}

}