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
package org.opennms.features.apilayer.common;

import java.util.Objects;

import org.opennms.distributed.core.api.Identity;
import org.opennms.integration.api.v1.runtime.RuntimeInfo;
import org.opennms.integration.api.v1.runtime.Version;
import org.osgi.framework.FrameworkUtil;

public abstract class AbstractRuntimeInfo implements RuntimeInfo {

    private final Version version;
    private final Identity identity;

    public AbstractRuntimeInfo(Identity identity) {
        version = new VersionBean(FrameworkUtil.getBundle(getClass()).getVersion().toString());
        this.identity = Objects.requireNonNull(identity);
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public boolean isMeridian() {
        return version.getMajor() >= 2015;
    }

    @Override
    public String getSystemId() {
        return identity.getId();
    }

    @Override
    public String getSystemLocation() {
        return identity.getLocation();
    }
}
