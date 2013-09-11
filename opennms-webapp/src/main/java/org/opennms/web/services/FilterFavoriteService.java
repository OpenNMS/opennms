package org.opennms.web.services;

import org.apache.commons.lang.StringUtils;
import org.opennms.netmgt.dao.api.FilterFavoriteDao;
import org.opennms.netmgt.model.OnmsFilterFavorite;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

// TODO MVR getFilters in getFavorites umbenennen
public class FilterFavoriteService {

    public static class FavoriteFilterException extends Exception {
        public FavoriteFilterException(String msg) {
          super(msg);
        }
    }

    @Autowired
    private FilterFavoriteDao filterDao;

    public boolean deleteFilter(String filterId, String username) {
        OnmsFilterFavorite filter = getFilter(filterId, username);
        return deleteFilter(filter);
    }

    public OnmsFilterFavorite getFilter(String filterId, String userName) {
        try {
            Integer filterIdInteger = Integer.valueOf(filterId);
            if (filterIdInteger == null) return null;
            return getFilter(filterIdInteger, userName);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public OnmsFilterFavorite getFilter(Integer filterId, String userName) {
        OnmsFilterFavorite filter = filterDao.get(filterId);
        if (filter != null && filter.getUsername().equalsIgnoreCase(userName)) {
            return filter;
        }
        return null; // not visible for this user
    }

    public boolean deleteFilter(int filterId, String userName) {
        OnmsFilterFavorite filter = getFilter(filterId, userName);
        return deleteFilter(filter);
    }

    public List<OnmsFilterFavorite> getFilters(String userName, OnmsFilterFavorite.Page page) {
        List<OnmsFilterFavorite> filters = filterDao.findBy(userName, page);
        return filters;
    }

    public OnmsFilterFavorite createFilter(String userName, String filterName, String filterString, OnmsFilterFavorite.Page page) throws FavoriteFilterException {
        validate(userName, filterName, filterString, page);
        OnmsFilterFavorite filter = new OnmsFilterFavorite();
        filter.setUsername(userName);
        filter.setFilter(filterString);
        filter.setName(filterName);
        filter.setPage(page);
        filterDao.save(filter);
        filterDao.flush(); // TODO MVR remove flush
        return filter;
    }

    private void validate(String userName, String filterName, String filter, OnmsFilterFavorite.Page page) throws FavoriteFilterException {
        if (StringUtils.isEmpty(userName)) throw new FavoriteFilterException("No username specified.");
        if (StringUtils.isEmpty(filterName))  throw new FavoriteFilterException("No favorite name specified.");
        if (StringUtils.isEmpty(filter)) throw new FavoriteFilterException("The specified favorite is empty.");
        if (filterDao.existsFilter(userName, filterName, page)) {
            throw new FavoriteFilterException("A favorite with this name already exists.");
        }
    }

    private boolean deleteFilter(OnmsFilterFavorite filter) {
        if (filter != null) {
            filterDao.delete(filter);
            filterDao.flush(); // TODO MVR remove flush
            return true;
        }
        return false;
    }
}
