package org.opennms.features.reporting.dao.remoterepository;

import java.io.File;

import javax.xml.bind.JAXB;

import org.opennms.features.reporting.model.remoterepository.RemoteRepositoryConfig;

public class DefaultRemoteRepositoryConfigDao implements
        RemoteRepositoryConfigDao {

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
            // TODO Tak: logging and fail safety
            System.out.println("fail to unmarshal: " + REMOTE_REPOSITORY_XML);
        }
        return config;
    }

    @Override
    public Boolean isRepositoryActive() {
        return readConfig().isRepositoryActive();
    }

    @Override
    public String getURI() {
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
}
