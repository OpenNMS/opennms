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
package org.opennms.netmgt.threshd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.mate.api.Scope;
import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.config.threshd.Basethresholddef;
import org.opennms.netmgt.config.threshd.Expression;
import org.opennms.netmgt.config.threshd.ResourceFilter;
import org.opennms.netmgt.config.threshd.Threshold;
import org.opennms.netmgt.config.threshd.ThresholdType;

/**
 * <p>Abstract BaseThresholdDefConfigWrapper class.</p>
 *
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:cmiskell@opennms.org">Craig Miskell</a>
 */
public abstract class BaseThresholdDefConfigWrapper {
    Basethresholddef m_baseDef;
    
    /**
     * <p>Constructor for BaseThresholdDefConfigWrapper.</p>
     *
     * @param baseDef a {@link org.opennms.netmgt.config.threshd.Basethresholddef} object.
     */
    protected BaseThresholdDefConfigWrapper(Basethresholddef baseDef) {
        m_baseDef=baseDef;
    }
    
    /**
     * <p>getConfigWrapper</p>
     *
     * @param baseDef a {@link org.opennms.netmgt.config.threshd.Basethresholddef} object.
     * @return a {@link org.opennms.netmgt.threshd.BaseThresholdDefConfigWrapper} object.
     * @throws org.opennms.netmgt.threshd.ThresholdExpressionException if any.
     */
    public static BaseThresholdDefConfigWrapper getConfigWrapper(Basethresholddef baseDef) throws ThresholdExpressionException {
        if(baseDef instanceof Threshold) {
            return new ThresholdConfigWrapper((Threshold)baseDef);
        } else if(baseDef instanceof Expression) {
            return new ExpressionConfigWrapper((Expression)baseDef);
        }
        return null;
    }

    /**
     * <p>getDatasourceExpression</p>
     *
     * @return a descriptive string for the data source - typically either a data source name, or an expression of data source names
     */
    public abstract String getDatasourceExpression();
    
    /**
     * Returns the names of the datasources required to evaluate this threshold
     *
     * @return Collection of the names of datasources
     */
    public abstract Collection<String> getRequiredDatasources();
    
    /**
     * <p>getDsType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDsType() {
        return m_baseDef.getDsType();
    }
    
    /**
     * <p>getDsLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public Optional<String> getDsLabel() {
        return m_baseDef.getDsLabel();
    }
    
    /**
     * <p>getRearm</p>
     *
     * @return a Double.
     */
    public Double getRearm() {
        return StringUtils.parseDouble(m_baseDef.getRearm(), null);
    }

    public String getRearmString() {
        return m_baseDef.getRearm();
    }
    
    /**
     * <p>getTrigger</p>
     *
     * @return a Integer.
     */
    public Integer getTrigger() {
        return StringUtils.parseInt(m_baseDef.getTrigger(), null);
    }

    public String getTriggerString() {
        return m_baseDef.getTrigger();
    }
    
    /**
     * <p>getType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public ThresholdType getType() {
        return m_baseDef.getType();
    }
    
    /**
     * <p>getValue</p>
     *
     * @return a Double.
     */
    public Double getValue() {
        return StringUtils.parseDouble(m_baseDef.getValue(), null);
    }


    public String getValueString() {
        return m_baseDef.getValue();
    }

    
    /**
     * <p>hasRearm</p>
     *
     * @return a boolean.
     */
    public boolean hasRearm() {
        return m_baseDef.getRearm() != null;
    }
    
    /**
     * <p>hasTrigger</p>
     *
     * @return a boolean.
     */
    public boolean hasTrigger() {
        return m_baseDef.getTrigger() != null;
    }
    
    /**
     * <p>hasValue</p>
     *
     * @return a boolean.
     */
    public boolean hasValue() {
        return m_baseDef.getValue() != null;
    }
    
    /**
     * <p>getTriggeredUEI</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public Optional<String> getTriggeredUEI() {
        return m_baseDef.getTriggeredUEI();
    }
    
    /**
     * <p>getRearmedUEI</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public Optional<String> getRearmedUEI() {
        return m_baseDef.getRearmedUEI();
    }
    
    /**
     * <p>getBasethresholddef</p>
     *
     * @return a {@link org.opennms.netmgt.config.threshd.Basethresholddef} object.
     */
    public Basethresholddef getBasethresholddef() {
        return m_baseDef;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof BaseThresholdDefConfigWrapper) {
            final BaseThresholdDefConfigWrapper that = (BaseThresholdDefConfigWrapper)obj;
            return Objects.equals(this.getType(), that.getType())
                    && Objects.equals(this.getDsType(), that.getDsType())
                    && Objects.equals(this.getDatasourceExpression(), that.getDatasourceExpression())
                    && Objects.equals(this.getDsLabel(), that.getDsLabel())
                    && Objects.equals(this.getTriggeredUEI(), that.getTriggeredUEI())
                    && Objects.equals(this.getRearmedUEI(), that.getRearmedUEI())
                    && Objects.equals(this.getValueString(), that.getValueString())
                    && Objects.equals(this.getRearmString(), that.getRearmString())
                    && Objects.equals(this.getTriggerString(), that.getTriggerString())
                    && Objects.equals(this.getBasethresholddef().getFilterOperator(), that.getBasethresholddef().getFilterOperator())
                    && Objects.equals(this.getBasethresholddef().getRelaxed(), that.getBasethresholddef().getRelaxed())
                    && Objects.equals(this.getBasethresholddef().getResourceFilters(), that.getBasethresholddef().getResourceFilters());
        }
        return false;
    }
    
    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(m_baseDef);
    }

    /**
     * <p>merge</p>
     *
     * @param threshold a {@link org.opennms.netmgt.threshd.BaseThresholdDefConfigWrapper} object.
     */
    public void merge(BaseThresholdDefConfigWrapper threshold) {
        m_baseDef = threshold.getBasethresholddef();
    }
    
    public abstract void accept(ThresholdDefVisitor thresholdDefVisitor);

    public ThresholdEvaluatorState.ThresholdValues interpolateThresholdValues(Scope scope) {
        Double thresholdValue = interpolateDoubleValue(getValueString(), scope).orElse(getValue());
        Double rearm = interpolateDoubleValue(getRearmString(), scope).orElse(getRearm());
        Integer trigger = interpolateIntegerValue(getTriggerString(), scope).orElse(getTrigger());
        return new ThresholdEvaluatorState.ThresholdValues(thresholdValue, rearm, trigger);
    }

    private Optional<Double> interpolateDoubleValue(String value, Scope scope) {
        String interpolatedValue = Interpolator.interpolate(value, scope).output;
        return Optional.ofNullable(StringUtils.parseDouble(interpolatedValue, null));
    }

    private Optional<Integer> interpolateIntegerValue(String value, Scope scope) {
        String interpolatedValue = Interpolator.interpolate(value, scope).output;
        return Optional.ofNullable(StringUtils.parseInt(interpolatedValue, null));
    }

}

