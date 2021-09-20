/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.services;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.dao.api.FilterFavoriteDao;
import org.opennms.netmgt.model.OnmsFilterFavorite;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service to handle CRUD operations and such on {@link OnmsFilterFavorite} objects.
 */
public class FilterFavoriteService {

    public static class FilterFavoriteException extends Exception {
        public FilterFavoriteException(String msg) {
            super(msg);
        }
    }

    @Autowired
    private FilterFavoriteDao favoriteDao;

    /**
     * Returns a favorite only if the given user is allowed to see that favorite and if the given filterString matches with the stored filter criteria inside the favorite.
     * If the favorite does not exist for the user or the filter criteria does not match null is return.
     *
     * @param favoriteId   The id of the favorite.
     * @param username     The username which tries to load the favorite.
     * @param filterString The expected filter criteria.
     * @return The requested favorite or null.
     */
    public OnmsFilterFavorite getFavorite(String favoriteId, String username, String filterString) {
        OnmsFilterFavorite favorite = getFavorite(favoriteId, username);
        if (favorite == null) return null;
        if (favorite.getFilter().equals(unescapeAndDecode(filterString))) {
            return favorite;
        }
        return null;
    }

    /**
     * Deletes the given favorite, but only if the given username is allowed to delete.
     *
     * @param favoriteId
     * @param username
     * @return true if the favorite was deleted, otherwise false.
     */
    public boolean deleteFavorite(String favoriteId, String username) {
        OnmsFilterFavorite favorite = getFavorite(favoriteId, username);
        return deleteFavorite(favorite);
    }

    /**
     * Returns the requested favorite if the favorite exists, the favoriteId is a valid Integer and if the given username is allowed to load the favorite.
     *
     * @param favoriteId The id of the favorite.
     * @param userName   The user which tries to load the favorite.
     * @return The favorite or null if the favoriteId is not an Integer,
     *         the favorite does not exist or the given username is not allowed to see the requested favorite.
     */
    public OnmsFilterFavorite getFavorite(String favoriteId, String userName) {
        try {
            Integer filterIdInteger = Integer.valueOf(favoriteId);
            if (filterIdInteger == null) return null;
            return getFavorite(filterIdInteger, userName);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Returns the requested favorite if the favorite exists and if the given username is allowed to load the favorite.
     *
     * @param favoriteId The id of the favorite.
     * @param userName   The user which tries to load the favorite.
     * @return The favorite or null if the favorite does not exist or the given username is not allowed to see the requested favorite.
     */
    public OnmsFilterFavorite getFavorite(Integer favoriteId, String userName) {
        OnmsFilterFavorite favorite = favoriteDao.get(favoriteId);
        if (favorite != null && favorite.getUsername().equalsIgnoreCase(userName)) {
            sanitizeFavorite(favorite);
            return favorite;
        }
        return null; // not visible for this user
    }

    /**
     * Deletes the favorite. The deletion is only performed if the favorite exists and the given user is allowed to delete.
     * @param favoriteId
     * @param userName
     * @return
     */
    public boolean deleteFavorite(int favoriteId, String userName) {
        OnmsFilterFavorite favorite = getFavorite(favoriteId, userName);
        return deleteFavorite(favorite);
    }

    /**
     * Loads all favorites for the user and the given page.
     * @param userName
     * @param page
     * @return
     */
    public List<OnmsFilterFavorite> getFavorites(String userName, OnmsFilterFavorite.Page page) {
        List<OnmsFilterFavorite> favorites = favoriteDao.findBy(userName, page);
        favorites.forEach(this::sanitizeFavorite);
        return favorites;
    }

    public OnmsFilterFavorite createFavorite(String userName, String favoriteName, String filterString, OnmsFilterFavorite.Page page) throws FilterFavoriteException {
        // Validate input
        validate(userName, favoriteName, filterString, page);

        // Create filter
        OnmsFilterFavorite filter = new OnmsFilterFavorite();
        filter.setUsername(userName);
        filter.setFilter(filterString);
        filter.setName(favoriteName);
        filter.setPage(page);

        sanitizeFavorite(filter);

        favoriteDao.save(filter);
        return filter;
    }

    protected void setFilterFavoriteDao(FilterFavoriteDao favoriteDao) {
        this.favoriteDao = favoriteDao;
    }
    
    protected FilterFavoriteDao getFilterFavoriteDao() {
		return favoriteDao;
	}

    private void validate(String userName, String favoriteName, String filter, OnmsFilterFavorite.Page page) throws FilterFavoriteException {
        if (StringUtils.isEmpty(userName)) throw new FilterFavoriteException("No username specified.");
        if (StringUtils.isEmpty(favoriteName)) throw new FilterFavoriteException("No favorite name specified.");
        if (StringUtils.isEmpty(filter)) throw new FilterFavoriteException("The specified favorite is empty.");
        if (favoriteDao.existsFilter(userName, favoriteName, page)) {
            throw new FilterFavoriteException("A favorite with this name already exists.");
        }
    }

    private void sanitizeFavorite(OnmsFilterFavorite favorite) {
        // Input string is URL-Encoded and some html-entities are already escaped.
        // Revert this process, to allow WebSecurityUtils to work properly.
        favorite.setName(unescapeAndDecode(favorite.getName()));
        favorite.setFilter(unescapeAndDecode(favorite.getFilter()));

        // Sanitize input
        favorite.setName(WebSecurityUtils.sanitizeString(favorite.getName()));
        favorite.setFilter(WebSecurityUtils.sanitizeString(favorite.getFilter()));
    }

    protected boolean deleteFavorite(OnmsFilterFavorite favorite) {
        if (favorite != null) {
            favoriteDao.delete(favorite);
            return true;
        }
        return false;
    }

    private static String unescapeAndDecode(String input) {
        try {
            return StringEscapeUtils.unescapeHtml(URLDecoder.decode(input, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
