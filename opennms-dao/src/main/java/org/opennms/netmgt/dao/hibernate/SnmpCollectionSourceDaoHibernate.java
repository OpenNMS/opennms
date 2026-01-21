/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.dao.hibernate;

import org.opennms.netmgt.dao.api.SnmpCollectionSourceDao;
import org.opennms.netmgt.model.SnmpCollectionSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class SnmpCollectionSourceDaoHibernate extends AbstractDaoHibernate<SnmpCollectionSource, Integer> implements SnmpCollectionSourceDao {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpCollectionSourceDaoHibernate.class);

    public SnmpCollectionSourceDaoHibernate() {
        super(SnmpCollectionSource.class);
    }

    @Override
    public SnmpCollectionSource get(Integer id) {
        return super.get(id);
    }

    @Override
    public SnmpCollectionSource findByName(String name) {
        List<SnmpCollectionSource> list = find("from SnmpCollectionSource s where s.name = ?", name);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<SnmpCollectionSource> findAllEnabled() {
        return find("from SnmpCollectionSource s where s.enabled = true");
    }

    @Override
    public void deleteAll(final Collection<SnmpCollectionSource> list) {
        super.deleteAll(list);
    }

    @Override
    public Map<Integer, String> getIdToNameMap() {
        return findObjects(Object[].class,
                "select s.id, s.name from SnmpCollectionSource s").stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0],
                        row -> (String) row[1]
                ));
    }

    @Override
    public Map<String, Object> filterDataCollectionSource(final String filter, final String sortBy,final  String order, Integer totalRecords, Integer offset, Integer limit) {

        int resultCount = (totalRecords != null) ? totalRecords : 0;
        List<SnmpCollectionSource> dataCollectionSourceList = Collections.emptyList();
        try {
            List<Object> queryParams = new ArrayList<>();
            List<String> conditions = new ArrayList<>();

            // Add filter conditions dynamically
            if (filter != null && !filter.trim().isEmpty()) {
                String escapedFilter = "%" + escapeLike(filter.trim().toLowerCase()) + "%";
                conditions.add("lower(s.name) like ? escape '\\'");
                queryParams.add(escapedFilter);

                conditions.add("lower(s.vendor) like ? escape '\\'");
                queryParams.add(escapedFilter);

                conditions.add("lower(s.description) like ? escape '\\'");
                queryParams.add(escapedFilter);


            }

            String whereClause = conditions.isEmpty() ? "" : " where " + String.join(" OR ", conditions);

            // COUNT QUERY: get total matching records if not already provided
            if (resultCount == 0) {
                String countQuery = "select count(s.id) from SnmpCollectionSource s " + whereClause;
                resultCount = super.queryInt(countQuery, queryParams.toArray());
            }

            // DATA QUERY: fetch paginated results
            if (resultCount > 0) {

                String orderBy = "";
                String sortField = sortBy;

                String sortOrder = "ASC".equalsIgnoreCase(order) ? "ASC" : "DESC";

                Set<String> allowedSortFields = Set.of("name", "vendor", "description");

                if (!allowedSortFields.contains(sortBy)) {
                    sortField = "createdTime";
                }

                orderBy = " order by " + sortField + " " + sortOrder;

                String dataQuery = "from SnmpCollectionSource s " + whereClause + orderBy;
                dataCollectionSourceList = findWithPagination(dataQuery, queryParams.toArray(), offset, limit);
            }

        } catch (Exception e ) {
            LOG.debug("Error filterDataCollectionSource method while fetching the records {} ", e);
        }

        // Return map with results
        return Map.of("totalRecords", resultCount, "dataCollectionSourceList", dataCollectionSourceList);
    }

    /**
     * Escapes special characters (%, _, \, /, [, ]) in a string
     * to make it safe for SQL LIKE queries.
     *
     * @param input the input string
     * @return the escaped string
     */
    private String escapeLike(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
                .replace("@", "\\@")
                .replace("/", "\\/")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace(".", "\\.");
    }
}
