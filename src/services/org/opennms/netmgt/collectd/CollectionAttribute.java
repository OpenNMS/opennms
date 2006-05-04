package org.opennms.netmgt.collectd;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CollectionAttribute {
    
    public boolean equals(Object obj) {
        if (obj instanceof CollectionAttribute) {
            CollectionAttribute attr = (CollectionAttribute) obj;
            return attr.m_mibObj.equals(m_mibObj);
        }
        return false;
    }

    public int hashCode() {
        return m_mibObj.hashCode();
    }

    private MibObject m_mibObj;
    private DataSource m_ds;
    private String m_collectionName;

    public CollectionAttribute(String collectionName, MibObject mibObj) {
        m_collectionName = collectionName;
        m_mibObj = mibObj;
        m_ds = DataSource.dataSourceForMibObject(m_mibObj, m_collectionName);
    }

    public MibObject getMibObj() {
        return m_mibObj;
    }

    public DataSource getDs() {
        return m_ds;
    }

    // FIXME: CollectionAttribute should be a tracker of its own
    // Also these should be created directly by the DAO rather 
    // than MibObject.
    public static List getCollectionTrackers(List objList) {
        ArrayList trackers = new ArrayList(objList.size());
        for (Iterator iter = objList.iterator(); iter.hasNext();) {
            CollectionAttribute attr = (CollectionAttribute) iter.next();
            trackers.add(attr.getMibObj().getCollectionTracker());
        }
        
        return trackers;
    }

}
