package org.opennms.web.tags.filters;

import org.opennms.netmgt.model.OnmsFilterFavorite;
import org.opennms.web.event.EventUtil;
import org.opennms.web.filter.Filter;
import org.opennms.web.filter.FilterUtil;
import org.opennms.web.filter.QueryParameters;

import javax.servlet.ServletContext;
import java.net.URLDecoder;
import java.util.List;

public class EventFilterCallback implements FilterCallback {

    private final ServletContext servletContext;

    public EventFilterCallback(ServletContext servletContext) {
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
                filterStrings[i] = EventUtil.getFilterString(filters.get(i));
            }
            return toFilterString(filterStrings);
        }
        return "";
    }

    @Override
    public List<Filter> parse(String filterString) {
        String[] filterParameter = filterString.split("&amp;");
        for (int i=0; i< filterParameter.length; i++) {
            if (filterParameter[i].startsWith("filter=")) {
                filterParameter[i] = filterParameter[i].replaceFirst("filter=", "");
            }
        }
        return parse(filterParameter);

    }

    @Override
    public List<Filter> parse(String[] filters) {
        if (filters != null) {
            for (int i = 0; i < filters.length; i++) {
                filters[i] = URLDecoder.decode(filters[i]);
            }
        }
        return EventUtil.getFilterList(filters, servletContext);
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
        buffer.append("&amp;").append(toFilterString(parameters.getFilters()));
        if (favorite != null) {
            buffer.append("&favoriteId=" + favorite.getId());
        }
        return (buffer.toString());
    }

}
