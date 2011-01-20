//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jul 24: Add serialVersionUID, refactor logging. - dj@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.nodemap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.opennms.core.utils.StreamUtils;
import org.opennms.web.MissingParameterException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.opennms.core.utils.DBUtils;
import org.opennms.netmgt.config.DataSourceFactory;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import net.sf.json.JSONSerializer;

/**
 * <p>MailerServlet class.</p>
 *
 * @author <A HREF="mailto:jacinta@opennms.org">Jacinta Remedios </A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jacinta@opennms.org">Jacinta Remedios </A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jacinta@opennms.org">Jacinta Remedios </A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */

	//select node.nodeid, node.nodelabel, node_geolocation.geolocationlatitude, node_geolocation.geolocationlongitude from node inner join node_geolocation on node_geolocation.nodeid = node.nodeid;

	//select node.nodeid, node.nodelabel, node_geolocation.geolocationlatitude, node_geolocation.geolocationlongitude, outages.outageid, outages.serviceid, service.servicename from ((node inner join node_geolocation on node_geolocation.nodeid = node.nodeid) inner join outages on node.nodeid = outages.nodeid) inner join service on service.serviceid = outages.serviceid;


public class MapNodeServlet extends HttpServlet {

    private Timestamp getNow() {
	Calendar cal = new GregorianCalendar();
	Date end = cal.getTime();

	return new Timestamp(end.getTime());
    }

    private Timestamp getYesterday() {
	Calendar cal = new GregorianCalendar();
	cal.add(Calendar.DATE, -1);
	Date start = cal.getTime();

	return new Timestamp(start.getTime());
    }


    public void init() throws ServletException {
        try {
            DataSourceFactory.init();
        } catch (Exception e) {
        }
    }
    
    private static final String GET_BS_QUERY = "SELECT node.nodeid, node.nodelabel, getManagePercentAvailNodeWindow(node.nodeid, ?, ?), node_geolocation.geolocationlatitude, node_geolocation.geolocationlongitude, node.foreignid FROM node INNER JOIN node_geolocation ON node_geolocation.nodeid = node.nodeid WHERE node.nodeparentid IS NULL ORDER BY node.nodeid";

    private static final String GET_CPE_QUERY = "SELECT node.nodeid, node.nodelabel, getManagePercentAvailNodeWindow(node.nodeid, ?, ?), node_geolocation.geolocationlatitude, node_geolocation.geolocationlongitude, node.nodeparentid, node.foreignid FROM node INNER JOIN node_geolocation ON node_geolocation.nodeid = node.nodeid WHERE node.nodeparentid IS NOT NULL ORDER BY node.nodeparentid";

    private static final String GET_CAT_QUERY = "SELECT node.nodeid, node.nodelabel, getManagePercentAvailNodeWindow(node.nodeid, ?, ?), node_geolocation.geolocationlatitude, node_geolocation.geolocationlongitude, node.nodeparentid, node.foreignid, category_node.categoryid FROM (node INNER JOIN node_geolocation ON node_geolocation.nodeid = node.nodeid) INNER JOIN category_node on category_node.nodeid = node.nodeid WHERE category_node.categoryid = ?";

    private static final String GET_UCAT_QUERY = "SELECT node.nodeid, node.nodelabel, getManagePercentAvailNodeWindow(node.nodeid, ?, ?), node_geolocation.geolocationlatitude, node_geolocation.geolocationlongitude, node.nodeparentid, node.foreignid, category_node.categoryid FROM (node INNER JOIN node_geolocation ON node_geolocation.nodeid = node.nodeid) LEFT JOIN category_node on category_node.nodeid = node.nodeid WHERE category_node.categoryid IS NULL";

    private static final String GET_OUTAGES_QUERY = "SELECT nodeid, eventuei,severity FROM alarms WHERE nodeid IS NOT NULL ORDER BY nodeid, lasteventtime desc";


    //    INDETERMINATE(1, "Indeterminate", "lightblue"),
    //CLEARED(2, "Cleared", "white"),
    //NORMAL(3, "Normal", "green"),
    //WARNING(4, "Warning", "cyan"),
    //MINOR(5, "Minor", "yellow"),
    //MAJOR(6, "Major", "orange"),
    //CRITICAL(7, "Critical", "red");

