/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.syslogd;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.netmgt.config.SyslogdConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JuniperSyslogParser extends SyslogParser {
    private static final Logger LOG = LoggerFactory.getLogger(JuniperSyslogParser.class);

    //                                                                PRI         TIMESTAMP                                          HOST      PROCESS/ID          MESSAGE
    private static final Pattern m_juniperPattern = Pattern.compile("^<(\\d+)>\\s*(\\S\\S\\S\\s+\\d{1,2}\\s+\\d\\d:\\d\\d:\\d\\d)\\s+(\\S+)\\s+(\\S+)\\[(\\d+)\\]: (.*?)$", Pattern.MULTILINE);

    public JuniperSyslogParser(final SyslogdConfig config, final String text) {
        super(config, text);
    }

    @Override
    protected Pattern getPattern() {
        return m_juniperPattern;
    }
    
    @Override
    public SyslogMessage parse() throws SyslogParserException {
        if (!this.find()) {
            if (traceEnabled()) {
                LOG.trace("'{}' did not match '{}', falling back to the custom parser", m_juniperPattern, getText());
                final SyslogParser custom = new CustomSyslogParser(getConfig(), getText());
                return custom.parse();
            }
            return null;
        }

        final Matcher matcher = getMatcher();
        final SyslogMessage message = new SyslogMessage();
        message.setParserClass(getClass());

        try {
            final int priorityField = Integer.parseInt(matcher.group(1));
            message.setFacility(SyslogFacility.getFacilityForCode(priorityField));
            message.setSeverity(SyslogSeverity.getSeverityForCode(priorityField));
        } catch (final NumberFormatException nfe) {
            LOG.debug("Unable to parse '{}' as a PRI code.", matcher.group(1), nfe);
        }
        Date date = parseDate(matcher.group(2));
        if (date == null) date = new Date();
        message.setDate(date);

        message.setHostName(matcher.group(3));
        message.setProcessName(matcher.group(4));
        try {
            final Integer pid = Integer.parseInt(matcher.group(5));
            message.setProcessId(pid);
        } catch (final NumberFormatException nfe) {
            LOG.debug("Unable to parse '{}' as a process ID.", matcher.group(5), nfe);
        }
        message.setMessage(matcher.group(6).trim());

        return message;
    }


}
