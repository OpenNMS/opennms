/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 * 
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * 
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.categories;


import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

@XmlRootElement(name = "common")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("categories.xsd")
public class Common implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * A common rule that will be applied to all
     *  categories in this group in addition to the category's
     *  rule.
     */
    @XmlElement(name = "rule", required = true)
    private String m_rule;

    public Common() {
    }

    public Common(final String rule) {
        m_rule = rule;
    }

    public String getRule() {
        return m_rule;
    }

    public void setRule(final String rule) {
        if (rule == null) {
            throw new IllegalArgumentException("rule is a required field!");
        }
        m_rule = rule;
    }

    @Override
    public int hashCode() {
        int hash = Objects.hash(m_rule);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Common) {
            final Common temp = (Common)obj;
            return Objects.equals(temp.m_rule, m_rule);
        }
        return false;
    }

}
