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
package org.opennms.features.config.service.api.callback;

import org.opennms.features.config.service.api.CmJaxbConfigDao;
import org.opennms.features.config.service.api.ConfigUpdateInfo;

import java.util.function.Consumer;

/**
 * It is default update notifier for AbstractCmJaxbConfigDao.
 *
 * @param <E> entity class
 */
public class DefaultCmJaxbConfigDaoUpdateCallback<E> implements Consumer<ConfigUpdateInfo> {
    private CmJaxbConfigDao<E> abstractCmJaxbConfigDao;

    public DefaultCmJaxbConfigDaoUpdateCallback(CmJaxbConfigDao<E> abstractCmJaxbConfigDao) {
        this.abstractCmJaxbConfigDao = abstractCmJaxbConfigDao;
    }

    @Override
    public void accept(ConfigUpdateInfo configUpdateInfo) {
        // trigger to reload, which will replace the entity in lastKnownEntityMap
        abstractCmJaxbConfigDao.loadConfig(configUpdateInfo.getConfigId());
    }
}