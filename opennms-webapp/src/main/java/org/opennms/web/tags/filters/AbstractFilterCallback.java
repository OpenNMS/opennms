/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.tags.filters;


import org.opennms.netmgt.model.OnmsFilterFavorite;
import org.opennms.web.filter.Filter;
import org.opennms.web.filter.FilterUtil;
import org.opennms.web.filter.QueryParameters;

import javax.servlet.ServletContext;
import java.net.URLDecoder;
import java.util.List;

import static org.opennms.web.filter.FilterUtil.getFilterParameters;

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
        String[] filterParameter = getFilterParameters(filterString);
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
