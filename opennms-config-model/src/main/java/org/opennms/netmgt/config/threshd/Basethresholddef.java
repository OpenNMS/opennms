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
package org.opennms.netmgt.config.threshd;



import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "basethresholddef")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("thresholding.xsd")
public abstract class Basethresholddef implements Serializable {
    private static final long serialVersionUID = 2L;

    private static final FilterOperator DEFAULT_FILTER_OPERATOR = FilterOperator.OR;

    /**
     * An optional flag to tell the threshold processor to evaluate the expression
     * even if there are unknown values.
     *  This can be useful when processing expressions with conditionals. Default: false
     */
    @XmlAttribute(name = "relaxed")
    private Boolean m_relaxed;

    /**
     * An optional description for the threshold, to help identify what is their
     * purpose.
     */
    @XmlAttribute(name = "description")
    private String m_description;

    /**
     * Threshold type. "high" to trigger if the value exceeds the threshold,
     *  "low" to trigger if the value drops below the threshold,
     *  "relativeChange" to trigger if the value changes more than the proportion
     * represented by the threshold, or
     *  "absoluteChange" to trigger if the value changes by more than the
     * threshold value
     */
    @XmlAttribute(name = "type", required = true)
    private ThresholdType m_type;

    /**
     * RRD datasource type. "node" indicates a node level datasource.
     *  "if" indicates an interface level datasource.
     */
    @XmlAttribute(name = "ds-type", required = true)
    private String m_dsType;

    /**
     * Threshold value. If the datasource value rises above this
     *  value, in the case of a "high" threshold, or drops below this
     *  value, in the case of a "low" threshold the threshold is
     *  considered to have been exceeded and the exceeded count will
     *  be incremented. Any time that the datasource value drops below
     *  this value, in the case of a "high" threshold, or rises above
     *  this value, in the case of a "low" threshold the exceeded
     *  count is reset back to zero. Whenever the exceeded count
     *  reaches the trigger value then a threshold event is generated.
     */
    @XmlAttribute(name = "value", required = true)
    private String m_value;

    /**
     * Rearm value. Identifies the value that the datasource must
     *  fall below, in the case of a "high" threshold or rise above,
     *  in the case of a "low" threshold, before the threshold will
     *  rearm, and once again be eligible to generate an event.
     */
    @XmlAttribute(name = "rearm", required = true)
    private String m_rearm;

    /**
     * Trigger value. Identifies the number of consecutive polls that
     *  the datasource value must exceed the defined threshold value
     *  before a threshold event is generated.
     */
    @XmlAttribute(name = "trigger", required = true)
    private String m_trigger;

    /**
     * Value to retrieve from strings.properties to label this
     *  datasource.
     */
    @XmlAttribute(name = "ds-label")
    private String m_dsLabel;

     /**
     * An optional, human-readable label for an expression
     */
    @XmlAttribute(name = "expr-label")
    private String m_exprLabel;

    /**
     * The UEI to send when this threshold is triggered. If not
     *  specified, defaults to standard threshold UEIs
     */
    @XmlAttribute(name = "triggeredUEI")
    private String m_triggeredUEI;

    /**
     * The UEI to send when this threshold is re-armed. If not
     *  specified, defaults to standard threshold UEIs
     */
    @XmlAttribute(name = "rearmedUEI")
    private String m_rearmedUEI;

    /**
     * The operator to be used when applying filters. The
     *  default is "or". If you want to match all filters,
     *  you should specify "and";
     */
    @XmlAttribute(name = "filterOperator")
    @XmlJavaTypeAdapter(FilterOperatorAdapter.class)
    private FilterOperator m_filterOperator;

    /**
     * The filter used to select the ds by a string
     */
    @XmlElement(name = "resource-filter")
    private List<ResourceFilter> m_resourceFilters = new ArrayList<>();

    public Basethresholddef() { }

    public Boolean getRelaxed() {
        return m_relaxed != null ? m_relaxed : Boolean.FALSE;
    }

    public void setRelaxed(final Boolean relaxed) {
        m_relaxed = relaxed;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(m_description);
    }

    public void setDescription(final String description) {
        m_description = ConfigUtils.normalizeAndTrimString(description);
    }

    public ThresholdType getType() {
        return m_type;
    }

