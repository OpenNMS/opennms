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
package org.opennms.web.admin.groups;

import java.io.IOException;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.config.users.DutySchedule;
import org.opennms.netmgt.config.groups.Group;

/**
 * A servlet that handles adding new duty schedules to a group
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class AddGroupDutySchedulesServlet extends HttpServlet {
    private static final long serialVersionUID = -4167184420524735471L;

    /** {@inheritDoc} */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession userSession = request.getSession(true);

        Group group = (Group) userSession.getAttribute("group.modifyGroup.jsp");

        Vector<Object> newSchedule = new Vector<>();

        int dutyAddCount = WebSecurityUtils.safeParseInt(request.getParameter("numSchedules"));

        for (int j = 0; j < dutyAddCount; j++) {
            // add 7 false boolean values for each day of the week
            for (int i = 0; i < 7; i++) {
                newSchedule.addElement(Boolean.FALSE);
            }

            // add two strings for the begin and end time
            newSchedule.addElement("0");
            newSchedule.addElement("0");

            group.addDutySchedule((new DutySchedule(newSchedule)).toString());
        }

        // forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/userGroupView/groups/modifyGroup.jsp");
        dispatcher.forward(request, response);
    }
}
