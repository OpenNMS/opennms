/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.tags.filters;


import org.opennms.netmgt.model.OnmsFilterFavorite;
import org.opennms.web.filter.Filter;
import org.opennms.web.filter.FilterUtil;
import org.opennms.web.filter.QueryParameters;

import javax.servlet.ServletContext;
import java.net.URLDecoder;
import java.util.List;

public abstract class AbstractFilterCallback implements FilterCallback {
    private final ServletContext servletContext;

    public AbstractFilterCallback(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public String toFilterString(String[] filters) {
        if (filters != null) {
            return FilterUtil.toFilterURL(filters);
        }
        return "";
    }

    @Override
    public String toFilterString(List<Filter> filters) {
        if( filters != null ) {
            String[] filterStrings = new String[filters.size()];
            for (int i=0; i<filterStrings.length; i++) {
                filterStrings[i] = getIndividualFilterString(filters.get(i));
            }
            return toFilterString(filterStrings);
        }
        return "";
    }

    /**
     * This method is used to parse filter favorites that are stored in the database
     * in "filter=foo%3Dbar&amp;filter=foo2%3Dbar2" format
     */
    @Override
    public List<Filter> parse(String filterString) {
        String[] filterParameter = filterString.split("&amp;");
        for (int i=0; i< filterParameter.length; i++) {
            if (filterParameter[i].startsWith("filter=")) {
                filterParameter[i] = filterParameter[i].replaceFirst("filter=", "");
                filterParameter[i] = URLDecoder.decode(filterParameter[i]);
            }
        }
        return parse(filterParameter);
    }

    @Override
    public List<Filter> parse(String[] filters) {
        /*
        We don't need to do this; the values have already been URL-decoded.

        if (filters != null) {
            for (int i = 0; i < filters.length; i++) {
                filters[i] = URLDecoder.decode(filters[i]);
            }
        }
        */
        return getIndividualFilterList(filters, servletContext);
    }



    @Override
    public String createLink(String urlBase, QueryParameters parameters, OnmsFilterFavorite favorite) {
        final StringBuilder buffer = new StringBuilder(urlBase);
        buffer.append("?sortby=");
        buffer.append(parameters.getSortStyleShortName());
        buffer.append("&amp;acktype=");
        buffer.append(parameters.getAckType().getShortName());
        if (parameters.getLimit() > 0) {
            buffer.append("&amp;limit=").append(parameters.getLimit());
        }
        if (parameters.getDisplay() != null) {
            buffer.append("&amp;display=").append(parameters.getDisplay());
        }
        String filters = toFilterString(parameters.getFilters());
        if (filters != null && filters.length() > 0) {
            buffer.append("&amp;").append(filters);
        }
        if (favorite != null) {
            buffer.append("&amp;favoriteId=" + favorite.getId());
        }
        return (buffer.toString());
    }

    protected abstract String getIndividualFilterString(Filter filter);

    protected abstract List<Filter> getIndividualFilterList(String[] filters, ServletContext servletContext);

}
