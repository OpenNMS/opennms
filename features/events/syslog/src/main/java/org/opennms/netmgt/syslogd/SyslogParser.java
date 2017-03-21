/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.dao.api.AbstractInterfaceToNodeCache;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.model.events.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;


public class SyslogParser {
    private static final Logger LOG = LoggerFactory.getLogger(SyslogParser.class);
    private static final String datePattern="((19|20)\\d{2})-([1-9]|0[1-9]|1[0-2])-(0[1-9]|[1-9]|[12][0-9]|3[01])";
    private Matcher m_matcher = null;
    private final SyslogdConfig m_config;
    private final ByteBuffer m_text;
    private Boolean m_found = null;
    private Boolean m_matched = null;
    private boolean m_traceEnabled = false;
    private static final LoadingCache<String,Class<? extends SyslogParser>> PARSER_CLASSES = CacheBuilder.newBuilder().build(
        new CacheLoader<String,Class<? extends SyslogParser>>() {
            public Class<? extends SyslogParser> load(String className) {
                try {
                    return Class.forName(className).asSubclass(SyslogParser.class);
                } catch (final Exception e) {
                    LOG.debug("Unable to instantiate Syslog parser class specified in config: {}", className, e);
                    return CustomSyslogParser.class;
                }
            }
        }
    );

    public static SyslogParser getParserInstance(SyslogdConfig config, ByteBuffer text) throws MessageDiscardedException {
        Class<? extends SyslogParser> parserClass = PARSER_CLASSES.getUnchecked(config.getParser());

        final SyslogParser retval;
        try {
            Constructor<? extends SyslogParser> m = parserClass.getConstructor(SyslogdConfig.class, ByteBuffer.class);
            retval = (SyslogParser)m.newInstance(config, text);
        } catch (final Exception ex) {
            LOG.debug("Unable to get parser for class '{}'", parserClass.getName(), ex);
            throw new MessageDiscardedException(ex);
        }

        return retval;
    }

    protected static String fromByteBuffer(ByteBuffer buffer) {
        return StandardCharsets.US_ASCII.decode(buffer).toString();
    }

    protected SyslogParser(final SyslogdConfig config, final ByteBuffer text) {
        if (config == null) {
            throw new IllegalArgumentException("Config argument to SyslogParser must not be null");
        } else if (text == null) {
            throw new IllegalArgumentException("Text argument to SyslogParser must not be null");
        }
        m_config = config;
        m_text = text.duplicate();
        m_traceEnabled = LOG.isTraceEnabled();
    }

    public boolean find() {
        if (m_found == null) {
            getMatcher().reset();
            m_found = getMatcher().find();
        }
        return m_found;
    }

    public boolean matches() {
        if (m_matched == null) {
            getMatcher().reset();
            m_matched = getMatcher().matches();
        }
        return m_matched;
    }

    protected Boolean matched() {
        return m_matched;
    }

    protected SyslogdConfig getConfig() {
        return m_config;
    }

    protected ByteBuffer getText() {
        m_text.rewind();
        return m_text;
    }

    protected boolean traceEnabled() {
        return m_traceEnabled;
    }

    /* override this to get your custom pattern */
    protected Pattern getPattern() {
        return Pattern.compile("^.*$");
    }

    /* override this to parse data from the matcher */
    protected SyslogMessage parse() throws SyslogParserException {
        final SyslogMessage message = new SyslogMessage();
        message.setMessage(getMatcher().group().trim());
        return message;
    }

    protected Matcher getMatcher() {
        if (m_matcher == null) {
            m_matcher = getPattern().matcher(SyslogParser.fromByteBuffer(getText()));
        }
        return m_matcher;
    }

    public static final EventBuilder toEventBuilder(SyslogMessage message, String systemId, String location) {
        if (message == null) {
            return null;
        }

        // Build a basic event out of the syslog message
        final String priorityTxt = message.getSeverity().toString();
        final String facilityTxt = message.getFacility().toString();

        EventBuilder bldr = new EventBuilder();
        bldr.setUei("uei.opennms.org/syslogd/" + facilityTxt + "/" + priorityTxt);
        bldr.setSource("syslogd");

        // Set constant values in EventBuilder

        // Set monitoring system
        bldr.setDistPoller(systemId);
        // Set event host
        bldr.setHost(InetAddressUtils.getLocalHostName());
        // Set default event destination to logndisplay
        bldr.setLogDest("logndisplay");


        // Set values from SyslogMessage in the EventBuilder

        bldr.addParam("hostname", message.getHostName());

        final InetAddress hostAddress = message.getHostAddress();
        if (hostAddress != null) {
            // Set nodeId
            InterfaceToNodeCache cache = AbstractInterfaceToNodeCache.getInstance();
            if (cache != null) {
                int nodeId = cache.getNodeId(location, hostAddress);
                if (nodeId > 0) {
                    bldr.setNodeid(nodeId);
                }
            }

            bldr.setInterface(hostAddress);
        }

        if (message.getDate() == null) {
            if (message.getYear() != null) bldr.setYear(message.getYear());
            if (message.getMonth() != null) bldr.setMonth(message.getMonth());
            if (message.getDayOfMonth() != null) bldr.setDayOfMonth(message.getDayOfMonth());
            if (message.getHourOfDay() != null) bldr.setHourOfDay(message.getHourOfDay());
            if (message.getMinute() != null) bldr.setMinute(message.getMinute());
            if (message.getSecond() != null) bldr.setSecond(message.getSecond());
            if (message.getMillisecond() != null) bldr.setMillisecond(message.getMillisecond());
            if (message.getTimeZone() != null) bldr.setTimeZone(message.getTimeZone());
        } else {
            bldr.setTime(message.getDate());
        }

        bldr.setLogMessage(message.getMessage());
        // Using parms provides configurability.
        bldr.addParam("syslogmessage", message.getMessage());

        bldr.addParam("severity", priorityTxt);

        bldr.addParam("timestamp", SyslogMessage.getRfc3164FormattedDate(bldr.currentEventTime()));

        if (message.getMessageID() != null) {
            bldr.addParam("messageid", message.getMessageID());
        }

        if (message.getProcessName() != null) {
            bldr.addParam("process", message.getProcessName());
        }

        bldr.addParam("service", facilityTxt);

        if (message.getProcessId() != null) {
            bldr.addParam("processid", message.getProcessId().toString());
        }

        return bldr;
    }

    protected static Date parseDate(final String dateString) {
        try {
            // Date pattern has been created and checked inside if loop instead of 
            // parsing date inside the exception class.
            if (dateString.matches(datePattern)) {
                final DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
                return df.parse(dateString);
            } else {
                final DateFormat df = new SimpleDateFormat("MMM dd HH:mm:ss", Locale.ROOT);
                
                // Ugh, what's a non-lame way of forcing it to parse to "this year"?
                Date date = df.parse(dateString);
                final Calendar c = df.getCalendar();
                c.setTime(date);
                c.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
                return c.getTime();
            }
        } catch (final Exception e) {
            LOG.debug("Unable to parse date '{}'", dateString, e);
            return null;
        }
    }
}
