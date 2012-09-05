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
import org.opennms.core.utils.LogUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.Parameter;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;
import org.opennms.netmgt.xml.event.Parm;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

class InsSession extends InsAbstractSession {


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
				LogUtils.debugf(this, "Client wrote: %s from %s", line, server.getInetAddress());

				if (status == STARTING_SESSION_STATUS) {
					if (line.equalsIgnoreCase(START_AUTHENTICATION_REQUEST)) {
						if (getSharedASCIIString() != null) {
							LogUtils.debugf(this, "Starting authentication, sending %s to the client", AUTH_REQUIRED_ACK);
							streamToClient.println(AUTH_REQUIRED_ACK);
							status = AUTHENTICATING_STATUS;
						} else {
							LogUtils.debugf(this, "Starting authentication, sending %s to the client", AUTH_NOT_REQUIRED_ACK);
							streamToClient.println(AUTH_NOT_REQUIRED_ACK);
							status = AUTHENTICATED_STATUS;
						}
						continue readingFromClient;
					} else {
						// security reset (a malicious user may use DOS attack before authentication)
						LogUtils.warnf(this, "Wrong client request");
						break readingFromClient;
					}
				}

				if (status == AUTHENTICATING_STATUS) {
					if (getSharedASCIIString() != null) {
						// authentication required (security check)
						if (line.equals(getSharedASCIIString())) {
							status = AUTHENTICATED_STATUS;
							LogUtils.debugf(this, "Authentication success!");
							streamToClient.println(AUTHENTICATION_SUCCESS);
							continue readingFromClient;
						} else {
							streamToClient.println(RESET_SIGNAL);
							LogUtils.warnf(this, "Authentication failure! Resetting session.");
							break readingFromClient;
						}
					}
				}

