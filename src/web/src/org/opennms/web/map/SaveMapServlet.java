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
import java.util.*;
/**
 * @author mmigliore
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SaveMapServlet extends HttpServlet
{
	private static final String LOG4J_CATEGORY = "OpenNMS.Map";
	Category log;
	final int NEW_MAP = 2;
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        log = ThreadCategory.getInstance(this.getClass());
    	
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
      String query=request.getQueryString();
      String queryNodes = request.getParameter("Nodes");
      int mapId = Integer.parseInt(request.getParameter("MapId"));
      String mapName = request.getParameter("MapName");
      String mapBackground = request.getParameter("MapBackground");
      VMap vm=null;
      
      log.info("Saving map "+mapName+" with mapId="+mapId+" the query received is '"+query+"'");
      try{
      	Manager m = new Manager();
      	m.startSession();
      	if(mapId==NEW_MAP){
      		log.info("The map is a new map");
          	vm=m.newMap(mapName,"",request.getRemoteUser(),request.getRemoteUser());
          }else{
          	log.info("Fetching map from database");
          	vm=m.getMap(mapId);
          }
      	log.info("Updating map attributes and nodes");
		vm.removeAllElements(); 
		StringTokenizer st = new StringTokenizer(queryNodes,"**");
		while(st.hasMoreTokens()){
			String nodeToken = st.nextToken();
			StringTokenizer nodeST = new StringTokenizer(nodeToken,"@@");
			int counter=1;
			String icon="";
			int id=0,x=0,y=0;
			while(nodeST.hasMoreTokens()){
				String tmp = nodeST.nextToken();
				if(counter==1)
				{
					id=Integer.parseInt(tmp);
				}
				if(counter==2)
				{
					x=Integer.parseInt(tmp);
				}
				if(counter==3)
				{
					y=Integer.parseInt(tmp);
				}
				if(counter==4)
				{
					icon=tmp;
				}
				counter++;
				VElement ve=null;
				if(id>0){
					ve=m.newNode(id);
				}else{
					ve=m.newSubmap(id);
				}
				ve.setX(x);
				ve.setY(y);
				ve.setIcon(icon);
				vm.addElement(ve);
			}
		}      	
      	vm.setUserLastModifies(request.getRemoteUser());
      	vm.setName(mapName);
      	vm.setBackground(mapBackground);
      	m.save(vm);
      	m.endSession();
      	log.info("Map saved");
      }catch(Exception e){
      	log.error("Map save error: "+e);
      	throw new ServletException(e);
      }
      SimpleDateFormat formatter = new SimpleDateFormat("HH.mm.ss dd/MM/yy");
      String strToSend = "saveMapOK@@"+vm.getId()+"@@"+vm.getBackground()+"@@"+vm.getAccessMode()+"@@"+vm.getName()+"@@"+vm.getOwner()+"@@"+vm.getUserLastModifies()+"@@"+formatter.format(vm.getCreateTime())+"@@"+formatter.format(vm.getLastModifiedTime());
      bw.write(strToSend);
      bw.close();
      log.info("Sending response to the client '"+strToSend+"'");
    }

    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
      doPost(request,response); 	
    }
    

}
