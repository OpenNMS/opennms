package org.opennms.netmgt.dao;

import java.util.List;

import org.opennms.netmgt.model.Acknowledgeable;
import org.opennms.netmgt.model.Acknowledgment;

public interface AcknowledgementDao extends OnmsDao<Acknowledgment, Integer> {

    List<Acknowledgeable> findAcknowledgable(Acknowledgment ack);

    void updateAckable(Acknowledgeable ackable);
    

}
