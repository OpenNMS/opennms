package org.opennms.netmgt.provision.persist;

import java.net.URL;
import java.util.Date;
import java.util.Set;

import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;

/**
 * <p>
 * The fused foreign source repository always returns data from the deployed foreign source
 * repository.  When updating or deleting data, it always updates the deployed foreign source
 * repository, and deletes from the pending.
 * </p>
 * <p>
 * One thing to note -- if you are importing/saving a requisition to the fused foreign
 * source repository, any pending changes to the foreign source will be promoted to the
 * deployed repository as well.
 */
public class FusedForeignSourceRepository extends AbstractForeignSourceRepository implements ForeignSourceRepository {
    @Autowired
    @Qualifier("pending")
    private FilesystemForeignSourceRepository m_pendingForeignSourceRepository;
    
    @Autowired
    @Qualifier("deployed")
    private FilesystemForeignSourceRepository m_deployedForeignSourceRepository;

    public synchronized Requisition deployResourceRequisition(Resource resource) throws ForeignSourceRepositoryException {
        Requisition r = m_deployedForeignSourceRepository.deployResourceRequisition(resource);
        updateDeployedForeignSource(r.getForeignSource());
        m_pendingForeignSourceRepository.delete(r);
        return r;
    }
    
    private synchronized void updateDeployedForeignSource(String foreignSourceName) {
        ForeignSource deployed = m_deployedForeignSourceRepository.getForeignSource(foreignSourceName);
        ForeignSource pending = m_pendingForeignSourceRepository.getForeignSource(foreignSourceName);

        if (pending.isDefault()) {
            // if pending is default, assume deployed is valid, be it default or otherwise
            m_pendingForeignSourceRepository.delete(pending);
        } else {
            if (deployed.isDefault()) {
                // if pending is not default, and deployed is, assume pending should override deployed
                m_deployedForeignSourceRepository.save(pending);
            } else {
                // otherwise, compare dates, pending updates deployed if it's timestamp is newer
                Date pendingDate = pending.getDateStampAsDate();
                Date deployedDate = deployed.getDateStampAsDate();
                if (!deployedDate.after(pendingDate)) {
                    m_deployedForeignSourceRepository.save(pending);
                }
            }
        }
        m_pendingForeignSourceRepository.delete(pending);
    }

    public synchronized void delete(ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        m_pendingForeignSourceRepository.delete(foreignSource);
        m_deployedForeignSourceRepository.delete(foreignSource);
    }

    public synchronized void delete(Requisition requisition) throws ForeignSourceRepositoryException {
        m_pendingForeignSourceRepository.delete(requisition);
        m_deployedForeignSourceRepository.delete(requisition);
    }

    public ForeignSource getForeignSource(String foreignSourceName) throws ForeignSourceRepositoryException {
        return m_deployedForeignSourceRepository.getForeignSource(foreignSourceName);
    }

    public int getForeignSourceCount() throws ForeignSourceRepositoryException {
        return m_deployedForeignSourceRepository.getForeignSourceCount();
    }

    public Set<ForeignSource> getForeignSources() throws ForeignSourceRepositoryException {
        return m_deployedForeignSourceRepository.getForeignSources();
    }

    public Requisition getRequisition(String foreignSourceName) throws ForeignSourceRepositoryException {
        return m_deployedForeignSourceRepository.getRequisition(foreignSourceName);
    }

    public Requisition getRequisition(ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        return m_deployedForeignSourceRepository.getRequisition(foreignSource);
    }

    public URL getRequisitionURL(String foreignSource) {
        return m_deployedForeignSourceRepository.getRequisitionURL(foreignSource);
    }

    public Set<Requisition> getRequisitions() throws ForeignSourceRepositoryException {
        return m_deployedForeignSourceRepository.getRequisitions();
    }

    public synchronized void save(ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        m_pendingForeignSourceRepository.delete(foreignSource);
        m_deployedForeignSourceRepository.save(foreignSource);
    }

    public synchronized void save(Requisition requisition) throws ForeignSourceRepositoryException {
        m_pendingForeignSourceRepository.delete(requisition);
        m_deployedForeignSourceRepository.save(requisition);
    }

}
