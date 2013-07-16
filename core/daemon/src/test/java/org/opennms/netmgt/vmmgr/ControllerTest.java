/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.vmmgr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.ServerSocket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;

public class ControllerTest {

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
    }
    
    @After
    public void runTest() throws Throwable {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test
    public void testClientTimeout() throws Exception {
        final ServerSocket server = new ServerSocket(0);
        
        final Controller c = new Controller();
        c.setInvokeUrl(Controller.DEFAULT_INVOKER_URL.replaceAll(":8181", ":" + server.getLocalPort()));
        c.setHttpRequestReadTimeout(2000);
        
        Thread clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                c.invokeOperation("testClientTimeout");
            }
        }, this.getClass().getSimpleName() + "-clientThread");
        
        final StringBuffer exceptionBuffer = new StringBuffer();
        
        UncaughtExceptionHandler handler  = new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable t) {
                exceptionBuffer.append(t.toString());
            }
        };
                
        clientThread.setUncaughtExceptionHandler(handler);
        
        clientThread.start();
        
        Thread acceptThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    server.accept();
                } catch (IOException e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
        }, this.getClass().getSimpleName() + "-acceptThread");

        acceptThread.setUncaughtExceptionHandler(handler);
        
        acceptThread.start();
        
        acceptThread.join(1000);
        assertFalse("the accept thread should have stopped because it should have received a connection", acceptThread.isAlive());

        clientThread.join(c.getHttpRequestReadTimeout() * 2);
        assertFalse("the client thread should have stopped within " + c.getHttpRequestReadTimeout() + " because it should have timed out its connection", clientThread.isAlive());
        
        assertEquals("exception buffer is non-empty: " + exceptionBuffer.toString(), 0, exceptionBuffer.length());
        
//        assertEquals("there should be exactly one logged message", 1, MockLogAppender.getEvents().length);
//        assertEquals("the first log message should be an error", Level.ERROR, MockLogAppender.getEvents()[0].getLevel());
        
        MockLogAppender.resetEvents();
    }
}
