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

import org.opennms.features.config.service.api.CmJaxbConfigDao;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.impl.AbstractCmJaxbConfigDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.events.EventBuilder;

import java.util.function.Consumer;

public class ConfigurationReloadEventCallback<E> implements Consumer<ConfigUpdateInfo> {
    private EventForwarder eventForwarder;
    private CmJaxbConfigDao<E> cmJaxbConfigDao;

    public ConfigurationReloadEventCallback(EventForwarder eventForwarder) {
        this.eventForwarder = eventForwarder;
    }

    @Override
    public void accept(ConfigUpdateInfo configUpdateInfo) {
        // Fire reload event
        EventBuilder eventBuilder = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI,
                "config-rest");
        eventBuilder.addParam(EventConstants.PARM_DAEMON_NAME, configUpdateInfo.getConfigName());
        eventForwarder.sendNow(eventBuilder.getEvent());
    }
}
