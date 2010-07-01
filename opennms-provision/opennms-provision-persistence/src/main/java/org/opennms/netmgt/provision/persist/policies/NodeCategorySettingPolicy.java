package org.opennms.netmgt.provision.persist.policies;

import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.BasePolicy;
import org.opennms.netmgt.provision.NodePolicy;
import org.opennms.netmgt.provision.annotations.Require;
import org.opennms.netmgt.provision.annotations.Policy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
/**
 * <p>NodeCategorySettingPolicy class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Scope("prototype")
@Policy("Set Node Category")
public class NodeCategorySettingPolicy extends BasePolicy<OnmsNode> implements NodePolicy {
    
    private String m_category; 
    
    /** {@inheritDoc} */
    @Override
    public OnmsNode act(OnmsNode node) {
        if (getCategory() == null) {
            return node;
        }

        OnmsCategory category = new OnmsCategory(getCategory());
        
        node.addCategory(category);
        
        return node;
        
    }

    
    /**
     * <p>getCategory</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Require(value = { }) 
    public String getCategory() {
        return m_category;
    }

    /**
     * <p>setCategory</p>
     *
     * @param category a {@link java.lang.String} object.
     */
    public void setCategory(String category) {
        m_category = category;
    }

    /**
     * <p>getType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getType() {
        return getCriteria("type");
    }

    /**
     * <p>setType</p>
     *
     * @param type a {@link java.lang.String} object.
     */
    public void setType(String type) {
        putCriteria("type", type);
    }

    /**
     * <p>getSysObjectId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSysObjectId() {
        return getCriteria("sysObjectId");
    }

    /**
     * <p>setSysObjectId</p>
     *
     * @param sysObjectId a {@link java.lang.String} object.
     */
    public void setSysObjectId(String sysObjectId) {
        putCriteria("sysObjectId", sysObjectId);
    }

    /**
     * <p>getSysName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSysName() {
        return getCriteria("sysName");
    }

    /**
     * <p>setSysName</p>
     *
     * @param sysName a {@link java.lang.String} object.
     */
    public void setSysName(String sysName) {
        putCriteria("sysName", sysName);
    }

    /**
     * <p>getSysDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSysDescription() {
        return getCriteria("sysDescription");
    }

    /**
     * <p>setSysDescription</p>
     *
     * @param sysDescription a {@link java.lang.String} object.
     */
    public void setSysDescription(String sysDescription) {
        putCriteria("sysDescription", sysDescription);
    }

    /**
     * <p>getSysLocation</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSysLocation() {
        return getCriteria("sysLocation");
    }

    /**
     * <p>setSysLocation</p>
     *
     * @param sysLocation a {@link java.lang.String} object.
     */
    public void setSysLocation(String sysLocation) {
        putCriteria("sysLocation", sysLocation);
    }

    /**
     * <p>getSysContact</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSysContact() {
        return getCriteria("sysContact");
    }

    /**
     * <p>setSysContact</p>
     *
     * @param sysContact a {@link java.lang.String} object.
     */
    public void setSysContact(String sysContact) {
        putCriteria("sysContact", sysContact);
    }

    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLabel() {
        return getCriteria("label");
    }

    /**
     * <p>setLabel</p>
     *
     * @param label a {@link java.lang.String} object.
     */
    public void setLabel(String label) {
        putCriteria("label", label);
    }

    /**
     * <p>getLabelSource</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLabelSource() {
        return getCriteria("labelSource");
    }

    /**
     * <p>setLabelSource</p>
     *
     * @param labelSource a {@link java.lang.String} object.
     */
    public void setLabelSource(String labelSource) {
        putCriteria("labelSource", labelSource);
    }

    /**
     * <p>getNetBiosName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNetBiosName() {
        return getCriteria("netBiosName");
    }

    /**
     * <p>setNetBiosName</p>
     *
     * @param netBiosName a {@link java.lang.String} object.
     */
    public void setNetBiosName(String netBiosName) {
        putCriteria("netBiosName", netBiosName);
    }

    /**
     * <p>getNetBiosDomain</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNetBiosDomain() {
        return getCriteria("netBiosDomain");
    }

    /**
     * <p>setNetBiosDomain</p>
     *
     * @param netBiosDomain a {@link java.lang.String} object.
     */
    public void setNetBiosDomain(String netBiosDomain) {
        putCriteria("netBiosDomain", netBiosDomain);
    }

    /**
     * <p>getOperatingSystem</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getOperatingSystem() {
        return getCriteria("operatingSystem");
    }

    /**
     * <p>setOperatingSystem</p>
     *
     * @param operatingSystem a {@link java.lang.String} object.
     */
    public void setOperatingSystem(String operatingSystem) {
        putCriteria("operatingSystem", operatingSystem);
    }

    /**
     * <p>getForeignId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getForeignId() {
        return getCriteria("foreignId");
    }

    /**
     * <p>setForeignId</p>
     *
     * @param foreignId a {@link java.lang.String} object.
     */
    public void setForeignId(String foreignId) {
        putCriteria("foreignId", foreignId);
    }

    /**
     * <p>getForeignSource</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getForeignSource() {
        return getCriteria("foreignSource");
    }

    /**
     * <p>setForeignSource</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     */
    public void setForeignSource(String foreignSource) {
        putCriteria("foreignSource", foreignSource);
    }

}
