//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Apr 23: Fix warning when using Category.getInstance. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.test.mock;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.opennms.core.utils.LogUtils;

/**
 * @author brozow
 */
public class MockLogAppenderTest extends TestCase {
    protected void setUp() throws Exception {
        super.setUp();
        MockLogAppender.setupLogging(false);
        MockLogAppender.resetLogLevel();
    }
	
    protected void tearDown() throws Exception {
        super.tearDown();
    }
	
    public void testInfo() {
        LogUtils.infof(this, "An Info message");
        assertTrue("Messages were logged with a warning level or higher", MockLogAppender.noWarningsOrHigherLogged());
    }
    
    public void testWarn() {
        LogUtils.warnf(this, "A warn message");
        assertFalse("Messages were not logged with a warning level or higher", MockLogAppender.noWarningsOrHigherLogged());
    }
    
    public void testError() {
        LogUtils.errorf(this, "An error message");
        assertFalse("Messages were not logged with a warning level or higher", MockLogAppender.noWarningsOrHigherLogged());
        
    }
    
    public void testInfoWithException() {
        LogUtils.infof(this, new NullPointerException(), "An info message with exception");
        assertTrue("Messages were logged with a warning level or higher", MockLogAppender.noWarningsOrHigherLogged());
    }
    
    public void testErrorWithException() {
        LogUtils.errorf(this, new NullPointerException(), "An error message with exception");
        assertFalse("Messages were not logged with a warning level or higher", MockLogAppender.noWarningsOrHigherLogged());
    }
	
	public void xtestInfoMessage() throws InterruptedException {
        LogUtils.infof(this, "An Info message");
		
        assertTrue("Messages were logged with a warning level or higher", MockLogAppender.noWarningsOrHigherLogged());

        final LoggingEvent[] events = MockLogAppender.getEventsGreaterOrEqual(Level.ALL);
		
		assertEquals("Number of logged events", 1, events.length);
		
		assertEquals("Logged event level", Level.INFO, events[0].getLevel());
		assertEquals("Logged message", "An Info message", events[0].getMessage());
	}
	
	public void testWarnLimit() throws InterruptedException {
		LogUtils.infof(this, "An Info message");
		LogUtils.warnf(this, "A warn message");
		
        assertFalse("Messages were not logged with a warning level or higher", MockLogAppender.noWarningsOrHigherLogged());

        final LoggingEvent[] events = MockLogAppender.getEventsGreaterOrEqual(Level.WARN);
		
		assertEquals("Number of logged events", 1, events.length);
		
		assertEquals("Logged event level", Level.WARN, events[0].getLevel());
		assertEquals("Logged message", "A warn message", events[0].getMessage());
	}
	
	public void testWarnAssert() throws InterruptedException {
		LogUtils.infof(this, "An Info message");
		LogUtils.warnf(this, "A warn message");
 
		try {
			MockLogAppender.assertNotGreaterOrEqual(Level.WARN);
		} catch (final AssertionFailedError e) {
			return;
		}
		
		fail("Did not receive expected AssertionFailedError from MockLogAppender.assertNotGreatorOrEqual");
	}
	
	public void testErrorAssert() throws InterruptedException {
		LogUtils.infof(this, "An Info message");
		LogUtils.warnf(this, "A warn message");

		try {
			MockLogAppender.assertNotGreaterOrEqual(Level.ERROR);
		} catch (final AssertionFailedError e) {
			fail("Received unexpected AssertionFailedError: " + e);
		}
	}
        
	public void testDiscardHibernateAnnotationBinderWarnings() {
		final Logger log = Logger.getLogger("org.hibernate.cfg.AnnotationBinder");
		log.info("An Info message");
		log.warn("A warn message");

		try {
			MockLogAppender.assertNotGreaterOrEqual(Level.WARN);
		} catch (final AssertionFailedError e) {
			fail("Received unexpected AssertionFailedError: " + e);
		}
	}
}
