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
