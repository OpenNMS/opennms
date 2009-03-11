package org.opennms.netmgt.provision.persist.policies;

import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.BasePolicy;
import org.opennms.netmgt.provision.NodePolicy;
import org.opennms.netmgt.provision.annotations.Policy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Policy("Set Node Category")
public class NodeCategorySettingPolicy extends BasePolicy implements NodePolicy {
    private String m_category;
    private String m_type;
    private String m_sysObjectId;
    private String m_sysName;
    private String m_sysDescription;
    private String m_sysLocation;
    private String m_sysContact;
    private String m_label;
    private String m_labelSource;
    private String m_netBiosName;
    private String m_netBiosDomain;
    private String m_operatingSystem;
    private String m_foreignId;
    private String m_foreignSource;
    
    public OnmsNode apply(OnmsNode node) {
        if (node == null) {
            return null;
        }

        String category = m_category;
        if (category == null) {
            return node;
        }

        if (m_type != null) {
            if (!match(node.getType(), m_type)) {
                return node;
            }
        }
        if (m_sysObjectId != null) {
            if (!match(node.getSysObjectId(), m_sysObjectId)) {
                return node;
            }
        }
        if (m_sysName != null) {
            if (!match(node.getSysName(), m_sysName)) {
                return node;
            }
        }
        if (m_sysDescription != null) {
            if (!match(node.getSysDescription(), m_sysDescription)) {
                return node;
            }
        }
        if (m_sysLocation != null) {
            if (!match(node.getSysLocation(), m_sysLocation)) {
                return node;
            }
        }
        if (m_sysContact != null) {
            if (!match(node.getSysContact(), m_sysContact)) {
                return node;
            }
        }
        if (m_label != null) {
            if (!match(node.getLabel(), m_label)) {
                return node;
            }
        }
        if (m_labelSource != null) {
            if (!match(node.getLabelSource(), m_labelSource)) {
                return node;
            }
        }
        if (m_netBiosName != null) {
            if (!match(node.getNetBiosName(), m_netBiosName)) {
                return node;
            }
        }
        if (m_netBiosDomain != null) {
            if (!match(node.getNetBiosDomain(), m_netBiosDomain)) {
                return node;
            }
        }
        if (m_operatingSystem != null) {
            if (!match(node.getOperatingSystem(), m_operatingSystem)) {
                return node;
            }
        }
        if (m_foreignId != null) {
            if (!match(node.getForeignId(), m_foreignId)) {
                return node;
            }
        }
        if (m_foreignSource != null) {
            if (!match(node.getForeignSource(), m_foreignSource)) {
                return node;
            }
        }

        node.addCategory(new OnmsCategory(category));

        return node;
    }

    public String getCategory() {
        return m_category;
    }

    public void setCategory(String category) {
        m_category = category;
    }

    public String getType() {
        return m_type;
    }

    public void setType(String type) {
        m_type = type;
    }

    public String getSysObjectId() {
        return m_sysObjectId;
    }

    public void setSysObjectId(String sysObjectId) {
        m_sysObjectId = sysObjectId;
    }

    public String getSysName() {
        return m_sysName;
    }

    public void setSysName(String sysName) {
        m_sysName = sysName;
    }

    public String getSysDescription() {
        return m_sysDescription;
    }

    public void setSysDescription(String sysDescription) {
        m_sysDescription = sysDescription;
    }

    public String getSysLocation() {
        return m_sysLocation;
    }

    public void setSysLocation(String sysLocation) {
        m_sysLocation = sysLocation;
    }

    public String getSysContact() {
        return m_sysContact;
    }

    public void setSysContact(String sysContact) {
        m_sysContact = sysContact;
    }

    public String getLabel() {
        return m_label;
    }

    public void setLabel(String label) {
        m_label = label;
    }

    public String getLabelSource() {
        return m_labelSource;
    }

    public void setLabelSource(String labelSource) {
        m_labelSource = labelSource;
    }

    public String getNetBiosName() {
        return m_netBiosName;
    }

    public void setNetBiosName(String netBiosName) {
        m_netBiosName = netBiosName;
    }

    public String getNetBiosDomain() {
        return m_netBiosDomain;
    }

    public void setNetBiosDomain(String netBiosDomain) {
        m_netBiosDomain = netBiosDomain;
    }

    public String getOperatingSystem() {
        return m_operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        m_operatingSystem = operatingSystem;
    }

    public String getForeignId() {
        return m_foreignId;
    }

    public void setForeignId(String foreignId) {
        m_foreignId = foreignId;
    }

    public String getForeignSource() {
        return m_foreignSource;
    }

    public void setForeignSource(String foreignSource) {
        m_foreignSource = foreignSource;
    }
}
