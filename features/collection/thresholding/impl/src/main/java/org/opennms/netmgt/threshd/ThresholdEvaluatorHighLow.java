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

import com.google.common.annotations.VisibleForTesting;

/**
 * <p>ThresholdEvaluatorHighLow class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ThresholdEvaluatorHighLow implements ThresholdEvaluator {
    private static final Logger LOG = LoggerFactory.getLogger(ThresholdEvaluatorHighLow.class);
    /**
     * <p>Constructor for ThresholdEvaluatorHighLow.</p>
     */
    public ThresholdEvaluatorHighLow() {
        
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean supportsType(ThresholdType type) {
        return ThresholdType.LOW.equals(type) || ThresholdType.HIGH.equals(type);
    }
    
    /** {@inheritDoc} */
    @Override
    public ThresholdEvaluatorState getThresholdEvaluatorState(BaseThresholdDefConfigWrapper threshold, ThresholdingSession thresholdingSession) {
        // can get DS expression from threshold
        
        return new ThresholdEvaluatorStateHighLow(threshold, thresholdingSession);
    }
    
    public static class ThresholdEvaluatorStateHighLow extends AbstractThresholdEvaluatorState<ThresholdEvaluatorStateHighLow.State> {
        /**
         * Object containing threshold configuration data.
         */
        private BaseThresholdDefConfigWrapper m_thresholdConfig;

        static class State extends AbstractThresholdEvaluatorState.AbstractState {
            private static final long serialVersionUID = 1L;

            /**
             * Threshold exceeded count
             */
            private int m_exceededCount;

            /**
             * Threshold armed flag
             *
             * This flag must be true before evaluate() will return true (indicating
             * that the threshold has been triggered). This flag is initialized to true
             * by the constructor and is set to false each time the threshold is
             * triggered. It can only be reset by the current value of the datasource
             * falling below (for high threshold) or rising above (for low threshold)
             * the rearm value.
             */
            private boolean m_armed;

            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                sb.append("exceededCount=").append(m_exceededCount);
                sb.append("\narmed=").append(m_armed);
                String superString = super.toString();

                if (superString != null) {
                    sb.append("\n").append(superString);
                }

                return sb.toString();
            }
        }
        
        private CollectionResourceWrapper m_lastCollectionResourceUsed;

        public ThresholdEvaluatorStateHighLow(BaseThresholdDefConfigWrapper threshold, ThresholdingSession thresholdingSession) {
            super(threshold, thresholdingSession, ThresholdEvaluatorStateHighLow.State.class);
            setThresholdConfig(threshold);
        }

        @Override
        protected void initializeState() {
            state = new State();
            setExceededCount(0);
            setArmed(true);
        }

        private boolean isArmed() {
            return state.m_armed;
        }

        private void setArmed(boolean armed) {
            if(armed != state.m_armed) {
                state.m_armed = armed;
                markDirty();
            }
        }

        private int getExceededCount() {
            return state.m_exceededCount;
        }

        private void setExceededCount(int exceededCount) {
            if(exceededCount != state.m_exceededCount) {
                state.m_exceededCount = exceededCount;
                markDirty();
            }
        }

        @Override
        public BaseThresholdDefConfigWrapper getThresholdConfig() {
            return m_thresholdConfig;
        }

        private void setThresholdConfig(BaseThresholdDefConfigWrapper thresholdConfig) {
            Assert.notNull(thresholdConfig.getType(), "threshold must have a 'type' value set");
            Assert.notNull(thresholdConfig.getDatasourceExpression(), "threshold must have a 'ds-name' value set");
            Assert.notNull(thresholdConfig.getDsType(), "threshold must have a 'ds-type' value set");
            Assert.isTrue(thresholdConfig.hasValue(), "threshold must have a 'value' value set");
            Assert.isTrue(thresholdConfig.hasRearm(), "threshold must have a 'rearm' value set");
            Assert.isTrue(thresholdConfig.hasTrigger(), "threshold must have a 'trigger' value set");

            m_thresholdConfig = thresholdConfig;
        }
        
        private ThresholdType getType() {
            return getThresholdConfig().getType();
        }

        @Override
        public Status evaluateAfterFetch(double dsValue, ThresholdValues thresholdValues) {
            Double thresholdValue  = thresholdValues != null && thresholdValues.getThresholdValue() != null ?
                    thresholdValues.getThresholdValue() : getThresholdConfig().getValue();
            Double rearm  = thresholdValues != null && thresholdValues.getRearm() != null ?
                    thresholdValues.getRearm() : getThresholdConfig().getRearm();
            Integer trigger = thresholdValues != null && thresholdValues.getTrigger() != null ?
                    thresholdValues.getTrigger() : getThresholdConfig().getTrigger();

            if(thresholdValue == null || rearm == null || trigger == null) {
                return Status.NO_CHANGE;
            }

            if (isThresholdExceeded(dsValue, thresholdValue)) {
                if (isArmed()) {
                    setExceededCount(getExceededCount() + 1);

                    LOG.debug("evaluate: {} threshold exceeded, count={}", getType(), getExceededCount());

                    if (isTriggerCountExceeded(trigger)) {
                        LOG.debug("evaluate: {} threshold triggered", getType());
                        setExceededCount(1);
                        setArmed(false);
                        return Status.TRIGGERED;
                    }
                }
            } else if (isRearmExceeded(dsValue, rearm)) {
                if (!isArmed()) {
                    LOG.debug("evaluate: {} threshold rearmed", getType());
                    setArmed(true);
                    setExceededCount(0);
                    return Status.RE_ARMED;
                }
                if (getExceededCount() > 0) {
                    LOG.debug("evaluate: resetting {} threshold count to 0, because the current value indicates that the in-progress threshold has been rearmed, but it doesn't triggered yet.", getType());
                    setExceededCount(0);
                }
            } else {
                LOG.debug("evaluate: resetting {} threshold count to 0", getType());
                setExceededCount(0);
            }

            return Status.NO_CHANGE;
        }

        protected boolean isThresholdExceeded(double dsValue, Double value) {
            if (ThresholdType.HIGH.equals(getThresholdConfig().getType())) {
                return dsValue >= value;
            } else if (ThresholdType.LOW.equals(getThresholdConfig().getType())) {
                return dsValue <= value;
            } else {
                throw new IllegalStateException("This thresholding strategy can only be used for thresholding types of 'high' and 'low'.");
            }
        }

        @VisibleForTesting
        protected boolean isThresholdExceeded(double dsValue) {
            Double thresholdValue = state.getThresholdValues() != null && state.getThresholdValues().getThresholdValue() != null ?
                    state.getThresholdValues().getThresholdValue() : getThresholdConfig().getValue();
            return isThresholdExceeded(dsValue, thresholdValue);
        }

        protected boolean isRearmExceeded(double dsValue, Double rearm) {
            if (ThresholdType.HIGH.equals(getThresholdConfig().getType())) {
                return dsValue <= rearm;
            } else if (ThresholdType.LOW.equals(getThresholdConfig().getType())) {
                return dsValue >= rearm;
            } else {
                throw new IllegalStateException("This thresholding strategy can only be used for thresholding types of 'high' and 'low'.");
            }
        }

        @VisibleForTesting
        protected boolean isRearmExceeded(double dsValue) {
            Double rearm = state.getThresholdValues() != null && state.getThresholdValues().getRearm() != null ?
                    state.getThresholdValues().getRearm() : getThresholdConfig().getRearm();
            return isRearmExceeded(dsValue, rearm);
        }

        protected boolean isTriggerCountExceeded(Integer trigger) {
            return getExceededCount() >= trigger;
        }

        protected boolean isTriggerCountExceeded() {
            Integer trigger = state.getThresholdValues() != null && state.getThresholdValues().getTrigger() != null ?
                    state.getThresholdValues().getTrigger() : getThresholdConfig().getTrigger();
            return isTriggerCountExceeded(trigger);
        }
        
        @Override
        public Event getEventForState(Status status, Date date, double dsValue, ThresholdValues thresholdValues, CollectionResourceWrapper resource) {
            /*
             * If resource is null, we will use m_lastCollectionResourceUsed; else we will use provided resource.
             * For future calls we will preserve the latest not null resource on m_lastCollectionResourceUsed.
             * See ThresholdEntity.merge
             */
            if (resource == null) {
                resource = m_lastCollectionResourceUsed;
            }
            m_lastCollectionResourceUsed = resource;
            String uei;
            switch (status) {
            case TRIGGERED:
                uei=getThresholdConfig().getTriggeredUEI().orElse(null);
                if (ThresholdType.LOW.equals(getThresholdConfig().getType())) {
                    if(uei==null || "".equals(uei)) {
                        uei=EventConstants.LOW_THRESHOLD_EVENT_UEI;
                    }
                    return createBasicEvent(uei, date, dsValue, thresholdValues, resource);
                } else if (ThresholdType.HIGH.equals(getThresholdConfig().getType())) {
                    if(uei==null || "".equals(uei)) {
                        uei=EventConstants.HIGH_THRESHOLD_EVENT_UEI;
                    }
                    return createBasicEvent(uei, date, dsValue, thresholdValues, resource);
                } else {
                    throw new IllegalArgumentException("Threshold type " + getThresholdConfig().getType() + " is not supported");
                } 
                
            case RE_ARMED:
                uei=getThresholdConfig().getRearmedUEI().orElse(null);
                if (ThresholdType.LOW.equals(getThresholdConfig().getType())) {
                    if(uei==null || "".equals(uei)) {
                        uei=EventConstants.LOW_THRESHOLD_REARM_EVENT_UEI;
                    }
                    return createBasicEvent(uei, date, dsValue, thresholdValues , resource);
                } else if (ThresholdType.HIGH.equals(getThresholdConfig().getType())) {
                    if(uei==null || "".equals(uei)) {
                        uei=EventConstants.HIGH_THRESHOLD_REARM_EVENT_UEI;
                    }
                    return createBasicEvent(uei, date, dsValue, thresholdValues, resource);
                } else {
                    throw new IllegalArgumentException("Threshold type " + getThresholdConfig().getType() + " is not supported");
                } 
                
            case NO_CHANGE:
                return null;

            default:
                throw new IllegalArgumentException("Status " + status + " is not supported for converting to an event.");
            }
        }
        
        private Event createBasicEvent(String uei, Date date, double dsValue, ThresholdValues thresholdValues, CollectionResourceWrapper resource) {
            if(thresholdValues == null) {
                thresholdValues = state.getThresholdValues();
            }
            Map<String,String> params = new HashMap<String,String>();
            params.put("threshold", Double.toString(thresholdValues.getThresholdValue()));
            params.put("trigger", Integer.toString(thresholdValues.getTrigger()));
            params.put("rearm", Double.toString(thresholdValues.getRearm()));
            return createBasicEvent(uei, date, dsValue, resource, params);
        }
        
        @Override
        public ThresholdEvaluatorState getCleanClone() {
            return new ThresholdEvaluatorStateHighLow(m_thresholdConfig, getThresholdingSession());
        }

        @Override
        public boolean isTriggered() {
            return !isArmed();
        }
        
        @Override
        public void clearStateBeforePersist() {
            setArmed(true);
            setExceededCount(0);
            state.setCached(false);
        }
        
    }

}
