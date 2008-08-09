/*
 * Copyright 1999,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.opennms.netmgt.xmlrpcd;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.secure.SecurityTool;
import org.opennms.netmgt.xmlrpcd.TimeoutSecureXmlRpcTransportFactory;

/**
 * This class was copied from org.apache.xmlrpc.secure.SecureXmlRpcClient with
 * a read timeout added to non https clients.
 */
public class TimeoutSecureXmlRpcClient 
    extends XmlRpcClient
{
    protected int timeout;

    /** 
     * Construct a XML-RPC client with this URL, with timeout.
     */
    public TimeoutSecureXmlRpcClient (URL url, int timeout) {
        super(url, new TimeoutSecureXmlRpcTransportFactory(url, timeout));
        this.timeout = timeout;
    }

    /** 
     * Construct a XML-RPC client for the URL represented by this String, with 
     * timeout.
     */
    public TimeoutSecureXmlRpcClient (String url, int timeout) throws MalformedURLException {
        super(new URL(url), new TimeoutSecureXmlRpcTransportFactory(new URL(url), timeout));
        this.timeout = timeout;
    }
   
    /** 
     * Construct a XML-RPC client for the specified hostname and port, with 
     * timeout.
     */
    public TimeoutSecureXmlRpcClient (String hostname, int port, int timeout) throws MalformedURLException 
    {
        this("https://" + hostname + ':' + port + "/RPC2", timeout);
    }
    
    /**
     * This allows us to setup
     */
     public void setup() throws Exception
     {
         SecurityTool.setup();    
     }

    /** 
     * Just for testing.
     */
    public static void main (String args[]) throws Exception
    {
        // XmlRpc.setDebug (true);
        try {
            String url = args[0];
            int timeout = Integer.parseInt(args[1]);
            String method = args[2];
            Vector v = new Vector ();
            for (int i=3; i<args.length; i++) try {
                v.addElement (new Integer (Integer.parseInt (args[i])));
            } catch (NumberFormatException nfx) {
                v.addElement (args[i]);
            }
            TimeoutSecureXmlRpcClient client = new TimeoutSecureXmlRpcClient (url, timeout);
            try {
                System.err.println (client.execute (method, v));
            } catch (Exception ex) {
                System.err.println ("Error: "+ex.getMessage());
            }
        } catch (Exception x) {
            System.err.println (x);
            System.err.println ("Usage: java " +
                                TimeoutSecureXmlRpcClient.class.getName() +
                                " <url> <timeout> <method> [args]");
            System.err.println ("Arguments are sent as integers or strings.");
        }
    }
}
