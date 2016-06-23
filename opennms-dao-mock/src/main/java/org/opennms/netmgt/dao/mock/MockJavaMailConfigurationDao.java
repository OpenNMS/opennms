/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.mock;

import java.util.List;

import org.opennms.netmgt.config.javamail.End2endMailConfig;
import org.opennms.netmgt.config.javamail.ReadmailConfig;
import org.opennms.netmgt.config.javamail.SendmailConfig;
import org.opennms.netmgt.dao.api.JavaMailConfigurationDao;
import org.springframework.dao.DataAccessResourceFailureException;

public class MockJavaMailConfigurationDao implements JavaMailConfigurationDao {

    @Override
    public SendmailConfig getDefaultSendmailConfig() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SendmailConfig getSendMailConfig(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SendmailConfig> getSendmailConfigs() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ReadmailConfig getDefaultReadmailConfig() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ReadmailConfig getReadMailConfig(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ReadmailConfig> getReadmailConfigs() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public End2endMailConfig getEnd2endConfig(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<End2endMailConfig> getEnd2EndConfigs() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void verifyMarshaledConfiguration() throws IllegalStateException {
        // TODO Auto-generated method stub
    }

    @Override
    public void reloadConfiguration() throws DataAccessResourceFailureException {
        // TODO Auto-generated method stub
    }

    @Override
    public void addSendMailConfig(SendmailConfig sendmailConfig) {
        // TODO Auto-generated method stub
    }

    @Override
    public void addReadMailConfig(ReadmailConfig readmailConfig) {
        // TODO Auto-generated method stub
    }

    @Override
    public void addEnd2endMailConfig(End2endMailConfig end2endConfig) {
        // TODO Auto-generated method stub
    }

    @Override
    public void saveConfiguration() {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean removeSendMailConfig(String sendmailConfigName) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean removeReadMailConfig(String readmailConfigName) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean removeEnd2endConfig(String end2endConfigName) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setDefaultSendmailConfig(String sendmailConfigName) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setDefaultReadmailConfig(String sendmailConfigName) {
        // TODO Auto-generated method stub
    }

}
