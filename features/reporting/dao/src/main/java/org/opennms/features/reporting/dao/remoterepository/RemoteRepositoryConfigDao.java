package org.opennms.features.reporting.dao.remoterepository;

public interface RemoteRepositoryConfigDao {
    public Boolean isRepositoryActive();
    public String getURI();
    public String getLoginUser();
    public String getLoginRepoPassword();
    public String getRepositoryName();
}
