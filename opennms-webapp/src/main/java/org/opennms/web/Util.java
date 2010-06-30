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
// 2008 Aug 29: Update to new TcpEventProxy constructors. - dj@opennms.org
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
package org.opennms.web;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.opennms.core.resource.Vault;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.utils.TcpEventProxy;
import org.opennms.web.element.NetworkElementFactory;

/**
 * Provides convenience functions for web-based interfaces.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.6.12
 */
public abstract class Util extends Object {

    /**
     * Return a string that represents the fully qualified URL for our servlet
     * context, suitable for use in the HTML <em>base</em> tag.
     *
     * <p>
     * As an example, suppose your host was www.mycompany.com, you are serving
     * from port 80, and your web application name was "opennms," then this
     * method would return: <code>http://www.mycompany.com:80/opennms/</code>
     * </p>
     *
     * <p>
     * If this guess is wrong, you can override it by setting the property
     * <code>opennms.web.base-url</code> in opennms.properties
     * (for embedded Jetty) or WEB-INF/configuration.properties (for Tomcat).
     * </p>
     *
     * @param request
     *            the servlet request you are servicing
     * @return a {@link java.lang.String} object.
     */
    public static String calculateUrlBase(HttpServletRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String tmpl = Vault.getProperty("opennms.web.base-url");
        if (tmpl == null) {
            tmpl = "%s://%x%c/";
        }
        return substituteUrl(request, tmpl);
    }

    /** Constant <code>substKeywords={ 's', 'h', 'p', 'x', 'c' }</code> */
    protected static final char[] substKeywords = { 's', 'h', 'p', 'x', 'c' };

    /**
     * <p>substituteUrl</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param tmpl a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    protected static String substituteUrl(HttpServletRequest request,
                                          String tmpl) {
        String[] replacements = {
            request.getScheme(),                        // %s
            request.getServerName(),                    // %h
            Integer.toString(request.getServerPort()),  // %p
            getHostHeader(request),                     // %x
            request.getContextPath()                    // %c
        };

        StringBuffer out = new StringBuffer(48);
        for (int i = 0; i < tmpl.length();) {
            char c = tmpl.charAt(i++);
            if (c == '%' && i < tmpl.length()) {
                char d = tmpl.charAt(i++);
                for (int key = 0; key < substKeywords.length; ++key) {
                    if (d == substKeywords[key]) {
                        out.append(replacements[key]);
                        break;
                    }
                }
            }
            else {
                out.append(c);
            }
        }
        return out.toString();
    }

    /** Constant <code>hostHeaders="{X-Forwarded-Host,     // Apache ProxyP"{trunked}</code> */
    protected static final String[] hostHeaders = {
        "X-Forwarded-Host",     // Apache ProxyPass
        "X-Host",               // lighttpd
        "Host"                  // unproxied
    };

    /**
     * Obtains the host and port used by the end user.
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getHostHeader(HttpServletRequest request) {
        for (int i = 0; i < hostHeaders.length; ++i) {
            String ret = request.getHeader(hostHeaders[i]);
            if (ret != null) {
                return ret;
            }
        }
        return request.getServerName() + ":"
                + Integer.toString(request.getServerPort());
    }

    /**
     * Convenience method for resolving the human-readable hostname for an IP
     * address, if at all possible. If the hostname cannot be found, this method
     * returns the IP address parameter.
     *
     * @param ipAddress
     *            the IP address for which you want the hostname
     * @deprecated Please use {@link NetworkElementFactory#getHostname
     *             NetworkElementFactory.getHostname} instead.
     * @return a {@link java.lang.String} object.
     */
    public static String getHostname(String ipAddress) {
        String hostname = ipAddress;

        try {
            hostname = NetworkElementFactory.getHostname(ipAddress);
        } catch (Exception e) {
            // ignore this exception and just return the IP address
        }

        return (hostname);
    }

