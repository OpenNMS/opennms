/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2009 Apr 09: Expose data source indexes in threshold-derived events  - jeffg@opennms.org
 * 2007 Jan 29: Extracted high/low evaluation code and common parts of event building from ThresholdEntity and deduplicated code. - dj@opennms.org
 * 
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

package org.opennms.netmgt.threshd;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Date;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;
import org.springframework.util.Assert;

/**
 * <p>ThresholdEvaluatorHighLow class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ThresholdEvaluatorHighLow implements ThresholdEvaluator {

    /**
     * <p>Constructor for ThresholdEvaluatorHighLow.</p>
     */
    public ThresholdEvaluatorHighLow() {
        
    }
    
    /** {@inheritDoc} */
    public boolean supportsType(String type) {
        return "low".equals(type) || "high".equals(type);
    }
    
    /** {@inheritDoc} */
    public ThresholdEvaluatorState getThresholdEvaluatorState(BaseThresholdDefConfigWrapper threshold) {
        return new ThresholdEvaluatorStateHighLow(threshold);
    }
    
    public static class ThresholdEvaluatorStateHighLow implements ThresholdEvaluatorState {
        /**
         * Castor Threshold object containing threshold configuration data.
         */
        private BaseThresholdDefConfigWrapper m_thresholdConfig;

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

        public ThresholdEvaluatorStateHighLow(BaseThresholdDefConfigWrapper threshold) {
            Assert.notNull(threshold, "threshold argument cannot be null");
            
            setThresholdConfig(threshold);
            setExceededCount(0);
            setArmed(true);
        }    

        public boolean isArmed() {
            return m_armed;
        }

        public void setArmed(boolean armed) {
            m_armed = armed;
        }

        public int getExceededCount() {
            return m_exceededCount;
        }

        public void setExceededCount(int exceededCount) {
            m_exceededCount = exceededCount;
        }

        public BaseThresholdDefConfigWrapper getThresholdConfig() {
            return m_thresholdConfig;
        }

        public void setThresholdConfig(BaseThresholdDefConfigWrapper thresholdConfig) {
            Assert.notNull(thresholdConfig.getType(), "threshold must have a 'type' value set");
            Assert.notNull(thresholdConfig.getDatasourceExpression(), "threshold must have a 'ds-name' value set");
            Assert.notNull(thresholdConfig.getDsType(), "threshold must have a 'ds-type' value set");
            Assert.isTrue(thresholdConfig.hasValue(), "threshold must have a 'value' value set");
            Assert.isTrue(thresholdConfig.hasRearm(), "threshold must have a 'rearm' value set");
            Assert.isTrue(thresholdConfig.hasTrigger(), "threshold must have a 'trigger' value set");

            m_thresholdConfig = thresholdConfig;
        }
        
        public String getType() {
            return getThresholdConfig().getType().toString();
        }
        
        public Status evaluate(double dsValue) {
            if (isThresholdExceeded(dsValue)) {
                if (isArmed()) {
                    setExceededCount(getExceededCount() + 1);

                    if (log().isDebugEnabled()) {
                        log().debug("evaluate: " + getType() + " threshold exceeded, count=" + getExceededCount());
                    }

                    if (isTriggerCountExceeded()) {
                        log().debug("evaluate: " + getType() + " threshold triggered");
                        setExceededCount(1);
                        setArmed(false);
                        return Status.TRIGGERED;
                    }
                }
            } else if (isRearmExceeded(dsValue)) {
                if (!isArmed()) {
                    log().debug("evaluate: " + getType() + " threshold rearmed");
                    setArmed(true);
                    setExceededCount(0);
                    return Status.RE_ARMED;
                }
            } else {
                log().debug("evaluate: resetting " + getType() + " threshold count to 0");
                setExceededCount(0);
            }

            return Status.NO_CHANGE;
        }

        protected boolean isThresholdExceeded(double dsValue) {
            if ("high".equals(getThresholdConfig().getType())) {
                return dsValue >= getThresholdConfig().getValue();
            } else if ("low".equals(getThresholdConfig().getType())) {
                return dsValue <= getThresholdConfig().getValue();
            } else {
                throw new IllegalStateException("This thresholding strategy can only be used for thresholding types of 'high' and 'low'.");
            }
        }

        protected boolean isRearmExceeded(double dsValue) {
            if ("high".equals(getThresholdConfig().getType())) {
                return dsValue <= getThresholdConfig().getRearm();
            } else if ("low".equals(getThresholdConfig().getType())) {
                return dsValue >= getThresholdConfig().getRearm();
            } else {
                throw new IllegalStateException("This thresholding strategy can only be used for thresholding types of 'high' and 'low'.");
            }
        }

        protected boolean isTriggerCountExceeded() {
            return getExceededCount() >= getThresholdConfig().getTrigger();
        }
        
        public Event getEventForState(Status status, Date date, double dsValue, String dsInstance) {
            String uei;
            switch (status) {
            case TRIGGERED:
                uei=getThresholdConfig().getTriggeredUEI();
                if ("low".equals(getThresholdConfig().getType())) {
                    if(uei==null || "".equals(uei)) {
                        uei=EventConstants.LOW_THRESHOLD_EVENT_UEI;
                    }
                    return createBasicEvent(uei, date, dsValue, dsInstance);
                } else if ("high".equals(getThresholdConfig().getType())) {
                    if(uei==null || "".equals(uei)) {
                        uei=EventConstants.HIGH_THRESHOLD_EVENT_UEI;
                    }
                    return createBasicEvent(uei, date, dsValue, dsInstance);
                } else {
                    throw new IllegalArgumentException("Threshold type " + getThresholdConfig().getType().toString() + " is not supported");
                } 
                
            case RE_ARMED:
                uei=getThresholdConfig().getRearmedUEI();
                if ("low".equals(getThresholdConfig().getType())) {
                    if(uei==null || "".equals(uei)) {
                        uei=EventConstants.LOW_THRESHOLD_REARM_EVENT_UEI;
                    }
                    return createBasicEvent(uei, date, dsValue, dsInstance);
                } else if ("high".equals(getThresholdConfig().getType())) {
                    if(uei==null || "".equals(uei)) {
                        uei=EventConstants.HIGH_THRESHOLD_REARM_EVENT_UEI;
                    }
                    return createBasicEvent(uei, date, dsValue, dsInstance);
                } else {
                    throw new IllegalArgumentException("Threshold type " + getThresholdConfig().getType().toString() + " is not supported");
                } 
                
            case NO_CHANGE:
                return null;

            default:
                throw new IllegalArgumentException("Status " + status + " is not supported for converting to an event.");
            }
        }
        
        private Event createBasicEvent(String uei, Date date, double dsValue, String dsInstance) {
            // create the event to be sent
            Event event = new Event();
            event.setUei(uei);

            // set the source of the event to the datasource name
            event.setSource("OpenNMS.Threshd." + getThresholdConfig().getDatasourceExpression());

            // Set event host
            try {
                event.setHost(InetAddress.getLocalHost().getHostName());
            } catch (UnknownHostException e) {
                event.setHost("unresolved.host");
                log().warn("Failed to resolve local hostname: " + e, e);
            }

            // Set event time
            event.setTime(EventConstants.formatToString(date));

            // Add appropriate parms
            Parms eventParms = new Parms();
            Parm eventParm = null;
            Value parmValue = null;

            // Add datasource name
            eventParm = new Parm();
            eventParm.setParmName("ds");
            parmValue = new Value();
            parmValue.setContent(getThresholdConfig().getDatasourceExpression());
            eventParm.setValue(parmValue);
            eventParms.addParm(eventParm);

            // Add last known value of the datasource fetched from its RRD file
            eventParm = new Parm();
            eventParm.setParmName("value");
            parmValue = new Value();
            String pattern = System.getProperty("org.opennms.threshd.value.decimalformat", "###.##");
            DecimalFormat valueFormatter = new DecimalFormat(pattern);
            parmValue.setContent(valueFormatter.format(dsValue));
            eventParm.setValue(parmValue);
            eventParms.addParm(eventParm);

            // Add configured threshold value
            eventParm = new Parm();
            eventParm.setParmName("threshold");
            parmValue = new Value();
            parmValue.setContent(Double.toString(getThresholdConfig().getValue()));
            eventParm.setValue(parmValue);
            eventParms.addParm(eventParm);

            // Add configured trigger value
            eventParm = new Parm();
            eventParm.setParmName("trigger");
            parmValue = new Value();
            parmValue.setContent(Integer.toString(getThresholdConfig().getTrigger()));
            eventParm.setValue(parmValue);
            eventParms.addParm(eventParm);

            // Add configured rearm value
            eventParm = new Parm();
            eventParm.setParmName("rearm");
            parmValue = new Value();
            parmValue.setContent(Double.toString(getThresholdConfig().getRearm()));
            eventParm.setValue(parmValue);
            eventParms.addParm(eventParm);
            
            // Add the instance name of the resource in question
            eventParm = new Parm();
            eventParm.setParmName("instance");
            parmValue = new Value();
            parmValue.setContent(dsInstance != null ? dsInstance : "null");
            eventParm.setValue(parmValue);
            eventParms.addParm(eventParm);
            
            // Add Parms to the event
            event.setParms(eventParms);
            
            return event;
        }
        
        private final Category log() {
            return ThreadCategory.getInstance(getClass());
        }

        public ThresholdEvaluatorState getCleanClone() {
            return new ThresholdEvaluatorStateHighLow(m_thresholdConfig);
        }
    }

}
