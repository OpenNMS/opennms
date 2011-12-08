package org.opennms.features.reporting.model.remoterepository;

import javax.xml.bind.annotation.*;

/**
 * Class RemoteRepositoryConfig.
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name = "report-repository-config")
public class RemoteRepositoryConfig {

    private Boolean m_repositoryActive;
    private String m_URI;
    private String m_loginUser;
    private String m_loginRepoPassword;
    private String m_repositoryName;
    private String m_repositoryDescription;
    private String m_repositoryManagementURL;
    private String m_repositoryId;
     
    public String getLoginRepoPassword() {
        return m_loginRepoPassword;
    }
    public String getLoginUser() {
        return m_loginUser;
    }
    public String getRepositoryDescription() {
        return m_repositoryDescription;
    } 
    public String getRepositoryId() {
        return m_repositoryId;
    }
    public String getRepositoryManagementURL() {
        return m_repositoryManagementURL;
    }
    public String getRepositoryName() {
        return m_repositoryName;
    }
    public String getURI() {
        return m_URI;
    }
    public Boolean isRepositoryActive() {
        return m_repositoryActive;
    }
    @XmlElement(name = "login-repo-password")
    public void setLoginRepoPassword(String loginRepoPassword) {
        m_loginRepoPassword = loginRepoPassword;
    }
    @XmlElement(name = "login-user")
    public void setLoginUser(String loginUser) {
        m_loginUser = loginUser;
    }
    @XmlElement(name = "active")
    public void setRepositoryActive(Boolean repositoryActive) {
        m_repositoryActive = repositoryActive;
    }
    @XmlElement(name = "repository-description")
    public void setRepositoryDescription(String repositoryDescription) {
        m_repositoryDescription = repositoryDescription;
    }
    @XmlElement(name = "repository-id")
    public void setRepositoryId(String repositoryId) {
        m_repositoryId = repositoryId;
    }
    @XmlElement(name = "repository-management-url")
    public void setRepositoryManagementURL(String repositoryManagementURL) {
        m_repositoryManagementURL = repositoryManagementURL;
    }
    @XmlElement(name = "repository-name")
    public void setRepositoryName(String repositoryName) {
        m_repositoryName = repositoryName;
    }
    @XmlElement(name = "uri")
    public void setURI(String uri) {
        m_URI = uri;
    }
    @Override
    public String toString() {
        return "RemoteRepositoryConfig [m_repositoryActive="
                + m_repositoryActive + ", m_URI=" + m_URI + ", m_loginUser="
                + m_loginUser + ", m_loginRepoPassword="
                + m_loginRepoPassword + ", m_repositoryName="
                + m_repositoryName + ", m_repositoryDescription="
                + m_repositoryDescription + ", m_repositoryManagementURL="
                + m_repositoryManagementURL + ", m_repositoryId="
                + m_repositoryId + "]";
    }
}
