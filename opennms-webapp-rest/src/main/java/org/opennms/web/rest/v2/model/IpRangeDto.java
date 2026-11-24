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

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "An inclusive range of IP addresses.")
public class IpRangeDto {

    @Schema(description = "First address of the range.", example = "192.168.0.1", required = true)
    private String begin;

    @Schema(description = "Last address of the range.", example = "192.168.0.254", required = true)
    private String end;

    public String getBegin() {
        return begin;
    }

    public void setBegin(final String begin) {
        this.begin = begin;
    }
    public String getEnd() {
        return end;
    }

    public void setEnd(final String end) {
        this.end = end;
    }
}
