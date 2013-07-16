/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.xmlrpcd;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import junit.framework.AssertionFailedError;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.xmlrpc.WebServer;
import org.apache.xmlrpc.XmlRpcHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * <p>
 * Mock XML-RPC server that anticipates specific XML-RPC method calls.
 * </p>
 * 
 * @author mikeh@aiinet.com
 * @author dj@gregor.com
 */
public class XmlrpcAnticipator implements XmlRpcHandler {
    /**
     * Represents an XML-RPC call as a String method name and a Vector of
     * method arguments.  Note: the equals method looks for Hashtables in the
     * Vector and ignores comparisons on the values for any entries with a key
     * of "description". 
     * 
     * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
     */
    public class XmlrpcCall {
        private String m_method;
        private Vector<Object> m_vector;
        
        public XmlrpcCall(String method, Vector<Object> vector) {
            assertNotNull("null method not allowed", method);
            assertNotNull("null vector not allowed", vector);

            m_method = method;
            m_vector = vector;
        }

        @Override
        public String toString() {
            final StringBuffer b = new StringBuffer();
            b.append("Method: " + m_method + "\n");
            for (final Object o : m_vector) {
                b.append("Parameter (" + o.getClass().getName() + ") "+ o + "\n");
            }
            return b.toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(9, 3)
                .append(m_method)
                .append(m_vector)
                .toHashCode();
        }

        @Override
        public boolean equals(final Object o) {
            if (o == null) return false;
            if (!(o instanceof XmlrpcCall)) return false;
            final XmlrpcCall c = (XmlrpcCall) o;
            if (!m_method.equals(c.m_method)) {
                return false;
            }
            if (m_vector.size() != c.m_vector.size()) {
                return false;
            }
            for (int i = 0; i < m_vector.size(); i++) {
                Object a = m_vector.get(i);
                Object b = c.m_vector.get(i);
                if (!a.getClass().getName().equals(b.getClass().getName())) {
                    return false;
                }
                if (a instanceof Hashtable<?,?>) {
                    if (!hashtablesMatchIgnoringDescriptionKeys(a, b)) {
                        return false;
                    }
                } else if (!a.equals(b)) {
                    return false;
                }
            }
            return true;
        }
        
        @SuppressWarnings("unchecked")
        private boolean hashtablesMatchIgnoringDescriptionKeys(Object a, Object b) {
            Hashtable<String, String> ha = (Hashtable<String, String>) a;
            Hashtable<String, String> hb = (Hashtable<String, String>) b;
            
            if (ha.size() != hb.size()) {
                return false;
            }
            
            if (!ha.keySet().equals(hb.keySet())) {
                return false;
            }
            
            for (String key : ha.keySet()) {
                if (key.equals("description")) {
                    // This shouldn't happen, but let's test anyway
                    if (!hb.containsKey(key)) {
                        return false;
                    }
                } else {
                    if (!ha.get(key).equals(hb.get(key))) {
                        return false;
                    }
                }
            }
            
            return true;
        }
    }

    private List<XmlrpcCall> m_anticipated = new ArrayList<XmlrpcCall>();

    private List<XmlrpcCall> m_unanticipated = new ArrayList<XmlrpcCall>();
    
    private WebServer m_webServer = null;
    
    private int m_port;

    /** default port number */
    private static final int DEFAULT_PORT_NUMBER = 9000;

    /** logger */
    private Logger m_logger = LoggerFactory.getLogger(getClass());

    private static final String CHECK_METHOD_NAME = "XmlrpcAnticipatorCheck";

    public XmlrpcAnticipator(int port, boolean delayWebServer) throws IOException {
        m_port = port;
        if (!delayWebServer) {
            setupWebServer();
        }
    }

    public XmlrpcAnticipator(int port) throws IOException {
        this(port, false);
    }

    public XmlrpcAnticipator() throws IOException {
        this(DEFAULT_PORT_NUMBER, false);
    }

    public void setupWebServer() throws IOException {
        m_logger.info("XmlrpcAnticipator starting on port number " + m_port);

        m_webServer = new WebServer(m_port);
        m_webServer.addHandler("$default", this);
        m_webServer.start();
        waitForStartup();

        m_logger.info("XmlrpcAnticipator running on port number " + m_port);
    }


    /**
     *  Stop listening for OpenNMS events.
     *
     */
    public void shutdown() throws IOException {
        if (m_webServer == null) {
            return;
        }
        
        m_webServer.shutdown();
        waitForShutdown();
        
        m_webServer = null;
    }
    
