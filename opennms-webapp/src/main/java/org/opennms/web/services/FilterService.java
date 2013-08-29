package org.opennms.web.services;

import org.opennms.netmgt.dao.api.FilterDao;
import org.opennms.netmgt.model.OnmsFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class FilterService {

    @Autowired
    FilterDao filterDao;

//    public void saveFilter(String userName, String filterName, String filterValue) {
//
//
//    }
//
//    public void deleteFilter(int filterId, String userName) {
//        OnmsFilter filter = m_filterDao.findBy(filterId);
//        if (filter != null && filter.getUsername().equalsIgnoreCase(userName)) {
//            m_filterDao.delete(filter);
//        }
//        // error not there or not allowed
//    }
//
//    public OnmsFilter getFilter(String userName, String filterName) {
//        OnmsFilter filter = m_filterDao.findBy(userName, filterName);
//        return filter;
//    }

    @Transactional
    public List<OnmsFilter> getFilters(String userName, OnmsFilter.Page page) {
        List<OnmsFilter> filters = filterDao.findBy(userName, page);
        return filters;
    }

    @Transactional
    public OnmsFilter createFilter(String userName, String filterName, String filterString, OnmsFilter.Page page) {
        OnmsFilter filter = new OnmsFilter();
        filter.setUsername(userName);
        filter.setFilter(filterString);
        filter.setName(filterName);
        filter.setPage(page);
        filterDao.save(filter);
//        filterDao.flush();
        return filter;
    }

//    public void setFilterDao(FilterDao filterDao) {
//        this.filterDao = filterDao;
//    }
}
