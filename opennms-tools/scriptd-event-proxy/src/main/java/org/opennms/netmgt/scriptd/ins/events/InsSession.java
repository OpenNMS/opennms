package org.opennms.netmgt.scriptd.ins.events;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.criterion.Restrictions;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.model.events.Constants;
import org.opennms.netmgt.model.events.Parameter;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;

import org.opennms.netmgt.xml.event.Parms;

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
        Category log = getLog();
		input = "";

		try {
			// Get input from the client
			BufferedReader in = new BufferedReader(new InputStreamReader(server
					.getInputStream()));

			readingFromClient: while ((line = in.readLine()) != null) {
				input = input + "\n" + line;
				log.debug("Client wrote: " + line + " from "
						+ server.getInetAddress());

				if (status == STARTING_SESSION_STATUS) {
					if (line.equalsIgnoreCase(START_AUTHENTICATION_REQUEST)) {
						if (sharedAuthAsciiString != null) {
							// authorization required
							streamToClient.println(AUTH_REQUIRED_ACK);
							log.debug("Starting authentication, sending "
									+ AUTH_REQUIRED_ACK + " to the client");
							status = AUTHENTICATING_STATUS;
						} else {
							// authorization not required
							streamToClient.println(AUTH_NOT_REQUIRED_ACK);
							log.debug("Starting authentication, sending "
									+ AUTH_NOT_REQUIRED_ACK + " to the client");
							status = AUTHENTICATED_STATUS;
						}
						continue readingFromClient;
					} else {
						// security reset (a malicious user may use DOS attack
						// before authentication)
						log.warn("Wrong client request");
						break readingFromClient;
					}
				}

				if (status == AUTHENTICATING_STATUS) {
					if (sharedAuthAsciiString != null) {
						// authentication required (security check)
						if (line.equals(sharedAuthAsciiString)) {
							status = AUTHENTICATED_STATUS;
							log.debug("Authentication success!");
							streamToClient.println(AUTHENTICATION_SUCCESS);
							continue readingFromClient;
						} else {
							streamToClient.println(RESET_SIGNAL);
							log
									.warn("Authentication failure! Resetting session.");
							break readingFromClient;
						}
					}
				}

				if (status == AUTHENTICATED_STATUS || status == DATAFLOW_STATUS) {
					if (line.equalsIgnoreCase(LIST_CURRENT_ALARM_REQUEST)) {
				        log.debug("Fetching Events from Database");
					    getEventsByCriteria();
						status = DATAFLOW_STATUS;
						synchronized (streamToClient) {
							 streamToClient.println(ACTIVE_ALARM_BEGIN);
	                         StringWriter sw = getOutput();
  		                     if (sw != null) {
   		                        log.info("String Writer:" + sw.getBuffer().toString()); 
		                        streamToClient.print(sw.toString());
		                     } else {
		                        log.error("String Writer is null");
//			                      break readingFromClient;
		                     }
                             streamToClient.println(ACTIVE_ALARM_END);
                             continue readingFromClient;
							
						}
					} else {
						if (line.equalsIgnoreCase(STOP_ALARM_REQUEST)) {
							log.debug("Closing session due client request.");
							break readingFromClient;
						} else {
							log.warn("Wrong client request");
							continue readingFromClient;
						}
					}
				}
			}

			log.debug("Overall message from " + server.getInetAddress()
					+ " is:" + input);
			log.debug("\nClosing session with " + server.getInetAddress()
					+ "...\n\n");
			server.close();
		} catch (IOException ioe) {
			log.warn("IOException on socket listen: " + ioe, ioe);
		}
	}

	public PrintStream getStreamToClient() {
		if (status == DATAFLOW_STATUS)
			return streamToClient;
		else
			return null;
	}

	private StringWriter getOutput() {
        Category log = getLog();
        log.debug("Sending alarms to the client");
        StringWriter sw = new StringWriter();
        List<Event> events = getEvents();
        if (events != null && events.size() > 0) {
            Iterator<Event> ite = events.iterator();
            while (ite.hasNext()) {
                Event xmlEvent = ite.next();
                try {
                    log.info("Marshal Event with id: " + xmlEvent.getDbid()); 
                    xmlEvent.marshal(sw);
                    log.info("Flushing Event with id: " + xmlEvent.getDbid()); 
                    sw.flush();
                } catch (MarshalException e) {
                    log.error("Marshall Exception: " + e);
                } catch (ValidationException e) {
                    log.error("Validation Exception: " + e);
                }
            }
        }
        return sw;

	}

	private Event getXMLEvent(OnmsEvent ev) {
        Category log = getLog();
        log.info("Working on XML Event for id: " + ev.getId()); 
        log.debug("Setting Event id: " + ev.getId()); 
        Event e = new Event();
        e.setDbid(ev.getId());

        //UEI
        if (ev.getEventUei() != null ) {
            log.debug("Setting Event uei: " + ev.getEventUei()); 
            e.setUei(ev.getEventUei());
        } else {
            log.warn("No Event uei found: skipping event....");
            return null;
        }

        // Source
        if (ev.getEventSource() != null ) {
            log.debug("Setting Event source: " + ev.getEventSource()); 
            e.setSource(ev.getEventSource());
        } else {
            log.info("No Event source found."); 
        }

        //nodeid
        if (ev.getNode() != null) {
            log.debug("Setting Event nodeid: " + ev.getNode().getId()); 
            e.setNodeid(ev.getNode().getId());
        } else {
            log.info("No Event node found."); 
        }


        // timestamp
        if (ev.getEventTime() != null) {
            log.debug("Setting event date timestamp to GMT");
            DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            log.debug("Setting Event Time: " + ev.getEventTime()); 
            e.setTime(dateFormat.format(ev.getEventTime()));
        } else {
            log.info("No Event time found."); 
        }
        
        // host
        if (ev.getEventHost() != null) {
            log.debug("Setting Event Host: " + ev.getEventHost());
            e.setHost(ev.getEventHost());
        } else {
            log.info("No Event host found.");
        }
        
        // interface
        if (ev.getIpAddr() != null) {
            log.debug("Setting Event Interface/ipaddress: " + ev.getIpAddr());
            e.setInterface(ev
                           .getIpAddr());
        } else {
            log.info("No Event ip address found.");
        }
        
        // Service Name
        if (ev.getServiceType() != null) {
            log.debug("Setting Event Service Name: " + ev.getServiceType().getName());
            e.setService(ev.getServiceType().getName());
        } else {
            log.info("No Event service name found.");
        }

        // Description
        if (ev.getEventDescr() != null ) {
            log.debug("Setting Event Description");
            e.setDescr(ev.getEventDescr());
        } else {
            log.info("No Event ip address found.");
        }
        
        // Log message
        if (ev.getEventLogMsg() != null) {
            Logmsg msg = new Logmsg();
            log.debug("Setting Event Log Message");
            msg.setContent(ev.getEventLogMsg());
            e.setLogmsg(msg);
        } else {
            log.info("No Event log Message found.");
        }

        // severity
        if (ev.getEventSeverity() != null) {
            log.debug("Setting Event Severity");
            e.setSeverity(Constants.getSeverityString(ev.getEventSeverity()));
        } else {
            log.info("No Event severity found.");
        }

          if (ev.getIfIndex() != null && ev.getIfIndex() > 0 ) {
              e.setIfIndex(ev.getIfIndex());
              e.setIfAlias(getIfAlias(ev.getNode().getId(),ev.getIfIndex()));
          } else {
              e.setIfIndex(-1);
              e.setIfAlias("-1");
          }

        
        // operator Instruction
        if (ev.getEventOperInstruct() != null) {
            log.debug("Setting Event Operator Instruction");
            e.setOperinstruct(ev.getEventOperInstruct());
        } else {
            log.info("No Event operator Instruction found.");
        }

        // parms
        if (ev.getEventParms() != null ) {
            log.debug("Setting Event Parms: " + ev.getEventParms());
            Parms parms = Parameter.decode(ev.getEventParms());
            if (parms != null ) e.setParms(parms);
        } else {
            log.info("No Event parms found.");
        }

        AlarmData ad = new AlarmData();
        OnmsAlarm onmsAlarm = ev
                .getAlarm();
        try {
            if (onmsAlarm != null) {
                ad
                        .setReductionKey(onmsAlarm
                                .getReductionKey());
                ad
                        .setAlarmType(onmsAlarm
                                .getAlarmType());
                ad
                        .setClearKey(onmsAlarm
                                .getClearKey());
                e
                        .setAlarmData(ad);
            }
        } catch (ObjectNotFoundException e1) {
            log
                    .warn("correlated alarm data not found "
                            + e1);
        }
        log
        .info("return Event with id: " + ev.getId()); 
        return e;
    }
	
	@SuppressWarnings("unchecked")
    private void getEventsByCriteria() {
        Category log = getLog();
        log.debug("Entering getEventsByCriteria.....");
        log.debug("clearing events");
        clearEvents();
        BeanFactoryReference bf = BeanUtils.getBeanFactory("daoContext");
        final EventDao eventDao = BeanUtils.getBean(bf,"eventDao", EventDao.class);
        TransactionTemplate transTemplate = BeanUtils.getBean(bf, "transactionTemplate",TransactionTemplate.class);
        try {
                transTemplate.execute(new TransactionCallback() {
                public Object doInTransaction(final TransactionStatus status) {
                    Category log = getLog();
                    log.debug("entering transaction call back: selection with criteria: " + criteriaRestriction);
                    final OnmsCriteria criteria = new OnmsCriteria(OnmsEvent.class);
                    criteria.add(Restrictions.sqlRestriction(criteriaRestriction));
                    List<OnmsEvent> events = eventDao.findMatching(criteria);
                    log.info("Found "+ events.size() + " event/s (with criteria): " + criteriaRestriction);
                    if (events != null && events.size()>0) {
                        Iterator<OnmsEvent> ite = events.iterator();
                        while (ite.hasNext()) {
                            Event xmlEvent = getXMLEvent(ite.next());
                            if (xmlEvent != null) addEvent(xmlEvent);
                        }
                    }
                    return new Object();
                }

            });
        
        } catch (final RuntimeException e) {
            log.error("Error while getting events ",e);
        }
        
    }
	
	private void addEvent(Event event) {
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
