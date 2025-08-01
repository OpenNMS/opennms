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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.config.threshd.ThresholdType;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.threshd.api.ThresholdingSession;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.util.Assert;

/**
 * Implements a relative change threshold check.  A 'value' setting of
 * less than 1.0 means that a threshold will fire if the current value
 * is less than or equal to the previous value multiplied by the 'value'
 * setting.  A 'value' setting greater than 1.0 causes the threshold to
 * fire if the current value is greater than or equal to the previous
 * value multiplied by the 'value' setting.  A 'value' setting of 1.0
 * (unity) is not allowed, as it represents no change.  Zero valued
 * samples (0.0) are ignored, as 0.0 multiplied by anything is 0.0 (if
 * they were not ignored, an interface that gets no traffic would always
 * trigger a threshold, for example).
 *
 * @author ranger
 * @version $Id: $
 */
public class ThresholdEvaluatorRelativeChange implements ThresholdEvaluator {
    
    private static final ThresholdType TYPE = ThresholdType.RELATIVE_CHANGE;

    /** {@inheritDoc} */
    @Override
    public ThresholdEvaluatorState getThresholdEvaluatorState(BaseThresholdDefConfigWrapper threshold, ThresholdingSession thresholdingSession) {
        return new ThresholdEvaluatorStateRelativeChange(threshold, thresholdingSession);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsType(ThresholdType type) {
        return TYPE.equals(type);
    }
    
    public static class ThresholdEvaluatorStateRelativeChange extends AbstractThresholdEvaluatorState<ThresholdEvaluatorStateRelativeChange.State> {
        private BaseThresholdDefConfigWrapper m_thresholdConfig;

        static class State extends AbstractThresholdEvaluatorState.AbstractState {
            private static final long serialVersionUID = 1L;
            private double m_multiplier;
            private double m_lastSample = 0.0;
            private double m_previousTriggeringSample;

            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                sb.append("multiplier=").append(m_multiplier);
                sb.append("\nlastSample=").append(m_lastSample);
                sb.append("\npreviousTriggeringSample=").append(m_previousTriggeringSample);
                String superString = super.toString();

                if (superString != null) {
                    sb.append("\n").append(superString);
                }

                return sb.toString();
            }
        }

        public ThresholdEvaluatorStateRelativeChange(BaseThresholdDefConfigWrapper threshold, ThresholdingSession thresholdingSession) {
            super(threshold, thresholdingSession, ThresholdEvaluatorStateRelativeChange.State.class);
            setThresholdConfig(threshold);
        }

        @Override
        protected void initializeState() {
            state = new State();
        }

        private void setThresholdConfig(BaseThresholdDefConfigWrapper thresholdConfig) {
            Assert.notNull(thresholdConfig.getType(), "threshold must have a 'type' value set");
            Assert.notNull(thresholdConfig.getDatasourceExpression(), "threshold must have a 'ds-name' value set");
            Assert.notNull(thresholdConfig.getDsType(), "threshold must have a 'ds-type' value set");
            Assert.isTrue(thresholdConfig.hasValue(), "threshold must have a 'value' value set");
            Assert.isTrue(thresholdConfig.hasRearm(), "threshold must have a 'rearm' value set");
            Assert.isTrue(thresholdConfig.hasTrigger(), "threshold must have a 'trigger' value set");

            Assert.isTrue(TYPE.equals(thresholdConfig.getType()), "threshold for ds-name '" + thresholdConfig.getDatasourceExpression() + "' has type of '" + thresholdConfig.getType() + "', but this evaluator only supports thresholds with a 'type' value of '" + TYPE + "'");

            m_thresholdConfig = thresholdConfig;
        }

        @Override
        public BaseThresholdDefConfigWrapper getThresholdConfig() {
            return m_thresholdConfig;
        }

        @Override
        public Status evaluateAfterFetch(double dsValue, ThresholdValues thresholdValues) {
        	//Fix for Bug 2275 so we handle negative numbers
        	//It will not handle values which cross the 0 boundary (from - to +, or v.v.) properly, but
        	// after some discussion, we can't come up with a sensible scenario when that would actually happen.
        	// If such a scenario eventuates, reconsider
            Double multiplier = thresholdValues != null && thresholdValues.getThresholdValue() != null ?
                    thresholdValues.getThresholdValue() : m_thresholdConfig.getValue();
            if(multiplier == null) {
                return Status.NO_CHANGE;
            }
            setMultiplier(multiplier);
        	dsValue=Math.abs(dsValue);
            if (getLastSample() != 0.0) {
                double threshold = getMultiplier() * getLastSample();

                if (getMultiplier() < 1.0) {
                    if (dsValue <= threshold) {
                        setPreviousTriggeringSample(getLastSample());
                        setLastSample(dsValue);
                        return Status.TRIGGERED;
                    }
                } else {
                    if (dsValue >= threshold) {
                        setPreviousTriggeringSample(getLastSample());
                        setLastSample(dsValue);
                        return Status.TRIGGERED;
                    }
                }

                setLastSample(dsValue);
            }

            setLastSample(dsValue);
            return Status.NO_CHANGE;
        }

        private Double getLastSample() {
            return state.m_lastSample;
        }

        private void setLastSample(double lastSample) {
            if (state.m_lastSample != lastSample) {
                state.m_lastSample = lastSample;
                markDirty();
            }
        }

        @Override
        public Event getEventForState(Status status, Date date, double dsValue, ThresholdValues thresholdValues, CollectionResourceWrapper resource) {
            if (status == Status.TRIGGERED) {
                final String uei=getThresholdConfig().getTriggeredUEI().orElse(EventConstants.RELATIVE_CHANGE_THRESHOLD_EVENT_UEI);
                return createBasicEvent(uei, date, dsValue, thresholdValues, resource);
            } else {
                return null;
            }
        }
        
        private Event createBasicEvent(String uei, Date date, double dsValue, ThresholdValues thresholdValues, CollectionResourceWrapper resource) {
            if(thresholdValues == null) {
                thresholdValues = state.getThresholdValues();
            }
            Map<String,String> params = new HashMap<String,String>();
            params.put("previousValue", formatValue(getPreviousTriggeringSample()));
            params.put("multiplier", Double.toString(thresholdValues.getThresholdValue()));
            // params.put("trigger", Integer.toString(getThresholdConfig().getTrigger()));
            // params.put("rearm", Double.toString(getThresholdConfig().getRearm()));
            return createBasicEvent(uei, date, dsValue, resource, params);
        }

        private double getPreviousTriggeringSample() {
            return state.m_previousTriggeringSample;
        }
        
        private void setPreviousTriggeringSample(double previousTriggeringSample) {
            if (state.m_previousTriggeringSample != previousTriggeringSample) {
                state.m_previousTriggeringSample = previousTriggeringSample;
                markDirty();
            }
        }

        private double getMultiplier() {
            return state.m_multiplier;
        }

        private void setMultiplier(double multiplier) {
            if (state.m_multiplier != multiplier) {
                state.m_multiplier = multiplier;
                markDirty();
            }
        }

        @Override
        public ThresholdEvaluatorState getCleanClone() {
            return new ThresholdEvaluatorStateRelativeChange(m_thresholdConfig, getThresholdingSession());
        }

        // FIXME This must be implemented correctly
        @Override
        public boolean isTriggered() {
            return false;
        }

        @Override
        public void clearStateBeforePersist() {
            initializeState();
        }

    }

}
