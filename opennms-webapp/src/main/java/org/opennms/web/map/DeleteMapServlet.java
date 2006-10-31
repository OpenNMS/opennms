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
	
    Category log;
    
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
      
    	ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
      log = ThreadCategory.getInstance(this.getClass());
      
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));

      String action = request.getParameter("action");
      
      String strToSend = action+"OK";
      int mapId = Integer.parseInt(request.getParameter("MapId"));

      if (log.isInfoEnabled())
    	  log.info("Deleting map with id="+mapId);
      
      try{
    	if(action.equals(MapsConstants.DELETEMAP_ACTION)){
	      	Manager m = new Manager();
	      	m.startSession();
	      	m.deleteMap(mapId);
	      	m.endSession();
    	}else{
    		strToSend=MapsConstants.DELETEMAP_ACTION+"Failed";
    	}
      }catch(Exception e){
      	log.error("Delete map error "+e);
      	strToSend=MapsConstants.DELETEMAP_ACTION+"Failed";
      }finally{
	      bw.write(strToSend);
	      bw.close();
      } 
    }

    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
      doPost(request,response); 	
    }
    

}
