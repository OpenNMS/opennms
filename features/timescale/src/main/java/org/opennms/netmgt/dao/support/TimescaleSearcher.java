/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.timescale.support.TimescaleUtils;
import org.opennms.newts.api.Context;
import org.opennms.newts.api.Resource;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class TimescaleSearcher {


    private DataSource dataSource;
    private Connection connection;

    private LoadingCache<String, Set<ResourcePath>> allResources = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<String, Set<ResourcePath>>() {
                        public Set<ResourcePath> load(String key) throws SQLException {
                            return getAllResources();
                        }
                    });

    @Autowired
    public TimescaleSearcher(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Set<ResourcePath> getAllResources() throws SQLException {
        // TODO Patrick: make db stuff proper. cache?
        String sql = "select distinct resource from timeseries";
        if(connection == null) {
            this.connection = this.dataSource.getConnection();

        }
        PreparedStatement statement = connection.prepareStatement(sql);

        ResultSet rs = statement.executeQuery();
        Set<ResourcePath> resources = new HashSet<>();
        while(rs.next()){
            String resourceString = rs.getString("resource");
            ResourcePath resource = new ResourcePath(TimescaleUtils.toResourcePath(resourceString), TimescaleUtils.toMetricName(resourceString));
            addIncludingParent(resources, resource);
        }
        rs.close();
        // resources.sort(Comparator.naturalOrder());
        return  resources;
    }
    public void addIncludingParent(Set<ResourcePath> allPaths, ResourcePath newPath) {
        ResourcePath currentPath = newPath;
        allPaths.add(currentPath);
//        while(currentPath.hasParent()) {
//            currentPath = currentPath.getParent();
//            allPaths.add(currentPath);
//        }
    }

    public Map<String, String> getResourceAttributes(Context m_context, String toResourceId) {
        // TODO: Patrick: where do we save the resource attributes?
        Map<String, String> map = new HashMap<>();
        map.put("fakeAttribute1", "fakeValue1");
        map.put("fakeAttribute2", "fakeValue2");
        return map;
    }

    public SearchResults search(ResourcePath path, int depth, boolean fetchMetrics) throws SQLException {

        Set<ResourcePath> all = allResources.getUnchecked("doesntmatter");
        List<ResourcePath> relevantResources = all.stream()
                .filter(p -> path.relativeDepth(p)>-1)
                .filter(p -> path.relativeDepth(p) == depth +1 )
                .collect(Collectors.toList());
        SearchResults results = new SearchResults();
        for(ResourcePath relevantPath : relevantResources) {
            Resource resource = new Resource(TimescaleUtils.toResourceId(relevantPath),
                    Optional.of(getResourceAttributes(null, relevantPath.toString())));
            results.addResult(resource, Arrays.asList("metrics 1", "metrics 2")); // TODO Patrick get real values
        }
        return results;
    }
}
