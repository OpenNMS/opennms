package org.opennms.web.tags.filters;

import org.opennms.netmgt.model.OnmsFilterFavorite;
import org.opennms.web.filter.Filter;
import org.opennms.web.filter.QueryParameters;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface FilterCallback {

    String toFilterString(List<Filter> filters);

    String toFilterString(String[] filters);

    List<Filter> parse(String[] filters);

    List<Filter> parse(String filterString);

    String createLink(
            String urlBase,
            QueryParameters parameters,
            OnmsFilterFavorite favorite);
}
