/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.persist;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

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
 * </p>
 */
public class FusedForeignSourceRepository extends AbstractForeignSourceRepository implements ForeignSourceRepository, InitializingBean {
    private ForeignSourceRepository m_pendingForeignSourceRepository;
    private ForeignSourceRepository m_deployedForeignSourceRepository;

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_pendingForeignSourceRepository, "Pending foreign source repository must not be null.");
        Assert.notNull(m_deployedForeignSourceRepository, "Deployed foreign source repository must not be null.");
    }

    public ForeignSourceRepository getPendingForeignSourceRepository() {
        return m_pendingForeignSourceRepository;
    }
    
    public void setPendingForeignSourceRepository(final ForeignSourceRepository fsr) {
        m_pendingForeignSourceRepository = fsr;
    }

    public ForeignSourceRepository getDeployedForeignSourceRepository() {
        return m_deployedForeignSourceRepository;
    }
    
    public void setDeployedForeignSourceRepository(final ForeignSourceRepository fsr) {
        m_deployedForeignSourceRepository = fsr;
    }

    /**
     * <p>getActiveForeignSourceNames</p>
     *
     * @return a {@link java.util.Set} object.
     */
    @Override
    public Set<String> getActiveForeignSourceNames() {
        Set<String> fsNames = m_pendingForeignSourceRepository.getActiveForeignSourceNames();
        fsNames.addAll(m_deployedForeignSourceRepository.getActiveForeignSourceNames());
        return fsNames;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized Requisition importResourceRequisition(Resource resource) throws ForeignSourceRepositoryException {
        Requisition r = m_deployedForeignSourceRepository.importResourceRequisition(resource);
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

    /** {@inheritDoc} */
    @Override
    public synchronized void delete(ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        m_pendingForeignSourceRepository.delete(foreignSource);
        m_deployedForeignSourceRepository.delete(foreignSource);
    }

    /**
     * <p>delete</p>
     *
     * @param requisition a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    @Override
    public synchronized void delete(Requisition requisition) throws ForeignSourceRepositoryException {
        m_pendingForeignSourceRepository.delete(requisition);
        m_deployedForeignSourceRepository.delete(requisition);
    }

    /** {@inheritDoc} */
    @Override
    public ForeignSource getForeignSource(String foreignSourceName) throws ForeignSourceRepositoryException {
        return m_deployedForeignSourceRepository.getForeignSource(foreignSourceName);
    }

    /**
     * <p>getForeignSourceCount</p>
     *
     * @return a int.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    @Override
    public int getForeignSourceCount() throws ForeignSourceRepositoryException {
        return m_deployedForeignSourceRepository.getForeignSourceCount();
    }

    /**
     * <p>getForeignSources</p>
     *
     * @return a {@link java.util.Set} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    @Override
    public Set<ForeignSource> getForeignSources() throws ForeignSourceRepositoryException {
        return m_deployedForeignSourceRepository.getForeignSources();
    }

    /** {@inheritDoc} */
    @Override
    public Requisition getRequisition(String foreignSourceName) throws ForeignSourceRepositoryException {
        return m_deployedForeignSourceRepository.getRequisition(foreignSourceName);
    }

    /**
     * <p>getRequisition</p>
     *
     * @param foreignSource a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    @Override
    public Requisition getRequisition(ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        return m_deployedForeignSourceRepository.getRequisition(foreignSource);
    }

    /** {@inheritDoc} */
    @Override
    public URL getRequisitionURL(String foreignSource) {
        return m_deployedForeignSourceRepository.getRequisitionURL(foreignSource);
    }

    /**
     * <p>getRequisitions</p>
     *
     * @return a {@link java.util.Set} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    @Override
    public Set<Requisition> getRequisitions() throws ForeignSourceRepositoryException {
        return m_deployedForeignSourceRepository.getRequisitions();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void save(ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        m_pendingForeignSourceRepository.delete(foreignSource);
        m_deployedForeignSourceRepository.save(foreignSource);
    }

    /**
     * <p>save</p>
     *
     * @param requisition a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    @Override
    public synchronized void save(final Requisition requisition) throws ForeignSourceRepositoryException {
        final String foreignSource = requisition.getForeignSource();

        final URL pendingUrl = m_pendingForeignSourceRepository.getRequisitionURL(foreignSource);
        final File pendingFile = pendingUrl == null? null : new File(pendingUrl.getFile());

        /*
        final URL deployedUrl = m_deployedForeignSourceRepository.getRequisitionURL(foreignSource);
        final File deployedFile = deployedUrl == null? null : new File(deployedUrl.getFile());
        */

        final List<File> pendingSnapshots = RequisitionFileUtils.findSnapshots(m_pendingForeignSourceRepository, foreignSource);

        /* determine whether to delete the pending requisition */
        boolean deletePendingRequisition = true;
        if (pendingSnapshots.size() > 0) {
            for (final File snap : pendingSnapshots) {
                if (FileUtils.isFileNewer(pendingFile, snap)) {
                    // the pending file is newer than an in-process snapshot, don't delete it
                    deletePendingRequisition = false;
                    break;
                } else if (snap.lastModified() == pendingFile.lastModified() && snap.length() != pendingFile.length()) {
                    // if the dates are the same, but they're different lengths, err on the side of caution and leave the pending file
                    deletePendingRequisition = false;
                    break;
                }
            }
        }
        if (deletePendingRequisition) {
            m_pendingForeignSourceRepository.delete(requisition);
        }

        /* determine whether this requisition was imported from a snapshot, and if so, delete its snapshot file */
        RequisitionFileUtils.deleteResourceIfSnapshot(requisition);
 
        m_deployedForeignSourceRepository.save(requisition);
    }

    @Override
    public void flush() throws ForeignSourceRepositoryException {
        // Unnecessary, there is no caching/delayed writes in FusedForeignSourceRepository
    }

}
