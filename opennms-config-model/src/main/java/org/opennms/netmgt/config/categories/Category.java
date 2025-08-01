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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "category")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("categories.xsd")
public class Category implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The category label. NOTE: category labels will need
     *  to be unique across category groups.
     */
    @XmlElement(name = "label", required = true)
    private String m_label;

    /**
     * A comment describing the category.
     */
    @XmlElement(name = "comment")
    private String m_comment;

    /**
     * The normal threshold value for the category in
     *  percent. The UI displays the category in green if the overall
     *  availability for the category is equal to or greater than this
     *  value.
     */
    @XmlElement(name = "normal", required = true)
    private Double m_normalThreshold;

    /**
     * The warning threshold value for the category in
     *  percent. The UI displays the category in yellow if the overall
     *  availability for the category is equal to or greater than this
     *  value but less than the normal threshold. If availability is less
     *  than this value, category is displayed in red.
     */
    @XmlElement(name = "warning", required = true)
    private Double m_warningThreshold;

    /**
     * A service relevant to this category. For a
     *  nodeid/ip/service tuple to be added to a category, it will need to
     *  pass the rule(categorygroup rule & category rule) and the
     *  service will need to be in the category service list. If there are
     *  no services defined, all tuples that pass the rule are added to
     *  the category.
     */
    @XmlElement(name = "service")
    private List<String> m_services = new ArrayList<>();

    /**
     * The category rule.
     */
    @XmlElement(name = "rule", required = true)
    private String m_rule;

    public Category() {
    }

    public Category(final String label, final String comment, final Double normalThreshold, final Double warningThreshold, final String rule, final String... services) {
        setLabel(label);
        setComment(comment);
        setNormalThreshold(normalThreshold);
        setWarningThreshold(warningThreshold);
        setRule(rule);

        m_services.addAll(Arrays.asList(services));
    }

    public String getLabel() {
        return m_label;
    }

    public void setLabel(final String label) {
        m_label = ConfigUtils.assertNotEmpty(label, "label");
    }

    public Optional<String> getComment() {
        return Optional.ofNullable(m_comment);
    }

    public void setComment(final String comment) {
        m_comment = ConfigUtils.normalizeString(comment);
    }

    public Double getNormalThreshold() {
        return m_normalThreshold;
    }

    public void setNormalThreshold(final Double normal) {
        m_normalThreshold = ConfigUtils.assertNotNull(normal, "normal-threshold");
    }

    public Double getWarningThreshold() {
        return m_warningThreshold;
    }

    public void setWarningThreshold(final Double warning) {
        m_warningThreshold = ConfigUtils.assertNotNull(warning, "warning-threshold");
    }

    public List<String> getServices() {
        return m_services;
    }

    public void setServices(final List<String> services) {
        if (services == m_services) return;
        m_services.clear();
        if (services != null) m_services.addAll(services);
    }

    public void addService(final String service) {
        m_services.add(service);
    }

    /**
     */
    public void clearServices() {
        m_services.clear();
    }

    public String getRule() {
        return m_rule;
    }

    public void setRule(final String rule) {
        m_rule = ConfigUtils.assertNotEmpty(rule, "rule");
    }

    public boolean isValid() {
        return Objects.nonNull(m_label) && Objects.nonNull(m_normalThreshold) && Objects.nonNull(m_warningThreshold);
    }

    public int hashCode() {
        return Objects.hash(
                            m_label, 
                            m_comment, 
                            m_normalThreshold, 
                            m_warningThreshold, 
                            m_services, 
                            m_rule);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Category) {
            final Category temp = (Category)obj;
            return Objects.equals(temp.m_label, m_label)
                    && Objects.equals(temp.m_comment, m_comment)
                    && Objects.equals(temp.m_normalThreshold, m_normalThreshold)
                    && Objects.equals(temp.m_warningThreshold, m_warningThreshold)
                    && Objects.equals(temp.m_services, m_services)
                    && Objects.equals(temp.m_rule, m_rule);
        }
        return false;
    }

}
