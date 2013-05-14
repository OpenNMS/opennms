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
import java.util.List;
import java.util.StringTokenizer;

/**
 * <p>Category class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class Category implements Cloneable {
    /**
     */
    private String m_label;

    /**
     */
    private String m_rule;

    /**
     */
    private String m_normal;

    /**
     */
    private String m_warning;

    /**
     */
    private List<String> m_services;

    /**
     */
    private String m_comments;

    /**
     * <p>Constructor for Category.</p>
     */
    public Category() {
        m_services = new ArrayList<String>();
    }

    /**
     * <p>Constructor for Category.</p>
     *
     * @param aLabel a {@link java.lang.String} object.
     * @param aRule a {@link java.lang.String} object.
     * @param aNormal a {@link java.lang.String} object.
     * @param aWarning a {@link java.lang.String} object.
     */
    public Category(String aLabel, String aRule, String aNormal, String aWarning) {
        m_label = aLabel;
        m_rule = aRule;
        m_normal = aNormal;
        m_warning = aWarning;
        m_comments = "";
        m_services = new ArrayList<String>();
    }

    /**
     * <p>clone</p>
     *
     * @return a {@link org.opennms.web.admin.views.parsers.Category} object.
     */
    @Override
    public Category clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }

        Category newCategory = new Category(m_label, m_rule, m_normal, m_warning);
        newCategory.setComments(m_comments);

        for (String service : m_services) {
            newCategory.addService(service);
        }

        return newCategory;
    }

    /**
     * This constructor creates a Category by parsing a string in the
     * UserManager format
     *
     * @param aDataString
     *            a string in the format "label$normal$warning$rule"
     */
    public Category(String aDataString) {
        // each parameter is stored in the string delimited by the $ symbol
        StringTokenizer tokens = new StringTokenizer(aDataString, "$");

        while (tokens.hasMoreTokens()) {
            // add a new threshold panel with the Tab name, the high value, the
            // low value and the
            // rule in that order, parsed from the tokenizer
            m_label = tokens.nextToken();

            // this is a hack because of the dumb ass way this data is being
            // kept
            String normal = tokens.nextToken();
            try {
                // this is a check to see if the UserManager has put a stupid
                // string
                // default value for this category. This would take the form
                // "Common$Type your rule here...#" instead of
                // "Common$95.5$90$isHTTP", so if the second value parsed is not
                // a float, then don't bother trying to extract the rest
                Float.valueOf(normal);

                m_normal = (normal);
                m_warning = (tokens.nextToken());
                m_rule = (tokens.nextToken());
            } catch (NumberFormatException e) {
                // do nothing with this exception, if a token doesn't exist for
                // any of the
                // parameters the default value is already set.
            }
        }
    }

    /**
     * <p>setLabel</p>
     *
     * @param aValue a {@link java.lang.String} object.
     */
    public void setLabel(String aValue) {
        m_label = aValue;
    }

    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLabel() {
        return m_label;
    }

    /**
     * <p>setRule</p>
     *
     * @param aValue a {@link java.lang.String} object.
     */
    public void setRule(String aValue) {
        m_rule = aValue;
    }

    /**
     * <p>getRule</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRule() {
        return m_rule;
    }

    /**
     * <p>setNormal</p>
     *
     * @param aValue a {@link java.lang.String} object.
     */
    public void setNormal(String aValue) {
        m_normal = aValue;
    }

    /**
     * <p>getNormal</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNormal() {
        return m_normal;
    }

    /**
     * <p>setWarning</p>
     *
     * @param aValue a {@link java.lang.String} object.
     */
    public void setWarning(String aValue) {
        m_warning = aValue;
    }

    /**
     * <p>getWarning</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getWarning() {
        return m_warning;
    }

    /**
     * Adds a service to this category
     *
     * @param name
     *            the service name.
     */
    public void addService(String name) {
        m_services.add(name);
    }

    /**
     * Returns the list of services in this category
     *
     * @return the list of services.
     */
    public List<String> getServices() {
        return m_services;
    }

    /**
     * Sets the list of services
     *
     * @param services
     *            a list of service names
     */
    public void setServices(List<String> services) {
        m_services = services;
    }

    /**
     * <p>setComments</p>
     *
     * @param comment a {@link java.lang.String} object.
     */
    public void setComments(String comment) {
        m_comments = comment;
    }

    /**
     * <p>getComments</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getComments() {
        return m_comments;
    }

    /**
     * <p>getUserManagerFormat</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUserManagerFormat() {
        return m_label + "$" + m_normal + "$" + m_warning + "$" + m_rule + "#";
    }
}
