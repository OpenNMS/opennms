//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.web.graph;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.charts.ChartUtils;
/**
 * @author david
 *
 */
public class PurdyChartServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public PurdyChartServlet() {
        super();
    }
    
/*    public void init() {
        try {
            ChartConfigFactory.init();
            DataSourceFactory.init();
        } catch (MarshalException e) {
            log().error("init: Error marshalling chart-configuration.xml: ",e);
        } catch (ValidationException e) {
            log().error("init: Error validating chart-configuration.xml: ",e);
        } catch (IOException e) {
            log().error("init: Error reading chart-configuration.xml: ",e);
        } catch (ClassNotFoundException e) {
            log().error("init: Error initializing database connection factory: ",e);
        }
    }
*/    

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String chartName = request.getParameter("chart-name");
        String buffered = request.getParameter("buffered");
        
        if (chartName == null) {
            log().warn("doGet: request doesn't contain a chart-name parameter.");
            return;
        }
        
        if (buffered == null) {
            buffered = "0";
        }

/*        response.setContentType("text/html");
        PrintWriter pw = response.getWriter();
        pw.println("<html>");
        pw.println("<body>");
        pw.println("<h1>"+chartName+"</h1>");
        pw.close();
*/
        response.setContentType("image/png");
        OutputStream out = response.getOutputStream();
        
        log().debug("doGet: displaying chart: "+chartName);
        
        try {
                ChartUtils.getBarChartPNG(chartName, out);
        } catch (MarshalException e) {
            log().error("Error marshalling chart-configuration.xml: ",e);
        } catch (ValidationException e) {
            log().error("Error validating chart-configuration.xml: ",e);
        } catch (IOException e) {
            log().error("Error reading chart-configuration.xml: ",e);
        } catch (SQLException e) {
            log().error("Error in SQL for chart: "+chartName,e);
        }

        out.flush();
        out.close();
        
/*        pw = response.getWriter();
        response.setContentType("text/html");
        pw.println("</body>");
        pw.println("</html>");
*/        
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance();
    }
    

}
