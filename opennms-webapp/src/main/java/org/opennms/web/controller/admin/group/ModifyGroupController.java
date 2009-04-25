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
// 2007 Jun 24: Add serialVersionUID. - dj@opennms.org
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.netmgt.config.GroupDao;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * A servlet that handles putting the Group object into the request and
 * forwarding on to a particular jsp
 * 
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class ModifyGroupController extends AbstractController implements InitializingBean {
    private static final long serialVersionUID = 1L;
    
    private GroupDao m_groupDao;
    private CategoryDao m_categoryDao;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession userSession = request.getSession(true);

        String groupName = request.getParameter("groupName");
        Group group = m_groupDao.getGroup(groupName);
        userSession.setAttribute("group.modifyGroup.jsp", group);
        userSession.setAttribute("allCategoriesList", getAllCategoryNames());
        userSession.setAttribute("categoryListNotInGroup", getAllCategoriesMinusInGroup(groupName));
        userSession.setAttribute("categoryListInGroup", getCategoryList(groupName));
            
        ModelAndView mav = new ModelAndView("admin/userGroupView/groups/modifyGroup");
        return mav;
    }
    
    private String[] getAllCategoriesMinusInGroup(String groupName){
        String[] allCategoryNames = getAllCategoryNames();
        
        List<String> unauthorizedCategories = new ArrayList<String>();
        Collections.addAll(unauthorizedCategories, allCategoryNames);
        
        List<String> matchingNames = Arrays.asList(getCategoryList(groupName));
        unauthorizedCategories.removeAll(matchingNames);
        
        return unauthorizedCategories.toArray(new String[0]);
    }

    /**
     * @return
     */
    private String[] getAllCategoryNames() {
        List<String> allCategoryNames = new ArrayList<String>();
        
        List<OnmsCategory> categories = m_categoryDao.findAll();
        
        for(OnmsCategory category : categories){
            allCategoryNames.add(category.getName());
        }
        return allCategoryNames.toArray(new String[0]);
    }
    
    private String[] getCategoryList(String groupName){
        List<String> catList = new ArrayList<String>();
        
        List<OnmsCategory> categories = m_categoryDao.getCategoriesWithAuthorizedGroup(groupName);
        
        for(OnmsCategory category : categories){
            catList.add(category.getName());
        }
        
        return catList.toArray(new String[0]);
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_categoryDao);
        Assert.notNull(m_groupDao);
    }
    
    public void setCategoryDao(CategoryDao categoryDao){
        m_categoryDao = categoryDao;
    }
    
    public void setGroupDao(GroupDao groupDao) {
        m_groupDao = groupDao;
    }
    
    public CategoryDao getCategoryDao(){
        return m_categoryDao;
    }
}
