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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm.AlarmType;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm.x733ProbableCause;
import org.opennms.netmgt.alarmd.api.Northbounder;
import org.opennms.netmgt.alarmd.api.NorthbounderException;
import org.opennms.netmgt.dao.api.NodeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * AbstractNorthBounder
 * 
 * The purpose of this class is manage the queue of alarms that need to be forward and receive queries to/from a Southbound Interface.
 * 
 * It passes Alarms on to the forwardAlarms method implemented by base classes in batches as they are 
 * added to the queue.  The forwardAlarms method does the actual work of sending them to the Southbound Interface.
 * 
 * preserve, accept and discard are called to add the Alarms to the queue as appropriate.  
 * 
 * @author <a mailto:david@opennms.org>David Hustace</a>
 */

public abstract class AbstractNorthbounder implements Northbounder, Runnable,
        StatusFactory<NorthboundAlarm> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractNorthbounder.class);
    private final String m_name;
    private final AlarmQueue<NorthboundAlarm> m_queue;
    protected NodeDao m_nodeDao;
    
    private volatile boolean m_stopped = true;

    private long m_retryInterval = 1000;

    protected AbstractNorthbounder(String name) {
        m_name = name;
        m_queue = new AlarmQueue<NorthboundAlarm>(this);
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    @Override
    public String getName() {
        return m_name;
    }

    public void setNaglesDelay(long delay) {
        m_queue.setNaglesDelay(delay);
    }

    public void setRetryInterval(int retryInterval) {
        m_retryInterval = retryInterval;
    }

    public void setMaxBatchSize(int maxBatchSize) {
        m_queue.setMaxBatchSize(maxBatchSize);
    }

    public void setMaxPreservedAlarms(int maxPreservedAlarms) {
        m_queue.setMaxPreservedAlarms(maxPreservedAlarms);
    }

    /** Override this to perform actions before startup. **/
    protected void onPreStart() {
    }

    /** Override this to perform actions after startup. **/
    protected void onPostStart() {
    }

    @Override
    public final void start() throws NorthbounderException {
        if (!m_stopped)
            return;
        this.onPreStart();
        m_stopped = false;
        m_queue.init();
        Thread thread = new Thread(this, getName() + "-Thread");
        thread.start();
        this.onPostStart();
    }

    @Override
    public final void onAlarm(NorthboundAlarm alarm)
            throws NorthbounderException {
        if (accepts(alarm)) {
            m_queue.accept(alarm);
        }
    };

    protected abstract boolean accepts(NorthboundAlarm alarm);

    protected void preserve(NorthboundAlarm alarm)
            throws NorthbounderException {
        m_queue.preserve(alarm);
    }

    protected void discard(NorthboundAlarm alarm)
            throws NorthbounderException {
        m_queue.discard(alarm);
    }

    /** Override this to perform actions when stopping. **/
    protected void onStop() {
    }

    @Override
    public final void stop() throws NorthbounderException {
        this.onStop();
        m_stopped = true;
    }

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
            // thread interrupted so complete it
        }

    }

    @Override
    public NorthboundAlarm createSyncLostMessage() {
        return NorthboundAlarm.SYNC_LOST_ALARM;
    }

    public abstract void forwardAlarms(List<NorthboundAlarm> alarms)
            throws NorthbounderException;

    protected Map<String, Object> createMapping(
            Map<Integer, Map<String, Object>> alarmMappings,
            NorthboundAlarm alarm) {
        Map<String, Object> mapping;
        mapping = new HashMap<String, Object>();
        mapping.put("ackUser", alarm.getAckUser());
        mapping.put("appDn", alarm.getAppDn());
        mapping.put("logMsg", alarm.getLogMsg());
        mapping.put("objectInstance", alarm.getObjectInstance());
        mapping.put("objectType", alarm.getObjectType());
        mapping.put("ossKey", alarm.getOssKey());
        mapping.put("ossState", alarm.getOssState());
        mapping.put("ticketId", alarm.getTicketId());
        mapping.put("alarmUei", alarm.getUei());
        mapping.put("ackTime", nullSafeToString(alarm.getAckTime(), ""));

        AlarmType alarmType = alarm.getAlarmType() == null ? AlarmType.NOTIFICATION
                                                          : alarm.getAlarmType();
        mapping.put("alarmType", alarmType.name());

        String count = alarm.getCount() == null ? "1"
                                               : alarm.getCount().toString();
        mapping.put("count", count);

        mapping.put("firstOccurrence",
                    nullSafeToString(alarm.getFirstOccurrence(), ""));
        mapping.put("alarmId", alarm.getId().toString());
        mapping.put("ipAddr", nullSafeToString(alarm.getIpAddr(), ""));
        mapping.put("lastOccurrence",
                    nullSafeToString(alarm.getLastOccurrence(), ""));

        if (alarm.getNodeId() != null) {
            LOG.debug("Adding nodeId: " + alarm.getNodeId().toString());
            mapping.put("nodeId", alarm.getNodeId().toString());
            String nodeLabel = m_nodeDao.getLabelForId(alarm.getNodeId());
            mapping.put("nodeLabel", nodeLabel == null ? "?" : nodeLabel);
        } else {
            mapping.put("nodeId", "");
            mapping.put("nodeLabel", "");
        }

        String poller = alarm.getPoller() == null ? "localhost"
                                                 : alarm.getPoller().getName();
        mapping.put("distPoller", poller);

        String service = alarm.getService() == null ? "" : alarm.getService();
        mapping.put("ifService", service);

        mapping.put("severity", nullSafeToString(alarm.getSeverity(), ""));
        mapping.put("ticketState",
                    nullSafeToString(alarm.getTicketState(), ""));

        mapping.put("x733AlarmType", alarm.getX733Type());

        try {
            mapping.put("x733ProbableCause",
                        nullSafeToString(x733ProbableCause.get(alarm.getX733Cause()),
                                         ""));
        } catch (Exception e) {
            LOG.info("Exception caught setting X733 Cause: {}",
                     alarm.getX733Cause(), e);
            mapping.put("x733ProbableCause", "");
        }

        buildParmMappings(alarm, mapping);

        alarmMappings.put(alarm.getId(), mapping);
        return mapping;
    }

    private String nullSafeToString(Object obj, String defaultString) {
        if (obj != null) {
            defaultString = obj.toString();
        }
        return defaultString;
    }

    private void buildParmMappings(final NorthboundAlarm alarm,
            final Map<String, Object> mapping) {
        List<EventParm<?>> parmCollection = new LinkedList<EventParm<?>>();
        String parms = alarm.getEventParms();
        if (parms == null)
            return;

        char separator = ';';
        String[] parmArray = StringUtils.split(parms, separator);
        for (String string : parmArray) {

            char nameValueDelim = '=';
            String[] nameValueArray = StringUtils.split(string,
                                                        nameValueDelim);
            String parmName = nameValueArray[0];
            String parmValue = StringUtils.split(nameValueArray[1], '(')[0];

            EventParm<String> eventParm = new EventParm<String>(parmName,
                                                                parmValue);
            parmCollection.add(eventParm);
        }

        for (int i = 0; i < parmCollection.size(); i++) {
            EventParm<?> parm = parmCollection.get(i);
            Integer parmOffset = i + 1;
            mapping.put("parm[name-#" + parmOffset + "]", parm.getParmName());
            mapping.put("parm[#" + parmOffset + "]",
                        parm.getParmValue().toString());
            mapping.put("parm[" + parm.getParmName() + "]",
                        parm.getParmValue().toString());
        }
    }

    private static class EventParm<T extends Object> {
        private String m_parmName;
        private T m_parmValue;

        EventParm(String name, T value) {
            m_parmName = name;
            m_parmValue = value;
        }

        public String getParmName() {
            return m_parmName;
        }

        public T getParmValue() {
            return (T) m_parmValue;
        }
    }

}