    private void waitForStartup() throws IOException {
        boolean keepRunning = true;
        Socket s = null;
        
        while (keepRunning) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // do nothing
            }
            
            try {
                s = new Socket("localhost", m_port);
                keepRunning = false;
                
                sendCheckCall(s);
            } catch (ConnectException e) {
                // do nothing
            } finally {
                if (s != null) {
                    s.close();
                }
            }
        }
    }

    private void sendCheckCall(Socket s) throws IOException {
    	OutputStream out = null;
    	PrintWriter p = null;
    	try {
    		out = s.getOutputStream();
    		p = new PrintWriter(out);
	        p.print("POST / HTTP/1.0\r\n");
	        p.print("Connection: close\r\n");
	        p.print("\r\n");
	
	        p.print("<?xml.version=\"1.0\"?><methodCall><methodName>" + CHECK_METHOD_NAME + "</methodName><params></params></methodCall>\r\n");
    	} finally {
    		IOUtils.closeQuietly(p);
    		IOUtils.closeQuietly(out);
    		out = null;
    		p = null;
    	}
    }
    
    private void waitForShutdown() throws IOException {
        boolean keepRunning = true;
        Socket s = null;
        
        while (keepRunning) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // do nothing
            }
            
            try {
                s = new Socket("localhost", m_port);

                sendCheckCall(s);
            } catch (ConnectException e) {
                keepRunning = false; 
            } finally {
                if (s != null) {
                    s.close();
                }
            }
        }
    }
    
    public void anticipateCall(String method, Object... args) {
        Vector<Object> params = new Vector<Object>();
        for(Object arg: args) {
            params.add(arg);
        }
        m_anticipated.add(new XmlrpcCall(method, params));
    }

    // Implements Apache XMLRPC API
    @SuppressWarnings("unchecked")
    @Override
    public Object execute(String method, Vector vector) {
        if (m_webServer == null) {
            String message = "Hey!  We aren't initialized (anymore)!  "
                + "We should not be receiving execute calls!";
            System.err.println(message);
            System.err.println(new XmlrpcCall(method, vector));
            vector.add(message);
            return vector;
        }
        
        ourExecute(method, vector);
        return vector;
    }
    
    public synchronized void ourExecute(String method, Vector<Object> vector) {
        // Ignore internal checks
        if (method.equals(CHECK_METHOD_NAME)) {
            return;
        }
        
        XmlrpcCall c = new XmlrpcCall(method, vector);
        if (m_anticipated.contains(c)) {
            m_anticipated.remove(c);
        } else {
            m_unanticipated.add(c);
        }
    }

    public synchronized Collection<XmlrpcCall> getAnticipated() {
        return Collections.unmodifiableCollection(m_anticipated);
    }

    public void reset() {
        m_anticipated = new ArrayList<XmlrpcCall>();
        m_unanticipated = new ArrayList<XmlrpcCall>();
    }

    /**
     * @return
     */
    public Collection<XmlrpcCall> unanticipatedEvents() {
        return Collections.unmodifiableCollection(m_unanticipated);
    }

    public void verifyAnticipated() {
        StringBuffer problems = new StringBuffer();

        if (m_anticipated.size() > 0) {
            problems.append(m_anticipated.size() +
            " expected calls still outstanding:\n");
            problems.append(listCalls("\t", m_anticipated));
        }
        if (m_unanticipated.size() > 0) {
            problems.append(m_unanticipated.size() +
            " unanticipated calls received:\n");
            problems.append(listCalls("\t", m_unanticipated));
        }

        if (problems.length() > 0) {
            problems.deleteCharAt(problems.length() - 1);
            problems.insert(0, "XML-RPC Anticipator listening at port " + m_port + " has:\n");
            throw new AssertionFailedError(problems.toString());
        }
    }

    private static String listCalls(String prefix,
            Collection<XmlrpcCall> calls) {
        StringBuffer b = new StringBuffer();

        for (Iterator<XmlrpcCall> it = calls.iterator(); it.hasNext();) {
            XmlrpcCall call = it.next();
            b.append(prefix);
            b.append(call);
            b.append("\n");
        }

        return b.toString();
    }

    @Override
    protected void finalize() {
        try {
            shutdown();
        } catch (IOException e) {
            System.err.println("IOException received while shutting down WebServer in finalize()");
            e.printStackTrace();
        }
    }

}
