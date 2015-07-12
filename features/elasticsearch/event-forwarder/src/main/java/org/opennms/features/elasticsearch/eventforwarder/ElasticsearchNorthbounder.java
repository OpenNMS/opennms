package org.opennms.features.elasticsearch.eventforwarder;

import org.opennms.core.soa.Registration;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.soa.support.DefaultServiceRegistry;
import org.opennms.features.elasticsearch.eventforwarder.internal.DefaultAlarmForwarder;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.api.Northbounder;
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

    private ServiceRegistry serviceRegistry;
    private volatile DefaultAlarmForwarder alarmForwarder;

    private static final Logger LOG = LoggerFactory.getLogger(ForwardingEventListener.class);

    private Registration registration = null;

    public ElasticsearchNorthbounder() {
        super("ElasticsearchNorthbounder");
        LOG.debug("ElasticsearchNorthbounder created");
    }

    public void init() {
        if(serviceRegistry==null) {
            LOG.info("ElasticsearchNorthbounder serviceRegistry not found, accessing static instance");
            serviceRegistry = DefaultServiceRegistry.INSTANCE;
        }

        LOG.info("ElasticsearchNorthbounder init");
        if(serviceRegistry !=null) {
            LOG.debug("Registering ElasticsearchNorthbounder destination to registry: "+serviceRegistry);
            LOG.debug("Registry class name: "+serviceRegistry.getClass().getCanonicalName());
            registration=serviceRegistry.register(this, org.opennms.netmgt.alarmd.api.Northbounder.class);
        } else {
            LOG.warn("ElasticsearchNorthbounder could not register itself on the Service Registry because it is null. Alarms will not be forwarded to ES");
        }
    }

    @Override
    protected boolean accepts(NorthboundAlarm alarm) {
        LOG.debug("ElasticsearchNorthbounder accepts:"+alarm);
        return true; // accept ANY alarm
    }

    @Override
    public void forwardAlarms(List<NorthboundAlarm> alarms) throws NorthbounderException {
        for(NorthboundAlarm alarm: alarms) {
            LOG.debug("ElasticsearchNorthbounder Forwarding alarm: "+alarm);
            alarmForwarder.sendNow(alarm);
        }
    }

    public DefaultAlarmForwarder getAlarmForwarder() {
        return alarmForwarder;
    }

    public void setAlarmForwarder(DefaultAlarmForwarder alarmForwarder) {
        this.alarmForwarder = alarmForwarder;
    }

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
}
