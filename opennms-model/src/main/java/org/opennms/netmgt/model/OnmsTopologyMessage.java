package org.opennms.netmgt.model;

public class OnmsTopologyMessage {

    public static OnmsTopologyMessage  create(OnmsTopologyRef messagebody) {
        return new OnmsTopologyMessage(messagebody, TopologyMessageStatus.NEW);
    }
    
    public static OnmsTopologyMessage  update(OnmsTopologyRef messagebody) {
        return new OnmsTopologyMessage(messagebody, TopologyMessageStatus.UPDATE);
    }
    
    public static OnmsTopologyMessage  delete(OnmsTopologyRef messagebody) {
        return new OnmsTopologyMessage(messagebody, TopologyMessageStatus.DELETE);        
    }

    
    public enum TopologyMessageStatus {
        NEW,
        UPDATE,
        DELETE
    }

    private final OnmsTopologyRef m_messagebody;
    private final TopologyMessageStatus m_messagestatus;

    private OnmsTopologyMessage(OnmsTopologyRef messagebody, TopologyMessageStatus messagestatus) {
        m_messagebody=messagebody;
        m_messagestatus=messagestatus;
    }

    public OnmsTopologyRef getMessagebody() {
        return m_messagebody;
    }

    public TopologyMessageStatus getMessagestatus() {
        return m_messagestatus;
    }

}
