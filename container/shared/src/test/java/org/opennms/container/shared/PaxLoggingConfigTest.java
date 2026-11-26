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
package org.opennms.container.shared;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.AsyncAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.properties.PropertiesConfiguration;
import org.apache.logging.log4j.core.config.properties.PropertiesConfigurationBuilder;
import org.apache.logging.log4j.status.StatusData;
import org.apache.logging.log4j.status.StatusListener;
import org.apache.logging.log4j.status.StatusLogger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Loads the Minion etc/org.ops4j.pax.logging.cfg the way pax-logging does and checks that
 * log4j accepts it. pax-logging rejects the whole file on any error, which leaves the container
 * without logging (NMS-20637), so a mistake here only shows up at runtime otherwise.
 */
public class PaxLoggingConfigTest {

    private static final String PREFIX = "log4j2.";

    /** ${name} or ${name:-default}, as resolved by Karaf's config substitution before log4j sees the file. */
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}:]+?)(?::-([^}]*))?}");

    /** Appender type that only exists inside the pax-logging bundle. */
    private static final String PAX_OSGI_TYPE = "PaxOsgi";

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void configIsAcceptedByLog4j() throws IOException {
        final Properties log4jProperties = loadAsPaxLoggingDoes();

        final List<String> errors = new ArrayList<>();
        final StatusListener listener = new StatusListener() {
            @Override
            public void log(final StatusData data) {
                if (data.getLevel().isMoreSpecificThan(Level.ERROR)) {
                    errors.add(data.getFormattedStatus());
                }
            }

            @Override
            public Level getStatusLevel() {
                return Level.ERROR;
            }

            @Override
            public void close() {
            }
        };
        StatusLogger.getLogger().registerListener(listener);
        final PropertiesConfiguration config;
        try {
            // build() is the step that threw "No type attribute provided for component appenderRef"
            config = new PropertiesConfigurationBuilder().setRootProperties(log4jProperties).build();
            config.initialize();
        } finally {
            StatusLogger.getLogger().removeListener(listener);
        }

        for (final LoggerConfig logger : config.getLoggers().values()) {
            checkRefs(config, logger, errors);
        }
        checkRefs(config, config.getRootLogger(), errors);
        assertEquals("log4j errors for " + configFile(), List.of(), errors);

        final Appender trapAudit = config.getAppender("TrapAuditAsync");
        assertTrue("TrapAuditAsync should be an Async appender, was " + trapAudit, trapAudit instanceof AsyncAppender);
        assertArrayEquals(new String[] {"TrapAuditRollingFile"}, ((AsyncAppender) trapAudit).getAppenderRefStrings());
    }

    private static void checkRefs(final PropertiesConfiguration config, final LoggerConfig logger, final List<String> errors) {
        for (final AppenderRef ref : logger.getAppenderRefs()) {
            if (config.getAppender(ref.getRef()) == null) {
                errors.add("logger '" + logger.getName() + "' refers to missing appender '" + ref.getRef() + "'");
            }
        }
    }

    /**
     * Mimics what Karaf and pax-logging do before handing the file to log4j: resolve placeholders,
     * keep only the log4j2.* keys and strip that prefix. The PaxOsgi appender is dropped because its
     * plugin is only available inside the pax-logging bundle.
     */
    private Properties loadAsPaxLoggingDoes() throws IOException {
        final String logDir = tmp.newFolder("log").getAbsolutePath();
        final String dataDir = tmp.newFolder("data").getAbsolutePath();

        final Properties raw = new Properties();
        try (Reader reader = Files.newBufferedReader(configFile())) {
            raw.load(reader);
        }
        raw.setProperty("karaf.log", logDir);
        raw.setProperty("karaf.data", dataDir);

        final List<String> paxOsgiNames = new ArrayList<>();
        final Properties result = new Properties();
        for (final String key : raw.stringPropertyNames()) {
            if (!key.startsWith(PREFIX)) {
                continue;
            }
            final String value = resolve(raw.getProperty(key), raw);
            result.setProperty(key.substring(PREFIX.length()), value);
            if (key.endsWith(".type") && PAX_OSGI_TYPE.equals(value)) {
                paxOsgiNames.add(key.substring(PREFIX.length(), key.length() - ".type".length()));
            }
        }
        for (final String appender : paxOsgiNames) {
            final String name = result.getProperty(appender + ".name");
            result.stringPropertyNames().stream()
                    .filter(k -> k.startsWith(appender + ".") || k.contains(".appenderRef." + name + "."))
                    .forEach(result::remove);
        }
        return result;
    }

    private static String resolve(final String value, final Properties raw) {
        final Matcher m = PLACEHOLDER.matcher(value);
        final StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String replacement = raw.getProperty(m.group(1));
            replacement = replacement != null ? resolve(replacement, raw) : m.group(2);
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement != null ? replacement : m.group()));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static Path configFile() {
        return Paths.get(System.getProperty("basedir", "."), "src", "main", "resources", "etc", "org.ops4j.pax.logging.cfg");
    }
}
