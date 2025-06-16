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
package org.opennms.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import junit.framework.AssertionFailedError;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author brozow
 */
public class MockLogAppenderTest {
    private static final Logger LOG = LoggerFactory.getLogger(MockLogAppenderTest.class);

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true, MockLogAppender.DEFAULT_LOG_LEVEL);
        MockLogAppender.resetState();
    }

    @Test
    public void testDefaultLevelInfo() {
        MockLogAppender.setupLogging(true, "INFO");
        LoggerFactory.getLogger(getClass()).debug("A debug message");
        MockLogAppender.assertNoLogging();
    }

    @Test
    public void testDefaultLevelDebug() {
        MockLogAppender.setupLogging(true, "DEBUG");
        LoggerFactory.getLogger(getClass()).trace("A trace message");
        MockLogAppender.assertNoLogging();
        LoggerFactory.getLogger(getClass()).debug("A debug message");
        MockLogAppender.assertLogAtLevel(Level.DEBUG);
    }

    @Test
    public void testInfo() {
        LOG.info("An Info message");
        assertTrue("Messages were logged with a warning level or higher", MockLogAppender.noWarningsOrHigherLogged());
    }

    @Test
    public void testWarn() {
        LOG.warn("A warn message");
        assertFalse("Messages were not logged with a warning level or higher", MockLogAppender.noWarningsOrHigherLogged());
    }

    @Test
    public void testError() {
        LOG.error("An error message");
        assertFalse("Messages were not logged with a warning level or higher", MockLogAppender.noWarningsOrHigherLogged());

    }

    @Test
    public void testInfoWithException() {
        LOG.info("An info message with exception", new NullPointerException());
        assertTrue("Messages were logged with a warning level or higher", MockLogAppender.noWarningsOrHigherLogged());
    }

    @Test
    public void testErrorWithException() {
        LOG.error("An error message with exception", new NullPointerException());
        assertFalse("Messages were not logged with a warning level or higher", MockLogAppender.noWarningsOrHigherLogged());
    }

    @Test
    public void xtestInfoMessage() throws InterruptedException {
        LOG.info("An Info message");

        assertTrue("Messages were logged with a warning level or higher", MockLogAppender.noWarningsOrHigherLogged());

        final LoggingEvent[] events = MockLogAppender.getEventsGreaterOrEqual(Level.TRACE);

        assertEquals("Number of logged events", 1, events.length);

        assertEquals("Logged event level", Level.INFO, events[0].getLevel());
        assertEquals("Logged message", "An Info message", events[0].getMessage());
    }

    @Test
    public void testWarnLimit() throws InterruptedException {
        LOG.info("An Info message");
        LOG.warn("A warn message");

        assertFalse("Messages were not logged with a warning level or higher", MockLogAppender.noWarningsOrHigherLogged());

        final LoggingEvent[] events = MockLogAppender.getEventsGreaterOrEqual(Level.WARN);

        assertEquals("Number of logged events", 1, events.length);

        assertEquals("Logged event level", Level.WARN, events[0].getLevel());
        assertEquals("Logged message", "A warn message", events[0].getMessage());
    }

    @Test
    public void testWarnAssert() throws InterruptedException {
        LOG.info("An Info message");
        LOG.warn("A warn message");

        try {
            MockLogAppender.assertNoWarningsOrGreater();
        } catch (final AssertionFailedError e) {
            return;
        }

        fail("Did not receive expected AssertionFailedError from MockLogAppender.assertNotGreatorOrEqual");
    }

    public void testErrorAssert() throws InterruptedException {
        LOG.info("An Info message");
        LOG.warn("A warn message");

        try {
            MockLogAppender.assertNoErrorOrGreater();
        } catch (final AssertionFailedError e) {
            fail("Received unexpected AssertionFailedError: " + e);
        }
    }

    public void testDiscardHibernateAnnotationBinderWarnings() {
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger("org.hibernate.cfg.AnnotationBinder");
        log.info("An Info message");
        log.warn("A warn message");

        try {
            MockLogAppender.assertNoWarningsOrGreater();
        } catch (final AssertionFailedError e) {
            fail("Received unexpected AssertionFailedError: " + e);
        }
    }
}
