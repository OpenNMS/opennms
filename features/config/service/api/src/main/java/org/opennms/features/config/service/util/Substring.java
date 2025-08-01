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
package org.opennms.features.config.service.util;

import java.util.Objects;

public class Substring {

    private String value;

    public Substring(String value) {
        this.value = Objects.requireNonNull(value);
    }

    public Substring getBeforeLast(String delimiter) {
        int index = this.value.lastIndexOf(delimiter);
        if (index > -1) {
            this.value = this.value.substring(0, this.value.lastIndexOf(delimiter));
        } else {
            // we keep the current string
        }
        return this;
    }

    public Substring getAfterLast(String delimiter) {
        int index = this.value.lastIndexOf(delimiter);
        if (index > -1) {
            this.value = this.value.substring(this.value.lastIndexOf(delimiter) + delimiter.length());
        } else {
            this.value = "";
        }
        return this;
    }

    public Substring getAfter(String delimiter) {
        int index = this.value.indexOf(delimiter);
        if (index > -1) {
            this.value = this.value.substring(this.value.indexOf(delimiter) + delimiter.length());
        } else {
            this.value = "";
        }
        return this;
    }

    public String toString() {
        return this.value;
    }
}
