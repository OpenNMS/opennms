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

/**
 * Checks a configuration before the configuration manager stores it, beyond what its schema checks.
 *
 * <p>Implementations are found with {@link java.util.ServiceLoader}: list them in
 * {@code META-INF/services/org.opennms.features.config.service.api.ConfigValidator}. Unlike a
 * {@link EventType#VALIDATE} handler, which only runs once the bean registering it has started, a validator runs on
 * every write, including the imports of the configuration upgrade.</p>
 */
public interface ConfigValidator {

    /**
     * @return the name of the schema whose configurations this validator checks
     */
    String getConfigName();

    /**
     * @param info the name and id of the configuration and the complete configuration about to be stored
     * @throws org.opennms.features.config.exception.ValidationException when the configuration must not be stored
     */
    void validate(ConfigUpdateInfo info);
}
