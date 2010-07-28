/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package edu.ncsu.pdgrenon;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LogMessage
 *
 * @author brozow
 */
public class BaseLogMessage implements LogMessage {
    
    public enum MsgType {
        ERROR,
        BEGIN_COLLECTION,
        END_COLLECTION,
        BEGIN_PERSIST,
        END_PERSIST,
    }
    
    private static final DateFormat s_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,S");
    private static final String s_regexp = "(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3}) DEBUG \\[([^\\]]+)] Collectd: collector.collect: (collectData: begin|collectData: end|error|persistDataQueueing: begin|persistDataQueueing: end): (\\d+/\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}/[\\w-]+).*";
    private static final Pattern s_pattern = Pattern.compile(s_regexp);

    private static MsgType toMsgType(String msgIndicator) {
        if ("error".equals(msgIndicator)) {
            return MsgType.ERROR;
        }
        if ("collectData: begin".equals(msgIndicator)) {
            return MsgType.BEGIN_COLLECTION;
        }
        if ("collectData: end".equals(msgIndicator)) {
            return MsgType.END_COLLECTION;
        }
        if ("persistDataQueueing: begin".equals(msgIndicator)) {
            return MsgType.BEGIN_PERSIST;
        }
        if ("persistDataQueueing: end".equals(msgIndicator)) {
            return MsgType.END_PERSIST;
        }
        throw new IllegalArgumentException("No MsgType corresponding to indicator " + msgIndicator);
    }
    

    public static Date parseTimestamp(String dateString) {
        try {
            return s_format.parse(dateString);
        } catch (ParseException e) {
            throw new IllegalArgumentException(dateString + " is not a valid dateString");
        }
    }


    public static BaseLogMessage create(String logMessage) {
        Matcher m = s_pattern.matcher(logMessage);
        if (m.matches()) {
            return new BaseLogMessage(parseTimestamp(m.group(1)), m.group(2), toMsgType(m.group(3)), m.group(4));
        } else {
            return null;
        }
    }

    private final Date m_timestamp;
    private final String m_threadName;
    private final MsgType m_msgType;
    private final String m_serviceId;

    private BaseLogMessage(Date timestamp, String threadName, MsgType msgType, String serviceId) {
        m_timestamp = timestamp;
        m_threadName = threadName;
        m_msgType = msgType;
        m_serviceId = serviceId;
    }

    public Date getDate() {
        return m_timestamp;
    }

    public String getThread() {
        return m_threadName;
    }

    public MsgType getMsgType() {
        return m_msgType;
    }

    public String getServiceID() {
        return m_serviceId;
    }

    public boolean is(MsgType msgType) {
        return m_msgType.equals(msgType);
    }

	public boolean isBeginMessage() {
		return is(MsgType.BEGIN_COLLECTION) || is(MsgType.BEGIN_PERSIST);
	}


	public boolean isCollectorBeginMessage() {
		return is(MsgType.BEGIN_COLLECTION);
	}


	public boolean isCollectorEndMessage() {
		return is(MsgType.END_COLLECTION);
	}


	public boolean isEndMessage() {
		return is(MsgType.END_COLLECTION) || is (MsgType.END_PERSIST);
	}


	public boolean isErrorMessage() {
		return is(MsgType.ERROR);
	}


	public boolean isPersistMessage() {
		return is(MsgType.BEGIN_PERSIST)  || is (MsgType.END_PERSIST);
	}

}
