/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.jaxb.callback;

import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.xml.event.Event;

public class ConfigurationReloadEventCallbackTest {
    private String configName = "provisiond";

    @Test
    public void testEventTriggered() {
        EventForwarder eventForwarder = Mockito.mock(EventForwarder.class);

        ConfigUpdateInfo info = new ConfigUpdateInfo(configName);
        ConfigurationReloadEventCallback callback = new ConfigurationReloadEventCallback(eventForwarder);
        callback.accept(info);
        Mockito.verify(eventForwarder, Mockito.times(1)).sendNow(Mockito.any(Event.class));
    }
}