    /**
     * Encapsulate the deprecated encode method to fix it in one place.
     *
     * @param string
     *            string to be encoded
     * @return encoded string
     */
    public static String encode(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // UTF-8 should *never* throw this
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * Encapsulate the deprecated decode method to fix it in one place.
     *
     * @param string
     *            string to be decoded
     * @return decoded string
     */
    public static String decode(String string) {
        try {
            return URLDecoder.decode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // UTF-8 should *never* throw this
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * Convenience method for resolving the human-readable hostname for an IP
     * address, if at all possible. If the hostname cannot be found, from the
     * table, this method returns the IP address parameter. This method doesnt
     * throw any exception.
     *
     * @param ipAddress
     *            the IP address for which you want the hostname
     * @deprecated Please use {@link NetworkElementFactory#getHostname
     *             NetworkElementFactory.getHostname} instead.
     * @return a {@link java.lang.String} object.
     */
    public static String resolveIpAddress(String ipAddress) {
        String hostname = ipAddress;

        try {
            hostname = NetworkElementFactory.getHostname(ipAddress);
        } catch (Exception e) {
            // ignore this exception and just return the IP address
        }

        return (hostname);
    }


    /**
     * Creates hidden tags for all the parameters given in the request.
     *
     * @param request
     *            the <code>HttpServletRequest</code> to read the parameters
     *            from
     * @return A string containing an HTML &lt;input type="hidden" name="
     *         <code>paramName</code>" value=" <code>paramValue</code>"
     *         /&gt; tag for each parameter.
     */
    public static String makeHiddenTags(HttpServletRequest request) {
        return (makeHiddenTags(request, new HashMap(), new String[0]));
    }

    /**
     * Creates hidden tags for all the parameters given in the request.
     *
     * @param request
     *            the <code>HttpServletRequest</code> to read the parameters
     *            from
     * @param additions
     *            a map of extra parameters to create hidden tags for
     * @return A string containing an HTML &lt;input type="hidden" name="
     *         <code>paramName</code>" value=" <code>paramValue</code>"
     *         /&gt; tag for each parameter.
     */
    public static String makeHiddenTags(HttpServletRequest request, Map additions) {
        return (makeHiddenTags(request, additions, new String[0]));
    }

    /**
     * Creates hidden tags for all the parameters given in the request.
     *
     * @param request
     *            the <code>HttpServletRequest</code> to read the parameters
     *            from
     * @param ignores
     *            A string array containing request parameters to ignore
     * @return A string containing an HTML &lt;input type="hidden" name="
     *         <code>paramName</code>" value=" <code>paramValue</code>"
     *         /&gt; tag for each parameter.
     */
    public static String makeHiddenTags(HttpServletRequest request, String[] ignores) {
        return (makeHiddenTags(request, new HashMap(), ignores));
    }

    /**
     * Creates hidden tags for all the parameters given in the request plus the
     * additions, except for the parameters and additions listed in the ignore
     * list.
     *
     * @param request
     *            the <code>HttpServletRequest</code> to read the parameters
     *            from
     * @param additions
     *            a map of extra parameters to create hidden tags for
     * @param ignores
     *            the list of parameters not to create a hidden tag for
     * @return A string containing an HTML &lt;input type="hidden" name="
     *         <code>paramName</code>" value=" <code>paramValue</code>"
     *         /&gt; tag for each parameter not in the ignore list.
     */
    public static String makeHiddenTags(HttpServletRequest request, Map additions, String[] ignores) {
        return (makeHiddenTags(request, additions, ignores, IgnoreType.BOTH));
    }

    /**
     * Creates hidden tags for all the parameters given in the request plus the
     * additions, except for the parmeters listed in the ignore list.
     *
     * @param request
     *            the <code>HttpServletRequest</code> to read the parameters
     *            from
     * @param additions
     *            a map of extra parameters to create hidden tags for
     * @param ignores
     *            the list of parameters not to create a hidden tag for
     * @param ignoreType
     *            whether the ignore list applies to the request parameters,
     *            values in the additions map, or both
     * @return A string containing an HTML &lt;input type="hidden" name="
     *         <code>paramName</code>" value=" <code>paramValue</code>"
     *         /&gt; tag for each parameter not in the ignore list.
     */
    public static String makeHiddenTags(HttpServletRequest request, Map additions, String[] ignores, IgnoreType ignoreType) {
        if (request == null || additions == null || ignores == null || ignoreType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        StringBuffer buffer = new StringBuffer();

        ArrayList<String> ignoreList = new ArrayList<String>();
        for (int i = 0; i < ignores.length; i++) {
            ignoreList.add(ignores[i]);
        }

        Enumeration names = request.getParameterNames();

        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            String[] values = request.getParameterValues(name);

            if ((ignoreType == IgnoreType.ADDITIONS_ONLY || !ignoreList.contains(name)) && values != null) {
                for (int i = 0; i < values.length; i++) {
                    buffer.append("<input type=\"hidden\" name=\"");
                    buffer.append(name);
                    buffer.append("\" value=\"");
                    buffer.append(WebSecurityUtils.sanitizeString(values[i]));
                    buffer.append("\" />");
                    buffer.append("\n");
                }
            }
        }

        // Add the additions in
        Set keySet = additions.keySet();
        Iterator keys = keySet.iterator();

        while (keys.hasNext()) {
            String name = (String) keys.next();

            // handle both a String value or a String[] value
            Object tmp = additions.get(name);
            String[] values = (tmp instanceof String[]) ? ((String[]) tmp) : (new String[] { (String) tmp });

            if ((ignoreType == IgnoreType.REQUEST_ONLY || !ignoreList.contains(name)) && values != null) {
                for (int i = 0; i < values.length; i++) {
                    buffer.append("<input type=\"hidden\" name=\"");
                    buffer.append(name);
                    buffer.append("\" value=\"");
                    buffer.append(values[i]);
                    buffer.append("\" />");
                    buffer.append("\n");
                }
            }
        }

        return (buffer.toString());
    }

    /**
     * Creates a query string of the format "key1=value1&amp;key2=value2" for
     * each parameter in the given <code>HttpServletRequest</code>.
     *
     * @see #makeQueryString( HttpServletRequest, Map, String[] )
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @return a {@link java.lang.String} object.
     */
    public static String makeQueryString(HttpServletRequest request) {
        return (makeQueryString(request, new HashMap(), new String[0]));
    }

    /**
     * Creates a query string of the format "key1=value1&amp;key2=value2" for
     * each parameter in the given <code>HttpServletRequest</code> and key in
     * given <code>Map</code>.
     *
     * @see #makeQueryString( HttpServletRequest, Map, String[] )
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param additions a {@link java.util.Map} object.
     * @return a {@link java.lang.String} object.
     */
    public static String makeQueryString(HttpServletRequest request, Map additions) {
        return (makeQueryString(request, additions, new String[0]));
    }

    /**
     * Creates a query string of the format "key1=value1&amp;key2=value2" for
     * each parameter in the given <code>HttpServletRequest</code> that is not
     * listed in the ignore list.
     *
     * @see #makeQueryString( HttpServletRequest, Map, String[] )
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param ignores an array of {@link java.lang.String} objects.
     * @return a {@link java.lang.String} object.
     */
    public static String makeQueryString(HttpServletRequest request, String[] ignores) {
        return (makeQueryString(request, new HashMap(), ignores));
    }

    /**
     * Creates a query string of the format "key1=value1&amp;key2=value2" for
     * each parameter in the given <code>HttpServletRequest</code> and key in
     * given <code>Map</code> that is not listed in the ignore list.
     *
     * @param request
     *            the <code>HttpServletRequest</code> to read the parameters
     *            from
     * @param additions
     *            a mapping of strings to strings or string arrays to be
     *            included in the query string
     * @param ignores
     *            the list of parameters and map entries not to include
     * @return A string in the <em>x-www-form-urlencoded</em> format that is
     *         suitable for adding to a URL as a query string.
     */
    public static String makeQueryString(HttpServletRequest request, Map additions, String[] ignores) {
        return (makeQueryString(request, additions, ignores, IgnoreType.BOTH));
    }

    /**
     * Creates a query string of the format "key1=value1&amp;key2=value2" for
     * each parameter in the given <code>HttpServletRequest</code> and key in
     * given <code>Map</code> that is not listed in the ignore list.
     *
     * @param request
     *            the <code>HttpServletRequest</code> to read the parameters
     *            from
     * @param additions
     *            a mapping of strings to strings or string arrays to be
     *            included in the query string
     * @param ignores
     *            the list of parameters and map entries not to include
     * @return A string in the <em>x-www-form-urlencoded</em> format that is
     *         suitable for adding to a URL as a query string.
     * @param ignoreType a {@link org.opennms.web.Util.IgnoreType} object.
     */
    public static String makeQueryString(HttpServletRequest request, Map additions, String[] ignores, IgnoreType ignoreType) {
        if (request == null || additions == null || ignores == null || ignoreType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        StringBuffer buffer = new StringBuffer();

        ArrayList<String> ignoreList = new ArrayList<String>();
        for (int i = 0; i < ignores.length; i++) {
            ignoreList.add(ignores[i]);
        }

        Enumeration names = request.getParameterNames();

        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            String[] values = request.getParameterValues(name);

            if ((ignoreType == IgnoreType.ADDITIONS_ONLY || !ignoreList.contains(name)) && values != null) {
                for (int i = 0; i < values.length; i++) {
                    buffer.append("&");
                    buffer.append(name);
                    buffer.append("=");
                    buffer.append(Util.encode(values[i]));
                }
            }
        }

        Set keySet = additions.keySet();
        Iterator keys = keySet.iterator();

        while (keys.hasNext()) {
            String name = (String) keys.next();

            // handle both a String value or a String[] value
            Object tmp = additions.get(name);
            String[] values;
            if (tmp instanceof String[]) {
                values = (String[]) tmp;
            } else if (tmp instanceof String) {
                values = new String[] { (String) tmp };
            } else {
                throw new IllegalArgumentException("addition \"" + name + "\" is not of type String or String[], but is of type: "
                                                   + tmp.getClass().getName());
            }

            if ((ignoreType == IgnoreType.REQUEST_ONLY || !ignoreList.contains(name)) && values != null) {
                for (int i = 0; i < values.length; i++) {
                    buffer.append("&");
                    buffer.append(name);
                    buffer.append("=");
                    buffer.append(Util.encode(values[i]));
                }
            }
        }

        // removes the first & from the buffer
        buffer.deleteCharAt(0);

        return (buffer.toString());
    }

    public static class IgnoreType extends Object {
        public static final int _REQUEST_ONLY = 0;

        public static final int _ADDITIONS_ONLY = 0;

        public static final int _BOTH = 0;

        public static final IgnoreType REQUEST_ONLY = new IgnoreType(_REQUEST_ONLY);

        public static final IgnoreType ADDITIONS_ONLY = new IgnoreType(_ADDITIONS_ONLY);

        public static final IgnoreType BOTH = new IgnoreType(_BOTH);

        protected int value;

        private IgnoreType(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    /**
     * <p>getOrderedMap</p>
     *
     * @param names an array of {@link java.lang.String} objects.
     * @return a {@link java.util.Map} object.
     */
    public static Map<String, String> getOrderedMap(String names[][]) {
        TreeMap<String, String> orderedMap = new TreeMap<String, String>();

        for (int i = 0; i < names.length; i++) {
            orderedMap.put(names[i][1], names[i][0]);
        }

        return orderedMap;
    }

    /**
     * <p>htmlify</p>
     *
     * @param input a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String htmlify(String input) {
        return (input == null ? null : input.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
    }
    
    /**
     * <p>createEventProxy</p>
     *
     * @return a {@link org.opennms.netmgt.model.events.EventProxy} object.
     */
    public static EventProxy createEventProxy() {
        /*
         * Rather than defaulting to localhost all the time, give an option in properties
         */
        String proxyHostName = Vault.getProperty("opennms.rtc.event.proxy.host") == null ? "127.0.0.1" : Vault.getProperty("opennms.rtc.event.proxy.host");
        String proxyHostPort = Vault.getProperty("opennms.rtc.event.proxy.port") == null ? Integer.toString(TcpEventProxy.DEFAULT_PORT) : Vault.getProperty("opennms.rtc.event.proxy.port");
        String proxyHostTimeout = Vault.getProperty("opennms.rtc.event.proxy.timeout") == null ? Integer.toString(TcpEventProxy.DEFAULT_TIMEOUT) : Vault.getProperty("opennms.rtc.event.proxy.timeout");
        InetAddress proxyAddr = null;
        EventProxy proxy = null;

        try {
            proxyAddr = InetAddress.getByName(proxyHostName);
        } catch (UnknownHostException e) {
            proxyAddr = null;
        }

        if (proxyAddr == null) {
            try {
                proxy = new TcpEventProxy();
            } catch (UnknownHostException e) {
                // XXX Ewwww!  We should just let the first UnknownException bubble up. 
                throw new UndeclaredThrowableException(e);
            }
        } else {
            proxy = new TcpEventProxy(new InetSocketAddress(proxyAddr, Integer.parseInt(proxyHostPort)), Integer.parseInt(proxyHostTimeout));
        }
        return proxy;
    }

    /**
     * An utility method to format a 'Date' into a string in the local specific
     * DEFALUT DateFormat style for both the date and time. This is used by the
     * webui and a change here should get all time display in the webui changed.
     *
     * @see java.text.DateFormat
     * @param date a {@link java.util.Date} object.
     * @return a {@link java.lang.String} object.
     */
    public static final String formatDateToUIString(Date date) {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(date);
    }

}
