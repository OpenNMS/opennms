/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.scriptd.ins.events;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.criterion.Restrictions;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.Parameter;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

class InsSession extends InsAbstractSession {
	private static final Logger LOG = LoggerFactory.getLogger(InsSession.class);

	private Socket server;

	private String line, input;

	// private CurrentAlarmsSender sendCurrentAlarmsThread=null;

	private PrintStream streamToClient;

	// Client requests
	private final static String START_AUTHENTICATION_REQUEST = "AUTH";

	private final static String LIST_CURRENT_ALARM_REQUEST = "LIST_CURRENT_ALARM_REQUEST";

	private final static String STOP_ALARM_REQUEST = "STOP_ALARM";

	// Server answers
	private final static String AUTH_REQUIRED_ACK = "AUTH_REQUIRED";

	private final static String AUTH_NOT_REQUIRED_ACK = "AUTH_NOT_REQUIRED";

	private final static String AUTHENTICATION_SUCCESS = "AUTH-SUCCESS";

	private final static String RESET_SIGNAL = "RESET";

	private final static String ACTIVE_ALARM_BEGIN = "ACTIVE_ALARM_BEGIN";

	private final static String ACTIVE_ALARM_END = "ACTIVE_ALARM_END";

	// session statuses
	private final int STARTING_SESSION_STATUS = 0;

	private final int AUTHENTICATING_STATUS = 1;

	private final int AUTHENTICATED_STATUS = 2;

	private final int DATAFLOW_STATUS = 3;

	private int status = STARTING_SESSION_STATUS;
	
	private List<Event> m_events = new ArrayList<Event>();

	InsSession(Socket server) throws IOException {
		this.server = server;
		streamToClient = new PrintStream(server.getOutputStream());
	}

	public void run() {
		input = "";

		InputStreamReader isr = null;
		BufferedReader in = null;
		
		try {
			// Get input from the client
			isr = new InputStreamReader(server.getInputStream());
			in = new BufferedReader(isr);

			readingFromClient: while ((line = in.readLine()) != null) {
				input = input + "\n" + line;
				LOG.debug("Client wrote: {} from {}", line, server.getInetAddress());

				if (status == STARTING_SESSION_STATUS) {
					if (line.equalsIgnoreCase(START_AUTHENTICATION_REQUEST)) {
						if (getSharedASCIIString() != null) {
							LOG.debug("Starting authentication, sending {} to the client", AUTH_REQUIRED_ACK);
							streamToClient.println(AUTH_REQUIRED_ACK);
							status = AUTHENTICATING_STATUS;
						} else {
							LOG.debug("Starting authentication, sending {} to the client", AUTH_NOT_REQUIRED_ACK);
							streamToClient.println(AUTH_NOT_REQUIRED_ACK);
							status = AUTHENTICATED_STATUS;
						}
						continue readingFromClient;
					} else {
						// security reset (a malicious user may use DOS attack before authentication)
						LOG.warn("Wrong client request");
						break readingFromClient;
					}
				}

				if (status == AUTHENTICATING_STATUS) {
					if (getSharedASCIIString() != null) {
						// authentication required (security check)
						if (line.equals(getSharedASCIIString())) {
							status = AUTHENTICATED_STATUS;
							LOG.debug("Authentication success!");
							streamToClient.println(AUTHENTICATION_SUCCESS);
							continue readingFromClient;
						} else {
							streamToClient.println(RESET_SIGNAL);
							LOG.warn("Authentication failure! Resetting session.");
							break readingFromClient;
						}
					}
				}

				if (status == AUTHENTICATED_STATUS || status == DATAFLOW_STATUS) {
					if (line.equalsIgnoreCase(LIST_CURRENT_ALARM_REQUEST)) {
				        LOG.debug("Fetching Events from Database");
					    getEventsByCriteria();
						status = DATAFLOW_STATUS;
						synchronized (streamToClient) {
							 streamToClient.println(ACTIVE_ALARM_BEGIN);
	                         final StringWriter sw = getOutput();
  		                     if (sw != null) {
   		                        final String output = sw.toString();
								LOG.info("String Writer: {}", output); 
		                        streamToClient.print(output);
		                     } else {
		                        LOG.error("String Writer is null");
		                     }
                             streamToClient.println(ACTIVE_ALARM_END);
                             continue readingFromClient;
							
						}
					} else {
						if (line.equalsIgnoreCase(STOP_ALARM_REQUEST)) {
							LOG.debug("Closing session due client request.");
							break readingFromClient;
						} else {
							LOG.warn("Wrong client request");
							continue readingFromClient;
						}
					}
				}
			}

			LOG.debug("Closing session.  Overall message from {} is: {}", server.getInetAddress(), input);
			server.close();
		} catch (final IOException ioe) {
			LOG.warn("Error while listening to socket.", ioe);
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(isr);
		}
	}

