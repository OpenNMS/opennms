package org.opennms.plugins.dbnotifier.alarmnotifier;


import java.sql.Timestamp;
import java.util.Calendar;

import javax.xml.bind.DatatypeConverter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.core.network.IPAddress;
import org.opennms.plugins.com.impossibl.postgres.jdbc.TimestampUtils;
import org.opennms.plugins.dbnotifier.DbNotification;
import org.opennms.plugins.dbnotifier.NotificationClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * sends memo change events into opennms
 * 
 * @author admin
 *
 */
public class MemosChangeNotificationClient implements NotificationClient {
	private static final Logger LOG = LoggerFactory
			.getLogger(MemosChangeNotificationClient.class);

	public static final String EVENT_SOURCE_NAME = "AlarmChangeNotifier";

	// uei definitions of memo change events
	public static final String STICKY_MEMO_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/StickyMemoUpdate";
	public static final String JOURNAL_MEMO_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/JournalMemoUpdate";
	
	public static final String MEMO_VALUES="memovalues";

	EventProxy eventProxy = null;

	public EventProxy getEventProxy() {
		return eventProxy;
	}

	public void setEventProxy(EventProxy eventProxy) {
		this.eventProxy = eventProxy;
	}

	@Override
	public void sendDbNotification(DbNotification dbNotification) {
		try{
			String payload = dbNotification.getPayload();

			JSONObject memoJsonObject=null;

			try {
				JSONParser parser = new JSONParser();
				Object obj;
				obj = parser.parse(payload);
				memoJsonObject = (JSONObject) obj;
				LOG.debug("payload memoJsonObject.toString():" + memoJsonObject.toString());

				memoJsonObject = jsonMemoTimeNormaliser(memoJsonObject);

			} catch (ParseException e1) {
				throw new RuntimeException("cannot parse notification payload to json object. payload="+ payload, e1);
			}

			if (! memoJsonObject.isEmpty() ){
				// received a memo update
				// sticky note event
				if("Memo".equals(memoJsonObject.get("type").toString())) {
					if (LOG.isDebugEnabled()) LOG.debug("sticky memo updated="+memoJsonObject.get("id"));
					EventBuilder eb= new EventBuilder( STICKY_MEMO_EVENT, EVENT_SOURCE_NAME);

					//copy in all values as json in params

					eb.addParam(MEMO_VALUES,memoJsonObject.toString());

					sendEvent(eb.getEvent());
				} else if("ReductionKeyMemo".equals(memoJsonObject.get("type").toString())) {
					if (LOG.isDebugEnabled()) LOG.debug("reduction key memo updated="+memoJsonObject.get("id"));
					EventBuilder eb= new EventBuilder( JOURNAL_MEMO_EVENT, EVENT_SOURCE_NAME);

					//copy in all values as json in params

					eb.addParam(MEMO_VALUES,memoJsonObject.toString());

					sendEvent(eb.getEvent());
				}
			}

		} catch (Exception e){
			LOG.error("problem creating opennms alarm change event from database notification", e);
		}

	}


	private void sendEvent(Event e){
		LOG.debug("sending event to opennms. event.tostring():" + e.toString());
		try {
			if (eventProxy != null) {
				eventProxy.send(e);
			} else {
				LOG.error("OpenNMS event proxy not set - cannot send events to opennms");
			}
		} catch (EventProxyException ex) {
			throw new RuntimeException(
					"event proxy problem sending Memo Change Event to OpenNMS:",
					ex);
		}
	}

	/**
	 * concerts all time values into a normalised time in memo json object from database
	 * example timestamps to translate
	 *  {
	 *    "suppressedtime":"2016-08-04 16:11:16.01+01",
	 *    "suppresseduntil":"2016-08-04 16:11:16.01+01",
	 *    "lasteventtime":"2016-08-04 16:11:16.01+01",
	 *    "alarmacktime":"2016-08-04 07:34:04.617+01",
	 *    "firsteventtime":"2016-08-04 16:11:16.01+01",
	 *    "firstautomationtime":"2016-08-04 16:12:03.272205+01",
	 *    "lastautomationtime":"2016-08-04 16:12:03.272205+01"
	 *  }
	 * @param jsonObject
	 * @return
	 */
	public JSONObject jsonMemoTimeNormaliser(JSONObject jsonObject){

		if(jsonObject.isEmpty()) return jsonObject;

		String created= (jsonObject.get("created")==null) ? null : timeNormaliser(jsonObject.get("created").toString());
		if (created!=null) jsonObject.put("created", created);

		String updated= (jsonObject.get("updated")==null) ? null : timeNormaliser(jsonObject.get("updated").toString());
		if (updated!=null) jsonObject.put("suppresseduntil", updated);

		return jsonObject;
	}

	/**
	 * converts postgres json time format to normalised time format for matching to Elastic Search 
	 * date_optional_time or strict_date_optional_time
	 * (A generic ISO datetime parser where the date is mandatory and the time is optional
	 * see https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-date-format.html#strict-date-time) 
	 * @param dbTimeStr
	 * @return
	 */
	public String timeNormaliser(String dbTimeStr){
		String normalisedTimeStr=null;

		Calendar alarmCreationCal=null;
		TimestampUtils timestampUtils= new TimestampUtils();
		Timestamp timestamp;
		try {
			timestamp = timestampUtils.toTimestamp(null, dbTimeStr);
			// using DatatypeConverter.printDateTime
			alarmCreationCal=Calendar.getInstance();
			alarmCreationCal.setTime(timestamp);
			normalisedTimeStr=  DatatypeConverter.printDateTime(alarmCreationCal);

			//alternative using simple date format
			//final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
			//SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIME_FORMAT); 
			//timeStr=  simpleDateFormat.format(timestamp);

		} catch (Exception e) {
			LOG.error("cannot parse database json time string dbTimeStr"+dbTimeStr, e);
		}
		return normalisedTimeStr;

	}


	@Override
	public void init() {
		LOG.debug("initialising MemosChangeNotificationClient");
		if (eventProxy == null)
			LOG.debug("OpenNMS event proxy not set - cannot send events to opennms");
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
