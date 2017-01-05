package org.opennms.features.elasticsearch.eventforwarder;

import org.opennms.features.elasticsearch.eventforwarder.internal.DefaultAlarmForwarder;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.api.NorthbounderException;
import org.opennms.netmgt.alarmd.api.support.AbstractNorthbounder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Registers as an alam northbounder and forwards alarm to ES
 *
 * Created:
 * User: unicoletti
 * Date: 1:25 PM 7/10/15
 */
public class ElasticsearchNorthbounder extends AbstractNorthbounder {

    private volatile DefaultAlarmForwarder alarmForwarder;

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchNorthbounder.class);

    public ElasticsearchNorthbounder() {
        super("ElasticsearchNorthbounder");
        LOG.debug("ElasticsearchNorthbounder created");
    }

    @Override
    protected boolean accepts(NorthboundAlarm alarm) {
        return true; // accept ANY alarm
    }

    @Override
    public void forwardAlarms(List<NorthboundAlarm> alarms) throws NorthbounderException {
        for(NorthboundAlarm alarm: alarms) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("ElasticsearchNorthbounder Forwarding alarm: "+alarm);
            }
            alarmForwarder.sendNow(alarm);
        }
    }

    public DefaultAlarmForwarder getAlarmForwarder() {
        return alarmForwarder;
    }

    public void setAlarmForwarder(DefaultAlarmForwarder alarmForwarder) {
        this.alarmForwarder = alarmForwarder;
    }
}
