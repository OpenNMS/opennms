package org.opennms.netmgt.provision.persist;

import java.net.URL;
import java.util.Date;
import java.util.Set;

import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;

/**
 * <p>
 * The fused foreign source repository always returns data from the active foreign source
 * repository.  When updating or deleting data, it always updates the active foreign source
 * repository, and deletes from the pending.
 * </p>
 * <p>
 * One thing to note -- if you are importing/saving a requisition to the fused foreign
 * source repository, any pending changes to the foreign source will be promoted to the
 * active repository as well.
 */
public class FusedForeignSourceRepository extends AbstractForeignSourceRepository implements ForeignSourceRepository, InitializingBean {
    @Autowired
    @Qualifier("pending")
    private FilesystemForeignSourceRepository m_pendingForeignSourceRepository;
    
    @Autowired
    @Qualifier("deployed")
    private FilesystemForeignSourceRepository m_activeForeignSourceRepository;

    public FusedForeignSourceRepository() {
    }

    public void afterPropertiesSet() {
        m_activeForeignSourceRepository.setUpdateDateStamps(false);
    }

    public synchronized Requisition deployResourceRequisition(Resource resource) throws ForeignSourceRepositoryException {
        Requisition r = m_activeForeignSourceRepository.deployResourceRequisition(resource);
        updateActiveForeignSource(r.getForeignSource());
        m_pendingForeignSourceRepository.delete(r);
        return r;
    }
    
    private synchronized void updateActiveForeignSource(String foreignSourceName) {
        ForeignSource active = m_activeForeignSourceRepository.getForeignSource(foreignSourceName);
        ForeignSource pending = m_pendingForeignSourceRepository.getForeignSource(foreignSourceName);

        if (pending.isDefault()) {
            // if pending is default, assume active is valid, be it default or otherwise
            m_pendingForeignSourceRepository.delete(pending);
        } else {
            if (active.isDefault()) {
                // if pending is not default, and active is, assume pending should override active
                m_activeForeignSourceRepository.save(pending);
            } else {
                // otherwise, compare dates, pending updates active if it's timestamp is newer
                Date pendingDate = pending.getDateStampAsDate();
                Date activeDate = active.getDateStampAsDate();
                if (!activeDate.after(pendingDate)) {
                    m_activeForeignSourceRepository.save(pending);
                }
            }
        }
        m_pendingForeignSourceRepository.delete(pending);
    }

    public synchronized void delete(ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        m_pendingForeignSourceRepository.delete(foreignSource);
        m_activeForeignSourceRepository.delete(foreignSource);
    }

    public synchronized void delete(Requisition requisition) throws ForeignSourceRepositoryException {
        m_pendingForeignSourceRepository.delete(requisition);
        m_activeForeignSourceRepository.delete(requisition);
    }

    public ForeignSource getForeignSource(String foreignSourceName) throws ForeignSourceRepositoryException {
        return m_activeForeignSourceRepository.getForeignSource(foreignSourceName);
    }

    public int getForeignSourceCount() throws ForeignSourceRepositoryException {
        return m_activeForeignSourceRepository.getForeignSourceCount();
    }

    public Set<ForeignSource> getForeignSources() throws ForeignSourceRepositoryException {
        return m_activeForeignSourceRepository.getForeignSources();
    }

    public Requisition getRequisition(String foreignSourceName) throws ForeignSourceRepositoryException {
        return m_activeForeignSourceRepository.getRequisition(foreignSourceName);
    }

    public Requisition getRequisition(ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        return m_activeForeignSourceRepository.getRequisition(foreignSource);
    }

    public URL getRequisitionURL(String foreignSource) {
        return m_activeForeignSourceRepository.getRequisitionURL(foreignSource);
    }

    public Set<Requisition> getRequisitions() throws ForeignSourceRepositoryException {
        return m_activeForeignSourceRepository.getRequisitions();
    }

    public synchronized void save(ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        m_pendingForeignSourceRepository.delete(foreignSource);
        m_activeForeignSourceRepository.save(foreignSource);
    }

    public void save(Requisition requisition) throws ForeignSourceRepositoryException {
        m_pendingForeignSourceRepository.delete(requisition);
        m_activeForeignSourceRepository.save(requisition);
    }

}