	public PrintStream getStreamToClient() {
		if (status == DATAFLOW_STATUS)
			return streamToClient;
		else
			return null;
	}

	private StringWriter getOutput() {
		LOG.debug("Sending alarms to the client");

		final StringWriter sw = new StringWriter();
		final List<Event> events = getEvents();
        if (events != null) {
        	for (final Event xmlEvent : events) {
        		LOG.info("Marshal Event with id: {}", xmlEvent.getDbid()); 
                JaxbUtils.marshal(xmlEvent, sw);
                LOG.debug("Flushing Event with id: {}", xmlEvent.getDbid()); 
                sw.flush();
            }
        }
        return sw;

	}

	private Event getXMLEvent(final OnmsEvent ev) {
		final Integer id = ev.getId();

		LOG.info("Working on XML Event for id: {}", id); 
		LOG.debug("Setting Event id: {}", id); 
		final Event e = new Event();
        e.setDbid(id);

        //UEI
        final String uei = ev.getEventUei();
		if (uei != null) {
            LOG.debug("Setting Event uei: {}", uei); 
            e.setUei(uei);
        } else {
        	LOG.warn("No Event uei found: skipping event....");
            return null;
        }

        // Source
        final String source = ev.getEventSource();
		if (source != null) {
        	LOG.debug("Setting Event source: {}", source); 
            e.setSource(source);
        } else {
        	LOG.info("No Event source found."); 
        }

        //nodeid
        final Integer nodeid = ev.getNode().getId();
		if (ev.getNode() != null && nodeid != null) {
            LOG.debug("Setting Event nodeid: {}", nodeid); 
            e.setNodeid(nodeid.longValue());
        } else {
            LOG.info("No Event node found."); 
        }

        // timestamp
        final Date time = ev.getEventTime();
		if (time != null) {
            LOG.debug("Setting event date timestamp to (GMT): {}", time);
            e.setTime(EventConstants.formatToString(time));
        } else {
        	LOG.info("No Event time found."); 
        }
        
        // host
        final String host = ev.getEventHost();
		if (host != null) {
            LOG.debug("Setting Event Host: {}", host);
            e.setHost(host);
        } else {
        	LOG.info("No Event host found.");
        }
        
        // interface
        final InetAddress ipAddr = ev.getIpAddr();
		if (ipAddr != null) {
            LOG.debug("Setting Event Interface/ipaddress: {}", ipAddr);
            e.setInterfaceAddress(ipAddr);
        } else {
            LOG.info("No Event ip address found.");
        }
        
        // Service Name
        if (ev.getServiceType() != null) {
            final String serviceName = ev.getServiceType().getName();
			LOG.debug("Setting Event Service Name: {}", serviceName);
            e.setService(serviceName);
        } else {
        	LOG.info("No Event service name found.");
        }

        // Description
        final String descr = ev.getEventDescr();
		if (descr != null ) {
            LOG.debug("Setting Event Description: {}", descr);
            e.setDescr(descr);
        } else {
            LOG.info("No Event ip address found.");
        }
        
        // Log message
        final String logmsg = ev.getEventLogMsg();
		if (logmsg != null) {
        	final Logmsg msg = new Logmsg();
            LOG.debug("Setting Event Log Message: {}", logmsg);
            msg.setContent(logmsg);
            e.setLogmsg(msg);
        } else {
            LOG.info("No Event log Message found.");
        }

        // severity
        final Integer severity = ev.getEventSeverity();
		if (severity != null) {
            LOG.debug("Setting Event Severity: {}", severity);
            e.setSeverity(OnmsSeverity.get(severity).getLabel());
        } else {
            LOG.info("No Event severity found.");
        }

          final Integer ifIndex = ev.getIfIndex();
		if (ifIndex != null && ifIndex > 0 ) {
              e.setIfIndex(ifIndex);
              e.setIfAlias(getIfAlias(nodeid,ifIndex));
          } else {
              e.setIfIndex(-1);
              e.setIfAlias("-1");
          }

        
        // operator Instruction
        final String operInstruct = ev.getEventOperInstruct();
		if (operInstruct != null) {
            LOG.debug("Setting Event Operator Instruction: {}", operInstruct);
            e.setOperinstruct(operInstruct);
        } else {
            LOG.info("No Event operator Instruction found.");
        }

        // parms
        final String eventParms = ev.getEventParms();
		if (eventParms != null) {
        	LOG.debug("Setting Event Parms: {}", eventParms);
        	final List<Parm> parms = Parameter.decode(eventParms);
            if (parms != null) e.setParmCollection(parms);
        } else {
            LOG.info("No Event parms found.");
        }

		final AlarmData ad = new AlarmData();
		final OnmsAlarm onmsAlarm = ev.getAlarm();
        try {
            if (onmsAlarm != null) {
                ad.setReductionKey(onmsAlarm.getReductionKey());
                ad.setAlarmType(onmsAlarm.getAlarmType());
                ad.setClearKey(onmsAlarm.getClearKey());
                e.setAlarmData(ad);
            }
        } catch (final ObjectNotFoundException e1) {
            LOG.warn("Correlated alarm data not found.", e1);
        }
        LOG.info("Returning event with id: {}", id);
        return e;
    }
	
