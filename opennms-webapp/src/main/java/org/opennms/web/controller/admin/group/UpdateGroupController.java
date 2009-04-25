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
// 2007 Aug 03: Change Castor methods clearX -> removeAllX. - dj@opennms.org
// 2007 Jun 24: Add serialVersionUID and use Java 5 generics. - dj@opennms.org
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

package org.opennms.web.controller.admin.group;

import java.text.ChoiceFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.netmgt.config.users.DutySchedule;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.group.WebGroup;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * A servlet that handles saving a group
 * 
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class UpdateGroupController extends AbstractController implements InitializingBean{
    private static final long serialVersionUID = 1L;
    
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession userSession = request.getSession(false);

        if (userSession != null) {
            //group.modifyGroup.jsp
            WebGroup newGroup = (WebGroup) userSession.getAttribute("group.modifyGroup.jsp");

            // get the rest of the group information from the form
            String users[] = request.getParameterValues("selectedUsers");
            
            newGroup.setUsers(new ArrayList<String>(Arrays.asList(users)));

            String[] selectedCategories = request.getParameterValues("selectedCategories");
            
            newGroup.setAuthorizedCategories(new ArrayList<String>(Arrays.asList(selectedCategories)));
            
            Vector<Object> newSchedule = new Vector<Object>();
            ChoiceFormat days = new ChoiceFormat("0#Mo|1#Tu|2#We|3#Th|4#Fr|5#Sa|6#Su");

            Collection<String> dutySchedules = newGroup.getDutySchedules();
            dutySchedules.clear();

            int dutyCount = WebSecurityUtils.safeParseInt(request.getParameter("dutySchedules"));
            for (int duties = 0; duties < dutyCount; duties++) {
                newSchedule.clear();
                String deleteFlag = request.getParameter("deleteDuty" + duties);
                // don't save any duties that were marked for deletion
                if (deleteFlag == null) {
                    for (int i = 0; i < 7; i++) {
                        String curDayFlag = request.getParameter("duty" + duties + days.format(i));
                        if (curDayFlag != null) {
                            newSchedule.addElement(new Boolean(true));
                        } else {
                            newSchedule.addElement(new Boolean(false));
                        }
                    }

                    newSchedule.addElement(request.getParameter("duty" + duties + "Begin"));
                    newSchedule.addElement(request.getParameter("duty" + duties + "End"));

                    DutySchedule newDuty = new DutySchedule(newSchedule);
                    dutySchedules.add(newDuty.toString());
                }
            }

            userSession.setAttribute("group.modifyGroup.jsp", newGroup);

        }

        return new ModelAndView(request.getParameter("redirect"));
    }

    /**
     * @param allCategories
     * @param categoryListInGroup
     * @return
     */
    private String[] removeAll(String[] a,  String[] b) {
        ArrayList<String> list = new ArrayList<String>();
        list.addAll(Arrays.asList(a));
        list.removeAll(Arrays.asList(b));
        return list.toArray(new String[list.size()]);
    }

    public void afterPropertiesSet() throws Exception {
        // TODO Auto-generated method stub
        
    }

}
