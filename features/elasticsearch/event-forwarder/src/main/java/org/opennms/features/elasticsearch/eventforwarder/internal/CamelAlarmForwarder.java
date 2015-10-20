package org.opennms.features.elasticsearch.eventforwarder.internal;

import org.opennms.netmgt.alarmd.api.NorthboundAlarm;

/**
 * Created:
 * User: unicoletti
 * Date: 1:48 PM 7/10/15
 */
public interface CamelAlarmForwarder {
    public void sendNow(NorthboundAlarm alarm);
}
