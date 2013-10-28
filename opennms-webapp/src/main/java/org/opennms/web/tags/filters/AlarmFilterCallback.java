package org.opennms.web.tags.filters;

import org.opennms.netmgt.model.OnmsFilterFavorite;
import org.opennms.web.alarm.AlarmUtil;
import org.opennms.web.filter.Filter;
import org.opennms.web.filter.QueryParameters;

import javax.servlet.ServletContext;
import java.util.List;

public class AlarmFilterCallback extends AbstractFilterCallback {

    public AlarmFilterCallback(ServletContext servletContext) {
        super(servletContext);
    }

    @Override
    protected String getIndividualFilterString(Filter filter) {
        return AlarmUtil.getFilterString(filter);
    }

    @Override
    protected List<Filter> getIndividualFilterList(String[] filters, ServletContext servletContext) {
        return AlarmUtil.getFilterList(filters, servletContext);
    }
}

