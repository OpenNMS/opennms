package org.opennms.features.reporting.dao.remoterepository;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXB;

import org.opennms.features.reporting.model.remoterepository.RemoteRepositoryConfig;
import org.opennms.features.reporting.model.remoterepository.RemoteRepositoryDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRemoteRepositoryConfigDAO implements
        RemoteRepositoryConfigDAO {

    Logger logger = LoggerFactory.getLogger(DefaultRemoteRepositoryConfigDAO.class);
    
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
    public String getJasperReportsVersion() {
        return this.readConfig().getJasperRepotsVersion();
    }
    
    @Override
    public Boolean isRepositoryActive(String repositoryID) {
        return this.getRepositoryById(repositoryID).isRepositoryActive();
    }

    @Override
    public URI getURI(String repositoryID) {
        return this.getRepositoryById(repositoryID).getURI();
    }

    @Override
    public String getLoginUser(String repositoryID) {
        return this.getRepositoryById(repositoryID).getLoginUser();
    }

    @Override
    public String getLoginRepoPassword(String repositoryID) {
        return this.getRepositoryById(repositoryID).getLoginRepoPassword();
    }

    @Override
    public String getRepositoryName(String repositoryID) {
        return this.getRepositoryById(repositoryID).getRepositoryName();
    }

    @Override
    public String getRepositoryDescription(String repositoryID) {
        return this.getRepositoryById(repositoryID).getRepositoryDescription();
    }

    @Override
    public String getRepositoryManagementURL(String repositoryID) {
        return this.getRepositoryById(repositoryID).getRepositoryManagementURL();
    }

    //TODO Tak: How to failsafe this?
    public RemoteRepositoryDefinition getRepositoryById(String repositoryID) {
        RemoteRepositoryDefinition result = null;
        for(RemoteRepositoryDefinition repository : this.getAllRepositories()) {
            if (repositoryID.equals(repository.getRepositoryId())) {
                return repository;
            }
        }
        return result;
    }

    @Override
    public List<RemoteRepositoryDefinition> getAllRepositories() {
        List<RemoteRepositoryDefinition> resultList = new ArrayList<RemoteRepositoryDefinition>();
        resultList.addAll(this.readConfig().getRepositoryList());
        return resultList;
    }

    @Override
    public List<RemoteRepositoryDefinition> getActiveRepositories() {
        List<RemoteRepositoryDefinition> resultList = new ArrayList<RemoteRepositoryDefinition>();
        for (RemoteRepositoryDefinition repository : this.readConfig().getRepositoryList()) {
            if (repository.isRepositoryActive()) {
                resultList.add(repository);
            }
        }
        return resultList;
    }
}
