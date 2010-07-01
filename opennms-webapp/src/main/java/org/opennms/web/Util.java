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
// 2008 Aug 31: Make makeQueryString work when the query string it produces is empty. - dj@opennms.org
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
 * @since 1.8.1
 */
public abstract class Util {

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
        return org.opennms.web.api.Util.calculateUrlBase(request);
    }

    /**
     * Obtains the host and port used by the end user.
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getHostHeader(HttpServletRequest request) {
        return org.opennms.web.api.Util.getHostHeader(request);
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
        return org.opennms.web.api.Util.encode(string);
    }

    /**
     * Encapsulate the deprecated decode method to fix it in one place.
     *
     * @param string
     *            string to be decoded
     * @return decoded string
     */
    public static String decode(String string) {
        return org.opennms.web.api.Util.decode(string);
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
        return (org.opennms.web.api.Util.makeHiddenTags(request, new HashMap(), new String[0]));
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
        return (org.opennms.web.api.Util.makeHiddenTags(request, additions, new String[0]));
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
        return (org.opennms.web.api.Util.makeHiddenTags(request, new HashMap(), ignores));
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
        return (org.opennms.web.api.Util.makeHiddenTags(request, additions, ignores, org.opennms.web.api.Util.IgnoreType.BOTH));
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
    public static String makeHiddenTags(HttpServletRequest request, Map additions, String[] ignores, org.opennms.web.api.Util.IgnoreType ignoreType) {
        return org.opennms.web.api.Util.makeHiddenTags(request, additions, ignores, ignoreType);
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
        return (org.opennms.web.api.Util.makeQueryString(request, new HashMap(), new String[0]));
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
        return (org.opennms.web.api.Util.makeQueryString(request, additions, new String[0]));
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
        return (org.opennms.web.api.Util.makeQueryString(request, new HashMap(), ignores));
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
        return (org.opennms.web.api.Util.makeQueryString(request, additions, ignores, org.opennms.web.api.Util.IgnoreType.BOTH));
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
     * @param ignoreType a {@link org.opennms.web.api.Util.IgnoreType} object.
     */
    public static String makeQueryString(HttpServletRequest request, Map additions, String[] ignores, org.opennms.web.api.Util.IgnoreType ignoreType) {
        return org.opennms.web.api.Util.makeQueryString(request, additions, ignores, ignoreType);
    }

    /**
     * <p>getOrderedMap</p>
     *
     * @param names an array of {@link java.lang.String} objects.
     * @return a {@link java.util.Map} object.
     */
    public static Map<String, String> getOrderedMap(String names[][]) {
        return org.opennms.web.api.Util.getOrderedMap(names);
    }

    /**
     * <p>htmlify</p>
     *
     * @param input a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String htmlify(String input) {
        return org.opennms.web.api.Util.htmlify(input);
    }
    
    /**
     * <p>createEventProxy</p>
     *
     * @return a {@link org.opennms.netmgt.model.events.EventProxy} object.
     */
    public static EventProxy createEventProxy() {
        return org.opennms.web.api.Util.createEventProxy();
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
        return org.opennms.web.api.Util.formatDateToUIString(date);
    }
    
    /**
     * <p>convertToJsSafeString</p>
     *
     * @param str a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String convertToJsSafeString(String str){
        return org.opennms.web.api.Util.convertToJsSafeString(str);
    }

}
