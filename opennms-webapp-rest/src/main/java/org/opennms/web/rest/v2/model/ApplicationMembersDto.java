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

/**
 * An application with its member services and the monitoring locations it is
 * polled from as a perspective.
 */
public class ApplicationMembersDto {

    private Integer id;

    private String name;

    private List<ApplicationServiceDto> services;

    private List<String> perspectiveLocations;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<ApplicationServiceDto> getServices() {
        return services;
    }

    public void setServices(final List<ApplicationServiceDto> services) {
        this.services = services;
    }

    public List<String> getPerspectiveLocations() {
        return perspectiveLocations;
    }

    public void setPerspectiveLocations(final List<String> perspectiveLocations) {
        this.perspectiveLocations = perspectiveLocations;
    }
}
