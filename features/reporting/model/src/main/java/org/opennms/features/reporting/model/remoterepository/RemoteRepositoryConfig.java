package org.opennms.features.reporting.model.remoterepository;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "remoteRepositoryConfig")
public class RemoteRepositoryConfig {

    private List<RemoteRepositoryDefinition> m_repositoryList = new ArrayList<RemoteRepositoryDefinition>();

    private String jasperRepotsVersion;
    
    @XmlElement(name = "remoteRepository")
    public List<RemoteRepositoryDefinition> getRepositoryList() {
        return m_repositoryList;
    }

    public void setRepositoryList(List<RemoteRepositoryDefinition> repositoryList) {
        this.m_repositoryList = repositoryList;
    }
    
    @XmlAttribute(name = "jasperReportsVersion")
    public String getJasperRepotsVersion() {
        return jasperRepotsVersion;
    }

    public void setJasperRepotsVersion(String jasperRepotsVersion) {
        this.jasperRepotsVersion = jasperRepotsVersion;
    }
    
    
}
