package org.opennms.web.services;

import java.util.List;

import org.opennms.netmgt.dao.api.FilterDao;
import org.opennms.netmgt.model.OnmsFilter;
import org.springframework.beans.factory.annotation.Autowired;

public class FilterService {

    @Autowired
    private FilterDao filterDao;

    public boolean deleteFilter(String filterId, String username) {
    	try {
    		Integer filterIdInteger = Integer.valueOf(filterId);
    		if (filterIdInteger == null) return false;
    		return deleteFilter(filterIdInteger.intValue(), username);
    	} catch (NumberFormatException nfe) {
    		return false;
    	}
    }
    
    public boolean deleteFilter(int filterId, String userName) {
        OnmsFilter filter = filterDao.get(filterId);
        if (filter != null && filter.getUsername().equalsIgnoreCase(userName)) {
            filterDao.delete(filter);
            filterDao.flush(); // TODO MVR remove flush
            return true;
        }
        return false; // error not there or not allowed
    }

    public List<OnmsFilter> getFilters(String userName, OnmsFilter.Page page) {
        List<OnmsFilter> filters = filterDao.findBy(userName, page);
        return filters;
    }

    public OnmsFilter createFilter(String userName, String filterName, String filterString, OnmsFilter.Page page) {
        OnmsFilter filter = new OnmsFilter();
        filter.setUsername(userName);
        filter.setFilter(filterString);
        filter.setName(filterName);
        filter.setPage(page);
        filterDao.save(filter);
        filterDao.flush(); // TODO MVR remove flush
        return filter;
    }
}
