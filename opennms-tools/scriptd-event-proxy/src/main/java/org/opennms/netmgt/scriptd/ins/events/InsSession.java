package org.opennms.netmgt.scriptd.ins.events;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Category;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.criterion.Restrictions;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.eventd.db.Constants;
import org.opennms.netmgt.eventd.db.Parameter;
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

	private Category log;

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

	InsSession(Socket server) throws IOException {
		log = ThreadCategory.getInstance(this.getClass());
		this.server = server;
		streamToClient = new PrintStream(server.getOutputStream());
	}

	public void run() {
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
						log.debug("Sending alarms to the client");
						status = DATAFLOW_STATUS;
						synchronized (streamToClient) {
							streamToClient.println(ACTIVE_ALARM_BEGIN);

							BeanFactoryReference bf = BeanUtils
									.getBeanFactory("daoContext");
							final EventDao eventDao = BeanUtils.getBean(bf,
									"eventDao", EventDao.class);

							TransactionTemplate transTemplate = BeanUtils
									.getBean(bf, "transactionTemplate",
											TransactionTemplate.class);
							final StringWriter sw = (StringWriter) transTemplate
									.execute(new TransactionCallback() {
										public Object doInTransaction(
												final TransactionStatus status) {
											List<OnmsEvent> events = null;
											StringWriter sw = new StringWriter();
											try {
												// TODO to control the query
												final OnmsCriteria criteria = new OnmsCriteria(
														OnmsEvent.class);
//												criteria
//														.add(Restrictions
//																.sqlRestriction("eventuei = '"
//																		+ alarmUEI
//																		+ "' and alarmid not in (select alarmid from events where eventuei = '"
//																		+ clearAlarmUEI
//																		+ "')"));
												criteria.add(Restrictions.sqlRestriction(criteriaRestriction));
												events = eventDao
														.findMatching(criteria);
												if (log.isDebugEnabled())
													log
															.debug("Found "
																	+ events
																			.size()
																	+ " open event/s (alarms)");
												Iterator<OnmsEvent> ite = events
														.iterator();
												while (ite.hasNext()) {
													OnmsEvent ev = ite.next();
													try {
														Event e = new Event();
														e.setDbid(ev.getId());
														e.setUei(ev
																.getEventUei());
														e
																.setSource(ev
																		.getEventSource());
														if (ev.getNode() != null)
															e.setNodeid(ev
																	.getNode()
																	.getId());
														DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
													        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
													   //     DateFormat dfInstance = DateFormat
													   //			.getDateInstance(DateFormat.FULL,DateFormat.FULL);
														e
																.setTime(dateFormat
																		.format(ev
																				.getEventTime()));
														e
																.setHost(ev
																		.getEventHost());
														e.setInterface(ev
																.getIpAddr());
														if (ev.getServiceType() != null)
															e
																	.setService(ev
																			.getServiceType()
																			.getName());
														e
																.setDescr(ev
																		.getEventDescr());
														Logmsg msg = new Logmsg();
														msg
																.setContent(ev
																		.getEventLogMsg());
														e.setLogmsg(msg);
														e
																.setSeverity(Constants
																		.getSeverityString(ev
																				.getEventSeverity()));
//TODO FIXME SEND IFINDEX and IFALIAS To INS														
//														if (ev.getIfIndex() != null) 
//															e.setIfIndex(ev.getIfIndex().toString());
														e.setIfIndex("-1");
//														if (ev.getIfAlias() != null)
//															e.setIfAlias(ev.getIfAlias());
														e.setIfAlias("-1");
														if (ev.getEventOperInstruct() != null)
															e.setOperinstruct(ev.getEventOperInstruct());
														if (ev.getEventParms() != null ) {
														    Parms parms = Parameter.decode(ev.getEventParms());
														
														    if (parms != null ) e.setParms(parms);
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
															log
																	.debug(
																			"correlated alarm data not found ",
																			e1);
														}
														e.marshal(sw);
													} catch (Exception ex) {
														log
																.error(
																		"Error while getting event ",
																		ex);
														return null;
													}
													sw.flush();
												}
											} catch (final RuntimeException e) {
												log
														.error(
																"Error while getting events ",
																e);
												return null;
											}
											return sw;

										}

									});
							if (sw != null)
								streamToClient.print(sw.toString());
							else
								break readingFromClient;
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

}
