package org.opennms.web.map;
/*
 * Created on 8-giu-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;


/**
 * @author mmigliore
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class InitMapsApplicationServlet extends HttpServlet
{	
	private static final String LOG4J_CATEGORY = "OpenNMS.Map";
	Category log;
 
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
    try{
      ThreadCategory.setPrefix(LOG4J_CATEGORY);
      log = ThreadCategory.getInstance(this.getClass());
      log.info("Init maps application");
      HttpSession userSession = request.getSession();
      String refreshTime = (String)userSession.getAttribute("refreshTime");
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
      String strToSend = "InitOK"+refreshTime;
      bw.write(strToSend);
      bw.close();
      log.info("Sending response to the client '"+strToSend+"'");
    }catch(Exception e){
    	log.error("Init maps application: "+e);
    	throw new ServletException(e);   	
    	}  
    }

    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
      doPost(request,response); 	
    }
    
  
}