    private void getStatus(List< HashMap<Integer, NodeResult> > hashes) throws  SQLException { 
	final DBUtils d = new DBUtils();
        Connection connection = null;

        try {
            PreparedStatement stmt;
	    ResultSet results;
	    NodeResult node;

	    connection = DataSourceFactory.getInstance().getConnection();
            d.watch(connection);

	    stmt = connection.prepareStatement(GET_OUTAGES_QUERY);
            d.watch(stmt);

	    results = stmt.executeQuery();
            d.watch(results);

            if (results != null) {
                while (results.next()) {
		    int i;
		    int nodeid = results.getInt(1);
		    String uei = results.getString(2);
		    int severity = results.getInt(3);
		    
		    for (HashMap<Integer, NodeResult> nodes : hashes) {
			node = nodes.get(nodeid);

			if (node != null) {
			    node.setEventUei(uei);
			    node.setSeverity(severity);
			}
			
			
		    }
                }
            }

	} finally {
            d.cleanUp();
        }

    }

    private HashMap<Integer, NodeResult> getCpes() throws  SQLException { 
        final DBUtils d = new DBUtils();
        Connection connection = null;
	HashMap<Integer, NodeResult> cpes = 
	    new HashMap<Integer, NodeResult>();

        try {
            PreparedStatement stmt;
	    ResultSet results;

            connection = DataSourceFactory.getInstance().getConnection();
            d.watch(connection);

	    stmt = connection.prepareStatement(GET_CPE_QUERY);
	    stmt.setTimestamp(1, getNow());
	    stmt.setTimestamp(2, getYesterday());
            d.watch(stmt);

	    results = stmt.executeQuery();
            d.watch(results);

            if (results != null) {
                while (results.next()) {
		    NodeResult cpe = new NodeResult(); 		   

		    cpe.setNodeId(results.getInt(1));
                    cpe.setNodeLabel(results.getString(2));
                    cpe.setAvail(results.getDouble(3));
                    cpe.setGeolocationLat(results.getDouble(4));
                    cpe.setGeolocationLon(results.getDouble(5));
                    cpe.setParentNodeId(results.getInt(6));
                    cpe.setForeignId(results.getString(7));
                    cpe.setType(NodeResult.NODE_TYPE_CPE);
		    
		    cpes.put(cpe.getNodeId(), cpe);
                }
            }
        } finally {
            d.cleanUp();
        }

        return cpes;
    }
   
   
    private HashMap<Integer, NodeResult> getCategory(int category_id) throws  SQLException { 
        final DBUtils d = new DBUtils();
        Connection connection = null;
	HashMap<Integer, NodeResult> nodes = 
	    new HashMap<Integer, NodeResult>();

        try {
            PreparedStatement stmt;
	    ResultSet results;

            connection = DataSourceFactory.getInstance().getConnection();
            d.watch(connection);

	    if (category_id >= 0) {
		stmt = connection.prepareStatement(GET_CAT_QUERY);
	    } else {
		stmt = connection.prepareStatement(GET_UCAT_QUERY);
	    }
	    stmt.setTimestamp(1, getNow());
	    stmt.setTimestamp(2, getYesterday());

	    if (category_id >= 0) {
		stmt.setInt(3, category_id);
	    }
            d.watch(stmt);

	    results = stmt.executeQuery();
            d.watch(results);

            if (results != null) {
                while (results.next()) {
		    NodeResult node = new NodeResult(); 		   

		    node.setNodeId(results.getInt(1));
                    node.setNodeLabel(results.getString(2));
                    node.setAvail(results.getDouble(3));
                    node.setGeolocationLat(results.getDouble(4));
                    node.setGeolocationLon(results.getDouble(5));
                    node.setParentNodeId(results.getInt(6));
                    node.setForeignId(results.getString(7));
                    node.setType(NodeResult.NODE_TYPE_GENERAL);
		    
		    nodes.put(node.getNodeId(), node);
                }
            }
        } finally {
            d.cleanUp();
        }

        return nodes;
    }


