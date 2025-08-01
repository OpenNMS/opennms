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
