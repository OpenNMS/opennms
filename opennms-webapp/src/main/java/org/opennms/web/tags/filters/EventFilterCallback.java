package org.opennms.web.tags.filters;

import org.opennms.netmgt.model.OnmsFilterFavorite;
import org.opennms.web.event.EventUtil;
import org.opennms.web.filter.Filter;
import org.opennms.web.filter.FilterUtil;
import org.opennms.web.filter.QueryParameters;

import javax.servlet.ServletContext;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class EventFilterCallback implements FilterCallback {

    private final ServletContext servletContext;

    public EventFilterCallback(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public String getFiltersString(List<Filter> filters) {
        StringBuffer buffer = new StringBuffer();
        if( filters != null ) {
            List<String> filterStrings = new ArrayList<String>();
            for (Filter eachFilter : filters) {
                filterStrings.add(EventUtil.getFilterString(eachFilter));
            }
            buffer.append("&amp;").append(FilterUtil.toFilterURL(filterStrings));
        }
        return( buffer.toString() );
    }

    @Override
    public List<Filter> parse(String filterString) {
        String[] filterParameter = filterString.split("&amp;");
        for (int i=0; i< filterParameter.length; i++) {
            if (filterParameter[i].startsWith("filter=")) {
                filterParameter[i] = filterParameter[i].replaceFirst("filter=", "");
            }
            filterParameter[i] = URLDecoder.decode(filterParameter[i]);
        }
        return EventUtil.getFilterList(filterParameter, servletContext);
    }

    @Override
    public String createLink(String urlBase, QueryParameters parameters, OnmsFilterFavorite favorite) {
        StringBuffer buffer = new StringBuffer(urlBase);
        buffer.append("?sortby=");
        buffer.append(parameters.getSortStyleShortName());
        buffer.append("&amp;acktype=");
        buffer.append(parameters.getAckType().getShortName());
        if (parameters.getLimit() > 0) {
            buffer.append("&amp;limit=").append(parameters.getLimit());
        }
        buffer.append(getFiltersString(parameters.getFilters()));
        if (favorite != null) {
            buffer.append("&favoriteId=" + favorite.getId());
        }
        return (buffer.toString());
    }

}
