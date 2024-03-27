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
package org.opennms.netmgt.config.dao.thresholding.api;

import java.util.Optional;

import org.opennms.netmgt.config.dao.common.api.ReadableDao;
import org.opennms.netmgt.config.threshd.Package;
import org.opennms.netmgt.config.threshd.Service;
import org.opennms.netmgt.config.threshd.ServiceStatus;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * Thresholding Daemon from the threshd-configuration xml file.
 *
 * A mapping of the configured URLs to the iplist they contain is built at
 * init() time so as to avoid numerous file reads.
 */
public interface ReadableThreshdDao extends ReadableDao<ThreshdConfiguration> {
    /**
     * This method is used to rebuild the package against iplist mapping when
     * needed. When a node gained service event occurs, threshd has to determine
     * which package the ip/service combination is in, but if the interface is a
     * newly added one, the package iplist should be rebuilt so that threshd
     * could know which package this ip/service pair is in.
     */
    void rebuildPackageIpListMap();

    /**
     * This method is used to determine if the named interface is included in
     * the passed package definition. If the interface belongs to the package
     * then a value of true is returned. If the interface does not belong to the
     * package a false value is returned.
     *
     * <strong>Note: </strong>Evaluation of the interface against a package
     * filter will only work if the IP is already in the database.
     *
     * @param iface The interface to test against the package.
     * @param pkg   The package to check for the inclusion of the interface.
     * @return True if the interface is included in the package, false
     * otherwise.
     */
    boolean interfaceInPackage(String iface, org.opennms.netmgt.config.threshd.Package pkg);

    /**
     * Returns true if the service is part of the package and the status of the
     * service is set to "on". Returns false if the service is not in the
     * package or it is but the status of the service is set to "off".
     *
     * @param svcName
     *            The service name to lookup.
     * @param pkg
     *            The package to lookup up service.
     * @return a boolean.
     */
    static boolean serviceInPackageAndEnabled(String svcName, Package pkg) {
        boolean result = false;

        for (Service tsvc : pkg.getServices()) {
            if (tsvc.getName().equalsIgnoreCase(svcName)) {
                // Ok its in the package. Now check the
                // status of the service
                final ServiceStatus status = tsvc.getStatus().orElse(ServiceStatus.OFF);
                if (status == ServiceStatus.ON) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
}
