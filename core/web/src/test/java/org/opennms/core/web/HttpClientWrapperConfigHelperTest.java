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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.opennms.core.web.HttpClientWrapperConfigHelper.PARAMETER_KEYS.useSystemProxy;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

public class HttpClientWrapperConfigHelperTest {

    @Test
    public void shouldSetProxySettingsCorrectly(){

        HttpClientWrapper client = Mockito.mock(HttpClientWrapper.class);

        // Param not set
        Map<String, Object> params = new HashMap<>();
        HttpClientWrapperConfigHelper.setUseSystemProxyIfDefined(client, params);
        verify(client, times(0)).useSystemProxySettings();

        // Param false
        params.put(useSystemProxy.getKey(), Boolean.FALSE);
        HttpClientWrapperConfigHelper.setUseSystemProxyIfDefined(client, params);
        verify(client, times(0)).useSystemProxySettings();

        // Param true
        params.put(useSystemProxy.getKey(), Boolean.TRUE);
        HttpClientWrapperConfigHelper.setUseSystemProxyIfDefined(client, params);
        verify(client, times(1)).useSystemProxySettings();
    }

}