    private void getEventsByCriteria() {
    	LOG.debug("clearing events");

    	clearEvents();
    	final BeanFactoryReference bf = BeanUtils.getBeanFactory("daoContext");
        final EventDao eventDao = BeanUtils.getBean(bf,"eventDao", EventDao.class);
        final TransactionTemplate transTemplate = BeanUtils.getBean(bf, "transactionTemplate",TransactionTemplate.class);
        try {
                transTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                public void doInTransactionWithoutResult(final TransactionStatus status) {
                	LOG.debug("Entering transaction call back: selection with criteria: {}", criteriaRestriction);
                    final OnmsCriteria criteria = new OnmsCriteria(OnmsEvent.class);
                    criteria.add(Restrictions.sqlRestriction(criteriaRestriction));
                    
                    final List<OnmsEvent> events = eventDao.findMatching(criteria);
                    LOG.info("Found {} event(s) with criteria: {}", events.size(), criteriaRestriction);
                    
                    for (final OnmsEvent onmsEvent : events) {
                    	final Event xmlEvent = getXMLEvent(onmsEvent);
                        if (xmlEvent != null) addEvent(xmlEvent);
                    }
                }

            });
        
        } catch (final RuntimeException e) {
            LOG.error("Error while getting events.", e);
        }
        
    }
	
	private void addEvent(final Event event) {
	    m_events.add(event);	    
	}
	
    private void clearEvents() {
        m_events.clear();
        
    }

    private List<Event> getEvents() {
        return m_events;      
    }
    
    final static char MULTIPLE_VAL_DELIM = ';';
    final static char NAME_VAL_DELIM = '=';
    final static char DB_ATTRIB_DELIM = ',';
    
 
}
