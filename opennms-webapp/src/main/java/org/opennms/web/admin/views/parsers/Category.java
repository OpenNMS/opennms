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
// 2007 Jul 24: Java 5 generics and for loops, make clone() return Category instead of Object. - dj@opennms.org
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
import java.util.List;
import java.util.StringTokenizer;

/**
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
     */
    public Category() {
        m_services = new ArrayList<String>();
    }

    /**
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
     */
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
     * @deprecated
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
     */
    public void setLabel(String aValue) {
        m_label = aValue;
    }

    /**
     */
    public String getLabel() {
        return m_label;
    }

    /**
     */
    public void setRule(String aValue) {
        m_rule = aValue;
    }

    /**
     */
    public String getRule() {
        return m_rule;
    }

    /**
     */
    public void setNormal(String aValue) {
        m_normal = aValue;
    }

    /**
     */
    public String getNormal() {
        return m_normal;
    }

    /**
     */
    public void setWarning(String aValue) {
        m_warning = aValue;
    }

    /**
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
     */
    public void setComments(String comment) {
        m_comments = comment;
    }

    /**
     */
    public String getComments() {
        return m_comments;
    }

    /**
     * @deprecated
     */
    public String getUserManagerFormat() {
        return m_label + "$" + m_normal + "$" + m_warning + "$" + m_rule + "#";
    }
}
