/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.controller.admin.group;

import java.io.IOException;
import java.text.ChoiceFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.users.DutySchedule;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.OnmsMapDao;
import org.opennms.netmgt.model.OnmsMap;
import org.opennms.web.group.WebGroup;
import org.opennms.web.group.WebGroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * GroupController
 *
 * @author brozow
 * @version $Id: $
 * @since 1.8.1
 */
public class GroupController extends AbstractController implements InitializingBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(GroupController.class);


    @Autowired
    private OnmsMapDao m_onmsMapDao;
    
    @Autowired
    private CategoryDao m_categoryDao;
    
    @Autowired
    private UserManager m_userManager;
    
    @Autowired
    private WebGroupRepository m_groupRepository;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,  HttpServletResponse response) throws Exception {
        String operation = request.getParameter("operation");
        
        if (!StringUtils.hasText(operation) && StringUtils.hasText(request.getParameter("groupName"))) {
            operation = "show";
        }
        
        
        LOG.debug("!!!! Calling operation {} in GroupController", operation);
        
        if ("create".equalsIgnoreCase(operation)){
            return createGroup(request, response);
        } else if ("addGroup".equalsIgnoreCase(operation)){
            return addGroup(request, response);
        } else if ("show".equalsIgnoreCase(operation)){
            return showGroup(request, response);
        } else if ("delete".equalsIgnoreCase(operation)){
            return deleteGroup(request, response);
        } else if ("edit".equalsIgnoreCase(operation)){
            return editGroup(request, response);
        } else if ("rename".equalsIgnoreCase(operation)){
            return renameGroup(request, response);
        } else if ("addDutySchedules".equalsIgnoreCase(operation)){
            return addDutySchedules(request, response);
        } else if ("removeDutySchedules".equalsIgnoreCase(operation)){
            return removeDutySchedules(request, response);
        } else if ("save".equalsIgnoreCase(operation)) {
            return saveGroup(request, response);
        } else if ("cancel".equalsIgnoreCase(operation)){
            return cancel(request, response);
        } else {
            return listGroups(request, response);
        }
    }
    
    private ModelAndView listGroups(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView("redirect:/admin/userGroupView/groups/list.htm");
    }

    private ModelAndView showGroup(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        String groupName = request.getParameter("groupName");
        if (!StringUtils.hasText(groupName)) {
            throw new ServletException("The groupName parameter is required");
        }
        
        WebGroup group = m_groupRepository.getGroup(groupName);

        
        return new ModelAndView("/admin/userGroupView/groups/groupDetail", "group", group);
    }

    private ModelAndView deleteGroup(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        String groupName = request.getParameter("groupName");
        
        if (StringUtils.hasText(groupName)) {
            m_groupRepository.deleteGroup(groupName);
        } 
        
        return listGroups(request, response);
    }

    private ModelAndView renameGroup(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        String oldName = request.getParameter("groupName");
        String newName = request.getParameter("newName");
        
        if (StringUtils.hasText(oldName) && StringUtils.hasText(newName)) {
            m_groupRepository.renameGroup(oldName, newName);
        }
        
        return listGroups(request, response);
    }

    private ModelAndView addDutySchedules(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession userSession = request.getSession(true);

        if (userSession != null) {
            //group.modifyGroup.jsp
            WebGroup group = (WebGroup) userSession.getAttribute("group.modifyGroup.jsp");

            updateGroup(request, group);
            
            Vector<Object> newSchedule = new Vector<Object>();

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


            userSession.setAttribute("group.modifyGroup.jsp", group);

        }

        return new ModelAndView("admin/userGroupView/groups/modifyGroup");

    }

    private ModelAndView removeDutySchedules(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession userSession = request.getSession(true);

        if (userSession != null) {
            //group.modifyGroup.jsp
            WebGroup newGroup = (WebGroup) userSession.getAttribute("group.modifyGroup.jsp");

            updateGroup(request, newGroup);

            userSession.setAttribute("group.modifyGroup.jsp", newGroup);

        }

        return new ModelAndView("admin/userGroupView/groups/modifyGroup");

    }

    private ModelAndView editGroup(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String groupName = request.getParameter("groupName");
        WebGroup group = m_groupRepository.getGroup(groupName);
        return editGroup(request, group);
    }

    private ModelAndView editGroup(HttpServletRequest request, WebGroup group)
            throws IOException, MarshalException, ValidationException {
        HttpSession userSession = request.getSession(true);
        userSession.setAttribute("group.modifyGroup.jsp", group);
        userSession.setAttribute("allCategories.modifyGroup.jsp", m_categoryDao.getAllCategoryNames().toArray(new String[0]));
        userSession.setAttribute("allUsers.modifyGroup.jsp", m_userManager.getUserNames().toArray(new String[0]));
        userSession.setAttribute("allVisibleMaps.modifyGroup.jsp", getVisibleMapsName(group).toArray(new String[0]));
            
        return new ModelAndView("admin/userGroupView/groups/modifyGroup");
    }
    
    private Collection<String> getVisibleMapsName(WebGroup group) {
      
        Collection<OnmsMap> maps = m_onmsMapDao.findVisibleMapsByGroup(group.getName());
        Collection<String> mapnames = new ArrayList<String>(maps.size());
        for (OnmsMap map: maps) {
            mapnames.add(map.getName());
        }
        return mapnames;
    }

    private ModelAndView saveGroup(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        HttpSession session = request.getSession(false);

        if (session != null) {
            WebGroup newGroup = (WebGroup) session.getAttribute("group.modifyGroup.jsp");
            updateGroup(request, newGroup);
            m_groupRepository.saveGroup(newGroup);

        }
        
        return cancel(request, response);
    }

    private ModelAndView cancel(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.removeAttribute("group.modifyGroup.jsp");
            session.removeAttribute("allCategories.modifyGroup.jsp");
            session.removeAttribute("allUsers.modifyGroup.jsp");
        }

        return listGroups(request, response);

    }


    private void updateGroup(HttpServletRequest request, WebGroup newGroup) {
        // get the rest of the group information from the form
        String defaultMap = request.getParameter("groupDefaultMap");
        if (!defaultMap.equals(""))
            newGroup.setDefaultMap(defaultMap);
        
        String users[] = request.getParameterValues("selectedUsers");
        
        List<String> userList = users == null ? Collections.<String>emptyList() : Arrays.asList(users);
        
        newGroup.setUsers(new ArrayList<String>(userList));

        String[] selectedCategories = request.getParameterValues("selectedCategories");
        
        List<String> categoryList = selectedCategories == null ? Collections.<String>emptyList() : Arrays.asList(selectedCategories);
        
        newGroup.setAuthorizedCategories(new ArrayList<String>(categoryList));
        
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
                        newSchedule.addElement(Boolean.TRUE);
                    } else {
                        newSchedule.addElement(Boolean.FALSE);
                    }
                }

                newSchedule.addElement(request.getParameter("duty" + duties + "Begin"));
                newSchedule.addElement(request.getParameter("duty" + duties + "End"));

                DutySchedule newDuty = new DutySchedule(newSchedule);
                dutySchedules.add(newDuty.toString());
            }
        }
    }
    
    private ModelAndView createGroup(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView("admin/userGroupView/groups/newGroup");
    }
    
    private ModelAndView addGroup(HttpServletRequest request, HttpServletResponse response) throws Exception {    
                
        String groupName = request.getParameter("groupName");
        String groupComment = request.getParameter("groupComment");
        if (groupComment == null) {
            groupComment = "";
        }

        boolean hasGroup = false;
        try {
            hasGroup = m_groupRepository.groupExists(groupName);
        } catch (Throwable e) {
            throw new ServletException("Can't determine if group " + groupName + " already exists in groups.xml.", e);
        }
        
        if (hasGroup) {
            return new ModelAndView("admin/userGroupView/groups/newGroup", "action", "redo");            
        } else {
            WebGroup newGroup = new WebGroup();
            newGroup.setName(groupName);
            newGroup.setComments(groupComment);
            
            return editGroup(request, newGroup);
        }
    }



}
