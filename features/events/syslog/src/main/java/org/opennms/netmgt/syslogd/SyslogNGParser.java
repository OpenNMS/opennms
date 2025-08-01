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
package org.opennms.netmgt.syslogd;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.netmgt.config.SyslogdConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A stricter variant of the {@link CustomSyslogParser} which parses
 * Syslog-NG's default format.  It ignores forwarding-regexp, 
 * matching-group-host, and matching-group-message and instead 
 * relies on a well-known properly-formatted syslog message.
 */
public class SyslogNGParser extends SyslogParser {
    private static final Logger LOG = LoggerFactory.getLogger(SyslogNGParser.class);

    //                                                                <PRI>        IDENT               TIMESTAMP                                                                                 HOST   PROCESS/ID                            MESSAGE
    private static final Pattern m_syslogNGPattern = Pattern.compile("^<(\\d{1,3})>(?:(\\S*?)(?::? )?)((?:\\d\\d\\d\\d-\\d\\d-\\d\\d)|(?:\\S\\S\\S\\s+\\d{1,2}\\s+\\d\\d:\\d\\d:\\d\\d)) (\\S+) (?:(\\S+?)(?:\\[(\\d+)\\])?:\\s+){0,1}(\\S.*?)$", Pattern.MULTILINE);

    public SyslogNGParser(final SyslogdConfig config, final ByteBuffer text) {
        super(config, text);
    }

    @Override
    protected Pattern getPattern() {
        return m_syslogNGPattern;
    }

    @Override
    protected SyslogMessage parse() throws SyslogParserException {
        if (!this.find()) {
            if (traceEnabled()) {
                LOG.trace("'{}' did not match '{}'", m_syslogNGPattern, getText());
            }
            return null;
        }

        final Matcher matcher = getMatcher();
        final SyslogMessage message = new SyslogMessage();
        message.setParserClass(getClass());
        try {
            int priorityField = Integer.parseInt(matcher.group(1));
            message.setFacility(SyslogFacility.getFacilityForCode(priorityField));
            message.setSeverity(SyslogSeverity.getSeverityForCode(priorityField));
        } catch (final NumberFormatException e) {
            LOG.debug("Unable to parse priority field '{}'", matcher.group(1), e);
        }
        if (matcher.group(2) != null && !matcher.group(2).isEmpty()) {
            message.setMessageID(matcher.group(2));
        }

        Date date = parseDate(matcher.group(3));
        if (date == null) date = new Date();
        message.setDate(date);

        message.setHostName(matcher.group(4));
        message.setProcessName(matcher.group(5));
        if (matcher.group(6) != null) {
            message.setProcessId(matcher.group(6));
        }
        message.setMessage(matcher.group(7).trim());

        return message;
    }
}
