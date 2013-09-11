package org.opennms.netmgt.dao.api;

import org.opennms.netmgt.model.OnmsFilterFavorite;

import java.util.List;

public interface FilterFavoriteDao extends OnmsDao<OnmsFilterFavorite, Integer> {
    OnmsFilterFavorite findBy(String userName, String filterName);

    List<OnmsFilterFavorite> findBy(String userName, OnmsFilterFavorite.Page page);

    boolean existsFilter(String userName, String filterName, OnmsFilterFavorite.Page page);
}
