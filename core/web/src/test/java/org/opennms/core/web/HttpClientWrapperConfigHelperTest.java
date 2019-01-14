/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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