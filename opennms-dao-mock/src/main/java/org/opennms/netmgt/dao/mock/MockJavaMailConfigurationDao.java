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
    public End2endMailConfig getEnd2EndConfig(String name) {
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

}
