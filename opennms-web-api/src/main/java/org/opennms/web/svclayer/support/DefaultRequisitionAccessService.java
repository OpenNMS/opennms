/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.svclayer.support;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.RequisitionFileUtils;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAssetCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategoryCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterfaceCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredServiceCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNodeCollection;
import org.opennms.web.api.RestUtils;
import org.opennms.web.svclayer.api.RequisitionAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessResourceFailureException;

import com.sun.jersey.core.util.MultivaluedMapImpl;

public class DefaultRequisitionAccessService implements RequisitionAccessService {
	
	private static final Logger LOG = LoggerFactory.getLogger(DefaultRequisitionAccessService.class);


    @Autowired
    @Qualifier("pending")
    private ForeignSourceRepository m_pendingForeignSourceRepository;

    @Autowired
    @Qualifier("deployed")
    private ForeignSourceRepository m_deployedForeignSourceRepository;

    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;

    private final ExecutorService m_executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(final Runnable r) {
            return new Thread(r, "Requisition-Accessor-Thread");
        }
    });

    static class RequisitionAccessor {
    	
    	private static final Logger LOG = LoggerFactory.getLogger(RequisitionAccessor.class);

        private final String m_foreignSource;

        private final ForeignSourceRepository m_pendingRepo;

        private final ForeignSourceRepository m_deployedRepo;

        private Requisition m_pending = null;

        public RequisitionAccessor(final String foreignSource, final ForeignSourceRepository pendingRepo, final ForeignSourceRepository deployedRepo) {
            m_foreignSource = foreignSource;
            m_pendingRepo = pendingRepo;
            m_deployedRepo = deployedRepo;
            m_pending = null;
        }

        public String getForeignSource() {
            return m_foreignSource;
        }

        public ForeignSourceRepository getPendingForeignSourceRepository() {
            return m_pendingRepo;
        }

        public ForeignSourceRepository getDeployedForeignSourceRepository() {
            return m_deployedRepo;
        }


        public Requisition getActiveRequisition(final boolean createIfMissing) {
            if (m_pending != null) {
                return m_pending;
            }

            final Requisition pending = RequisitionFileUtils.getLatestPendingOrSnapshotRequisition(getPendingForeignSourceRepository(), m_foreignSource);
            final Requisition deployed = getDeployedForeignSourceRepository().getRequisition(m_foreignSource);

            if (pending == null && deployed == null && createIfMissing) {
                return new Requisition(m_foreignSource);
            } else if (pending == null) {
                return deployed;
            } else if (deployed == null) {
                return pending;
            } else if (deployed.getDate().after(pending.getDate())) {
                // deployed is newer than pending
                return deployed;
            }
            return pending;
        }

        private void save(final Requisition requisition) {
            m_pending = requisition;
        }

        void addOrReplaceRequisition(final Requisition requisition) {
            if (requisition != null) {
                requisition.updateDateStamp();
                save(requisition);
            }
        }

        void addOrReplaceNode(final RequisitionNode node) {
            final Requisition req = getActiveRequisition(true);
            if (req != null) {
                req.updateDateStamp();
                req.putNode(node);
                save(req);
            }
        }

        void addOrReplaceInterface(final String foreignId, final RequisitionInterface iface) {
            final Requisition req = getActiveRequisition(true);
            if (req != null) {
                final RequisitionNode node = req.getNode(foreignId);
                if (node != null) {
                    req.updateDateStamp();
                    node.putInterface(iface);
                    save(req);
                }
            }
        }

        void addOrReplaceService(final String foreignId, final String ipAddress, final RequisitionMonitoredService service) {
            final Requisition req = getActiveRequisition(true);
            if (req != null) {
                final RequisitionNode node = req.getNode(foreignId);
                if (node != null) {
                    final RequisitionInterface iface = node.getInterface(ipAddress);
                    if (iface != null) {
                        req.updateDateStamp();
                        iface.putMonitoredService(service);
                        save(req);
                    }
                }
            }
        }

        void addOrReplaceNodeCategory(final String foreignId, final RequisitionCategory category) {
            final Requisition req = getActiveRequisition(true);
            if (req != null) {
                final RequisitionNode node = req.getNode(foreignId);
                if (node != null) {
                    req.updateDateStamp();
                    node.putCategory(category);
                    save(req);
                }
            }
        }

        void addOrReplaceNodeAssetParameter(final String foreignId, final RequisitionAsset asset) {
            final Requisition req = getActiveRequisition(true);
            if (req != null) {
                final RequisitionNode node = req.getNode(foreignId);
                if (node != null) {
                    req.updateDateStamp();
                    node.putAsset(asset);
                    save(req);
                }
            }
        }

        void updateRequisition(final MultivaluedMapImpl params) {
            final String foreignSource = m_foreignSource;
            LOG.debug("updateRequisition: Updating requisition with foreign source {}", foreignSource);
            if (params.isEmpty()) return;
            final Requisition req = getActiveRequisition(false);
            if (req != null) {
                req.updateDateStamp();
                RestUtils.setBeanProperties(req, params);
                save(req);
                LOG.debug("updateRequisition: Requisition with foreign source {} updated", foreignSource);
            }
        }

        void updateNode(final String foreignId, final MultivaluedMapImpl params) {
            final String foreignSource = m_foreignSource;
            LOG.debug("updateNode: Updating node with foreign source {} and foreign id {}", foreignSource, foreignId);
            if (params.isEmpty()) return;
            final Requisition req = getActiveRequisition(false);
            if (req != null) {
                final RequisitionNode node = req.getNode(foreignId);
                if (node != null) {
                    req.updateDateStamp();
                    RestUtils.setBeanProperties(node, params);
                    save(req);
                    LOG.debug("updateNode: Node with foreign source {} and foreign id {} updated", foreignSource, foreignId);
                }
            }
        }

        void updateInterface(final String foreignId, final String ipAddress, final MultivaluedMapImpl params) {
            final String foreignSource = m_foreignSource;
            LOG.debug("updateInterface: Updating interface {} on node {}/{}", ipAddress, foreignSource, foreignId);
            if (params.isEmpty()) return;
            final Requisition req = getActiveRequisition(false);
            if (req != null) {
                final RequisitionNode node = req.getNode(foreignId);
                if (node != null) {
                    final RequisitionInterface iface = node.getInterface(ipAddress);
                    if (iface != null) {
                        req.updateDateStamp();
                        RestUtils.setBeanProperties(iface, params);
                        save(req);
                        LOG.debug("updateInterface: Interface {} on node {}/{} updated", ipAddress, foreignSource, foreignId);
                    }
                }
            }
        }

        void deletePending() {
            LOG.debug("deletePendingRequisition: deleting pending requisition with foreign source {}", getForeignSource());
            Requisition req = getActiveRequisition(false);
            getPendingForeignSourceRepository().delete(req);
        }

        void deleteDeployed() {
            LOG.debug("deleteDeployedRequisition: deleting pending requisition with foreign source {}", getForeignSource());
            Requisition req = getActiveRequisition(false);
            getDeployedForeignSourceRepository().delete(req);
        }

        void deleteNode(final String foreignId) {
            LOG.debug("deleteNode: Deleting node {} from foreign source {}", foreignId, getForeignSource());
            final Requisition req = getActiveRequisition(false);
            if (req != null) {
                req.updateDateStamp();
                req.deleteNode(foreignId);
                save(req);
            }
        }

        void deleteInterface(final String foreignId, final String ipAddress) {
            LOG.debug("deleteInterface: Deleting interface {} from node {}/{}", ipAddress, getForeignSource(), foreignId);
            final Requisition req = getActiveRequisition(false);
            if (req != null) {
                final RequisitionNode node = req.getNode(foreignId);
                if (node != null) {
                    req.updateDateStamp();
                    node.deleteInterface(ipAddress);
                    save(req);
                }
            }
        }

        void deleteInterfaceService(final String foreignId, final String ipAddress, final String service) {
            LOG.debug("deleteInterfaceService: Deleting service {} from interface {} on node {}/{}", service, ipAddress, getForeignSource(), foreignId);
            final Requisition req = getActiveRequisition(false);
            if (req != null) {
                final RequisitionNode node = req.getNode(foreignId);
                if (node != null) {
                    final RequisitionInterface iface = node.getInterface(ipAddress);
                    if (iface != null) {
                        req.updateDateStamp();
                        iface.deleteMonitoredService(service);
                        save(req);
                    }
                }
            }
        }

        void deleteCategory(final String foreignId, final String category) {
            LOG.debug("deleteCategory: Deleting category {} from node {}/{}", category, getForeignSource(), foreignId);
            final Requisition req = getActiveRequisition(false);
            if (req != null) {
                final RequisitionNode node = req.getNode(foreignId);
                if (node != null) {
                    req.updateDateStamp();
                    node.deleteCategory(category);
                    save(req);
                }
            }
        }

        void deleteAssetParameter(final String foreignId, final String parameter) {
            LOG.debug("deleteAssetParameter: Deleting asset parameter {} from node {}/{}", parameter, getForeignSource(), foreignId);
            final Requisition req = getActiveRequisition(false);
            if (req != null) {
                final RequisitionNode node = req.getNode(foreignId);
                if (node != null) {
                    req.updateDateStamp();
                    node.deleteAsset(parameter);
                    save(req);
                }
            }
        }

        RequisitionAsset getAssetParameter(final String foreignId, final String parameter) {
            flush();

            final Requisition req = getActiveRequisition(false);
            final RequisitionNode node = req == null ? null : req.getNode(foreignId);
            return node == null ? null : node.getAsset(parameter);
        }

        RequisitionAssetCollection getAssetParameters(final String foreignId) {
            flush();

            final Requisition req = getActiveRequisition(false);
            final RequisitionNode node = req == null ? null : req.getNode(foreignId);
            return node == null ? null : new RequisitionAssetCollection(node.getAssets());
        }

        RequisitionCategory getCategory(final String foreignId, final String category) {
            flush();

            final Requisition req = getActiveRequisition(false);
            final RequisitionNode node = req == null ? null : req.getNode(foreignId);
            return node == null ? null : node.getCategory(category);
        }

        RequisitionCategoryCollection getCategories(final String foreignId) {
            flush();

            final Requisition req = getActiveRequisition(false);
            final RequisitionNode node = req == null ? null : req.getNode(foreignId);
            return node == null ? null : new RequisitionCategoryCollection(node.getCategories());
        }

        RequisitionInterface getInterfaceForNode(final String foreignId, final String ipAddress) {
            flush();

            final Requisition req = getActiveRequisition(false);
            final RequisitionNode node = req == null ? null : req.getNode(foreignId);
            return node == null ? null : node.getInterface(ipAddress);
        }

        RequisitionInterfaceCollection getInterfacesForNode(final String foreignId) {
            flush();

            final Requisition req = getActiveRequisition(false);
            final RequisitionNode node = req == null ? null : req.getNode(foreignId);
            return node == null ? null : new RequisitionInterfaceCollection(node.getInterfaces());
        }

        RequisitionNode getNode(final String foreignId) {
            flush();

            final Requisition req = getActiveRequisition(false);
            return req == null ? null : req.getNode(foreignId);
        }

        RequisitionNodeCollection getNodes() {
            flush();

            final Requisition req = getActiveRequisition(false);
            return req == null ? null : new RequisitionNodeCollection(req.getNodes());
        }

        Requisition getRequisition() {
            flush();

            return getActiveRequisition(false);
        }

        RequisitionMonitoredService getServiceForInterface(final String foreignId, final String ipAddress, final String service) {
            flush();

            final Requisition req = getActiveRequisition(false);
            final RequisitionNode node = req == null ? null : req.getNode(foreignId);
            final RequisitionInterface iface = node == null ? null : node.getInterface(ipAddress);

            return iface == null ? null : iface.getMonitoredService(service);
        }

        RequisitionMonitoredServiceCollection getServicesForInterface(final String foreignId, final String ipAddress) {
            flush();

            final Requisition req = getActiveRequisition(false);
            final RequisitionNode node = req == null ? null : req.getNode(foreignId);
            final RequisitionInterface iface = node == null ? null : node.getInterface(ipAddress);

            return iface == null ? null : new RequisitionMonitoredServiceCollection(iface.getMonitoredServices());
        }

        URL createSnapshot() throws MalformedURLException {
            flush();

            final Requisition pending = getPendingForeignSourceRepository().getRequisition(getForeignSource());
            final Requisition deployed = getDeployedForeignSourceRepository().getRequisition(getForeignSource());

            final URL activeUrl = pending == null || (deployed != null && deployed.getDateStamp().compare(pending.getDateStamp()) > -1)
                    ? getDeployedForeignSourceRepository().getRequisitionURL(getForeignSource())
                        : RequisitionFileUtils.createSnapshot(getPendingForeignSourceRepository(), getForeignSource(), pending.getDate()).toURI().toURL();

                    return activeUrl;
        }

        private void flush() {
            if (m_pending != null) {
                getPendingForeignSourceRepository().save(m_pending);
                m_pending = null;
            }

            getPendingForeignSourceRepository().flush();
            getDeployedForeignSourceRepository().flush();
        }

    }

    // This should only be accessed on the executor thread
    private final Map<String, RequisitionAccessor> m_accessors = new HashMap<String, RequisitionAccessor>();

    // should only called inside a submitted job on the executor thread
    private RequisitionAccessor getAccessor(final String foreignSource) {
        RequisitionAccessor accessor = m_accessors.get(foreignSource);
        if (accessor == null) {
            accessor = new RequisitionAccessor(foreignSource, m_pendingForeignSourceRepository, m_deployedForeignSourceRepository);
            m_accessors.put(foreignSource, accessor);
        }
        return accessor;
    }

    // should only be called inside a submitted job on the executor thread -
    // Used for operations that access
    // requisitions for multiple foreignSources
    public void flushAll() {
        for (final RequisitionAccessor accessor : m_accessors.values()) {
            accessor.flush();
        }
    }

    private ForeignSourceRepository getPendingForeignSourceRepository() {
        return m_pendingForeignSourceRepository;
    }

    private ForeignSourceRepository getDeployedForeignSourceRepository() {
        return m_deployedForeignSourceRepository;
    }

    private EventProxy getEventProxy() {
        return m_eventProxy;
    }

    private <T> T submitAndWait(final Callable<T> callable) {
        try {
            return m_executor.submit(callable).get();
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        } catch (final ExecutionException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw ((RuntimeException) e.getCause());
            } else {
                throw new RuntimeException(e.getCause());
            }
        }
    }

    private Future<?> submitWriteOp(final Runnable r) {
        return m_executor.submit(r);
    }

    // GLOBAL
    @Override
    public int getDeployedCount() {
        return submitAndWait(new Callable<Integer>() {
            @Override public Integer call() throws Exception {
                flushAll();
                return getDeployedForeignSourceRepository().getRequisitions().size();
            }
        });
    }

    // GLOBAL
    @Override
    public RequisitionCollection getDeployedRequisitions() {
        return submitAndWait(new Callable<RequisitionCollection>() {
            @Override public RequisitionCollection call() throws Exception {
                flushAll();
                return new RequisitionCollection(getDeployedForeignSourceRepository().getRequisitions());
            }
        });
    }

    // GLOBAL
    @Override
    public RequisitionCollection getRequisitions() {
        return submitAndWait(new Callable<RequisitionCollection>() {
            @Override public RequisitionCollection call() throws Exception {
                flushAll();

                final Set<Requisition> reqs = new TreeSet<Requisition>();
                final Set<String> fsNames = getPendingForeignSourceRepository().getActiveForeignSourceNames();
                fsNames.addAll(getDeployedForeignSourceRepository().getActiveForeignSourceNames());
                final Set<String> activeForeignSourceNames = fsNames;
                for (final String fsName : activeForeignSourceNames) {
                    final Requisition r = getAccessor(fsName).getActiveRequisition(false);
                    if (r != null) {
                        reqs.add(r);
                    }
                }
                return new RequisitionCollection(reqs);
            }

        });
    }

    // GLOBAL
    @Override
    public int getPendingCount() {
        return submitAndWait(new Callable<Integer>() {
            @Override public Integer call() throws Exception {
                flushAll();
                return getPendingForeignSourceRepository().getRequisitions().size();
            }
        });
    }

    @Override
    public Requisition getRequisition(final String foreignSource) {
        return submitAndWait(new Callable<Requisition>() {
            @Override public Requisition call() throws Exception {
                return getAccessor(foreignSource).getRequisition();
            }
        });
    }

    @Override
    public RequisitionNodeCollection getNodes(final String foreignSource) {
        return submitAndWait(new Callable<RequisitionNodeCollection>() {
            @Override public RequisitionNodeCollection call() throws Exception {
                return getAccessor(foreignSource).getNodes();
            }
        });
    }

    @Override
    public RequisitionNode getNode(final String foreignSource, final String foreignId) {
        return submitAndWait(new Callable<RequisitionNode>() {
            @Override public RequisitionNode call() throws Exception {
                return getAccessor(foreignSource).getNode(foreignId);
            }

        });
    }

    @Override
    public RequisitionInterfaceCollection getInterfacesForNode(final String foreignSource, final String foreignId) {
        return submitAndWait(new Callable<RequisitionInterfaceCollection>() {
            @Override public RequisitionInterfaceCollection call() throws Exception {
                return getAccessor(foreignSource).getInterfacesForNode(foreignId);
            }
        });
    }

    @Override
    public RequisitionInterface getInterfaceForNode(final String foreignSource, final String foreignId, final String ipAddress) {
        return submitAndWait(new Callable<RequisitionInterface>() {
            @Override public RequisitionInterface call() throws Exception {
                return getAccessor(foreignSource).getInterfaceForNode(foreignId, ipAddress);
            }
        });
    }

    @Override
    public RequisitionMonitoredServiceCollection getServicesForInterface(final String foreignSource, final String foreignId, final String ipAddress) {
        return submitAndWait(new Callable<RequisitionMonitoredServiceCollection>() {
            @Override public RequisitionMonitoredServiceCollection call() throws Exception {
                return getAccessor(foreignSource).getServicesForInterface(foreignId, ipAddress);
            }
        });
    }

    @Override
    public RequisitionMonitoredService getServiceForInterface(final String foreignSource, final String foreignId, final String ipAddress, final String service) {
        return submitAndWait(new Callable<RequisitionMonitoredService>() {
            @Override public RequisitionMonitoredService call() throws Exception {
                return getAccessor(foreignSource).getServiceForInterface(foreignId, ipAddress, service);
            }
        });
    }

    @Override
    public RequisitionCategoryCollection getCategories(final String foreignSource, final String foreignId) {
        return submitAndWait(new Callable<RequisitionCategoryCollection>() {
            @Override public RequisitionCategoryCollection call() throws Exception {
                return getAccessor(foreignSource).getCategories(foreignId);
            }
        });
    }

    @Override
    public RequisitionCategory getCategory(final String foreignSource, final String foreignId, final String category) {
        return submitAndWait(new Callable<RequisitionCategory>() {
            @Override public RequisitionCategory call() throws Exception {
                return getAccessor(foreignSource).getCategory(foreignId, category);
            }
        });
    }

    @Override
    public RequisitionAssetCollection getAssetParameters(final String foreignSource, final String foreignId) {
        return submitAndWait(new Callable<RequisitionAssetCollection>() {
            @Override public RequisitionAssetCollection call() throws Exception {
                return getAccessor(foreignSource).getAssetParameters(foreignId);
            }
        });
    }

    @Override
    public RequisitionAsset getAssetParameter(final String foreignSource, final String foreignId, final String parameter) {
        return submitAndWait(new Callable<RequisitionAsset>() {
            @Override public RequisitionAsset call() throws Exception {
                return getAccessor(foreignSource).getAssetParameter(foreignId, parameter);
            }
        });
    }

    @Override
    public void addOrReplaceRequisition(final Requisition requisition) {
        submitWriteOp(new Runnable() {
            @Override public void run() {
                getAccessor(requisition.getForeignSource()).addOrReplaceRequisition(requisition);
            }
        });
    }

    @Override
    public void addOrReplaceNode(final String foreignSource, final RequisitionNode node) {
        submitWriteOp(new Runnable() {
            @Override public void run() {
                getAccessor(foreignSource).addOrReplaceNode(node);
            }
        });
    }

    @Override
    public void addOrReplaceInterface(final String foreignSource, final String foreignId, final RequisitionInterface iface) {
        submitWriteOp(new Runnable() {
            @Override public void run() {
                getAccessor(foreignSource).addOrReplaceInterface(foreignId, iface);
            }
        });
    }

    @Override
    public void addOrReplaceService(final String foreignSource, final String foreignId, final String ipAddress, final RequisitionMonitoredService service) {
        submitWriteOp(new Runnable() {
            @Override public void run() {
                getAccessor(foreignSource).addOrReplaceService(foreignId, ipAddress, service);
            }
        });
    }

    @Override
    public void addOrReplaceNodeCategory(final String foreignSource, final String foreignId, final RequisitionCategory category) {
        submitWriteOp(new Runnable() {
            @Override public void run() {
                getAccessor(foreignSource).addOrReplaceNodeCategory(foreignId, category);
            }
        });
    }

    @Override
    public void addOrReplaceNodeAssetParameter(final String foreignSource, final String foreignId, final RequisitionAsset asset) {
        submitWriteOp(new Runnable() {
            @Override public void run() {
                getAccessor(foreignSource).addOrReplaceNodeAssetParameter(foreignId, asset);
            }
        });
    }

    @Override
    public void importRequisition(final String foreignSource, final String rescanExisting) {
        final URL activeUrl = createSnapshot(foreignSource);

        final String url = activeUrl.toString();
        LOG.debug("importRequisition: Sending import event with URL {}", url);
        final EventBuilder bldr = new EventBuilder(EventConstants.RELOAD_IMPORT_UEI, "Web");
        bldr.addParam(EventConstants.PARM_URL, url);
        if (rescanExisting != null) {
            bldr.addParam(EventConstants.PARM_IMPORT_RESCAN_EXISTING, rescanExisting);
        }

        try {
            getEventProxy().send(bldr.getEvent());
        } catch (final EventProxyException e) {
            throw new DataAccessResourceFailureException("Unable to send event to import group " + foreignSource, e);
        }

    }

    private URL createSnapshot(final String foreignSource) {
        return submitAndWait(new Callable<URL>() {
            @Override public URL call() throws Exception {
                return getAccessor(foreignSource).createSnapshot();
            }
        });
    }

    @Override
    public void updateRequisition(final String foreignSource, final MultivaluedMapImpl params) {
        submitWriteOp(new Runnable() {
            @Override public void run() {
                getAccessor(foreignSource).updateRequisition(params);
            }
        });
    }

    @Override
    public void updateNode(final String foreignSource, final String foreignId, final MultivaluedMapImpl params) {
        submitWriteOp(new Runnable() {
            @Override public void run() {
                getAccessor(foreignSource).updateNode(foreignId, params);
            }
        });
    }

    @Override
    public void updateInterface(final String foreignSource, final String foreignId, final String ipAddress, final MultivaluedMapImpl params) {
        submitWriteOp(new Runnable() {
            @Override public void run() {
                getAccessor(foreignSource).updateInterface(foreignId, ipAddress, params);
            }
        });
    }

    @Override
    public void deletePendingRequisition(final String foreignSource) {
        submitWriteOp(new Runnable() {
            @Override public void run() {
                getAccessor(foreignSource).deletePending();
            }
        });
    }

    @Override
    public void deleteDeployedRequisition(final String foreignSource) {
        submitWriteOp(new Runnable() {
            @Override public void run() {
                getAccessor(foreignSource).deleteDeployed();
            }
        });
    }

    @Override
    public void deleteNode(final String foreignSource, final String foreignId) {
        submitWriteOp(new Runnable() {
            @Override public void run() {
                getAccessor(foreignSource).deleteNode(foreignId);
            }
        });
    }

    @Override
    public void deleteInterface(final String foreignSource, final String foreignId, final String ipAddress) {
        submitWriteOp(new Runnable() {
            @Override public void run() {
                getAccessor(foreignSource).deleteInterface(foreignId, ipAddress);
            }
        });
    }

    @Override
    public void deleteInterfaceService(final String foreignSource, final String foreignId, final String ipAddress, final String service) {
        submitWriteOp(new Runnable() {
            @Override public void run() {
                getAccessor(foreignSource).deleteInterfaceService(foreignId, ipAddress, service);
            }
        });
    }

    @Override
    public void deleteCategory(final String foreignSource, final String foreignId, final String category) {
        submitWriteOp(new Runnable() {
            @Override public void run() {
                getAccessor(foreignSource).deleteCategory(foreignId, category);
            }
        });
    }

    @Override
    public void deleteAssetParameter(final String foreignSource, final String foreignId, final String parameter) {
        submitWriteOp(new Runnable() {
            @Override public void run() {
                getAccessor(foreignSource).deleteAssetParameter(foreignId, parameter);
            }
        });
    }

}
