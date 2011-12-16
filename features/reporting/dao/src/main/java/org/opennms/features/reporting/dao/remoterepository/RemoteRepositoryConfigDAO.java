package org.opennms.features.reporting.dao.remoterepository;

import java.net.URI;
import java.util.List;

import org.opennms.features.reporting.model.remoterepository.RemoteRepositoryDefinition;

public interface RemoteRepositoryConfigDAO {
    public Boolean isRepositoryActive(String repositoryID);
    public URI getURI(String repositoryID);
    public String getLoginUser(String repositoryID);
    public String getLoginRepoPassword(String repositoryID);
    public String getRepositoryName(String repositoryID);
    public String getRepositoryDescription(String repositoryID);
    public String getRepositoryManagementURL(String repositoryID);
    public List<RemoteRepositoryDefinition> getAllRepositories();
    public List<RemoteRepositoryDefinition> getActiveRepositories();
    public String getJasperReportsVersion();
}