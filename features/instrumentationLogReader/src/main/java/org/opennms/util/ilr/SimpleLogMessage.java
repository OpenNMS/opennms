/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.util.ilr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleLogMessage implements LogMessage {

    public static LogMessage create(String logMessage) {
        return new SimpleLogMessage(logMessage);
    }
    private String m_logMessage;

    private SimpleLogMessage(String logMessage) {
        m_logMessage = logMessage;
    }
    /* (non-Javadoc)
     * @see org.opennms.util.ilr.LogMessage#getMessage()
     */
    public String getMessage () {
        return m_logMessage;
    }
    /* (non-Javadoc)
     * @see org.opennms.util.ilr.LogMessage#isEndMessage()
     */
    @Override
    public boolean isEndMessage() {
        return getMessage().contains("end");
    }
    @Override
    public String toString() {
        return m_logMessage;
    }
    /* (non-Javadoc)
     * @see org.opennms.util.ilr.LogMessage#isPersistMessage()
     */
    @Override
    public boolean isPersistMessage() {
        return getMessage().contains("persist");
    }
    @Override
    public boolean isPersistBeginMessage() {
        return getMessage().contains("persistDataQueueing: begin");
    }
    @Override
    public boolean isPersistEndMessage() {
        return getMessage().contains("persistDataQueueing: end");
    }
    /* (non-Javadoc)
     * @see org.opennms.util.ilr.LogMessage#isBeginMessage()
     */
    @Override
    public boolean isBeginMessage() {
        return getMessage().contains("begin");
    }
    /* (non-Javadoc)
     * @see org.opennms.util.ilr.LogMessage#isErrorMessage()
     */
    @Override
    public boolean isErrorMessage() {
        return getMessage().contains("error");
    }
    /* (non-Javadoc)
     * @see org.opennms.util.ilr.LogMessage#isCollectorBeginMessage()
     */
    @Override
    public boolean isCollectorBeginMessage() {
        return getMessage().contains("collectData: begin:") || getMessage().contains("collector.initialize: begin");
    }
    /* (non-Javadoc)
     * @see org.opennms.util.ilr.LogMessage#isCollectorEndMessage()
     */
    @Override
    public boolean isCollectorEndMessage() {
        return getMessage().contains("collectData: end:") || getMessage().contains("collector.initialize: end");
    }

    /* (non-Javadoc)
     * @see org.opennms.util.ilr.LogMessage#getDate()
     */
    @Override
    public Date getDate() {
        String regex =  "\\d+-\\d+-\\d+\\s*\\d+:\\d+:\\d+,\\d+";
        Pattern timestamp = Pattern.compile(regex);
        Matcher timestampMatcher = timestamp.matcher(getMessage());
        SimpleDateFormat f = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss,S");
        if(timestampMatcher.find()) {	
            try {
                return f.parse(timestampMatcher.group());
            } catch (ParseException e) {
                throw (new IllegalArgumentException(e));
            }
        }else{
            throw (new IllegalArgumentException("Does not match"));
        }
    }
    /* (non-Javadoc)
     * @see org.opennms.util.ilr.LogMessage#getServiceID()
     */
    @Override
    public String getServiceID() {
        String regex = "\\d+/\\d+\\.\\d+\\.\\d+\\.\\d+/[\\w-]+";
        Pattern service = Pattern.compile(regex);
        Matcher serviceMatcher = service.matcher(getMessage());
        if(serviceMatcher.find()) {
            return serviceMatcher.group();
        }
        return null;
    }
    /* (non-Javadoc)
     * @see org.opennms.util.ilr.LogMessage#getThread()
     */
    @Override
    public String getThread() {
        String regex = "\\[(\\w+Scheduler-[^\\]]+)\\]";
        //example: [CollectdScheduler-50 Pool-fiber11]
        //         [LegacyScheduler-Thread-34-of-50]
        Pattern thread = Pattern.compile(regex);
        Matcher threadMatcher = thread.matcher(getMessage());
        if(threadMatcher.find()) {
            return threadMatcher.group(1);
        }
        return null;
    }
}
