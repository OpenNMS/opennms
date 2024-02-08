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
package org.opennms.netmgt.collection.api;

import java.util.Collections;
import java.util.Map;

import org.opennms.core.utils.LocationUtils;

/**
 * Boilerplate code for basic {@link ServiceCollector} implementations.
 *
 * If the {@link ServiceCollector} is expected to run on both OpenNMS and Minion,
 * consider extending {@link AbstractRemoteServiceCollector} instead.
 *
 * @author jwhite
 */
public abstract class AbstractServiceCollector implements ServiceCollector {

    @Override
    public void initialize() throws CollectionInitializationException  {
        // pass
    }

    @Override
    public void validateAgent(CollectionAgent agent, Map<String, Object> parameters) throws CollectionInitializationException {
        // pass
    }

    @Override
    public Map<String, Object> getRuntimeAttributes(CollectionAgent agent, Map<String, Object> parameters) {
        return Collections.emptyMap();
    }

    @Override
    public String getEffectiveLocation(String location) {
        // Always run at the default location
        return LocationUtils.DEFAULT_LOCATION_NAME;
    }

    @Override
    public Map<String, String> marshalParameters(Map<String, Object> parameters) {
        // We always run at the default location, so no marshaling should be perfomed
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> unmarshalParameters(Map<String, String> parameters) {
        // We always run at the default location, so no unmarshaling should be perfomed
        throw new UnsupportedOperationException();
    }
}
