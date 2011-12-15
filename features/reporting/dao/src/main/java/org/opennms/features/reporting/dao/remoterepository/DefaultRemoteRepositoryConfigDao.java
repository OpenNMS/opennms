package org.opennms.features.reporting.dao.remoterepository;

import java.io.File;
import java.net.URI;

import javax.xml.bind.JAXB;

import org.opennms.features.reporting.model.remoterepository.RemoteRepositoryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRemoteRepositoryConfigDao implements
        RemoteRepositoryConfigDao {

    Logger logger = LoggerFactory.getLogger(DefaultRemoteRepositoryConfigDao.class.getSimpleName());
    
    private final String REMOTE_REPOSITORY_XML = System.getProperty("opennms.home")
            + File.separator
            + "etc"
            + File.separator
            + "remote-repository.xml";

    private RemoteRepositoryConfig config;

    private RemoteRepositoryConfig readConfig() {
        try {
            config = JAXB.unmarshal(new File(REMOTE_REPOSITORY_XML), RemoteRepositoryConfig.class);
        } catch (Exception e) {
            logger.error("fail to unmarshal file '{}', '{}'", REMOTE_REPOSITORY_XML, e.getMessage());
            e.printStackTrace();
        }
        return config;
    }

    @Override
    public Boolean isRepositoryActive() {
        return readConfig().isRepositoryActive();
    }

    @Override
    public URI getURI() {
        return readConfig().getURI();
    }

    @Override
    public String getLoginUser() {
        return readConfig().getLoginUser();
    }

    @Override
    public String getLoginRepoPassword() {
        return readConfig().getLoginRepoPassword();
    }

    @Override
    public String getRepositoryName() {
        return readConfig().getRepositoryName();
    }

    @Override
    public String getRepositoryDescription() {
        return readConfig().getRepositoryDescription();
    }

    @Override
    public String getRepositoryManagementURL() {
        return readConfig().getRepositoryManagementURL();
    }

    @Override
    public String getRepositoryId() {
        return readConfig().getRepositoryId();
    }
}
