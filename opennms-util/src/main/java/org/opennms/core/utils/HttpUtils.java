//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
// 
// Modifications:
//
// 2007 Jun 23: Eliminate depricated method call. - dj@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
//
// Tab Size = 8
//
// NodeLabel.java,v 1.3 2001/10/16 20:22:54 ben Exp
//
package org.opennms.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * Provides convenience methods for use the HTTP POST method.
 * 
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 */
public final class HttpUtils extends Object {
    /** Private constructor so this class will not be instantiated. */
    private HttpUtils() {
    }

    /** Default buffer size for reading data. (Default is one kilobyte.) */
    public final static int DEFAULT_POST_BUFFER_SIZE = 1024;

    /**
     * Post a given <code>InputStream</code> s data to a URL.
     * 
     * @param url
     *            the <code>URL</code> to post to
     * @param dataStream
     *            an input stream containing the data to send
     * @return An <code>InputStream</a> that the programmer can read from
     * to get the HTTP server's response.
     */
    public static InputStream post(URL url, InputStream dataStream) throws IOException {
        return (post(url, dataStream, null, null, DEFAULT_POST_BUFFER_SIZE));
    }

    /**
     * Post a given <code>InputStream</code> s data to a URL using BASIC
     * authentication and the given username and password.
     * 
     * @param url
     *            the <code>URL</code> to post to
     * @param dataStream
     *            an input stream containing the data to send
     * @param username
     *            the username to use in the BASIC authentication
     * @param password
     *            the password to use in the BASIC authentication
     * @return An <code>InputStream</a> that the programmer can read from
     * to get the HTTP server's response.
     */
    public static InputStream post(URL url, InputStream dataStream, String username, String password) throws IOException {
        return (post(url, dataStream, username, password, DEFAULT_POST_BUFFER_SIZE));
    }

    /**
     * Post a given <code>InputStream</code> s data to a URL using BASIC
     * authentication, the given username and password, and a buffer size.
     * 
     * @param url
     *            the <code>URL</code> to post to
     * @param dataStream
     *            an input stream containing the data to send
     * @param username
     *            the username to use in the BASIC authentication
     * @param password
     *            the password to use in the BASIC authentication
     * @param bufSize
     *            the size of the buffer to read from <code>dataStream</code>
     *            and write to the HTTP server
     * @return An <code>InputStream</a> that the programmer can read from
     * to get the HTTP server's response.
     */
    public static InputStream post(URL url, InputStream dataStream, String username, String password, int bufSize) throws IOException {
        if (url == null || dataStream == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        if (bufSize < 1) {
            throw new IllegalArgumentException("Cannot use zero or negative buffer size.");
        }

        if (!"http".equals(url.getProtocol())) {
            throw new IllegalArgumentException("Cannot use non-HTTP URLs.");
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // in a post we both write output and read input
        conn.setDoOutput(true);
        conn.setDoInput(true);

        try {
            // the name of this method is post after all
            conn.setRequestMethod("POST");
        } catch (java.net.ProtocolException e) {
            // this would really really really bad... when can you not use POST
            // in HTTP?
            throw new IllegalStateException("Could not set a HttpURLConnection's method to POST.");
        }

        // add the authorization header if the username and password were given
        if (username != null && password != null) {
            byte[] authBytes = (username + ":" + password).getBytes();
            String authString = new String(Base64.encodeBase64(authBytes));
            conn.setRequestProperty("Authorization", "Basic " + authString);
        }

        // get the out-going HTTP connection
        OutputStream ostream = conn.getOutputStream();

        // initialize a buffer to use to read and write
        byte[] b = new byte[bufSize];

        // write the given data stream over the out-going HTTP connection
        int bytesRead = dataStream.read(b, 0, bufSize);
        while (bytesRead > 0) {
            ostream.write(b, 0, bytesRead);
            bytesRead = dataStream.read(b, 0, bufSize);
        }

        // close the out-going HTTP connection
        ostream.close();

        // return the in-coming HTTP connection so the programmer can read the
        // response
        return (conn.getInputStream());
    }

    /**
     * Post a given <code>Reader</code> s data to a URL using BASIC
     * authentication, the given username and password, and a buffer size.
     * 
     * @param url
     *            the <code>URL</code> to post to
     * @param dataReader
     *            an input reader containing the data to send
     * @param username
     *            the username to use in the BASIC authentication
     * @param password
     *            the password to use in the BASIC authentication
     * @param bufSize
     *            the size of the buffer to read from <code>dataStream</code>
     *            and write to the HTTP server
     * @return An <code>InputStream</a> that the programmer can read from
     * to get the HTTP server's response.
     */
    public static InputStream post(URL url, Reader dataReader, String username, String password, int bufSize) throws IOException {
        if (url == null || dataReader == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        if (bufSize < 1) {
            throw new IllegalArgumentException("Cannot use zero or negative buffer size.");
        }

        if (!"http".equals(url.getProtocol())) {
            throw new IllegalArgumentException("Cannot use non-HTTP URLs.");
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // in a post we both write output and read input
        conn.setDoOutput(true);
        conn.setDoInput(true);

        try {
            // the name of this method is post after all
            conn.setRequestMethod("POST");
        } catch (java.net.ProtocolException e) {
            // this would really really really bad... when can you not use POST
            // in HTTP?
            throw new IllegalStateException("Could not set a HttpURLConnection's method to POST.");
        }

        // add the authorization header if the username and password were given
        if (username != null && password != null) {
            byte[] authBytes = (username + ":" + password).getBytes();
            String authString = new String(Base64.encodeBase64(authBytes));
            conn.setRequestProperty("Authorization", "Basic " + authString);
        }
        
        // set the mime type
        conn.setRequestProperty("Content-type", "text/xml; charset=\"utf-8\"");

        // get the out-going HTTP connection
        OutputStreamWriter ostream = new OutputStreamWriter(conn.getOutputStream(), "US-ASCII");

        // log data
        Logger log = Logger.getLogger("POSTDATALOG");
        if (log.isDebugEnabled()) {
            String nl = System.getProperty("line.separator");
            log.debug(nl + "HTTP Post: Current time: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.GregorianCalendar().getTime()));
            log.debug(nl + "Data posted:" + nl);
        }

        // initialize a buffer to use to read and write
        char[] b = new char[bufSize];

        // write the given data stream over the out-going HTTP connection
        int bytesRead = dataReader.read(b, 0, bufSize);
        if (bytesRead > 0 && log.isDebugEnabled())
            log.debug(new String(b, 0, bytesRead));

        while (bytesRead > 0) {
            ostream.write(b, 0, bytesRead);
            bytesRead = dataReader.read(b, 0, bufSize);

            if (bytesRead > 0 && log.isDebugEnabled())
                log.debug(new String(b, 0, bytesRead));
        }

        // close the out-going HTTP connection
        ostream.close();

        // return the in-coming HTTP connection so the programmer can read the
        // response
        return (conn.getInputStream());
    }

}
