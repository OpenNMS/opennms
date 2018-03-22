/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.plugins.dbnotifier.alarmnotifier;


import static org.opennms.plugins.dbnotifier.alarmnotifier.AlarmChangeNotificationClient.ENCODING_TEXT;
import static org.opennms.plugins.dbnotifier.alarmnotifier.AlarmChangeNotificationClient.TYPE_JSON;

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
			JSONObject alarmIdJsonObject=null;
			String alarmId=null;
			String body=null;
			String author=null;
			String reductionkey=null;

			try {
				JSONParser parser = new JSONParser();
				Object obj;
				obj = parser.parse(payload);
				
				JSONArray jsonArray = (JSONArray) obj;
				if (LOG.isDebugEnabled()) LOG.debug("payload memo jsonArray.toString():" + jsonArray.toString());
				memoJsonObject = (JSONObject) jsonArray.get(0);
				memoJsonObject = jsonMemoTimeNormaliser(memoJsonObject);
				
				alarmIdJsonObject = (JSONObject) jsonArray.get(1);
				alarmId= (alarmIdJsonObject.get("alarmid")==null) ? null : alarmIdJsonObject.get("alarmid").toString();
				body= (memoJsonObject.get("body")==null) ? null : memoJsonObject.get("body").toString();
				author= (memoJsonObject.get("author")==null) ? null : memoJsonObject.get("author").toString();
				reductionkey= (memoJsonObject.get("reductionkey")==null) ? null : memoJsonObject.get("reductionkey").toString();
				
			} catch (ParseException e1) {
				throw new RuntimeException("cannot parse notification payload to json object. payload="+ payload, e1);
			}

			if (! memoJsonObject.isEmpty() ){
				// received a memo update
				// sticky note event
				if("Memo".equals(memoJsonObject.get("type").toString())) {
					if (LOG.isDebugEnabled()) LOG.debug("sticky memo updated="+memoJsonObject.get("id"));
					EventBuilder eb= new EventBuilder( AlarmChangeEventConstants.STICKY_MEMO_EVENT, EVENT_SOURCE_NAME);

					//copy in all values as json in params
					eb.addParam(AlarmChangeEventConstants.MEMO_VALUES_PARAM,memoJsonObject.toString(), TYPE_JSON, ENCODING_TEXT);
					eb.addParam(AlarmChangeEventConstants.MEMO_ALARMID_PARAM, alarmId );
					eb.addParam(AlarmChangeEventConstants.MEMO_BODY_PARAM, body );
					eb.addParam(AlarmChangeEventConstants.MEMO_AUTHOR_PARAM, author );

					sendEvent(eb.getEvent());
				} else if("ReductionKeyMemo".equals(memoJsonObject.get("type").toString())) {
					if (LOG.isDebugEnabled()) LOG.debug("reduction key memo updated="+memoJsonObject.get("id"));
					EventBuilder eb= new EventBuilder(AlarmChangeEventConstants.JOURNAL_MEMO_EVENT, EVENT_SOURCE_NAME);

					//copy in all values as json in params
					eb.addParam(AlarmChangeEventConstants.MEMO_VALUES_PARAM,memoJsonObject.toString(), TYPE_JSON, ENCODING_TEXT);
					eb.addParam(AlarmChangeEventConstants.MEMO_ALARMID_PARAM, alarmId );
					eb.addParam(AlarmChangeEventConstants.MEMO_BODY_PARAM, body );
					eb.addParam(AlarmChangeEventConstants.MEMO_AUTHOR_PARAM, author );
					eb.addParam(AlarmChangeEventConstants.MEMO_REDUCTIONKEY_PARAM, reductionkey );

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
			throw new RuntimeException(	"event proxy problem sending Memo Change Event to OpenNMS:",ex);
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
	 * converts postgres json time format to normalised time format for matching to Elasticsearch 
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
	}

}
