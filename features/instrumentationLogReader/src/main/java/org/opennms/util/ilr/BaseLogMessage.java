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
package org.opennms.util.ilr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LogMessage
 *
 * @author pdgrenon
 */
public class BaseLogMessage implements LogMessage {
    
    public enum MsgType {
        ERROR,
        BEGIN_COLLECTION,
        END_COLLECTION,
        BEGIN_PERSIST,
        END_PERSIST,
    }
    
    /**
     *  Use ThreadLocal SimpleDateFormat instances because SimpleDateFormat is not thread-safe.
     */
    private static final ThreadLocal<SimpleDateFormat> s_format = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,S");
        }
    };
    private static final String s_regexp = "(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3})[ ]+INFO[ ]+\\[([^\\]]+)\\] collector.collect: (begin|end|error|persistDataQueueing: begin|persistDataQueueing: end): ?([^/]+/\\d+/\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}/[\\w-_]+).*";
    private static final Pattern s_pattern = Pattern.compile(s_regexp);

    private static MsgType toMsgType(String msgIndicator) {
        if ("error".equals(msgIndicator)) {
            return MsgType.ERROR;
        }
        if ("begin".equals(msgIndicator)) {
            return MsgType.BEGIN_COLLECTION;
        }
        if ("end".equals(msgIndicator)) {
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
            return s_format.get().parse(dateString);
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

    @Override
    public Date getDate() {
        return m_timestamp;
    }

    @Override
    public String getThread() {
        return m_threadName;
    }

    public MsgType getMsgType() {
        return m_msgType;
    }

    @Override
    public String getServiceID() {
        return m_serviceId;
    }

    public boolean is(MsgType msgType) {
        return m_msgType.equals(msgType);
    }

    @Override
	public boolean isBeginMessage() {
		return is(MsgType.BEGIN_COLLECTION) || is(MsgType.BEGIN_PERSIST);
	}


    @Override
	public boolean isCollectorBeginMessage() {
		return is(MsgType.BEGIN_COLLECTION);
	}


    @Override
	public boolean isCollectorEndMessage() {
		return is(MsgType.END_COLLECTION);
	}


    @Override
	public boolean isEndMessage() {
		return is(MsgType.END_COLLECTION) || is (MsgType.END_PERSIST);
	}


    @Override
	public boolean isErrorMessage() {
		return is(MsgType.ERROR);
	}


    @Override
	public boolean isPersistMessage() {
		return is(MsgType.BEGIN_PERSIST)  || is (MsgType.END_PERSIST);
	}
	
    @Override
	public boolean isPersistBeginMessage(){
	    return is(MsgType.BEGIN_PERSIST);
	}
	
    @Override
	public boolean isPersistEndMessage() {
	    return is(MsgType.END_PERSIST);
	}

}
