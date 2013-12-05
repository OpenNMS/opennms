package org.opennms.web.tags.filters;

import java.util.List;

import javax.servlet.ServletContext;

import org.opennms.web.alarm.AlarmUtil;
import org.opennms.web.filter.Filter;

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

