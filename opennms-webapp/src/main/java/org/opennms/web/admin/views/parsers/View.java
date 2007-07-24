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
// 2007 Jul 24: Java 5 generics and for loops. - dj@opennms.org
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

package org.opennms.web.admin.views.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class View implements Cloneable {
    /**
     */
    private String m_viewName;

    /**
     */
    private String m_viewTitle;

    /**
     */
    private String m_viewComments;

    /**
     */
    private String m_commonRule;

    /**
     */
    private List<String> m_userMembers;

    /**
     */
    private List<String> m_groupMembers;

    /**
     */
    private List<Category> m_categories;

    /**
     */
    public View() {
        m_userMembers = new ArrayList<String>();
        m_groupMembers = new ArrayList<String>();
        m_categories = new ArrayList<Category>();
    }

    /**
     */
    public Object clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }

        View newView = new View();

        newView.setViewName(m_viewName);
        newView.setViewTitle(m_viewTitle);
        newView.setViewComments(m_viewComments);
        newView.setCommon(m_commonRule);

        for (String userMember : m_userMembers) {
            newView.addUserMember(userMember);
        }

        for (String groupMember : m_groupMembers) {
            newView.addGroupMember(groupMember);
        }

        for (Category category : m_categories) {
            newView.addCategory(category.clone());
        }

        return newView;
    }

    /**
     */
    public void setViewName(String aValue) {
        m_viewName = aValue;
    }

    /**
     */
    public void setViewTitle(String aValue) {
        m_viewTitle = aValue;
    }

    /**
     */
    public void setViewComments(String aValue) {
        m_viewComments = aValue;
    }

    /**
     * This method sets the common rule for the view
     * 
     * @param common
     *            the common rule.
     */
    public void setCommon(String common) {
        m_commonRule = common;
    }

    /**
     * This method returns the common rule for the view
     * 
     * @return the common rule.
     */
    public String getCommon() {
        return m_commonRule;
    }

    /**
     */
    public void addCategory(Category aCategory) {
        m_categories.add(aCategory);
    }

    /**
     */
    public String getViewName() {
        return m_viewName;
    }

    /**
     */
    public String getViewTitle() {
        return m_viewTitle;
    }

    /**
     */
    public String getViewComments() {
        return m_viewComments;
    }

    /**
     */
    public List<String> getUserMembers() {
        return m_userMembers;
    }

    /**
     */
    public void setUserMembers(List<String> users) {
        m_userMembers = users;
    }

    /**
     */
    public void addUserMember(String aMember) {
        m_userMembers.add(aMember);
    }

    /**
     */
    public void removeUserMember(String aMember) {
        m_userMembers.remove(aMember);
    }

    /**
     */
    public void clearUserMembers() {
        m_userMembers.clear();
    }

    /**
     */
    public List<String> getGroupMembers() {
        return m_groupMembers;
    }

    /**
     */
    public void setGroupMembers(List<String> groups) {
        m_groupMembers = groups;
    }

    /**
     */
    public List<String> getMembers() {
        List<String> all = new ArrayList<String>();

        all.addAll(m_userMembers);
        all.addAll(m_groupMembers);

        return all;
    }

    /**
     */
    public void addGroupMember(String aMember) {
        m_groupMembers.add(aMember);
    }

    /**
     */
    public void removeGroupMember(String aMember) {
        m_groupMembers.remove(aMember);
    }

    /**
     */
    public void clearGroupMembers() {
        m_groupMembers.clear();
    }

    /**
     */
    public boolean hasUserMember(String name) {
        return m_userMembers.contains(name);
    }

    /**
     */
    public boolean hasGroupMember(String name) {
        return m_groupMembers.contains(name);
    }

    /**
     */
    public List<Category> getCategories() {
        return m_categories;
    }

    /**
     */
    public Map<String, Category> getCategoriesMap() {
        Map<String, Category> categoriesMap = new HashMap<String, Category>();

        for (Category category : m_categories) {
            categoriesMap.put(category.getLabel(), category);
        }

        return categoriesMap;
    }

    /**
     */
    public void setCategories(List<Category> categories) {
        m_categories = categories;
    }

    /**
     */
    public void setCategories(Map<String, Category> categories) {
        m_categories.clear();

        for (Category category : categories.values()) {
            m_categories.add(category);
        }
    }

    /**
     */
    public int getCategoryCount() {
        return m_categories.size();
    }
}
