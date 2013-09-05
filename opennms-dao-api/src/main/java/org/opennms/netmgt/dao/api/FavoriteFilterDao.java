package org.opennms.netmgt.dao.api;

import org.opennms.netmgt.model.OnmsFilter;

import java.util.List;

public interface FavoriteFilterDao extends OnmsDao<OnmsFilter, Integer> {
    OnmsFilter findBy(String userName, String filterName);

    List<OnmsFilter> findBy(String userName, OnmsFilter.Page page);
}
