/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.api.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm.AlarmType;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm.x733ProbableCause;
import org.opennms.netmgt.alarmd.api.Northbounder;
import org.opennms.netmgt.alarmd.api.NorthbounderException;
import org.opennms.netmgt.model.OnmsEventParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractNorthBounder
 * 
 * <p>The purpose of this class is manage the queue of alarms that need to be forward and receive queries to/from a Southbound Interface.</p>
 * 
 * <p>It passes Alarms on to the forwardAlarms method implemented by base classes in batches as they are 
 * added to the queue.  The forwardAlarms method does the actual work of sending them to the Southbound Interface.</p>
 * 
 * <p>Preserve, accept and discard are called to add the Alarms to the queue as appropriate.</p>
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public abstract class AbstractNorthbounder implements Northbounder, Runnable, StatusFactory<NorthboundAlarm> {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractNorthbounder.class);

    /** The NBI name. */
    private final String m_name;

    /** The alarm queue. */
    private final AlarmQueue<NorthboundAlarm> m_queue;

    /** The stopped flag. */
    private volatile boolean m_stopped = true;

    /** The retry interval. */
    private long m_retryInterval = 1000;

    /**
     * Instantiates a new abstract northbounder.
     *
     * @param name the name
     */
    protected AbstractNorthbounder(String name) {
        m_name = name;
        m_queue = new AlarmQueue<NorthboundAlarm>(this);
        LOG.debug("Creating Northbounder instance {}", getName());
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.Northbounder#getName()
     */
    @Override
    public String getName() {
        return m_name;
    }

    /**
     * Sets the nagles delay.
     *
     * @param delay the new nagles delay
     */
    public void setNaglesDelay(long delay) {
        m_queue.setNaglesDelay(delay);
    }

    /**
     * Sets the retry interval.
     *
     * @param retryInterval the new retry interval
     */
    public void setRetryInterval(int retryInterval) {
        m_retryInterval = retryInterval;
    }

    /**
     * Sets the max batch size.
     *
     * @param maxBatchSize the new max batch size
     */
    public void setMaxBatchSize(int maxBatchSize) {
        m_queue.setMaxBatchSize(maxBatchSize);
    }

    /**
     * Sets the max preserved alarms.
     *
     * @param maxPreservedAlarms the new max preserved alarms
     */
    public void setMaxPreservedAlarms(int maxPreservedAlarms) {
        m_queue.setMaxPreservedAlarms(maxPreservedAlarms);
    }

    /** Override this to perform actions before startup. **/
    protected void onPreStart() {
    }

    /** Override this to perform actions after startup. **/
    protected void onPostStart() {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.Northbounder#start()
     */
    @Override
    public final void start() throws NorthbounderException {
        if (!m_stopped) {
            return;
        }
        this.onPreStart();
        m_stopped = false;
        m_queue.init();
        Thread thread = new Thread(this, getName() + "-Thread");
        thread.start();
        this.onPostStart();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.Northbounder#onAlarm(org.opennms.netmgt.alarmd.api.NorthboundAlarm)
     */
    @Override
    public final void onAlarm(NorthboundAlarm alarm) throws NorthbounderException {
        if (accepts(alarm)) {
            m_queue.accept(alarm);
        }
    };

    /**
     * Accepts.
     *
     * @param alarm the alarm
     * @return true, if successful
     */
    protected abstract boolean accepts(NorthboundAlarm alarm);

    /**
     * Preserve.
     *
     * @param alarm the alarm
     * @throws NorthbounderException the northbounder exception
     */
    protected void preserve(NorthboundAlarm alarm) throws NorthbounderException {
        m_queue.preserve(alarm);
    }

    /**
     * Discard.
     *
     * @param alarm the alarm
     * @throws NorthbounderException the northbounder exception
     */
    protected void discard(NorthboundAlarm alarm) throws NorthbounderException {
        m_queue.discard(alarm);
    }

    /** Override this to perform actions when stopping. **/
    protected void onStop() {
    }

    /** Override this to perform actions when reloading the configuration. **/
    public void reloadConfig() {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.Northbounder#stop()
     */
    @Override
    public final void stop() throws NorthbounderException {
        this.onStop();
        m_stopped = true;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            while (!m_stopped) {
                List<NorthboundAlarm> alarmsToForward = m_queue.getAlarmsToForward();
                try {
                    forwardAlarms(alarmsToForward);
                    m_queue.forwardSuccessful(alarmsToForward);
                } catch (Exception e) {
                    m_queue.forwardFailed(alarmsToForward);
                    if (!m_stopped) {
                        // a failure occurred so sleep a moment and try again
                        Thread.sleep(m_retryInterval);
                    }
                }
            }
        } catch (InterruptedException e) {
            LOG.warn("Thread '{}' was interrupted unexpected.", getName());
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.support.StatusFactory#createSyncLostMessage()
     */
    @Override
    public NorthboundAlarm createSyncLostMessage() {
        return NorthboundAlarm.SYNC_LOST_ALARM;
    }

    /**
     * Forward alarms.
     *
     * @param alarms the alarms
     * @throws NorthbounderException the northbounder exception
     */
    public abstract void forwardAlarms(List<NorthboundAlarm> alarms) throws NorthbounderException;

    /**
     * Creates the mapping.
     *
     * @param alarmMappings the alarm mappings
     * @param alarm the northbound alarm
     * @return the mapping object
     */
    protected Map<String, Object> createMapping(NorthboundAlarm alarm) {
        Map<String, Object> mapping;
        mapping = new HashMap<String, Object>();
        mapping.put("ackUser", alarm.getAckUser());
        mapping.put("appDn", alarm.getAppDn());
        mapping.put("logMsg", alarm.getLogMsg());
        mapping.put("description", alarm.getDesc());
        mapping.put("objectInstance", alarm.getObjectInstance());
        mapping.put("objectType", alarm.getObjectType());
        mapping.put("ossKey", alarm.getOssKey());
        mapping.put("ossState", alarm.getOssState());
        mapping.put("ticketId", alarm.getTicketId());
        mapping.put("alarmUei", alarm.getUei());
        mapping.put("ackTime", nullSafeToString(alarm.getAckTime(), ""));

        AlarmType alarmType = alarm.getAlarmType() == null ? AlarmType.NOTIFICATION : alarm.getAlarmType();
        mapping.put("alarmType", alarmType.name());

        String count = alarm.getCount() == null ? "1" : alarm.getCount().toString();
        mapping.put("count", count);

        mapping.put("firstOccurrence", nullSafeToString(alarm.getFirstOccurrence(), ""));
        mapping.put("alarmId", alarm.getId().toString());
        mapping.put("ipAddr", nullSafeToString(alarm.getIpAddr(), ""));
        mapping.put("lastOccurrence", nullSafeToString(alarm.getLastOccurrence(), ""));

        if (alarm.getNodeId() != null) {
            LOG.debug("Adding nodeId: " + alarm.getNodeId().toString());
            mapping.put("nodeId", alarm.getNodeId().toString());
            mapping.put("nodeLabel", alarm.getNodeLabel() == null ? "?" : alarm.getNodeLabel());
            mapping.put("nodeSysObjectId", alarm.getNodeSysObjectId() == null ? "?" : alarm.getNodeSysObjectId());
            mapping.put("foreignSource", alarm.getForeignSource() == null ? "?" : alarm.getForeignSource());
            mapping.put("foreignId", alarm.getForeignId() == null ? "?" : alarm.getForeignId());
        } else {
            mapping.put("nodeId", "");
            mapping.put("nodeLabel", "");
            mapping.put("nodeSysObjectId", "");
            mapping.put("foreignSource", "");
            mapping.put("foreignId", "");
        }

        String poller = alarm.getPoller() == null ? "localhost" : alarm.getPoller().getId();
        mapping.put("distPoller", poller);

        String service = alarm.getService() == null ? "" : alarm.getService();
        mapping.put("ifService", service);

        mapping.put("severity", nullSafeToString(alarm.getSeverity(), ""));
        mapping.put("ticketState", nullSafeToString(alarm.getTicketState(), ""));

        mapping.put("x733AlarmType", alarm.getX733Type());
        try {
            mapping.put("x733ProbableCause", nullSafeToString(x733ProbableCause.get(alarm.getX733Cause()), ""));
        } catch (Exception e) {
            LOG.info("Exception caught setting X733 Cause: {}", alarm.getX733Cause(), e);
            mapping.put("x733ProbableCause", nullSafeToString(x733ProbableCause.other, ""));
        }

        buildParmMappings(alarm, mapping);

        return mapping;
    }

    /**
     * Null safe to string.
     *
     * @param obj the object
     * @param defaultString the default string
     * @return the string
     */
    private String nullSafeToString(Object obj, String defaultString) {
        if (obj != null) {
            defaultString = obj.toString();
        }
        return defaultString;
    }

    /**
     * Builds the parameters mappings.
     *
     * @param alarm the alarm
     * @param mapping the mapping
     */
    private void buildParmMappings(final NorthboundAlarm alarm, final Map<String, Object> mapping) {
        if (alarm.getParameters().isEmpty()) {
            return;
        }
        int parmOffset = 1;
        for (OnmsEventParameter parm : alarm.getEventParametersCollection()) {
            mapping.put("parm[name-#" + parmOffset + "]", parm.getName());
            mapping.put("parm[#" + parmOffset + "]", parm.getValue());
            mapping.put("parm[" + parm.getName() + "]", parm.getValue());
            parmOffset++;
        }
    }

}
