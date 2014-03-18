package org.opennms.netmgt.config.internal.collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.opennms.netmgt.config.api.collection.IGroup;
import org.opennms.netmgt.config.api.collection.ISystemDef;


/**
 *  <systemDef name="Enterprise">
 *    <sysoidMask>.1.3.6.1.4.1.</sysoidMask>
 *    <collect>
 *      <include>mib2-host-resources-storage</include>
 *      <include>mib2-coffee-rfc2325</include>
 *    </collect>
 *  </systemDef>
 *   
 * @author brozow
 *
 */
@XmlRootElement(name="datacollection-group")
@XmlAccessorType(XmlAccessType.NONE)
public class SystemDef implements ISystemDef {

    @XmlAttribute(name="name")
    private String m_name;

    @XmlElement(name="sysoidMask")
    private String m_sysoidMask;

    @XmlElement(name="sysoid")
    private String m_sysoid;

    @XmlElementWrapper(name="collect")
    @XmlElement(name="include")
    private String[] m_includes;

    @XmlTransient
    private Table[] m_tables;

    @XmlTransient
    private Group[] m_groups;

    @Override
    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    @Override
    public String getSysoidMask() {
        return m_sysoidMask;
    }

    public void setSysoidMask(String sysoidMask) {
        m_sysoidMask = sysoidMask;
    }

    @Override
    public String getSysoid() {
        return m_sysoid;
    }

    public void setSysoid(String sysoid) {
        m_sysoid = sysoid;
    }

    @Override
    public String[] getIncludes() {
        return m_includes;
    }

    public void setIncludes(String[] includes) {
        m_includes = includes == null? null : includes.clone();
    }

    public IGroup[] getGroups() {
        return (IGroup[])m_groups;
    }

    public void setGroups(final IGroup[] groups) {
        m_groups = Group.asGroups(groups);
    }

}
