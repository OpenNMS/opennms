/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.threshd;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.config.threshd.ThresholdType;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.threshd.api.ThresholdingSession;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * This works similar to <tt>absoluteChange</tt>; however, the <tt>trigger</tt> value
 * is used to re-arm the event after so many iterations with an unchanged delta.
 *
 * @author bdymek
 * @version $Id: $
 */
public class ThresholdEvaluatorRearmingAbsoluteChange implements ThresholdEvaluator {
    private static final Logger LOG = LoggerFactory.getLogger(ThresholdEvaluatorRearmingAbsoluteChange.class);
    private static final ThresholdType TYPE = ThresholdType.REARMING_ABSOLUTE_CHANGE;

    /** {@inheritDoc} */
    @Override
    public ThresholdEvaluatorState getThresholdEvaluatorState(BaseThresholdDefConfigWrapper threshold, ThresholdingSession thresholdingSession) {
        return new ThresholdEvaluatorStateRearmingAbsoluteChange(threshold, thresholdingSession);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsType(ThresholdType type) {
        return TYPE.equals(type);
    }
    
    public static class ThresholdEvaluatorStateRearmingAbsoluteChange extends AbstractThresholdEvaluatorState<ThresholdEvaluatorStateRearmingAbsoluteChange.State> {
        private BaseThresholdDefConfigWrapper m_thresholdConfig;

        static class State extends AbstractThresholdEvaluatorState.AbstractState {
            private static final long serialVersionUID = 1L;
            private double m_lastSample = Double.NaN;
            private double m_previousTriggeringSample = Double.NaN;
            private int m_triggerCount = 0;

            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                sb.append("lastSample=").append(m_lastSample);
                sb.append("\npreviousTriggeringSample=").append(m_previousTriggeringSample);
                sb.append("\ntriggerCount=").append(m_triggerCount);
                String superString = super.toString();

                if (superString != null) {
                    sb.append("\n").append(superString);
                }

                return sb.toString();
            }
        }

        public ThresholdEvaluatorStateRearmingAbsoluteChange(BaseThresholdDefConfigWrapper threshold, ThresholdingSession thresholdingSession) {
            super(threshold, thresholdingSession, ThresholdEvaluatorStateRearmingAbsoluteChange.State.class);
            setThresholdConfig(threshold);
        }

        @Override
        protected void initializeState() {
            state = new State();
        }

        public String getType() {
        	return getThresholdConfig().getType().toString();
        }
        
        private void setThresholdConfig(BaseThresholdDefConfigWrapper thresholdConfig) {
            Assert.notNull(thresholdConfig.getType(), "threshold must have a 'type' value set");
            Assert.notNull(thresholdConfig.getDatasourceExpression(), "threshold must have a 'ds-name' value set");
            Assert.notNull(thresholdConfig.getDsType(), "threshold must have a 'ds-type' value set");
            Assert.isTrue(thresholdConfig.hasValue(), "threshold must have a 'value' value set");
            Assert.isTrue(thresholdConfig.hasRearm(), "threshold must have a 'rearm' value set");
            Assert.isTrue(thresholdConfig.hasTrigger(), "threshold must have a 'trigger' value set");

            Assert.isTrue(TYPE.equals(thresholdConfig.getType()), "threshold for ds-name '" + thresholdConfig.getDatasourceExpression() + "' has type of '" + thresholdConfig.getType() + "', but this evaluator only supports thresholds with a 'type' value of '" + TYPE + "'");

            Assert.isTrue(!Double.isNaN(thresholdConfig.getValue()), "threshold must have a 'value' value that is a number");
            Assert.isTrue(thresholdConfig.getValue() != Double.POSITIVE_INFINITY && thresholdConfig.getValue() != Double.NEGATIVE_INFINITY, "threshold must have a 'value' value that is not positive or negative infinity");
            
            m_thresholdConfig = thresholdConfig;        
        }

        @Override
        public BaseThresholdDefConfigWrapper getThresholdConfig() {
            return m_thresholdConfig;
        }

        @Override
        public Status evaluateAfterFetch(double dsValue) {
            try {
                if (!Double.valueOf(getPreviousTriggeringSample()).isNaN()) {
                    setTriggerCount(getTriggerCount() + 1);
                    if (!wasTriggered(dsValue) && (getTriggerCount() >= getThresholdConfig().getTrigger())) {
                        setPreviousTriggeringSample(Double.NaN);
                        setTriggerCount(0);
                        LOG.debug("{} threshold rearmed, sample value={}", TYPE, dsValue);
                        return Status.RE_ARMED;
                    }
                } else if (wasTriggered(dsValue)) {
                    setPreviousTriggeringSample(getLastSample());
                    setTriggerCount(0);
                    LOG.debug("{} threshold triggered, sample value={}", TYPE, dsValue);
                    return Status.TRIGGERED;
                }
            } finally {
                setLastSample(dsValue);
            }

            return Status.NO_CHANGE;
        }

        private boolean wasTriggered(double dsValue) {
            if (Double.valueOf(dsValue).isNaN())
                return false;
            if (getLastSample().isNaN())
                return false;

            double threshold = Math.abs(getLastSample() - dsValue);

            return threshold >= getThresholdConfig().getValue();
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
        public Event getEventForState(Status status, Date date, double dsValue, CollectionResourceWrapper resource) {
            if (status == Status.TRIGGERED) {
                final String uei=getThresholdConfig().getTriggeredUEI().orElse(EventConstants.REARMING_ABSOLUTE_CHANGE_EXCEEDED_EVENT_UEI);
                return createBasicEvent(uei, date, dsValue, resource);
            }
            
            if (status == Status.RE_ARMED) {
                final String uei=getThresholdConfig().getRearmedUEI().orElse(EventConstants.REARMING_ABSOLUTE_CHANGE_REARM_EVENT_UEI);
                return createBasicEvent(uei, date, dsValue, resource);
            } 
            
            return null;
        }
        
        private Event createBasicEvent(String uei, Date date, double dsValue, CollectionResourceWrapper resource) {
            Map<String,String> params = new HashMap<String,String>();
            params.put("previousValue", formatValue(getPreviousTriggeringSample()));
            params.put("threshold", Double.toString(getThresholdConfig().getValue()));
            params.put("trigger", Integer.toString(getThresholdConfig().getTrigger()));
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
        
        private int getTriggerCount() {
            return state.m_triggerCount;
        }
        
        private void setTriggerCount(int triggerCount) {
            if (state.m_triggerCount != triggerCount) {
                state.m_triggerCount = triggerCount;
                markDirty();
            }
        }
        
        @Override
        public ThresholdEvaluatorState getCleanClone() {
            return new ThresholdEvaluatorStateRearmingAbsoluteChange(m_thresholdConfig, getThresholdingSession());
        }

        @Override
        public boolean isTriggered() {
            return wasTriggered(state.m_previousTriggeringSample); // TODO Is that right ?
        }
        
        @Override
        public void clearStateBeforePersist() {
            // Based on what evaluator does for rearmed state
            setLastSample(Double.NaN);
            setTriggerCount(0);
            setPreviousTriggeringSample(Double.NaN);
        }

    }
}
