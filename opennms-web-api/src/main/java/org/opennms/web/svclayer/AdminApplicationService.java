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
/**
 * <p>AdminApplicationService interface.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
package org.opennms.web.svclayer;

import java.util.List;

import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.web.svclayer.support.DefaultAdminApplicationService.ApplicationAndMemberServices;
import org.opennms.web.svclayer.support.DefaultAdminApplicationService.EditModel;
import org.opennms.web.svclayer.support.DefaultAdminApplicationService.ServiceEditModel;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface AdminApplicationService {
    /**
     * <p>getApplication</p>
     *
     * @param applicationIdString a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.svclayer.support.DefaultAdminApplicationService.ApplicationAndMemberServices} object.
     */
    public ApplicationAndMemberServices getApplication(String applicationIdString);

    /**
     * <p>findAllMonitoredServices</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<OnmsMonitoredService> findAllMonitoredServices();

    public List<OnmsMonitoringLocation> findAllMonitoringLocations();

    /**
     * <p>findApplicationAndAllMonitoredServices</p>
     *
     * @param applicationIdString a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.svclayer.support.DefaultAdminApplicationService.EditModel} object.
     */
    public EditModel findApplicationAndAllMonitoredServices(String applicationIdString);

    @Transactional(readOnly = false)
    public void performEditServices(String appId, String editAction, String[] serviceAdds, String[] serviceDeletes);

    @Transactional(readOnly = false)
    public void performEditLocations(String appId, String editAction, String[] locationAdds, String[] locationDeletes);

    /**
     * <p>addNewApplication</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsApplication} object.
     */
    @Transactional(readOnly = false)
    public OnmsApplication addNewApplication(String name);

    /**
     * <p>findAllApplications</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<OnmsApplication> findAllApplications();

    /**
     * <p>removeApplication</p>
     *
     * @param applicationIdString a {@link java.lang.String} object.
     */
    @Transactional(readOnly = false)
    public void removeApplication(String applicationIdString);

    /**
     * <p>findByMonitoredService</p>
     *
     * @param id a int.
     * @return a {@link java.util.List} object.
     */
    public List<OnmsApplication> findByMonitoredService(int id);

    /**
     * <p>performServiceEdit</p>
     *
     * @param ifServiceIdString a {@link java.lang.String} object.
     * @param editAction a {@link java.lang.String} object.
     * @param toAdd an array of {@link java.lang.String} objects.
     * @param toDelete an array of {@link java.lang.String} objects.
     */
    @Transactional(readOnly = false)
    public void performServiceEdit(String ifServiceIdString, String editAction, String[] toAdd, String[] toDelete);

    /**
     * <p>findServiceApplications</p>
     *
     * @param ifServiceIdString a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.svclayer.support.DefaultAdminApplicationService.ServiceEditModel} object.
     */
    public ServiceEditModel findServiceApplications(String ifServiceIdString);

}
