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

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.map.MapNotFoundException;
import org.opennms.web.map.db.MapMenu;
import org.opennms.web.map.view.*;

/**
 * @author mmigliore
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LoadMapsServlet extends HttpServlet
{	
	private static final String LOG4J_CATEGORY = "OpenNMS.Map";
	Category log;

    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
      ThreadCategory.setPrefix(LOG4J_CATEGORY);
      log = ThreadCategory.getInstance(this.getClass());
      log.info("Loading maps");
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
      String strToSend="LoadOK";
      try{
      	Manager m = new Manager();
      	MapMenu[] maps = m.getAllMapMenus();
      	if(maps!=null){
	      	//create the string containing the main informations about all maps defined:
	      	// the string will have the form: mapid1,mapname1,mapowner1-mapid2,mapname2,mapowner2...
	      	for(int i=0; i<maps.length; i++){
	      		if(i>0){
	      			strToSend+="&";
	      		}
	      		strToSend+=mapToString(maps[i]);
	      		
	      	}
      	}
      	
      } catch(MapNotFoundException mnf){
      	 log.info("No maps found " + mnf);
      	 // do nothing
      }
      catch(Exception e){
      	log.error("Maps load error: "+e);
      	throw new ServletException(e);
      }
      bw.write(strToSend);
      bw.close();
      log.info("Sending response to the client '"+strToSend+"'");
      
    }

    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
      doPost(request,response); 	
    }
    
    private String mapToString(MapMenu map)throws Exception{
		String strToSend=map.getId()+"+"+map.getName()+"+"+map.getOwner();  
		return strToSend;   	
    }
}