    public void setType(final ThresholdType type) {
        m_type = ConfigUtils.assertNotNull(type, "type");
    }

    public String getDsType() {
        return m_dsType;
    }

    public void setDsType(final String dsType) {
        m_dsType = ConfigUtils.assertNotEmpty(dsType, "ds-type");
    }

    public String getValue() {
        return m_value;
    }

    public void setValue(final String value) {
        m_value = ConfigUtils.assertNotNull(value, "value");
    }

    public String getRearm() {
        return m_rearm;
    }

    public void setRearm(final String rearm) {
        m_rearm = ConfigUtils.assertNotNull(rearm, "rearm");
    }

    public String getTrigger() {
        return m_trigger;
    }

    public void setTrigger(final String trigger) {
        m_trigger = ConfigUtils.assertNotNull(trigger,  "trigger");
    }

    public Optional<String> getDsLabel() {
        return Optional.ofNullable(m_dsLabel);
    }

    public void setDsLabel(final String dsLabel) {
        m_dsLabel = ConfigUtils.normalizeString(dsLabel);
    }

    public Optional<String> getExprLabel() {
        return Optional.ofNullable(m_exprLabel);
    }

    public void setExprLabel(final String exprLabel) {
        m_exprLabel = ConfigUtils.normalizeString(exprLabel);
    }

    public Optional<String> getTriggeredUEI() {
        return Optional.ofNullable(m_triggeredUEI);
    }

    public void setTriggeredUEI(final String triggeredUEI) {
        m_triggeredUEI = ConfigUtils.normalizeString(triggeredUEI);
    }

    public Optional<String> getRearmedUEI() {
        return Optional.ofNullable(m_rearmedUEI);
    }

    public void setRearmedUEI(final String rearmedUEI) {
        m_rearmedUEI = ConfigUtils.normalizeString(rearmedUEI);
    }

    public FilterOperator getFilterOperator() {
        return m_filterOperator != null ? m_filterOperator : DEFAULT_FILTER_OPERATOR;
    }

    public void setFilterOperator(final FilterOperator filterOperator) {
        m_filterOperator = filterOperator;
    }

    public List<ResourceFilter> getResourceFilters() {
        return m_resourceFilters;
    }

    public void setResourceFilters(final List<ResourceFilter> resourceFilters) {
        if (resourceFilters == m_resourceFilters) return;
        m_resourceFilters.clear();
        if (resourceFilters != null) m_resourceFilters.addAll(resourceFilters);
    }

    /**
     * 
     * 
     * @param vResourceFilter
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addResourceFilter(final ResourceFilter vResourceFilter) throws IndexOutOfBoundsException {
        m_resourceFilters.add(vResourceFilter);
    }

    public boolean removeResourceFilter(final ResourceFilter resourceFilter) {
        return m_resourceFilters.remove(resourceFilter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_relaxed, 
                            m_description, 
                            m_type, 
                            m_dsType, 
                            m_value,
                            m_rearm, 
                            m_trigger, 
                            m_dsLabel, 
                            m_exprLabel, 
                            m_triggeredUEI, 
                            m_rearmedUEI, 
                            m_filterOperator, 
                            m_resourceFilters);
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Basethresholddef) {
            final Basethresholddef that = (Basethresholddef)obj;
            return Objects.equals(this.m_relaxed, that.m_relaxed)
                    && Objects.equals(this.m_description, that.m_description)
                    && Objects.equals(this.m_type, that.m_type)
                    && Objects.equals(this.m_dsType, that.m_dsType)
                    && Objects.equals(this.m_value, that.m_value)
                    && Objects.equals(this.m_rearm, that.m_rearm)
                    && Objects.equals(this.m_trigger, that.m_trigger)
                    && Objects.equals(this.m_dsLabel, that.m_dsLabel)
                    && Objects.equals(this.m_exprLabel, that.m_exprLabel)
                    && Objects.equals(this.m_triggeredUEI, that.m_triggeredUEI)
                    && Objects.equals(this.m_rearmedUEI, that.m_rearmedUEI)
                    && Objects.equals(this.m_filterOperator, that.m_filterOperator)
                    && Objects.equals(this.m_resourceFilters, that.m_resourceFilters);
        }
        return false;
    }

}
