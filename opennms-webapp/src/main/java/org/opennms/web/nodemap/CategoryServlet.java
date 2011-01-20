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

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.DBUtils;
import org.opennms.netmgt.config.DataSourceFactory;

public class CategoryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;


	public void init() throws ServletException {
        try {
            DataSourceFactory.init();
        } catch (Exception e) {
        }
    }
    
    private static final String GET_CAT_QUERY = "SELECT categoryid, categoryname, categorydescription FROM categories";


   private List<CategoryResult> getCats() throws SQLException {
        final DBUtils d = new DBUtils();
        Connection connection = null;
	List<CategoryResult> categories = new ArrayList<CategoryResult>();

        try {
	    PreparedStatement stmt;
	    ResultSet results;

            connection = DataSourceFactory.getInstance().getConnection();
            d.watch(connection);

	    stmt = connection.prepareStatement(GET_CAT_QUERY);
            d.watch(stmt);

	    results = stmt.executeQuery();
            d.watch(results);

            if (results != null) {
                while (results.next()) {
		    CategoryResult cat = new CategoryResult(); 

		    cat.setId(results.getInt(1));
                    cat.setName(results.getString(2));
                    cat.setDesc(results.getString(3));

		    categories.add(cat);
                }
            }
        } finally {
            d.cleanUp();
        }

       
        return categories;
    }

    private String getResponse() throws SQLException {
	String out = "";
	List<CategoryResult> cats = getCats();

	out += "[";
	for (CategoryResult cat : cats) {
	    out += cat.toJson() + ",\n";
	}
	out += "]";

	return out;
    }


    protected void doGet(HttpServletRequest req,
                         HttpServletResponse res)
        throws ServletException, IOException
    {
	String out_str;


        PrintWriter out = res.getWriter();

	try {
	    String obj_str = getResponse();
	    res.setContentType("text/plain");
	    out_str = obj_str;	   
	} catch (Exception e) {
	    final StackTraceElement[] stackTrace = e.getStackTrace();
	    String st = "";
	    for (StackTraceElement element : stackTrace) {
		st += "Exception thrown from " + element.getMethodName() + 
		    " in class " + element.getClassName() + 
		    " [on line number " + element.getLineNumber() + 
		    " of file " + element.getFileName() + "]<br/>";
	    }

	    res.setContentType("text/html");
	    out_str = "<HTML><HEAD><TITLE>Hello Client</TITLE>"+
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
