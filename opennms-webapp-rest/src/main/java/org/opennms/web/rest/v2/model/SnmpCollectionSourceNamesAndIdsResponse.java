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
package org.opennms.web.rest.v2.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SnmpCollectionSourceNamesAndIdsResponse {

    private Integer id;
    private String name;

    public SnmpCollectionSourceNamesAndIdsResponse(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Factory method to convert Map<Integer, String> to response objects
     */
    public static List<SnmpCollectionSourceNamesAndIdsResponse> fromMap(
            Map<Integer, String> sourceMap) {

        if (sourceMap == null || sourceMap.isEmpty()) {
            return List.of();
        }

        return sourceMap.entrySet()
                .stream()
                .map(entry ->
                        new SnmpCollectionSourceNamesAndIdsResponse(
                                entry.getKey(),
                                entry.getValue()))
                .collect(Collectors.toList());
    }
}
