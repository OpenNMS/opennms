package org.opennms.web.map;
/*
 * Created on 8-giu-2005
 *
 */

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.map.view.*;

/**
 * @author mmigliore
 *
 * This servlet is called for 
 * deleting a map from Database
 */
public class DeleteMapServlet extends HttpServlet
{

    static final long serialVersionUID = 2006102700;
	
    private static final String LOG4J_CATEGORY = "OpenNMS.Map";
	
    Category log;
    
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
      
    	ThreadCategory.setPrefix(LOG4J_CATEGORY);
      log = ThreadCategory.getInstance(this.getClass());
      
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));

      int mapId = Integer.parseInt(request.getParameter("MapId"));

      if (log.isInfoEnabled())
    	  log.info("Deleting map with id="+mapId);
      try{
      	Manager m = new Manager();
      	m.startSession();
      	m.deleteMap(mapId);
      	m.endSession();
      }catch(Exception e){
      	log.error("Delete map error "+e);
      	throw new ServletException(e);
      }
      if (log.isInfoEnabled())
    	  log.info("Map deleted");	
      
      bw.write("deleteMapOK");
      bw.close();
    }

    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
      doPost(request,response); 	
    }
    

}
