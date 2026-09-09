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

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.opennms.netmgt.model.SnmpCollectionResourceTypeDto;

import java.util.List;

/**
 * Documentation-only: describes the paged body the matching /datacollectionconf filter
 * handler builds from an ad-hoc Map. Nothing returns this type.
 */
@Schema(description = "One page of resource types belonging to a source.")
public class DataCollectionResourceTypePageResponse {

    @Schema(description = "Total number of matching rows, ignoring offset and limit.", example = "1")
    private Integer totalRecords;

    @ArraySchema(schema = @Schema(implementation = SnmpCollectionResourceTypeDto.class),
            arraySchema = @Schema(description = "The resource types on this page."))
    private List<SnmpCollectionResourceTypeDto> dataCollectionResourceTypeList;

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }

    public List<SnmpCollectionResourceTypeDto> getDataCollectionResourceTypeList() {
        return dataCollectionResourceTypeList;
    }

    public void setDataCollectionResourceTypeList(List<SnmpCollectionResourceTypeDto> dataCollectionResourceTypeList) {
        this.dataCollectionResourceTypeList = dataCollectionResourceTypeList;
    }
}
