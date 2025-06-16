/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.graph;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.charts.ChartException;
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
        } catch (ChartException e) {
            LOG.error("Error creating bar chart.",e);
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
