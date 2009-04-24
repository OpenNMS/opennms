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
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.config.GroupFactory;
import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.users.DutySchedule;
import org.opennms.web.WebSecurityUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * A servlet that handles saving a group
 * 
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class UpdateGroupController extends AbstractController implements InitializingBean{
    private static final long serialVersionUID = 1L;
    
    @SuppressWarnings("unchecked")
    private List<String> getDutySchedulesForGroup(Group group) {
        return (List<String>) group.getDutyScheduleCollection();
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String groupName = request.getParameter("groupName");
        
        GroupFactory.init();
        GroupManager groupFactory = GroupFactory.getInstance();
        Group newGroup = groupFactory.getGroup(groupName);
        
          // get the rest of the group information from the form
          newGroup.removeAllUser();

          String users[] = request.getParameterValues("selectedUsers");

          if (users != null) {
              for (int i = 0; i < users.length; i++) {
                  newGroup.addUser(users[i]);
              }
          }
          
          Vector<Object> newSchedule = new Vector<Object>();
          ChoiceFormat days = new ChoiceFormat("0#Mo|1#Tu|2#We|3#Th|4#Fr|5#Sa|6#Su");

          Collection<String> dutySchedules = getDutySchedulesForGroup(newGroup);
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
          
         
        RedirectView redirect = new RedirectView("saveGroup", true);
        ModelAndView mav = new ModelAndView(redirect);
        mav.addObject("groupName", groupName);
        mav.addObject("group", newGroup);
        mav.addObject("selectedCategories", request.getParameterValues("selectedCategories"));
        return mav;
    }

    public void afterPropertiesSet() throws Exception {
        // TODO Auto-generated method stub
        
    }

}
