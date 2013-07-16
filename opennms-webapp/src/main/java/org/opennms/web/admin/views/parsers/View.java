/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.admin.views.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>View class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
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
     * <p>Constructor for View.</p>
     */
    public View() {
        m_userMembers = new ArrayList<String>();
        m_groupMembers = new ArrayList<String>();
        m_categories = new ArrayList<Category>();
    }

    /**
     * <p>clone</p>
     *
     * @return a {@link java.lang.Object} object.
     */
    @Override
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
     * <p>setViewName</p>
     *
     * @param aValue a {@link java.lang.String} object.
     */
    public void setViewName(String aValue) {
        m_viewName = aValue;
    }

    /**
     * <p>setViewTitle</p>
     *
     * @param aValue a {@link java.lang.String} object.
     */
    public void setViewTitle(String aValue) {
        m_viewTitle = aValue;
    }

    /**
     * <p>setViewComments</p>
     *
     * @param aValue a {@link java.lang.String} object.
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
     * <p>addCategory</p>
     *
     * @param aCategory a {@link org.opennms.web.admin.views.parsers.Category} object.
     */
    public void addCategory(Category aCategory) {
        m_categories.add(aCategory);
    }

    /**
     * <p>getViewName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getViewName() {
        return m_viewName;
    }

    /**
     * <p>getViewTitle</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getViewTitle() {
        return m_viewTitle;
    }

    /**
     * <p>getViewComments</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getViewComments() {
        return m_viewComments;
    }

    /**
     * <p>getUserMembers</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getUserMembers() {
        return m_userMembers;
    }

    /**
     * <p>setUserMembers</p>
     *
     * @param users a {@link java.util.List} object.
     */
    public void setUserMembers(List<String> users) {
        m_userMembers = users;
    }

    /**
     * <p>addUserMember</p>
     *
     * @param aMember a {@link java.lang.String} object.
     */
    public void addUserMember(String aMember) {
        m_userMembers.add(aMember);
    }

    /**
     * <p>removeUserMember</p>
     *
     * @param aMember a {@link java.lang.String} object.
     */
    public void removeUserMember(String aMember) {
        m_userMembers.remove(aMember);
    }

    /**
     * <p>clearUserMembers</p>
     */
    public void clearUserMembers() {
        m_userMembers.clear();
    }

    /**
     * <p>getGroupMembers</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getGroupMembers() {
        return m_groupMembers;
    }

    /**
     * <p>setGroupMembers</p>
     *
     * @param groups a {@link java.util.List} object.
     */
    public void setGroupMembers(List<String> groups) {
        m_groupMembers = groups;
    }

    /**
     * <p>getMembers</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getMembers() {
        List<String> all = new ArrayList<String>();

        all.addAll(m_userMembers);
        all.addAll(m_groupMembers);

        return all;
    }

    /**
     * <p>addGroupMember</p>
     *
     * @param aMember a {@link java.lang.String} object.
     */
    public void addGroupMember(String aMember) {
        m_groupMembers.add(aMember);
    }

    /**
     * <p>removeGroupMember</p>
     *
     * @param aMember a {@link java.lang.String} object.
     */
    public void removeGroupMember(String aMember) {
        m_groupMembers.remove(aMember);
    }

    /**
     * <p>clearGroupMembers</p>
     */
    public void clearGroupMembers() {
        m_groupMembers.clear();
    }

    /**
     * <p>hasUserMember</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean hasUserMember(String name) {
        return m_userMembers.contains(name);
    }

    /**
     * <p>hasGroupMember</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean hasGroupMember(String name) {
        return m_groupMembers.contains(name);
    }

    /**
     * <p>getCategories</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Category> getCategories() {
        return m_categories;
    }

    /**
     * <p>getCategoriesMap</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, Category> getCategoriesMap() {
        Map<String, Category> categoriesMap = new HashMap<String, Category>();

        for (Category category : m_categories) {
            categoriesMap.put(category.getLabel(), category);
        }

        return categoriesMap;
    }

    /**
     * <p>setCategories</p>
     *
     * @param categories a {@link java.util.List} object.
     */
    public void setCategories(List<Category> categories) {
        m_categories = categories;
    }

    /**
     * <p>setCategories</p>
     *
     * @param categories a {@link java.util.Map} object.
     */
    public void setCategories(Map<String, Category> categories) {
        m_categories.clear();

        for (Category category : categories.values()) {
            m_categories.add(category);
        }
    }

    /**
     * <p>getCategoryCount</p>
     *
     * @return a int.
     */
    public int getCategoryCount() {
        return m_categories.size();
    }
}
