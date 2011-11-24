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
    
    public Boolean isRepositoryActive() {
        return m_repositoryActive;
    }
    @XmlElement(name = "active")
    public void setRepositoryActive(Boolean repositoryActive) {
        m_repositoryActive = repositoryActive;
    }
    public String getURI() {
        return m_URI;
    }
    @XmlElement(name = "uri")
    public void setURI(String uri) {
        m_URI = uri;
    }
    public String getLoginUser() {
        return m_loginUser;
    }
    @XmlElement(name = "login-user")
    public void setLoginUser(String loginUser) {
        m_loginUser = loginUser;
    }
    public String getLoginRepoPassword() {
        return m_loginRepoPassword;
    }
    @XmlElement(name = "login-repo-password")
    public void setLoginRepoPassword(String loginRepoPassword) {
        m_loginRepoPassword = loginRepoPassword;
    }
    public String getRepositoryName() {
        return m_repositoryName;
    }
    @XmlElement(name = "name")
    public void setRepositoryName(String repositoryName) {
        m_repositoryName = repositoryName;
    }
}
