/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.configservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.distributed.kvstore.api.BlobStore;
import org.opennms.features.distributed.kvstore.blob.inmemory.InMemoryMapBlobStore;

public class ConfigurationServiceTest {

    private final static String CONFIG_URI = "abc";

    private ConfigurationService service;
    private int callbackInvocations;

    @Before
    public void startUp() {
        callbackInvocations = 0;
        BlobStore store = InMemoryMapBlobStore.withDefaultTicks();
        service = new ConfigurationService(store);
        service.registerForUpdates(CONFIG_URI, this::callback);
    }

    @Test
    public void shouldInformAboutUpdates() {

        assertEquals(0, callbackInvocations);

        service.putConfiguration("some other uri", "");
        assertEquals(0, callbackInvocations);

        service.putConfiguration(CONFIG_URI, "config"); // changed (inserted)
        assertEquals(1, callbackInvocations);

        service.putConfiguration(CONFIG_URI, "config"); // same, nothing has changed
        assertEquals(1, callbackInvocations);

        service.putConfiguration(CONFIG_URI, "new config"); // changed (updated)
        assertEquals(2, callbackInvocations);
    }

    @Test
    public void shouldUpdate() {
        assertFalse(service.getConfigurationAsString(CONFIG_URI).isPresent());
        service.putConfiguration(CONFIG_URI, "config");
        assertTrue(service.getConfigurationAsString(CONFIG_URI).isPresent());
        assertEquals("config", service.getConfigurationAsString(CONFIG_URI).get());

        service.putConfiguration(CONFIG_URI, "new config");
        assertTrue(service.getConfigurationAsString(CONFIG_URI).isPresent());
        assertEquals("new config", service.getConfigurationAsString(CONFIG_URI).get());
    }

    private void callback(String uri) {
        callbackInvocations++;
    }

}
