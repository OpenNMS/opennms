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
package org.opennms.distributed.core.impl;

import java.util.Objects;

import org.opennms.distributed.core.api.Identity;
import org.opennms.distributed.core.api.SystemType;

public class IdentityImpl implements Identity {

    private final String id;
    private final String location;
    private final String type;

    public IdentityImpl(String id, String location, String type) {
        this.id = Objects.requireNonNull(id);
        this.location = Objects.requireNonNull(location);
        this.type = Objects.requireNonNull(type);
    }

    public IdentityImpl(String id, String location, SystemType type) {
        this(id, location, Objects.requireNonNull(type).name());
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public String getType() {
        return type;
    }
}
