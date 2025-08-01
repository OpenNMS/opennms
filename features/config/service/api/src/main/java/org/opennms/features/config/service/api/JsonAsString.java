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
package org.opennms.features.config.service.api;

import com.google.common.base.Objects;

/**
 * Ideally we would use JSONObject instead. BUT: we can't make Osgi and Spring to load only once. We run into
 * classloader problems when calling ConfigurationManagerService from Osgi. This is a workaround to still be type safe.
 */
public class JsonAsString {
    private final String json;

    public JsonAsString(final String json) {
        this.json = java.util.Objects.requireNonNull(json);
    }

    @Override
    public String toString() {
        return json;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonAsString that = (JsonAsString) o;
        return Objects.equal(json, that.json);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(json);
    }
}
