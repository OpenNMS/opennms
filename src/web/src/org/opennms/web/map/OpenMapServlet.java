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
import org.opennms.web.map.view.*;

import java.text.SimpleDateFormat;
/**
 * @author mmigliore
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class OpenMapServlet extends HttpServlet
{
	private static final String LOG4J_CATEGORY = "OpenNMS.Map";
	Category log;
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        log = ThreadCategory.getInstance(this.getClass());
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
        
      int mapId = Integer.parseInt(request.getParameter("MapId"));
      log.info("Fetching Map with id="+mapId);
      String strToSend="openMapOK"; 
      try{
      	SimpleDateFormat formatter = new SimpleDateFormat("HH.mm.ss dd/MM/yy");		
      	
      	Manager m = new Manager();
      	VMap map = m.getMap(mapId);
      	String createTime = "";
      	if(map.getCreateTime()!=null)
      		createTime =formatter.format(map.getCreateTime());
      	String lastModTime = "";
      	if(map.getLastModifiedTime()!=null)
      		lastModTime =formatter.format(map.getLastModifiedTime());
      	strToSend+=map.getId()+"@@"+map.getBackground()+"@@"+map.getAccessMode()+"@@"+map.getName()+"@@"+map.getOwner()+"@@"+map.getUserLastModifies()+"@@"+createTime+"@@"+lastModTime;
      	VElement[] elems = map.getAllElements();
      	if(elems!=null){
	      	for(int i=0;i<elems.length;i++){
	      		strToSend+="**"+elems[i].getId()+"@@"+elems[i].getX()+"@@"+elems[i].getY()+"@@"+elems[i].getIcon();
	      	}
	    }
      }catch(Exception e){
      	log.error("Map open error: "+e);
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
    
  
}
