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
import org.opennms.netmgt.model.SnmpCollectionSystemDefDto;

import java.util.List;

/**
 * Documentation-only: describes the paged body the matching /datacollectionconf filter
 * handler builds from an ad-hoc Map. Nothing returns this type.
 */
@Schema(description = "One page of system definitions belonging to a source.")
public class DataCollectionSystemDefPageResponse {

    @Schema(description = "Total number of matching rows, ignoring offset and limit.", example = "1")
    private Integer totalRecords;

    @ArraySchema(schema = @Schema(implementation = SnmpCollectionSystemDefDto.class),
            arraySchema = @Schema(description = "The system definitions on this page."))
    private List<SnmpCollectionSystemDefDto> dataCollectionSystemDefsList;

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }

    public List<SnmpCollectionSystemDefDto> getDataCollectionSystemDefsList() {
        return dataCollectionSystemDefsList;
    }

    public void setDataCollectionSystemDefsList(List<SnmpCollectionSystemDefDto> dataCollectionSystemDefsList) {
        this.dataCollectionSystemDefsList = dataCollectionSystemDefsList;
    }
}
