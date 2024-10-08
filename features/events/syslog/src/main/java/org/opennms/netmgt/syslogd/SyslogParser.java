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

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.time.YearGuesser;
import org.opennms.netmgt.config.SyslogdConfig;
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

    protected Date parseDate(final String dateString) {
        try {
            // Date pattern has been created and checked inside if loop instead of 
            // parsing date inside the exception class.
            if (dateString.matches(datePattern)) {
                final DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
                adjustTimeZone(df);
                return df.parse(dateString);
            } else {
                final DateFormat df = new SimpleDateFormat("MMM dd HH:mm:ss", Locale.ROOT);
                adjustTimeZone(df);
                Date date = df.parse(dateString);
                LocalDateTime parsedDateWithoutYear = LocalDateTime.ofInstant(date.toInstant(), df.getTimeZone().toZoneId());
                LocalDateTime parsedDateWithYear = YearGuesser.guessYearForDate(parsedDateWithoutYear, LocalDateTime.now(df.getTimeZone().toZoneId()));
                Instant adjustedInstant = parsedDateWithYear.atZone(df.getTimeZone().toZoneId()).toInstant();
                return Date.from(adjustedInstant);
            }
        } catch (final Exception e) {
            LOG.debug("Unable to parse date '{}'", dateString, e);
            return null;
        }
    }

    void adjustTimeZone(DateFormat df) {
        if(m_config.getTimeZone() !=null) {
            df.setTimeZone(m_config.getTimeZone());
        }
    }
}
