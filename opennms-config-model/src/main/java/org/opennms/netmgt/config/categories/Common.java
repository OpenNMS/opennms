/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
