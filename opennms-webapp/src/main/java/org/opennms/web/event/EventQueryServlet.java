/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.web.api.Util;
import org.opennms.web.event.filter.AfterDateFilter;
import org.opennms.web.event.filter.BeforeDateFilter;
import org.opennms.web.event.filter.IPAddrLikeFilter;
import org.opennms.web.event.filter.LogMessageMatchesAnyFilter;
import org.opennms.web.event.filter.LogMessageSubstringFilter;
import org.opennms.web.event.filter.NodeNameLikeFilter;
import org.opennms.web.event.filter.ServiceFilter;
import org.opennms.web.event.filter.SeverityFilter;
import org.opennms.web.filter.Filter;
import org.opennms.web.servlet.MissingParameterException;

/**
 * This servlet takes a large and specific request parameter set and maps it to
 * the more robust "filter" parameter set of the
 * servlet via a redirect.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class EventQueryServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = 1226547298266948865L;

    /**
     * The list of parameters that are extracted by this servlet and not passed
     * on to the servlet.
     */
    protected static String[] IGNORE_LIST = new String[] { "msgsub", "msgmatchany", "nodenamelike", "service", "iplike", "severity", "relativetime", "usebeforetime", "beforehour", "beforeminute", "beforeampm", "beforedate", "beforemonth", "beforeyear", "useaftertime", "afterhour", "afterminute", "afterampm", "afterdate", "aftermonth", "afteryear" };

    /**
     * The URL for the servlet. The
     * default is "list." This URL is a sibling URL, so it is relative to the
     * URL directory that was used to call this servlet (usually "event/").
     */
    protected String redirectUrl = "filter";

    /**
     * <p>init</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    @Override
    public void init() throws ServletException {
        ServletConfig config = this.getServletConfig();

        if (config.getInitParameter("redirect.url") != null) {
            redirectUrl = config.getInitParameter("redirect.url");
        }
    }

    /**
     * {@inheritDoc}
     *
     * Extracts the key parameters from the parameter set, translates them into
     * filter-based parameters, and then passes the modified parameter set to
     * the event filter.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Filter> filterArray = new ArrayList<Filter>();

        // convenient syntax for LogMessageSubstringFilter
        String msgSubstring = WebSecurityUtils.sanitizeString(request.getParameter("msgsub"));
        if (msgSubstring != null && msgSubstring.length() > 0) {
            filterArray.add(new LogMessageSubstringFilter(msgSubstring));
        }

        // convenient syntax for LogMessageMatchesAnyFilter
        String msgMatchAny = WebSecurityUtils.sanitizeString(request.getParameter("msgmatchany"));
        if (msgMatchAny != null && msgMatchAny.length() > 0) {
            filterArray.add(new LogMessageMatchesAnyFilter(msgMatchAny));
        }

        // convenient syntax for NodeNameContainingFilter
        String nodeNameLike = WebSecurityUtils.sanitizeString(request.getParameter("nodenamelike"));
        if (nodeNameLike != null && nodeNameLike.length() > 0) {
            filterArray.add(new NodeNameLikeFilter(nodeNameLike));
        }

        // convenient syntax for ServiceFilter
        String service = WebSecurityUtils.sanitizeString(request.getParameter("service"));
        if (service != null && !service.equalsIgnoreCase("any")) {
            filterArray.add(new ServiceFilter(WebSecurityUtils.safeParseInt(service), this.getServletContext()));
        }

        // convenient syntax for IPLikeFilter
        String ipLikePattern = WebSecurityUtils.sanitizeString(request.getParameter("iplike"));
        if (ipLikePattern != null && !ipLikePattern.equals("")) {
            filterArray.add(new IPAddrLikeFilter(ipLikePattern));
        }

        // convenient syntax for SeverityFilter
        String severity = WebSecurityUtils.sanitizeString(request.getParameter("severity"));
        if (severity != null && !severity.equalsIgnoreCase("any")) {
            filterArray.add(new SeverityFilter(WebSecurityUtils.safeParseInt(severity)));
        }

        // convenient syntax for AfterDateFilter as relative to current time
        String relativeTime = WebSecurityUtils.sanitizeString(request.getParameter("relativetime"));
        if (relativeTime != null && !relativeTime.equalsIgnoreCase("any")) {
            try {
                filterArray.add(EventUtil.getRelativeTimeFilter(WebSecurityUtils.safeParseInt(relativeTime)));
            } catch (IllegalArgumentException e) {
                // ignore the relative time if it is an illegal value
                this.log("Illegal relativetime value", e);
            }
        }

        String useBeforeTime = WebSecurityUtils.sanitizeString(request.getParameter("usebeforetime"));
        if (useBeforeTime != null && useBeforeTime.equals("1")) {
            try {
                filterArray.add(this.getBeforeDateFilter(request));
            } catch (IllegalArgumentException e) {
                // ignore the before time if it is an illegal value
                this.log("Illegal before time value", e);
            } catch (MissingParameterException e) {
                throw new ServletException(e);
            }
        }

        String useAfterTime = WebSecurityUtils.sanitizeString(request.getParameter("useaftertime"));
        if (useAfterTime != null && useAfterTime.equals("1")) {
            try {
                filterArray.add(this.getAfterDateFilter(request));
            } catch (IllegalArgumentException e) {
                // ignore the after time if it is an illegal value
                this.log("Illegal after time value", e);
            } catch (MissingParameterException e) {
                throw new ServletException(e);
            }
        }

        String queryString = "";

        if (filterArray.size() > 0) {
            String[] filterStrings = new String[filterArray.size()];

            for (int i = 0; i < filterStrings.length; i++) {
                Filter filter = filterArray.get(i);
                filterStrings[i] = EventUtil.getFilterString(filter);
            }

            Map<String, Object> paramAdditions = new HashMap<String, Object>();
            paramAdditions.put("filter", filterStrings);

            queryString = WebSecurityUtils.sanitizeString(Util.makeQueryString(request, paramAdditions, IGNORE_LIST));
        } else {
            queryString = WebSecurityUtils.sanitizeString(Util.makeQueryString(request, IGNORE_LIST));
        }

        response.sendRedirect(redirectUrl + "?" + queryString);
    }

    /**
     * <p>getBeforeDateFilter</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @return a {@link org.opennms.web.event.filter.BeforeDateFilter} object.
     */
    protected BeforeDateFilter getBeforeDateFilter(HttpServletRequest request) {
        Date beforeDate = this.getDateFromRequest(request, "before");
        return (new BeforeDateFilter(beforeDate));
    }

    /**
     * <p>getAfterDateFilter</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @return a {@link org.opennms.web.event.filter.AfterDateFilter} object.
     */
    protected AfterDateFilter getAfterDateFilter(HttpServletRequest request) {
        Date afterDate = this.getDateFromRequest(request, "after");
        return (new AfterDateFilter(afterDate));
    }

    /**
     * <p>getDateFromRequest</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param prefix a {@link java.lang.String} object.
     * @return a java$util$Date object.
     * @throws org.opennms.web.servlet.MissingParameterException if any.
     */
    protected Date getDateFromRequest(HttpServletRequest request, String prefix) throws MissingParameterException {
        if (request == null || prefix == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Calendar cal = Calendar.getInstance();

        // be lenient to handle the inputs easier
        // read the java.util.Calendar javadoc for more info
        cal.setLenient(true);

        // hour, from 1-12
        String hourString = WebSecurityUtils.sanitizeString(request.getParameter(prefix + "hour"));
        if (hourString == null) {
            throw new MissingParameterException(prefix + "hour", this.getRequiredDateFields(prefix));
        }

        cal.set(Calendar.HOUR, WebSecurityUtils.safeParseInt(hourString));

        // minute, from 0-59
        String minuteString = WebSecurityUtils.sanitizeString(request.getParameter(prefix + "minute"));
        if (minuteString == null) {
            throw new MissingParameterException(prefix + "minute", this.getRequiredDateFields(prefix));
        }

        cal.set(Calendar.MINUTE, WebSecurityUtils.safeParseInt(minuteString));

        // AM/PM, either AM or PM
        String amPmString = WebSecurityUtils.sanitizeString(request.getParameter(prefix + "ampm"));
        if (amPmString == null) {
            throw new MissingParameterException(prefix + "ampm", this.getRequiredDateFields(prefix));
        }

        if (amPmString.equalsIgnoreCase("am")) {
            cal.set(Calendar.AM_PM, Calendar.AM);
        } else if (amPmString.equalsIgnoreCase("pm")) {
            cal.set(Calendar.AM_PM, Calendar.PM);
        } else {
            throw new IllegalArgumentException("Illegal AM/PM value: " + amPmString);
        }

        // month, 0-11 (Jan-Dec)
        String monthString = WebSecurityUtils.sanitizeString(request.getParameter(prefix + "month"));
        if (monthString == null) {
            throw new MissingParameterException(prefix + "month", this.getRequiredDateFields(prefix));
        }

        cal.set(Calendar.MONTH, WebSecurityUtils.safeParseInt(monthString));

        // date, 1-31
        String dateString = WebSecurityUtils.sanitizeString(request.getParameter(prefix + "date"));
        if (dateString == null) {
            throw new MissingParameterException(prefix + "date", this.getRequiredDateFields(prefix));
        }

        cal.set(Calendar.DATE, WebSecurityUtils.safeParseInt(dateString));

        // year
        String yearString = WebSecurityUtils.sanitizeString(request.getParameter(prefix + "year"));
        if (yearString == null) {
            throw new MissingParameterException(prefix + "year", this.getRequiredDateFields(prefix));
        }

        cal.set(Calendar.YEAR, WebSecurityUtils.safeParseInt(yearString));

        return cal.getTime();
    }

    /**
     * <p>getRequiredDateFields</p>
     *
     * @param prefix a {@link java.lang.String} object.
     * @return an array of {@link java.lang.String} objects.
     */
    protected String[] getRequiredDateFields(String prefix) {
        return new String[] { prefix + "hour", prefix + "minute", prefix + "ampm", prefix + "date", prefix + "month", prefix + "year" };
    }

}
