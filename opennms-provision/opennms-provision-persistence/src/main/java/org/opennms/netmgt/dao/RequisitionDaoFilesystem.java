package org.opennms.netmgt.dao;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException;
import org.opennms.netmgt.provision.persist.OnmsForeignSource;
import org.opennms.netmgt.provision.persist.OnmsRequisition;
import org.opennms.netmgt.provision.persist.RuntimePersistenceException;
import org.springframework.beans.factory.annotation.Autowired;

public class RequisitionDaoFilesystem implements RequisitionDao {
    @Autowired
    private ForeignSourceRepository m_foreignSourceRepository = null;

    public RequisitionDaoFilesystem() {
    }
    
    public int countAll() {
        return findAll().size();
    }

    public List<OnmsRequisition> findAll() {
        try {
            return new ArrayList<OnmsRequisition>(m_foreignSourceRepository.getRequisitions());
        } catch (ForeignSourceRepositoryException e) {
            throw new RuntimePersistenceException("unable to get all requisitions", e);
        }
    }

    public OnmsRequisition get(String foreignSource) {
        try {
            return m_foreignSourceRepository.getRequisition(foreignSource);
        } catch (ForeignSourceRepositoryException e) {
            throw new RuntimePersistenceException("unable to get requisition for foreign source '" + foreignSource + "'", e);
        }
    }

    public OnmsRequisition get(OnmsForeignSource foreignSource) {
        return get(foreignSource.getName());
    }
    
    public void save(OnmsRequisition requisition) {
        try {
            m_foreignSourceRepository.save(requisition);
        } catch (ForeignSourceRepositoryException e) {
            throw new RuntimePersistenceException("unable to save requisition for foreign source '" + requisition.getForeignSource() + "'", e);
        }
    }

    public void delete(OnmsRequisition requisition) {
        try {
            m_foreignSourceRepository.delete(requisition);
        } catch (ForeignSourceRepositoryException e) {
            throw new RuntimePersistenceException("unable to delete requisition for foreign source '" + requisition.getForeignSource() + "'", e);
        }
    }
    
    public void setForeignSourceRepository(ForeignSourceRepository fsr) {
        m_foreignSourceRepository = fsr;
    }

}
