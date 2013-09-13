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
    private FilterFavoriteDao favoriteDao;

    // TODO MVR comment gets favorite only if tfilterString matches
    public OnmsFilterFavorite getFavorite(String favoriteId, String username, String filterString) {
        OnmsFilterFavorite favorite = getFavorite(favoriteId, username);
        if (favorite == null) return null;
        if (favorite.getFilter().equals(filterString)) {
            return favorite;
        }
        return null;
    }

    public boolean deleteFavorite(String favoriteId, String username) {
        OnmsFilterFavorite favorite = getFavorite(favoriteId, username);
        return deleteFavorite(favorite);
    }

    public OnmsFilterFavorite getFavorite(String favoriteId, String userName) {
        try {
            Integer filterIdInteger = Integer.valueOf(favoriteId);
            if (filterIdInteger == null) return null;
            return getFavorite(filterIdInteger, userName);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public OnmsFilterFavorite getFavorite(Integer favoriteId, String userName) {
        OnmsFilterFavorite favorite = favoriteDao.get(favoriteId);
        if (favorite != null && favorite.getUsername().equalsIgnoreCase(userName)) {
            return favorite;
        }
        return null; // not visible for this user
    }

    public boolean deleteFilter(int favoriteId, String userName) {
        OnmsFilterFavorite favorite = getFavorite(favoriteId, userName);
        return deleteFavorite(favorite);
    }

    public List<OnmsFilterFavorite> getFavorites(String userName, OnmsFilterFavorite.Page page) {
        List<OnmsFilterFavorite> favorites = favoriteDao.findBy(userName, page);
        return favorites;
    }

    public OnmsFilterFavorite createFavorite(String userName, String favoriteName, String filterString, OnmsFilterFavorite.Page page) throws FavoriteFilterException {
        validate(userName, favoriteName, filterString, page);
        OnmsFilterFavorite filter = new OnmsFilterFavorite();
        filter.setUsername(userName);
        filter.setFilter(filterString);
        filter.setName(favoriteName);
        filter.setPage(page);
        favoriteDao.save(filter);
        favoriteDao.flush(); // TODO MVR remove flush
        return filter;
    }

    private void validate(String userName, String favoriteName, String filter, OnmsFilterFavorite.Page page) throws FavoriteFilterException {
        if (StringUtils.isEmpty(userName)) throw new FavoriteFilterException("No username specified.");
        if (StringUtils.isEmpty(favoriteName))  throw new FavoriteFilterException("No favorite name specified.");
        if (StringUtils.isEmpty(filter)) throw new FavoriteFilterException("The specified favorite is empty.");
        if (favoriteDao.existsFilter(userName, favoriteName, page)) {
            throw new FavoriteFilterException("A favorite with this name already exists.");
        }
    }

    private boolean deleteFavorite(OnmsFilterFavorite favorite) {
        if (favorite != null) {
            favoriteDao.delete(favorite);
            favoriteDao.flush(); // TODO MVR remove flush
            return true;
        }
        return false;
    }
}
