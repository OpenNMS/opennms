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

package org.opennms.web.outage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.web.api.Util;
import org.opennms.web.outage.filter.LocationFilter;
import org.opennms.web.outage.filter.NegativeLocationFilter;
import org.opennms.web.filter.Filter;
import org.opennms.web.outage.filter.AssetFilter;
import org.opennms.web.outage.filter.ForeignSourceFilter;
import org.opennms.web.outage.filter.InterfaceFilter;
import org.opennms.web.outage.filter.LostServiceDateAfterFilter;
import org.opennms.web.outage.filter.LostServiceDateBeforeFilter;
import org.opennms.web.outage.filter.NegativeForeignSourceFilter;
import org.opennms.web.outage.filter.NegativeInterfaceFilter;
import org.opennms.web.outage.filter.NegativeNodeFilter;
import org.opennms.web.outage.filter.NegativeServiceFilter;
import org.opennms.web.outage.filter.NodeFilter;
import org.opennms.web.outage.filter.OutageIdFilter;
import org.opennms.web.outage.filter.RegainedServiceDateAfterFilter;
import org.opennms.web.outage.filter.RegainedServiceDateBeforeFilter;
import org.opennms.web.outage.filter.ServiceFilter;

/**
 * <p>Abstract OutageUtil class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class OutageUtil extends Object {
    /** Constant <code>DOWN_COLOR="red"</code> */
    protected static final String DOWN_COLOR = "red";

    /** Constant <code>FILTER_SERVLET_URL_BASE="outage/list.htm"</code> */
    public static final String FILTER_SERVLET_URL_BASE = "outage/list.htm";

    /**
     * <p>getFilter</p>
     *
     * @param filterString a {@link java.lang.String} object.
     * @return a org$opennms$web$filter$Filter object.
     */
    public static Filter getFilter(String filterString, ServletContext servletContext) {
        if (filterString == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter filter = null;

        StringTokenizer tokens = new StringTokenizer(filterString, "=");
        String type;
        String value;
        try {
            type = tokens.nextToken();
            value = tokens.nextToken();
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("Could not tokenize filter string: " + filterString);
        }

        if (type.equals(NodeFilter.TYPE)) {
            filter = new NodeFilter(WebSecurityUtils.safeParseInt(value), servletContext);
        } else if (type.equals(ForeignSourceFilter.TYPE)) {
            filter = new ForeignSourceFilter(value);
        } else if (type.equals(InterfaceFilter.TYPE)) {
            filter = new InterfaceFilter(value);
        } else if (type.equals(ServiceFilter.TYPE)) {
            filter = new ServiceFilter(WebSecurityUtils.safeParseInt(value), servletContext);
        } else if (type.equals(OutageIdFilter.TYPE)) {
            filter = new OutageIdFilter(WebSecurityUtils.safeParseInt(value));
        } else if (type.equals(NegativeForeignSourceFilter.TYPE)) {
            filter = new NegativeForeignSourceFilter(value);
        } else if (type.equals(NegativeNodeFilter.TYPE)) {
            filter = new NegativeNodeFilter(WebSecurityUtils.safeParseInt(value), servletContext);
        } else if (type.equals(NegativeInterfaceFilter.TYPE)) {
            filter = new NegativeInterfaceFilter(value);
        } else if (type.equals(NegativeServiceFilter.TYPE)) {
            filter = new NegativeServiceFilter(WebSecurityUtils.safeParseInt(value), servletContext);
        } else if (type.equals(LostServiceDateBeforeFilter.TYPE)) {
            filter = new LostServiceDateBeforeFilter(WebSecurityUtils.safeParseLong(value));
        } else if (type.equals(LostServiceDateAfterFilter.TYPE)) {
            filter = new LostServiceDateAfterFilter(WebSecurityUtils.safeParseLong(value));
        } else if (type.equals(RegainedServiceDateBeforeFilter.TYPE)) {
            filter = new RegainedServiceDateBeforeFilter(WebSecurityUtils.safeParseLong(value));
        } else if (type.equals(RegainedServiceDateAfterFilter.TYPE)) {
            filter = new RegainedServiceDateAfterFilter(WebSecurityUtils.safeParseLong(value));
        } else if (type.startsWith(AssetFilter.TYPE)) {
            filter = new AssetFilter(type, value);
        } else if (type.startsWith(LocationFilter.TYPE)) {
            filter = new LocationFilter(value);
        } else if (type.startsWith(NegativeLocationFilter.TYPE)) {
            filter = new NegativeLocationFilter(value);
        }

        return filter;
    }

    /**
     * <p>getFilterString</p>
     *
     * @param filter a org$opennms$web$filter$Filter object.
     * @return a {@link java.lang.String} object.
     */
    public static String getFilterString(Filter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return filter.getDescription();
    }

    /**
     * Returns the color to use for an outage, if no color then it returns null.
     *
     * @param outage a {@link org.opennms.web.outage.Outage} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getStatusColor(Outage outage) {
        if (outage == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String color = null;

        if (outage.getRegainedServiceTime() == null) {
            color = DOWN_COLOR;
        }

        return color;
    }

    /**
     * Returns the icon to use for an outage, if no icon then it returns null.
     *
     * @param outage a {@link org.opennms.web.outage.Outage} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getStatusLabel(Outage outage) {
        if (outage == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String label = null;

        if (outage.getRegainedServiceTime() == null) {
            label = "DOWN";
        }

        return label;
    }

    /** Constant <code>LINK_IGNORES="new String[] { sortby, outtype, limit, "{trunked}</code> */
    protected static final String[] LINK_IGNORES = new String[] { "sortby", "outtype", "limit", "multiple", "filter" };

    /**
     * <p>makeLink</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param sortStyle a {@link org.opennms.web.outage.SortStyle} object.
     * @param outageType a {@link org.opennms.web.outage.OutageType} object.
     * @param filters a {@link java.util.List} object.
     * @param limit a int.
     * @return a {@link java.lang.String} object.
     */
    public static String makeLink(HttpServletRequest request, SortStyle sortStyle, OutageType outageType, List<Filter> filters, int limit) {
        if (request == null || sortStyle == null || outageType == null || filters == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        if (limit < 1) {
            throw new IllegalArgumentException("Cannot take a zero or negative limit value.");
        }

        Map<String, Object> additions = new HashMap<String, Object>();
        additions.put("sortby", sortStyle.getShortName());
        additions.put("outtype", outageType.getShortName());
        additions.put("limit", Integer.toString(limit));

        if (filters != null) {
            String[] filterStrings = new String[filters.size()];

            for (int i = 0; i < filters.size(); i++) {
                filterStrings[i] = OutageUtil.getFilterString(filters.get(i));
            }

            additions.put("filter", filterStrings);
        }

        return FILTER_SERVLET_URL_BASE + "?" + Util.makeQueryString(request, additions, LINK_IGNORES, Util.IgnoreType.REQUEST_ONLY);
    }

    /**
     * <p>makeLink</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param parms a {@link org.opennms.web.outage.OutageQueryParms} object.
     * @return a {@link java.lang.String} object.
     */
    public static String makeLink(HttpServletRequest request, OutageQueryParms parms) {
        if (request == null || parms == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return makeLink(request, parms.sortStyle, parms.outageType, parms.filters, parms.limit);
    }

    /**
     * <p>makeLink</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param parms a {@link org.opennms.web.outage.OutageQueryParms} object.
     * @param sortStyle a {@link org.opennms.web.outage.SortStyle} object.
     * @return a {@link java.lang.String} object.
     */
    public static String makeLink(HttpServletRequest request, OutageQueryParms parms, SortStyle sortStyle) {
        if (request == null || parms == null || sortStyle == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return makeLink(request, sortStyle, parms.outageType, parms.filters, parms.limit);
    }

    /**
     * <p>makeLink</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param parms a {@link org.opennms.web.outage.OutageQueryParms} object.
     * @param outageType a {@link org.opennms.web.outage.OutageType} object.
     * @return a {@link java.lang.String} object.
     */
    public static String makeLink(HttpServletRequest request, OutageQueryParms parms, OutageType outageType) {
        if (request == null || parms == null || outageType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return makeLink(request, parms.sortStyle, outageType, parms.filters, parms.limit);
    }

    /**
     * <p>makeLink</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param parms a {@link org.opennms.web.outage.OutageQueryParms} object.
     * @param filters a {@link java.util.List} object.
     * @return a {@link java.lang.String} object.
     */
    public static String makeLink(HttpServletRequest request, OutageQueryParms parms, List<Filter> filters) {
        if (request == null || parms == null || filters == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return makeLink(request, parms.sortStyle, parms.outageType, filters, parms.limit);
    }

    /**
     * <p>makeLink</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param parms a {@link org.opennms.web.outage.OutageQueryParms} object.
     * @param filter a org$opennms$web$filter$Filter object.
     * @param add a boolean.
     * @return a {@link java.lang.String} object.
     */
    public static String makeLink(HttpServletRequest request, OutageQueryParms parms, Filter filter, boolean add) {
        if (request == null || parms == null || filter == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        List<Filter> newList = new ArrayList<Filter>(parms.filters);

        if (add) {
            newList.add(filter);
        } else {
            newList.remove(filter);
        }

        return makeLink(request, parms.sortStyle, parms.outageType, newList, parms.limit);
    }

    /**
     * <p>makeHiddenTags</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param sortStyle a {@link org.opennms.web.outage.SortStyle} object.
     * @param outageType a {@link org.opennms.web.outage.OutageType} object.
     * @param filters a {@link java.util.List} object.
     * @param limit a int.
     * @return a {@link java.lang.String} object.
     */
    public static String makeHiddenTags(HttpServletRequest request, SortStyle sortStyle, OutageType outageType, List<Filter> filters, int limit) {
        if (request == null || sortStyle == null || outageType == null || filters == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        if (limit < 1) {
            throw new IllegalArgumentException("Cannot take a zero or negative limit value.");
        }

        Map<String, Object> additions = new HashMap<String, Object>();
        additions.put("sortby", sortStyle.getShortName());
        additions.put("outtype", outageType.getShortName());
        additions.put("limit", Integer.toString(limit));

        if (filters != null) {
            String[] filterStrings = new String[filters.size()];

            for (int i = 0; i < filters.size(); i++) {
                filterStrings[i] = OutageUtil.getFilterString(filters.get(i));
            }

            additions.put("filter", filterStrings);
        }

        return Util.makeHiddenTags(request, additions, LINK_IGNORES, Util.IgnoreType.REQUEST_ONLY);
    }

    /**
     * <p>makeHiddenTags</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param parms a {@link org.opennms.web.outage.OutageQueryParms} object.
     * @return a {@link java.lang.String} object.
     */
    public static String makeHiddenTags(HttpServletRequest request, OutageQueryParms parms) {
        if (request == null || parms == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return makeHiddenTags(request, parms.sortStyle, parms.outageType, parms.filters, parms.limit);
    }

    /**
     * <p>makeHiddenTags</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param parms a {@link org.opennms.web.outage.OutageQueryParms} object.
     * @param sortStyle a {@link org.opennms.web.outage.SortStyle} object.
     * @return a {@link java.lang.String} object.
     */
    public static String makeHiddenTags(HttpServletRequest request, OutageQueryParms parms, SortStyle sortStyle) {
        if (request == null || parms == null || sortStyle == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return makeHiddenTags(request, sortStyle, parms.outageType, parms.filters, parms.limit);
    }

    /**
     * <p>makeHiddenTags</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param parms a {@link org.opennms.web.outage.OutageQueryParms} object.
     * @param outageType a {@link org.opennms.web.outage.OutageType} object.
     * @return a {@link java.lang.String} object.
     */
    public static String makeHiddenTags(HttpServletRequest request, OutageQueryParms parms, OutageType outageType) {
        if (request == null || parms == null || outageType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return makeHiddenTags(request, parms.sortStyle, outageType, parms.filters, parms.limit);
    }

    /**
     * <p>makeHiddenTags</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param parms a {@link org.opennms.web.outage.OutageQueryParms} object.
     * @param filters a {@link java.util.List} object.
     * @return a {@link java.lang.String} object.
     */
    public static String makeHiddenTags(HttpServletRequest request, OutageQueryParms parms, List<Filter> filters) {
        if (request == null || parms == null || filters == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return makeHiddenTags(request, parms.sortStyle, parms.outageType, filters, parms.limit);
    }

    /**
     * <p>makeHiddenTags</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param parms a {@link org.opennms.web.outage.OutageQueryParms} object.
     * @param filter a org$opennms$web$filter$Filter object.
     * @param add a boolean.
     * @return a {@link java.lang.String} object.
     */
    public static String makeHiddenTags(HttpServletRequest request, OutageQueryParms parms, Filter filter, boolean add) {
        if (request == null || parms == null || filter == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        List<Filter> newList = new ArrayList<Filter>(parms.filters);

        if (add) {
            newList.add(filter);
        } else {
            newList.remove(filter);
        }

        return makeHiddenTags(request, parms.sortStyle, parms.outageType, newList, parms.limit);
    }

}
