/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.api;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.opennms.core.resource.Vault;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.support.TcpEventProxy;

/**
 * Provides convenience functions for web-based interfaces.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public abstract class Util extends Object {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final Map<String, Object> EMPTY_MAP = Collections.emptyMap();

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
     * (for embedded Jetty).
     * </p>
     *
     * @param request
     *            the servlet request you are servicing
     * @return a {@link java.lang.String} object.
     */
    public static String calculateUrlBase(final HttpServletRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String tmpl = Vault.getProperty("opennms.web.base-url");
        if (tmpl == null) {
            tmpl = "%s://%x%c/";
        }
        final String retval = substituteUrl(request, tmpl);
        if (retval.endsWith("/")) {
        	return retval;
        } else {
        	return retval + "/";
        }
    }

    public static String calculateUrlBase(final HttpServletRequest request, final String path) {
    	if (request == null || path == null) {
    		throw new IllegalArgumentException("Cannot take null parameters.");
    	}
    	
    	String tmpl = Vault.getProperty("opennms.web.base-url");
        if (tmpl == null) {
            tmpl = "%s://%x%c";
        }
        return substituteUrl(request, tmpl).replaceAll("/+$", "") + "/" + path.replaceAll("^/+", "");
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
    protected static String substituteUrl(final HttpServletRequest request, final String tmpl) {
    	final String[] replacements = {
            request.getScheme(),                        // %s
            request.getServerName(),                    // %h
            Integer.toString(request.getServerPort()),  // %p
            getHostHeader(request),                     // %x
            request.getContextPath()                    // %c
        };

        final StringBuilder out = new StringBuilder(48);
        for (int i = 0; i < tmpl.length();) {
            final char c = tmpl.charAt(i++);
            if (c == '%' && i < tmpl.length()) {
                final char d = tmpl.charAt(i++);
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
    public static String getHostHeader(final HttpServletRequest request) {
        for (int i = 0; i < hostHeaders.length; ++i) {
            // Get the first value in the header (support for proxy-chaining)
            final String header = request.getHeader(hostHeaders[i]);
            if (header != null) {
                final String[] values = header.split(", *");
                if (values.length >= 1) {
                    return values[0];
                }
            }
        }
        return request.getServerName() + ":" + Integer.toString(request.getServerPort());
    }

    /**
     * Encapsulate the deprecated encode method to fix it in one place.
     *
     * @param string
     *            string to be encoded
     * @return encoded string
     */
    public static String encode(final String string) {
        try {
            return URLEncoder.encode(string, StandardCharsets.UTF_8.name());
        } catch (final UnsupportedEncodingException e) {
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
    public static String decode(final String string) {
        try {
            return URLDecoder.decode(string, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            // UTF-8 should *never* throw this
            throw new UndeclaredThrowableException(e);
        }
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
    public static String makeHiddenTags(final HttpServletRequest request) {
        return (makeHiddenTags(request, EMPTY_MAP, EMPTY_STRING_ARRAY));
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
    public static String makeHiddenTags(final HttpServletRequest request, final Map<String,Object> additions) {
        return (makeHiddenTags(request, additions, EMPTY_STRING_ARRAY));
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
    public static String makeHiddenTags(final HttpServletRequest request, final String[] ignores) {
        return (makeHiddenTags(request, EMPTY_MAP, ignores));
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
    public static String makeHiddenTags(final HttpServletRequest request, final Map<String,Object> additions, final String[] ignores) {
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
    public static String makeHiddenTags(final HttpServletRequest request, final Map<String,Object> additions, final String[] ignores, final IgnoreType ignoreType) {
        if (request == null || additions == null || ignores == null || ignoreType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        final StringBuilder buffer = new StringBuilder();

        final List<String> ignoreList = Arrays.asList(ignores);

        final Enumeration<String> names = request.getParameterNames();

        while (names.hasMoreElements()) {
        	final String name = names.nextElement();
        	final String[] values = request.getParameterValues(name);

            if ((ignoreType == IgnoreType.ADDITIONS_ONLY || !ignoreList.contains(name)) && values != null) {
            	for (final String value : values) {
                    buffer.append("<input type=\"hidden\" name=\"");
                    buffer.append(WebSecurityUtils.sanitizeString(name));
                    buffer.append("\" value=\"");
                    buffer.append(WebSecurityUtils.sanitizeString(value));
                    buffer.append("\" />");
                    buffer.append("\n");
                }
            }
        }

        for (final Entry<String,Object> entry : additions.entrySet()) {
            final String name = entry.getKey();
            // handle both a String value or a String[] value
        	final Object tmp = entry.getValue();
        	final String[] values = (tmp instanceof String[]) ? ((String[]) tmp) : (new String[] { (String) tmp });

            if ((ignoreType == IgnoreType.REQUEST_ONLY || !ignoreList.contains(name)) && values != null) {
            	for (final String value : values) {
                    buffer.append("<input type=\"hidden\" name=\"");
                    buffer.append(WebSecurityUtils.sanitizeString(name));
                    buffer.append("\" value=\"");
                    buffer.append(WebSecurityUtils.sanitizeString(value));
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
    public static String makeQueryString(final HttpServletRequest request) {
        return (makeQueryString(request, EMPTY_MAP, EMPTY_STRING_ARRAY));
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
    public static String makeQueryString(final HttpServletRequest request, final Map<String,Object> additions) {
        return (makeQueryString(request, additions, EMPTY_STRING_ARRAY));
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
    public static String makeQueryString(final HttpServletRequest request, final String[] ignores) {
        return (makeQueryString(request, EMPTY_MAP, ignores));
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
    public static String makeQueryString(final HttpServletRequest request, final Map<String,Object> additions, final String[] ignores) {
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
     * @param ignoreType a {@link org.opennms.web.api.Util.IgnoreType} object.
     */
    public static String makeQueryString(final HttpServletRequest request, final Map<String,Object> additions, final String[] ignores, final IgnoreType ignoreType) {
        if (request == null || additions == null || ignores == null || ignoreType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        final StringBuilder buffer = new StringBuilder();

        final List<String> ignoreList = Arrays.asList(ignores);

        final Enumeration<String> names = request.getParameterNames();

        while (names.hasMoreElements()) {
        	final String name = (String) names.nextElement();
        	final String[] values = request.getParameterValues(name);

            if ((ignoreType == IgnoreType.ADDITIONS_ONLY || !ignoreList.contains(name)) && values != null) {
                for (int i = 0; i < values.length; i++) {
                    buffer.append("&");
                    buffer.append(name);
                    buffer.append("=");
                    buffer.append(Util.encode(values[i]));
                }
            }
        }

        for (final Entry<String,Object> entry : additions.entrySet()) {
            final String name = entry.getKey();
            // handle both a String value or a String[] value
            final Object tmp = entry.getValue();
            final String[] values;
            if (tmp instanceof String[]) {
                values = (String[]) tmp;
            } else if (tmp instanceof String) {
                values = new String[] { (String) tmp };
            } else {
                throw new IllegalArgumentException("addition \"" + name + "\" is not of type String or String[], but is of type: " + tmp.getClass().getName());
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
        if (buffer.length() > 0 && buffer.charAt(0) == '&') {
            buffer.deleteCharAt(0);
        }

        return buffer.toString();
    }

    public static enum IgnoreType {
        REQUEST_ONLY,
        ADDITIONS_ONLY,
        BOTH
    }

    /**
     * <p>htmlify</p>
     *
     * @param input a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String htmlify(final String input) {
        return (input == null ? null : input.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
    }
    
    /**
     * <p>createEventProxy</p>
     * 
     * @deprecated Use dependency injection to wire in an instance of the {@link EventProxy} instead
     *
     * @return a {@link org.opennms.netmgt.events.api.EventProxy} object.
     */
    public static EventProxy createEventProxy() {
        /*
         * Rather than defaulting to localhost all the time, give an option in properties
         */
    	final String vaultHost = Vault.getProperty("opennms.rtc.event.proxy.host");
    	final String vaultPort = Vault.getProperty("opennms.rtc.event.proxy.port");
    	final String vaultTimeout = Vault.getProperty("opennms.rtc.event.proxy.timeout");

    	final String proxyHostName = vaultHost == null ? "127.0.0.1" : vaultHost;
		final String proxyHostPort = vaultPort == null ? Integer.toString(TcpEventProxy.DEFAULT_PORT) : vaultPort;
		final String proxyHostTimeout = vaultTimeout == null ? Integer.toString(TcpEventProxy.DEFAULT_TIMEOUT) : vaultTimeout;

		InetAddress proxyAddr = null;
        EventProxy proxy = null;

        proxyAddr = InetAddressUtils.addr(proxyHostName);

        if (proxyAddr == null) {
            try {
                proxy = new TcpEventProxy();
            } catch (final UnknownHostException e) {
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
     * @deprecated We should use the <code>fmt:formatDate</code> taglib at the JSP level 
     *   instead of converting {@link Date} instances into {@link String} instances inside 
     *   the model code.
     */
    public static final String formatDateToUIString(final Date date) {
        if (date != null) {
            return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(date);
        }
        return "";
    }
    
    /**
     * <p>convertToJsSafeString</p>
     *
     * @param str a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String convertToJsSafeString(final String str){
        return str
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\t", "\\t")
        .replace("\r", "\\r")
        .replace("\n", "\\n")
        .replace("\b", "\\b");
    }

    public static String getParameter(HttpServletRequest request, String name) {
        return getParameter(request, name, null);
    }

    // Returns request parameter or default if the parameter does not exist
    public static String getParameter(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getParameter(name);
        if (value == null || value.isEmpty() && defaultValue != null && !defaultValue.isEmpty()) {
            return defaultValue;
        }
        return value;
    }

}
