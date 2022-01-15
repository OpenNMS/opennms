/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.mock;

import com.atlassian.oai.validator.report.ValidationReport;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.features.config.exception.ConfigConversionException;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.netmgt.config.NotifdConfigManager;
import org.opennms.netmgt.config.NotificationFactory;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.config.notifications.Notifications;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * @author david
 */
public class MockNotificationManager extends NotificationManager {
    private ConfigDefinition def = XsdHelper.buildConfigDefinition("notification", "notifications.xsd",
            "notifications", ConfigurationManagerService.BASE_PATH);

    public MockNotificationManager(NotifdConfigManager configManager, DataSource db, String mgrString) throws IOException {
        super(configManager, db);

        String json = XsdHelper.getConverter(def).xmlToJson(mgrString);
        this.updateConfig(json);
    }

    @Override
    public void update() throws IOException {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.NotificationManager#getInterfaceFilter(java.lang.String)
     */
    protected String getInterfaceFilter(String rule) {
        return "SELECT DISTINCT ipaddr, servicename, nodeid FROM ifservices, service WHERE ifservices.serviceid = service.serviceid";
    }

    @Override
    public void updateConfig(String configJsonStr) {
        ValidationReport report = def.validate(configJsonStr);
        if (report.hasErrors()) {
            throw new ConfigConversionException(null, report.getMessages());
        }
        m_notifications = ConfigConvertUtil.jsonToObject(configJsonStr, Notifications.class);
    }

    @Override
    protected String getConfigName() {
        return NotificationFactory.CONFIG_NAME;
    }

    @Override
    protected String getDefaultConfigId() {
        return NotificationFactory.DEFAULT_CONFIG_ID;
    }
}
