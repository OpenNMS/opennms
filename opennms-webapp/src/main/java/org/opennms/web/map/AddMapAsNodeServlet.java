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
import org.opennms.core.resource.Vault;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.map.db.Element;
import org.opennms.web.map.view.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/**
 * @author mmigliore
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AddMapAsNodeServlet extends HttpServlet
{
	private static final String LOG4J_CATEGORY = "OpenNMS.Map";
	Category log;
	private Map maps;
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
      ThreadCategory.setPrefix(LOG4J_CATEGORY);
      log = ThreadCategory.getInstance(this.getClass());
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
        
      int mapId = Integer.parseInt(request.getParameter("MapId"));
      int parentMapId = Integer.parseInt(request.getParameter("parentMapId"));
      
      
      //build a map in wich each entry is [mapparentid, listofchildsids]
      log.info("building parent-childs Map...");
      maps = new HashMap();
      String SQL_PARENT_CHILD_MAP_QUERY = "select elementid,mapid from element where elementtype=?";
      try{
          	Connection connection = Vault.getDbConnection();
          	PreparedStatement ps = connection.prepareStatement(SQL_PARENT_CHILD_MAP_QUERY);
          	ps.setString(1,Element.MAP_TYPE);
          	ResultSet rs = ps.executeQuery();
          	while(rs.next()){
          		Integer parentId = new Integer(rs.getInt("mapid"));
          		Integer childId = new Integer(rs.getInt("elementid"));
          		List childs  = (List)maps.get(parentId);
          		if(childs==null){
          			childs=new ArrayList();
          		}
          		if(!childs.contains(childId)){
          			childs.add(childId);
          		}
          		maps.put(parentId,childs);
          	}
      }catch(SQLException se){
      	   	throw new ServletException(se);
      }
      Iterator it = maps.entrySet().iterator();
      while(it.hasNext()){
      	Map.Entry entry = (Map.Entry)it.next();
      	log.info("parent:"+(Integer)entry.getKey());
      	log.info("childs of "+(Integer)entry.getKey()+":"+(ArrayList)entry.getValue());
      }
      log.info("searching for loops...");
     // List parentList = new ArrayList();
     // preorderVisit(new Integer(parentMapId), parentList);
      
      List childList = new ArrayList();
      preorderVisit(new Integer(mapId), childList);
      for(int i=0; i<childList.size(); i++){
      	preorderVisit((Integer)childList.get(i), childList);
      }
      
      //log.info("parentvisit="+parentList);
      log.info("childvisit = "+childList);
      
      String strToSend="addMapAsNodeOK";
     try{
	    // boolean found = compareVisits(parentList, childList);

     	 if(childList.contains(new Integer(parentMapId))){
	       	log.info("loop found!");
	      	strToSend="LoopFound";
	      }else{
		      log.info("Fetching Map with id="+mapId);
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
		      	strToSend+=map.getId()+"+"+map.getName();
		      	VElement[] elems = map.getAllElements();
		      	if(elems!=null){
			      	for(int i=0;i<elems.length;i++){
			      		strToSend+="&"+elems[i].getId()+"&"+elems[i].getType();
			      	}
			    }
		      }catch(Exception e){
		      	log.error("Map open error: "+e);
		      	throw new ServletException(e);
		      }
	      }
	      bw.write(strToSend);
	      bw.close();
	      log.info("Sending response to the client '"+strToSend+"'");
     }catch(Exception e){
     	throw new ServletException(e);
     }
    }

    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
      doPost(request,response); 	
    }
   
    
    private void preorderVisit(Integer rootElem, List treeElems){
       	List childs = (List)maps.get(rootElem);
       	if(!treeElems.contains(rootElem)){
       		treeElems.add(rootElem);
       	}
       	if(childs!=null){
	    	Iterator it = childs.iterator();
	    	while(it.hasNext()){
	    		Integer child = (Integer)it.next();
	    		if(!treeElems.contains(child)){
	    			treeElems.add(child);
	    		}
	    		preorderVisit(child, treeElems);
	    	}   	    	
       	}
    }
    
    /**
     * compare two visits
     * @param visit1
     * @param visit2
     * @return true if the two visits contains at least one common element  
     */
    private boolean compareVisits(List visit1, List visit2)throws Exception{
    	for(int i=0; i<visit1.size(); i++){
    		for(int j=0; j<visit2.size();j++){
    			if(((Integer)visit1.get(i)).compareTo((Integer)visit2.get(j))==0)
    				return true;
    		}
    	}
    	return false;
    }
    
 /*   private void preorderSearch(Integer currElem, Integer elemToSearch){
    	List childs = (List)maps.get(currElem);
    	Iterator it = childs.iterator();
    	while(it.hasNext()){
    		Integer child = (Integer)it.next();
    		if(child==elemToSearch)
    			found = true;
    		preorderSearch(child, elemToSearch);
    	}
    }
    */
  
}