   private HashMap<Integer, NodeResult> getBasestations() throws SQLException {
        final DBUtils d = new DBUtils();
        Connection connection = null;
	HashMap<Integer, NodeResult> base_stations = 
	    new HashMap<Integer, NodeResult>();

        try {
	    PreparedStatement stmt;
	    ResultSet results;

            connection = DataSourceFactory.getInstance().getConnection();
            d.watch(connection);

	    stmt = connection.prepareStatement(GET_BS_QUERY);
	    stmt.setTimestamp(1, getNow());
	    stmt.setTimestamp(2, getYesterday());
            d.watch(stmt);

	    results = stmt.executeQuery();
            d.watch(results);

            if (results != null) {
                while (results.next()) {
		    BaseStationResult bs = new BaseStationResult(); 

		    bs.setNodeId(results.getInt(1));
                    bs.setNodeLabel(results.getString(2));
                    bs.setAvail(results.getDouble(3));
                    bs.setGeolocationLat(results.getDouble(4));
                    bs.setGeolocationLon(results.getDouble(5));
                    bs.setForeignId(results.getString(6));
                    bs.setType(NodeResult.NODE_TYPE_BASESTATION);
		    
		    base_stations.put(bs.getNodeId(), bs);
                }
            }
        } finally {
            d.cleanUp();
        }

       
        return base_stations;
    }

    private String getFromCat(int category_id) throws SQLException {
	String out = "";

	HashMap<Integer, NodeResult> nodes = getCategory(category_id);
	List< HashMap<Integer, NodeResult> > hashes = new ArrayList< HashMap<Integer, NodeResult> >();

	hashes.add(nodes);

	getStatus(hashes);
	
	out += "[";
	for (NodeResult node : nodes.values()) {
	    out += node.toJson() + ",\n";
	}
	out += "]";

	return out;
    }

    private String getAll() throws SQLException {
	String out = "";

	HashMap<Integer, NodeResult> bsh = getBasestations();
	HashMap<Integer, NodeResult> cpes = getCpes();

	List< HashMap<Integer, NodeResult> > hashes = new ArrayList< HashMap<Integer, NodeResult> >();

	hashes.add(bsh);
	hashes.add(cpes);

	getStatus(hashes);
	
	out += "/*";
	out += "cpes: " + cpes.values().size() + "\n";
	for (NodeResult cpe : cpes.values()) {
	    BaseStationResult bs = (BaseStationResult)bsh.get(cpe.getParentNodeId());

	    out += "cpe - " + cpe.getNodeId() + " bs " + bs + "\n"; 

	    if (bs != null) {
		bs.addCpe(cpe);
	    }
	}
	out += "*/";	

	out += "[";
	for (NodeResult bs : bsh.values()) {
	    out += bs.toJson() + ",\n";
	}
	out += "]";

	return out;
    }

    private String getResponse(String category) throws SQLException {
	if (category != null)  {
	    if (category.equals("ALL")) {
		return getAll();
	    } else if (category.equals("UCAT")) {
		return getFromCat(-1);
	    } else {
		int cat;

		try {
		    cat = Integer.parseInt(category);
		    return getFromCat(cat);
		} catch (NumberFormatException e) {
		    return getAll();		   
		}
	    }
	} else {
	    return getAll();
	}
	
    }


    protected void doGet(HttpServletRequest req,
                         HttpServletResponse res)
        throws ServletException, IOException
    {
	String out_str;

	String category = req.getParameter("cat");

        PrintWriter out = res.getWriter();

	try {
	    String obj_str = getResponse(category);
	    res.setContentType("text/plain");
	    out_str = obj_str;	   
	} catch (Exception e) {
	    final StackTraceElement[] stackTrace = e.getStackTrace();
	    String st = "";
	    int index = 0;
	    for (StackTraceElement element : stackTrace) {
		st += "Exception thrown from " + element.getMethodName() + 
		    " in class " + element.getClassName() + 
		    " [on line number " + element.getLineNumber() + 
		    " of file " + element.getFileName() + "]<br/>";
	    }

	    res.setContentType("text/html");
	    out_str = "<HTML><HEAD><TITLE>Unexpected Error</TITLE>"+
		"</HEAD><BODY>"+ e + "<br/>"+ st  + "</BODY></HTML>";
	}


        out.println(out_str);
        out.close();
    }
    
    public String getServletInfo()
    {
        return "HelloClientServlet 1.0 by Stefan Zeiger";
    }
}
