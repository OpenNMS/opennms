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
package org.opennms.web.svclayer;

import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.web.svclayer.model.RtcNodeModel;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>RtcService interface.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
@Transactional
public interface RtcService {
    /**
     * <p>getNodeList</p>
     *
     * @return a {@link org.opennms.web.svclayer.model.RtcNodeModel} object.
     */
    public RtcNodeModel getNodeList();
    /**
     * <p>getNodeListForCriteria</p>
     *
     * @param serviceCriteria a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     * @param outageCriteria a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     * @return a {@link org.opennms.web.svclayer.model.RtcNodeModel} object.
     */
    public RtcNodeModel getNodeListForCriteria(OnmsCriteria serviceCriteria, OnmsCriteria outageCriteria);
    /**
     * <p>createServiceCriteria</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     */
    public OnmsCriteria createServiceCriteria();
    /**
     * <p>createOutageCriteria</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     */
    public OnmsCriteria createOutageCriteria();
}
