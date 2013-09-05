package org.opennms.web.services;

import org.apache.commons.lang.StringUtils;
import org.opennms.netmgt.dao.api.FavoriteFilterDao;
import org.opennms.netmgt.model.OnmsFilter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class FavoriteFilterService {

    public static class FavoriteFilterException extends Exception {
        public FavoriteFilterException(String msg) {
          super(msg);
        }
    }

    @Autowired
    private FavoriteFilterDao filterDao;

    public boolean deleteFilter(String filterId, String username) {
        OnmsFilter filter = getFilter(filterId, username);
        return deleteFilter(filter);
    }

    public OnmsFilter getFilter(String filterId, String userName) {
        try {
            Integer filterIdInteger = Integer.valueOf(filterId);
            if (filterIdInteger == null) return null;
            return getFilter(filterIdInteger, userName);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public OnmsFilter getFilter(Integer filterId, String userName) {
        OnmsFilter filter = filterDao.get(filterId);
        if (filter != null && filter.getUsername().equalsIgnoreCase(userName)) {
            return filter;
        }
        return null; // not visible for this user
    }

    public boolean deleteFilter(int filterId, String userName) {
        OnmsFilter filter = getFilter(filterId, userName);
        return deleteFilter(filter);
    }

    public List<OnmsFilter> getFilters(String userName, OnmsFilter.Page page) {
        List<OnmsFilter> filters = filterDao.findBy(userName, page);
        return filters;
    }

    public OnmsFilter createFilter(String userName, String filterName, String filterString, OnmsFilter.Page page) throws FavoriteFilterException {
        validate(userName, filterName, filterString);
        OnmsFilter filter = new OnmsFilter();
        filter.setUsername(userName);
        filter.setFilter(filterString);
        filter.setName(filterName);
        filter.setPage(page);
        filterDao.save(filter);
        filterDao.flush(); // TODO MVR remove flush
        return filter;
    }

    private void validate(String userName, String filterName, String filter) throws FavoriteFilterException {
        if (StringUtils.isEmpty(userName)) throw new FavoriteFilterException("No username specified.");
        if (StringUtils.isEmpty(filterName))  throw new FavoriteFilterException("No filter name specified.");
        if (StringUtils.isEmpty(filter)) throw new FavoriteFilterException("The specified filter is empty.");
    }

    private boolean deleteFilter(OnmsFilter filter) {
        if (filter != null) {
            filterDao.delete(filter);
            filterDao.flush(); // TODO MVR remove flush
            return true;
        }
        return false;
    }
}
