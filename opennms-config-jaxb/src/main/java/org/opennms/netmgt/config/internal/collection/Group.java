package org.opennms.netmgt.config.internal.collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.config.api.collection.IGroup;
import org.opennms.netmgt.config.api.collection.IMibObject;

/**
 * 	<group name="mib2-coffee-rfc2325">
 *      <mibObj oid=".1.3.6.1.2.1.10.132.2" instance="0" alias="coffeePotCapacity" type="integer" />
 *      <mibObj oid=".1.3.6.1.2.1.10.132.4.1.2" instance="0" alias="coffeePotLevel" type="integer" />
 *      <mibObj oid=".1.3.6.1.2.1.10.132.4.1.6" instance="0" alias="coffeePotTemp" type="integer" />
 *  </group>
 *  
 * @author brozow
 *
 */
@XmlRootElement(name="group")
@XmlAccessorType(XmlAccessType.NONE)
public class Group implements IGroup {

    @XmlAttribute(name="name")
    private String m_name;

    @XmlElement(name="mibObj")
    private MibObject[] m_mibObjects = new MibObject[0];

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public IMibObject[] getMibObjects() {
        return (IMibObject[]) m_mibObjects;
    }

    public void setMibObjects(final IMibObject[] mibObjects) {
        m_mibObjects = MibObject.asMibObjects(mibObjects);
    }

    public static Group asGroup(final IGroup group) {
        if (group == null) return null;

        if (group instanceof Group) {
            return (Group)group;
        }

        final Group newGroup = new Group();
        newGroup.setName(group.getName());
        newGroup.setMibObjects(group.getMibObjects());
        return newGroup;
    }

    public static Group[] asGroups(final IGroup[] groups) {
        if (groups == null) return null;
        
        final Group[] newGroups = new Group[groups.length];
        for (int i=0; i < groups.length; i++) {
            newGroups[i] = Group.asGroup(groups[i]);
        }
        return newGroups;
    }
}
