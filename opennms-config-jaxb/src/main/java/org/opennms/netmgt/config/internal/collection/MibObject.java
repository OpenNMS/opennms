package org.opennms.netmgt.config.internal.collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.netmgt.config.api.collection.IGroup;
import org.opennms.netmgt.config.api.collection.IMibObject;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpObjIdXmlAdapter;

/**
 *  <mibObj oid=".1.3.6.1.2.1.10.132.2" instance="0" alias="coffeePotCapacity" type="integer" />
 *  
 * @author brozow
 *
 */
@XmlRootElement(name="mibObj")
@XmlAccessorType(XmlAccessType.NONE)
public class MibObject implements IMibObject {
    @XmlAttribute(name="oid")
    @XmlJavaTypeAdapter(SnmpObjIdXmlAdapter.class)
    private SnmpObjId m_oid;

    @XmlAttribute(name="alias")
    private String m_alias;

    @XmlAttribute(name="type")
    private String m_type;

    @XmlAttribute(name="instance")
    private String m_instance;

    @XmlTransient
    private Group m_group;

    public SnmpObjId getOid() {
        return m_oid;
    }

    public void setOid(SnmpObjId oid) {
        m_oid = oid;
    }

    public String getAlias() {
        return m_alias;
    }

    public void setAlias(String alias) {
        m_alias = alias;
    }

    public String getType() {
        return m_type;
    }

    public void setType(String type) {
        m_type = type;
    }

    public String getInstance() {
        return m_instance;
    }

    public void setInstance(String instance) {
        m_instance = instance;
    }

    public IGroup getGroup() {
        return (IGroup) m_group;
    }

    public void setGroup(final IGroup group) {
        m_group = Group.asGroup(group);
    }

    public static MibObject[] asMibObjects(final IMibObject[] mibObjects) {
        if (mibObjects == null) return null;

        final MibObject[] newMibObjects = new MibObject[mibObjects.length];
        for (int i=0; i < mibObjects.length; i++) {
            newMibObjects[i] = MibObject.asMibObject(mibObjects[i]);
        }
        return newMibObjects;
    }

    private static MibObject asMibObject(final IMibObject obj) {
        if (obj == null) return null;
        final MibObject mibObject = new MibObject();
        mibObject.setOid(obj.getOid());
        mibObject.setAlias(obj.getAlias());
        mibObject.setType(obj.getType());
        mibObject.setInstance(obj.getInstance());
        mibObject.setGroup(obj.getGroup());
        return mibObject;
    }

}

