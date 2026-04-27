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
package org.opennms.features.mibcompiler.rest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MibCompilerFileText {
    private final String name;
    private final String location;
    private final String contents;

    @JsonCreator
    public MibCompilerFileText(
            @JsonProperty("name") String name,
            @JsonProperty("location") String location,
            @JsonProperty("contents") String contents) {
        this.name = name;
        this.location = location;
        this.contents = contents;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getContents() {
        return contents;
    }
}
