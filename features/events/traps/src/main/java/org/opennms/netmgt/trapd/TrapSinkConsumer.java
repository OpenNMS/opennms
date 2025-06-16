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
package org.opennms.netmgt.trapd;

import static org.opennms.core.utils.InetAddressUtils.addr;

import javax.annotation.PostConstruct;

import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.logging.Logging;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.TrapdConfig;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.trapd.jmx.TrapdInstrumentation;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.eventconf.LogDestType;
import org.opennms.netmgt.xml.eventconf.Logmsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class TrapSinkConsumer implements MessageConsumer<TrapInformationWrapper, TrapLogDTO> {

	public static final TrapdInstrumentation trapdInstrumentation = new TrapdInstrumentation();

	private static final Logger LOG = LoggerFactory.getLogger(TrapSinkConsumer.class);

	/**
	 * The name of the local host.
	 */
	private static final String LOCALHOST_ADDRESS = InetAddressUtils.getLocalHostName();

	@Autowired
	private MessageConsumerManager messageConsumerManager;

	@Autowired
	private EventConfDao eventConfDao;

	@Autowired
	@Qualifier("eventIpcManager")
	private EventForwarder eventForwarder;

	@Autowired
	private InterfaceToNodeCache interfaceToNodeCache;

	@Autowired
	private TrapdConfig config;

	@Autowired
	private DistPollerDao distPollerDao;

	private EventCreator eventCreator;

	@PostConstruct
	public void init() throws Exception {
		messageConsumerManager.registerConsumer(this);
		eventCreator = new EventCreator(interfaceToNodeCache, eventConfDao);
	}

	@Override
	public SinkModule<TrapInformationWrapper, TrapLogDTO> getModule() {
		return new TrapSinkModule(config, distPollerDao.whoami());
	}

	@Override
	public void handleMessage(TrapLogDTO messageLog) {
		try (Logging.MDCCloseable mdc = Logging.withPrefixCloseable(Trapd.LOG4J_CATEGORY)) {
			final Log eventLog = toLog(messageLog);

			eventForwarder.sendNowSync(eventLog);

			// If configured, also send events for new suspects
			if (config.getNewSuspectOnTrap()) {
				eventLog.getEvents().getEventCollection().stream()
						.filter(e -> !e.hasNodeid())
						.forEach(e -> {
							sendNewSuspectEvent(e.getInterface(), e.getDistPoller());
							LOG.debug("Sent newSuspectEvent for interface {}", e.getInterface());
						});
			}
		}
	}

	private Log toLog(TrapLogDTO messageLog) {
		final Log log = new Log();
		final Events events = new Events();
		log.setEvents(events);

		for (TrapDTO eachMessage : messageLog.getMessages()) {
			try {
				final Event event = eventCreator.createEventFrom(
						eachMessage,
						messageLog.getSystemId(),
						messageLog.getLocation(),
						messageLog.getTrapAddress()
				);
				if (!shouldDiscard(event)) {
					if (event.getSnmp() != null) {
						trapdInstrumentation.incTrapsReceivedCount(event.getSnmp().getVersion());
					}
					events.addEvent(event);
				} else {
					LOG.debug("Trap discarded due to matching event having logmsg dest == discardtraps");
					trapdInstrumentation.incDiscardCount();
				}
			} catch (Throwable e) {
				LOG.error("Unexpected error processing trap: {}", eachMessage, e);
				trapdInstrumentation.incErrorCount();
			}
		}
		return log;
	}

	private void sendNewSuspectEvent(String trapInterface, String distPoller) {
		// construct event with 'trapd' as source
		EventBuilder bldr = new EventBuilder(org.opennms.netmgt.events.api.EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, "trapd");
		bldr.setInterface(addr(trapInterface));
		bldr.setHost(LOCALHOST_ADDRESS);
		bldr.setDistPoller(distPoller);

		// send the event to eventd
		eventForwarder.sendNow(bldr.getEvent());
	}

	private boolean shouldDiscard(Event event) {
		org.opennms.netmgt.xml.eventconf.Event econf = eventConfDao.findByEvent(event);
		if (econf != null) {
			final Logmsg logmsg = econf.getLogmsg();
			return logmsg != null && LogDestType.DISCARDTRAPS.equals(logmsg.getDest());
		}
		return false;
	}
}
