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
package org.opennms.web.admin.notification;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.WebSecurityUtils;

/**
 * A servlet that handles updating the ifservices table with the notice status
 *
 * @author <a href="mailto:jason@opennms.org">Jason Johns</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 */
public class ServiceNoticeUpdateServlet extends HttpServlet {
    private static final long serialVersionUID = -5205846787997118203L;
    private static final String UPDATE_SERVICE = "UPDATE ifservices SET notify = ? WHERE nodeID = ? AND ipaddr = ? AND serviceid = ?";

    /** {@inheritDoc} */
    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final HttpSession userSession = request.getSession(false);
        final Map<String, String> servicesCheckedMap = getServicesChecked(userSession);

        final String[] checkedServices = request.getParameterValues("serviceCheck");
        if (checkedServices != null) {
            for (final String checkedService : checkedServices) {
                servicesCheckedMap.put(checkedService, "Y");
            }
        }

        for (final Map.Entry<String,String> entry : servicesCheckedMap.entrySet()) {
            final String key = entry.getKey();

            // decompose the key into node ID, IP address and service ID
            final StringTokenizer tokenizer = new StringTokenizer(key, ",");
            final int nodeID = WebSecurityUtils.safeParseInt(tokenizer.nextToken());
            final String ipAddress = tokenizer.nextToken();
            final int serviceID = WebSecurityUtils.safeParseInt(tokenizer.nextToken());

            updateService(nodeID, ipAddress, serviceID, servicesCheckedMap.get(key));
        }

        response.sendRedirect("index.jsp");
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getServicesChecked(final HttpSession userSession) {
        return (Map<String, String>)userSession.getAttribute("service.notify.map");
    }

    /**
     */
    private void updateService(final int nodeID, final String interfaceIP, final int serviceID, final String notifyFlag) throws ServletException {
        Connection connection = null;

        final DBUtils d = new DBUtils(getClass());
        try {
            connection = DataSourceFactory.getInstance().getConnection();
            d.watch(connection);

            final PreparedStatement stmt = connection.prepareStatement(UPDATE_SERVICE);
            d.watch(stmt);
            stmt.setString(1, notifyFlag);
            stmt.setInt(2, nodeID);
            stmt.setString(3, interfaceIP);
            stmt.setInt(4, serviceID);

            stmt.executeUpdate();

            // close off the db connection
        } catch (final SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (final SQLException sqlEx) {
                throw new ServletException("Couldn't roll back update to service " + serviceID + " on interface " + interfaceIP + " notify as " + notifyFlag + " in the database.", sqlEx);
            }
            throw new ServletException("Error when updating to service " + serviceID + " on interface " + interfaceIP + " notify as " + notifyFlag + " in the database.", e);
        } finally {
            d.cleanUp();
        }
    }
}
