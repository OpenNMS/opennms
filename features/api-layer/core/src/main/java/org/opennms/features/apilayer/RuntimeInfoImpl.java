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
package org.opennms.features.apilayer;

import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.features.apilayer.common.VersionBean;
import org.opennms.integration.api.v1.runtime.Container;
import org.opennms.integration.api.v1.runtime.RuntimeInfo;
import org.opennms.integration.api.v1.runtime.Version;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.model.OnmsDistPoller;

public class RuntimeInfoImpl implements RuntimeInfo {

    private static final SystemInfoUtils sysInfoUtils = new SystemInfoUtils();

    private final Version version;
    private final String systemId;
    private final String systemLocation;

    public RuntimeInfoImpl(DistPollerDao distPollerDao) {
        version = new VersionBean(sysInfoUtils.getDisplayVersion());

        final OnmsDistPoller distPoller = distPollerDao.whoami();
        systemId = distPoller.getId();
        systemLocation = distPoller.getLocation();
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public boolean isMeridian() {
        return sysInfoUtils.getPackageName().contains("meridian");
    }

    @Override
    public Container getContainer() {
        return Container.OPENNMS;
    }

    @Override
    public String getSystemId() {
        return systemId;
    }

    @Override
    public String getSystemLocation() {
        return systemLocation;
    }
}
