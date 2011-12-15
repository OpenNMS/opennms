package org.opennms.features.reporting.dao.remoterepository;

import java.net.URI;

public interface RemoteRepositoryConfigDao {
    public String getRepositoryId();
    public Boolean isRepositoryActive();
    public URI getURI();
    public String getLoginUser();
    public String getLoginRepoPassword();
    public String getRepositoryName();
    public String getRepositoryDescription();
    public String getRepositoryManagementURL();
}
