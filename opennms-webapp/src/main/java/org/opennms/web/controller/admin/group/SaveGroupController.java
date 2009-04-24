/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.web.controller.admin.group;

import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.netmgt.config.GroupFactory;
import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

public class SaveGroupController extends AbstractController implements
        InitializingBean {
    
    CategoryDao m_categoryDao;
    
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession user = request.getSession(false);
        System.out.println("\n made it to the saveGroupController \n");
        if (user != null) {
            Group newGroup = (Group) user.getAttribute("group");

            if (newGroup != null) {
                // now save to the xml file
                try {
                    GroupManager groupFactory = GroupFactory.getInstance();
                    groupFactory.saveGroup(newGroup.getName(), newGroup);
                } catch (Exception e) {
                    throw new ServletException("Error saving group " + newGroup.getName(), e);
                }
            }
        }
        
        String groupName = request.getParameter("groupName");
        String groups[]  = request.getParameterValues("selectedGroups");
        if(groups != null){
            updateCategories(groupName, groups);
        }

        // forward the request for proper display
        //RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/userGroupView/groups/list.jsp");
        //dispatcher.forward(request, response);
        RedirectView redirect = new RedirectView("/admin/userGroupView/groups/list.jsp");
        ModelAndView mav = new ModelAndView(redirect);
        return mav;
    }

    public void afterPropertiesSet() throws Exception {
        // TODO Auto-generated method stub

    }
    
    @Transactional
    private void updateCategories(String groupName, String[] categoryNames) {
        
        for(String categoryName: categoryNames){
            OnmsCategory category = m_categoryDao.findByName(categoryName);
            Set<String> authGroups = category.getAuthorizedGroups();
            authGroups.add(groupName);
            
            category.setAuthorizedGroups(authGroups);
            m_categoryDao.update(category);
        }
        
    }
    
    public void setCategoryDao(CategoryDao categoryDao){
        m_categoryDao = categoryDao;
    }
    
    public CategoryDao getCategoryDao(){
        return m_categoryDao;
    }

}
