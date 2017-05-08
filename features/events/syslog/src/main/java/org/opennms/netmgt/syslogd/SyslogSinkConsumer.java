/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.syslogd;

import static org.opennms.core.utils.InetAddressUtils.addr;

import java.util.List;

import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.logging.Logging;
import org.opennms.core.logging.Logging.MDCCloseable;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.syslogd.api.SyslogConnection;
import org.opennms.netmgt.syslogd.api.SyslogMessageDTO;
import org.opennms.netmgt.syslogd.api.SyslogMessageLogDTO;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;

public class SyslogSinkConsumer implements MessageConsumer<SyslogConnection, SyslogMessageLogDTO>, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(SyslogSinkConsumer.class);

    @Autowired
    private MessageConsumerManager messageConsumerManager;

    @Autowired
    private SyslogdConfig syslogdConfig;

    @Autowired
    private DistPollerDao distPollerDao;

    @Autowired
    private EventForwarder eventForwarder;

    private final String localAddr;
    private final Timer consumerTimer;
    private final Timer toEventTimer;
    private final Timer broadcastTimer;

    public SyslogSinkConsumer(MetricRegistry registry) {
        consumerTimer = registry.timer("consumer");
        toEventTimer = registry.timer("consumer.toevent");
        broadcastTimer = registry.timer("consumer.broadcast");
        localAddr = InetAddressUtils.getLocalHostName();
    }

    @Override
    public SyslogSinkModule getModule() {
        return new SyslogSinkModule(syslogdConfig, distPollerDao);
    }

    @Override
    public void handleMessage(SyslogMessageLogDTO syslogDTO) {
        try (Context consumerCtx = consumerTimer.time()) {
            try (MDCCloseable mdc = Logging.withPrefixCloseable(Syslogd.LOG4J_CATEGORY)) {
                // Convert the Syslog UDP messages to Events
                final Log eventLog;
                try (Context toEventCtx = toEventTimer.time()) {
                    eventLog = toEventLog(syslogDTO);
                }
                // Broadcast the Events to the event bus
                try (Context broadCastCtx = broadcastTimer.time()) {
                    broadcast(eventLog);
                }
            }
        }
    }

    public Log toEventLog(SyslogMessageLogDTO messageLog) {
        final Log elog = new Log();
        final Events events = new Events();
        elog.setEvents(events);
        for (SyslogMessageDTO message : messageLog.getMessages()) {
            try {
                LOG.debug("Converting syslog message into event.");
                ConvertToEvent re = new ConvertToEvent(
                        messageLog.getSystemId(),
                        messageLog.getLocation(),
                        messageLog.getSourceAddress(),
                        messageLog.getSourcePort(),
                        message.getBytes(),
                        syslogdConfig
                    );
                events.addEvent(re.getEvent());
            } catch (final MessageDiscardedException e) {
                LOG.info("Message discarded, returning without enqueueing event.", e);
            } catch (final Throwable e) {
                LOG.error("Unexpected exception while processing SyslogConnection", e);
            }
        }
        return elog;
    }

    private void broadcast(Log eventLog)  {
        if (LOG.isTraceEnabled())  {
            for (Event event : eventLog.getEvents().getEventCollection()) {
                LOG.trace("Processing a syslog to event dispatch", event.toString());
                String uuid = event.getUuid();
                LOG.trace("Event {");
                LOG.trace("  uuid  = {}", (uuid != null && uuid.length() > 0 ? uuid : "<not-set>"));
                LOG.trace("  uei   = {}", event.getUei());
                LOG.trace("  src   = {}", event.getSource());
                LOG.trace("  iface = {}", event.getInterface());
                LOG.trace("  time  = {}", event.getTime());
                LOG.trace("  Msg   = {}", event.getLogmsg().getContent());
                LOG.trace("  Dst   = {}", event.getLogmsg().getDest());
                List<Parm> parms = (event.getParmCollection() == null ? null : event.getParmCollection());
                if (parms != null) {
                    LOG.trace("  parms {");
                    for (Parm parm : parms) {
                        if ((parm.getParmName() != null)
                                && (parm.getValue().getContent() != null)) {
                            LOG.trace("    ({}, {})", parm.getParmName().trim(), parm.getValue().getContent().trim());
                        }
                    }
                    LOG.trace("  }");
                }
                LOG.trace("}");
            }
        }
        eventForwarder.sendNowSync(eventLog);

        if (syslogdConfig.getNewSuspectOnMessage()) {
            eventLog.getEvents().getEventCollection().stream()
                .filter(e -> !e.hasNodeid())
                .forEach(e -> {
                    LOG.trace("Syslogd: Found a new suspect {}", e.getInterface());
                    sendNewSuspectEvent(localAddr, e.getInterface(), e.getDistPoller());
                });
        }
    }

    private void sendNewSuspectEvent(String localAddr, String trapInterface, String distPoller) {
        EventBuilder bldr = new EventBuilder(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, "syslogd");
        bldr.setInterface(addr(trapInterface));
        bldr.setHost(localAddr);
        bldr.setDistPoller(distPoller);
        eventForwarder.sendNow(bldr.getEvent());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Automatically register the consumer on initialization
        messageConsumerManager.registerConsumer(this);
    }

    public void setEventForwarder(EventForwarder eventForwarder) {
        this.eventForwarder = eventForwarder;
    }

    public void setMessageConsumerManager(MessageConsumerManager messageConsumerManager) {
        this.messageConsumerManager = messageConsumerManager;
    }

    public void setSyslogdConfig(SyslogdConfig syslogdConfig) {
        this.syslogdConfig = syslogdConfig;
    }

    public void setDistPollerDao(DistPollerDao distPollerDao) {
        this.distPollerDao = distPollerDao;
    }
}
