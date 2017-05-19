/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.graph;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.charts.ChartUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * <p>PurdyChartServlet class.</p>
 *
 * @author david
 */
public class PurdyChartServlet extends HttpServlet {
	
	private static final Logger LOG = LoggerFactory.getLogger(PurdyChartServlet.class);

    /**
     * 
     */
    private static final long serialVersionUID = 2449309268355063862L;

    /**
     * <p>Constructor for PurdyChartServlet.</p>
     */
    public PurdyChartServlet() {
        super();
    }
    
/*    public void init() {
        try {
            ChartConfigFactory.init();
            DataSourceFactory.init();
        } catch (IOException e) {
            log().error("init: Error reading chart-configuration.xml: ",e);
        } catch (ClassNotFoundException e) {
            log().error("init: Error initializing database connection factory: ",e);
        }
    }
*/    

    /** {@inheritDoc} */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String chartName = request.getParameter("chart-name");
        String buffered = request.getParameter("buffered");
        
        if (chartName == null) {
            LOG.warn("doGet: request doesn't contain a chart-name parameter.");
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
        
        LOG.debug("doGet: displaying chart: {}", chartName);
        
        try {
                ChartUtils.getBarChartPNG(chartName, out);
        } catch (IOException e) {
            LOG.error("Error reading chart-configuration.xml: ",e);
        } catch (SQLException e) {
            LOG.error("Error in SQL for chart: {}", chartName,e);
        }

        out.flush();
        out.close();
        
/*        pw = response.getWriter();
        response.setContentType("text/html");
        pw.println("</body>");
        pw.println("</html>");
*/        
    }

    

}