				if (status == AUTHENTICATED_STATUS || status == DATAFLOW_STATUS) {
					if (line.equalsIgnoreCase(LIST_CURRENT_ALARM_REQUEST)) {
				        LogUtils.debugf(this, "Fetching Events from Database");
					    getEventsByCriteria();
						status = DATAFLOW_STATUS;
						synchronized (streamToClient) {
							 streamToClient.println(ACTIVE_ALARM_BEGIN);
	                         final StringWriter sw = getOutput();
  		                     if (sw != null) {
   		                        final String output = sw.toString();
								LogUtils.infof(this, "String Writer: %s", output); 
		                        streamToClient.print(output);
		                     } else {
		                        LogUtils.errorf(this, "String Writer is null");
		                     }
                             streamToClient.println(ACTIVE_ALARM_END);
                             continue readingFromClient;
							
						}
					} else {
						if (line.equalsIgnoreCase(STOP_ALARM_REQUEST)) {
							LogUtils.debugf(this, "Closing session due client request.");
							break readingFromClient;
						} else {
							LogUtils.warnf(this, "Wrong client request");
							continue readingFromClient;
						}
					}
				}
			}

			LogUtils.debugf(this, "Closing session.  Overall message from %s is: %s", server.getInetAddress(), input);
			server.close();
		} catch (final IOException ioe) {
			LogUtils.warnf(this, ioe, "Error while listening to socket.");
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
		LogUtils.debugf(this, "Sending alarms to the client");

		final StringWriter sw = new StringWriter();
		final List<Event> events = getEvents();
        if (events != null) {
        	for (final Event xmlEvent : events) {
        		LogUtils.infof(this, "Marshal Event with id: %s", xmlEvent.getDbid()); 
                JaxbUtils.marshal(xmlEvent, sw);
                LogUtils.debugf(this, "Flushing Event with id: %s", xmlEvent.getDbid()); 
                sw.flush();
            }
        }
        return sw;

	}

	private Event getXMLEvent(final OnmsEvent ev) {
		final Integer id = ev.getId();

		LogUtils.infof(this, "Working on XML Event for id: %s", id); 
		LogUtils.debugf(this, "Setting Event id: %s", id); 
		final Event e = new Event();
        e.setDbid(id);

        //UEI
        final String uei = ev.getEventUei();
		if (uei != null) {
            LogUtils.debugf(this, "Setting Event uei: %s", uei); 
            e.setUei(uei);
        } else {
        	LogUtils.warnf(this, "No Event uei found: skipping event....");
            return null;
        }

        // Source
        final String source = ev.getEventSource();
		if (source != null) {
        	LogUtils.debugf(this, "Setting Event source: %s", source); 
            e.setSource(source);
        } else {
        	LogUtils.infof(this, "No Event source found."); 
        }

        //nodeid
        final Integer nodeid = ev.getNode().getId();
		if (ev.getNode() != null && nodeid != null) {
            LogUtils.debugf(this, "Setting Event nodeid: %s", nodeid); 
            e.setNodeid(nodeid.longValue());
        } else {
            LogUtils.infof(this, "No Event node found."); 
        }

        // timestamp
        final Date time = ev.getEventTime();
		if (time != null) {
            LogUtils.debugf(this, "Setting event date timestamp to (GMT): %s", time);
            e.setTime(EventConstants.formatToString(time));
        } else {
        	LogUtils.infof(this, "No Event time found."); 
        }
        
        // host
        final String host = ev.getEventHost();
		if (host != null) {
            LogUtils.debugf(this, "Setting Event Host: %s", host);
            e.setHost(host);
        } else {
        	LogUtils.infof(this, "No Event host found.");
        }
        
        // interface
        final InetAddress ipAddr = ev.getIpAddr();
		if (ipAddr != null) {
            LogUtils.debugf(this, "Setting Event Interface/ipaddress: %s", ipAddr);
            e.setInterfaceAddress(ipAddr);
        } else {
            LogUtils.infof(this, "No Event ip address found.");
        }
        
        // Service Name
        if (ev.getServiceType() != null) {
            final String serviceName = ev.getServiceType().getName();
			LogUtils.debugf(this, "Setting Event Service Name: %s", serviceName);
            e.setService(serviceName);
        } else {
        	LogUtils.infof(this, "No Event service name found.");
        }

        // Description
        final String descr = ev.getEventDescr();
		if (descr != null ) {
            LogUtils.debugf(this, "Setting Event Description: %s", descr);
            e.setDescr(descr);
        } else {
            LogUtils.infof(this, "No Event ip address found.");
        }
        
        // Log message
        final String logmsg = ev.getEventLogMsg();
		if (logmsg != null) {
        	final Logmsg msg = new Logmsg();
            LogUtils.debugf(this, "Setting Event Log Message: %s", logmsg);
            msg.setContent(logmsg);
            e.setLogmsg(msg);
        } else {
            LogUtils.infof(this, "No Event log Message found.");
        }

        // severity
        final Integer severity = ev.getEventSeverity();
		if (severity != null) {
            LogUtils.debugf(this, "Setting Event Severity: %s", severity);
            e.setSeverity(OnmsSeverity.get(severity).getLabel());
        } else {
            LogUtils.infof(this, "No Event severity found.");
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
            LogUtils.debugf(this, "Setting Event Operator Instruction: %s", operInstruct);
            e.setOperinstruct(operInstruct);
        } else {
            LogUtils.infof(this, "No Event operator Instruction found.");
        }

        // parms
        final String eventParms = ev.getEventParms();
		if (eventParms != null) {
        	LogUtils.debugf(this, "Setting Event Parms: %s", eventParms);
        	final List<Parm> parms = Parameter.decode(eventParms);
            if (parms != null) e.setParmCollection(parms);
        } else {
            LogUtils.infof(this, "No Event parms found.");
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
            LogUtils.warnf(this, e1, "Correlated alarm data not found.");
        }
        LogUtils.infof(this, "Returning event with id: %s", id);
        return e;
    }
	
    private void getEventsByCriteria() {
    	LogUtils.debugf(this, "clearing events");

    	clearEvents();
    	final BeanFactoryReference bf = BeanUtils.getBeanFactory("daoContext");
        final EventDao eventDao = BeanUtils.getBean(bf,"eventDao", EventDao.class);
        final TransactionTemplate transTemplate = BeanUtils.getBean(bf, "transactionTemplate",TransactionTemplate.class);
        try {
                transTemplate.execute(new TransactionCallback<Object>() {
                public Object doInTransaction(final TransactionStatus status) {
                	LogUtils.debugf(this, "Entering transaction call back: selection with criteria: %s", criteriaRestriction);
                    final OnmsCriteria criteria = new OnmsCriteria(OnmsEvent.class);
                    criteria.add(Restrictions.sqlRestriction(criteriaRestriction));
                    
                    final List<OnmsEvent> events = eventDao.findMatching(criteria);
                    LogUtils.infof(this, "Found %d event(s) with criteria: %s", events.size(), criteriaRestriction);
                    
                    for (final OnmsEvent onmsEvent : events) {
                    	final Event xmlEvent = getXMLEvent(onmsEvent);
                        if (xmlEvent != null) addEvent(xmlEvent);
                    }
                    return new Object();
                }

            });
        
        } catch (final RuntimeException e) {
            LogUtils.errorf(this, e, "Error while getting events.");
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
