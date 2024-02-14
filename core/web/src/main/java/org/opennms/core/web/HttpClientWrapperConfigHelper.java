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
package org.opennms.core.web;

import static org.opennms.core.web.HttpClientWrapperConfigHelper.PARAMETER_KEYS.useSystemProxy;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientWrapperConfigHelper {

    private static final Logger LOG = LoggerFactory.getLogger(HttpClientWrapperConfigHelper.class);

    public enum PARAMETER_KEYS {
        useSystemProxy("use-system-proxy");

        private String key;

        PARAMETER_KEYS(String key) {
            this.key = key;
        }

        String getKey() {
            return key;
        }
    }

    public static void setUseSystemProxyIfDefined(HttpClientWrapper httpClientWrapper, Map<String, Object> keyedParameters) {
        if (getKeyedBoolean(keyedParameters, useSystemProxy.getKey(), false)) {
            httpClientWrapper.useSystemProxySettings();
            LOG.debug("setting useSystemProxySettings() on HttpClientWrapper");
        }
    }

    // TODO: silly to pull in org.opennms.core.lib just for this, refactor org.opennms.core.utils.ParameterMap someday
    private static boolean getKeyedBoolean(final Map<String, Object> map, final String key, final boolean defaultValue) {
        if (map == null) return defaultValue;

        final Object value = map.get(key);
        if (value == null) return defaultValue;

        if (value instanceof String) {
            return Boolean.valueOf((String)value);
        }
        if (value instanceof Boolean) {
            return ((Boolean)value).booleanValue();
        }

        return defaultValue;
    }

}
