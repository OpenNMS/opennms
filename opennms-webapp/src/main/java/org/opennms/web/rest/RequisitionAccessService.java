package org.opennms.web.rest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessResourceFailureException;

public class RequisitionAccessService {
	
    @Autowired
    @Qualifier("pending")
    private ForeignSourceRepository m_pendingForeignSourceRepository;
    
    @Autowired
    @Qualifier("deployed")
    private ForeignSourceRepository m_deployedForeignSourceRepository;
    
    @Autowired
    private EventProxy m_eventProxy;

    private final ExecutorService m_executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "Requisition-Accessor-Thread");
		}
    });
    
    static class RequisitionAccessor {
    	private final String m_foreignSource;
		private final ForeignSourceRepository m_pendingRepo;
		private final ForeignSourceRepository m_deployedRepo;
		private Requisition m_pending = null;
    	
    	public RequisitionAccessor(String foreignSource, ForeignSourceRepository pendingRepo, ForeignSourceRepository deployedRepo) {
    		m_foreignSource = foreignSource;
    		m_pendingRepo = pendingRepo;
    		m_deployedRepo = deployedRepo;
    		m_pending = null;
		}
    	
		public String getForeignSource() { return m_foreignSource; }
    	public ForeignSourceRepository getPendingForeignSourceRepository() { return m_pendingRepo; }
    	public ForeignSourceRepository getDeployedForeignSourceRepository() { return m_deployedRepo; }
    	public void debug(final String format, final Object... args) {
            LogUtils.debugf(this, format, args);
        }
        
        public void warn(Throwable t, String format, Object... args) {
        	LogUtils.warnf(this, t, format, args);
        }

    	public Requisition getActiveRequisition(boolean createIfMissing) {
    		
    		if (m_pending != null) {
    			return m_pending;
    		}
    		
    		Requisition pending = getPendingForeignSourceRepository().getRequisition(m_foreignSource);
    	    Requisition deployed = getDeployedForeignSourceRepository().getRequisition(m_foreignSource);
    	
    	    if (pending == null && deployed == null && createIfMissing) {
    	        return new Requisition(m_foreignSource);
    	    } else if (pending == null) {
    	        return deployed;
    	    } else if (deployed == null) {
    	        return pending;
    	    } else if (deployed.getDateStamp().compare(pending.getDateStamp()) > -1) {
    	        // deployed is newer than pending
    	        return deployed;
    	    }
    	    return pending;
    	}

		public void save(Requisition requisition) {
			m_pending = requisition;
		}

		void addOrReplaceNode(final RequisitionNode node) {
			Requisition req = getActiveRequisition(true);
			if (req != null) {
			    req.putNode(node);
			    save(req);
			}
		}

		void addOrReplaceInterface(String foreignId, RequisitionInterface iface) {
			Requisition req = getActiveRequisition(true);
			if (req != null) {
				final RequisitionNode node = req.getNode(foreignId);
				if (node != null) {
					node.putInterface(iface);
					save(req);
				}
			}
		}

		void addOrReplaceService(final String foreignId, final String ipAddress, final RequisitionMonitoredService service) {
			Requisition req = getActiveRequisition(true);
			if (req != null) {
				final RequisitionNode node = req.getNode(foreignId);
				if (node != null) {
					RequisitionInterface iface = node.getInterface(ipAddress);
					if (iface != null) {
						iface.putMonitoredService(service);
						save(req);
					}
				}
			}
		}

		void addOrReplaceNodeCategory(final String foreignId, final RequisitionCategory category) {
			Requisition req = getActiveRequisition(true);
			if (req != null) {
				final RequisitionNode node = req.getNode(foreignId);
				if (node != null) {
					node.putCategory(category);
					save(req);
				}
			}
		}

		void addOrReplaceNodeAssetParameter(final String foreignId, final RequisitionAsset asset) {
			Requisition req = getActiveRequisition(true);
			if (req != null) {
				final RequisitionNode node = req.getNode(foreignId);
				if (node != null) {
					node.putAsset(asset);
					save(req);
				}
			}
		}

		void updateRequisition(final MultivaluedMapImpl params) {
			String foreignSource = m_foreignSource;
			debug("updateRequisition: Updating requisition with foreign source %s", foreignSource);
			Requisition req = getActiveRequisition(false);
			if (req != null) {
				RestUtils.setBeanProperties(req, params);
				debug("updateRequisition: Requisition with foreign source %s updated", foreignSource);
				save(req);
			}
		}

		void updateNode(String foreignId, MultivaluedMapImpl params) {
			String foreignSource = m_foreignSource;
			debug("updateNode: Updating node with foreign source %s and foreign id %s", foreignSource, foreignId);
			final Requisition req = getActiveRequisition(false);
			if (req != null) {
				final RequisitionNode node = req.getNode(foreignId);
				if (node != null) {
					RestUtils.setBeanProperties(node, params);
					debug("updateNode: Node with foreign source %s and foreign id %s updated", foreignSource, foreignId);
					save(req);
				}
			}
		}

		void updateInterface(String foreignId, String ipAddress, MultivaluedMapImpl params) {
			String foreignSource = m_foreignSource;
			debug("updateInterface: Updating interface %s on node %s/%s", ipAddress, foreignSource, foreignId);
			final Requisition req = getActiveRequisition(false);
			if (req != null) {
				final RequisitionNode node = req.getNode(foreignId);
				if (node != null) {
					RequisitionInterface iface = node.getInterface(ipAddress);
					if (iface != null) {
						RestUtils.setBeanProperties(iface, params);
						debug("updateInterface: Interface %s on node %s/%s updated", ipAddress, foreignSource, foreignId);
						save(req);
					}
				}
			}
		}

		void deletePending() {
			debug("deletePendingRequisition: deleting pending requisition with foreign source %s", getForeignSource());
			Requisition req = getActiveRequisition(false);
			getPendingForeignSourceRepository().delete(req);
		}

		void deleteDeployed() {
			debug("deleteDeployedRequisition: deleting pending requisition with foreign source %s", getForeignSource());
			Requisition req = getActiveRequisition(false);
			getDeployedForeignSourceRepository().delete(req);
		}

		void deleteNode(String foreignId) {
			debug("deleteNode: Deleting node %s from foreign source %s", foreignId, getForeignSource());
			final Requisition req = getActiveRequisition(false);
			if (req != null) {
				req.deleteNode(foreignId);
				save(req);
			}
		}

		void deleteInterface(String foreignId, String ipAddress) {
			debug("deleteInterface: Deleting interface %s from node %s/%s", ipAddress, getForeignSource(), foreignId);
			Requisition req = getActiveRequisition(false);
			if (req != null) {
				final RequisitionNode node = req.getNode(foreignId);
				if (node != null) {
					node.deleteInterface(ipAddress);
					save(req);
				}
			}
		}

		void deleteInterfaceService(String foreignId, String ipAddress, String service) {
			debug("deleteInterfaceService: Deleting service %s from interface %s on node %s/%s", service, ipAddress, getForeignSource(), foreignId);
			final Requisition req = getActiveRequisition(false);
			if (req != null) {
				final RequisitionNode node = req.getNode(foreignId);
				if (node != null) {
					RequisitionInterface iface = node.getInterface(ipAddress);
					if (iface != null) {
						iface.deleteMonitoredService(service);
						save(req);
					}
				}
			}
		}

		void deleteCategory(String foreignId, String category) {
			debug("deleteCategory: Deleting category %s from node %s/%s", category, getForeignSource(), foreignId);
			Requisition req = getActiveRequisition(false);
			if (req != null) {
				final RequisitionNode node = req.getNode(foreignId);
				if (node != null) {
					node.deleteCategory(category);
					save(req);
				}
			}
		}

		void deleteAssetParameter(String foreignId, String parameter) {
			debug("deleteAssetParameter: Deleting asset parameter %s from node %s/%s", parameter, getForeignSource(), foreignId);
			final Requisition req = getActiveRequisition(false);
			if (req != null) {
				final RequisitionNode node = req.getNode(foreignId);
				if (node != null) {
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
		
			Requisition req = getActiveRequisition(false);
			RequisitionNode node = req == null ? null : req.getNode(foreignId);
		
			return node == null ? null : node.getInterface(ipAddress);
		}

		RequisitionInterfaceCollection getInterfacesForNode(final String foreignId) {
			flush();
		
			Requisition req = getActiveRequisition(false);
			RequisitionNode node = req == null ? null : req.getNode(foreignId);
		
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
			return req == null ? null: new RequisitionNodeCollection(req.getNodes());
		}

		Requisition getRequisition() {
			flush();
		
			return getActiveRequisition(false);
		}

		RequisitionMonitoredService getServiceForInterface(final String foreignId, final String ipAddress, final String service) {
			flush();
		
			Requisition req = getActiveRequisition(false);
		    RequisitionNode node = req == null ? null : req.getNode(foreignId);
		    RequisitionInterface iface =  node == null ? null : node.getInterface(ipAddress);
		
		    return iface == null ? null : iface.getMonitoredService(service);
		}

		RequisitionMonitoredServiceCollection getServicesForInterface(final String foreignId, final String ipAddress) {
			flush();
		
			Requisition req = getActiveRequisition(false);
		    RequisitionNode node = req == null ? null : req.getNode(foreignId);
		    RequisitionInterface iface =  node == null ? null : node.getInterface(ipAddress);
		
		    return iface == null ? null : new RequisitionMonitoredServiceCollection(iface.getMonitoredServices());
		}

		URL createSnapshot() throws MalformedURLException {
			flush();
		
			Requisition pending = getPendingForeignSourceRepository().getRequisition(getForeignSource());
			Requisition deployed = getDeployedForeignSourceRepository().getRequisition(getForeignSource());
		
			URL activeUrl = pending == null || (deployed != null && deployed.getDateStamp().compare(pending.getDateStamp()) > -1) 
					? getDeployedForeignSourceRepository().getRequisitionURL(getForeignSource()) 
				    : RequisitionFileUtils.createSnapshot(getPendingForeignSourceRepository(), getForeignSource()).toURI().toURL();
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
    private RequisitionAccessor getAccessor(String foreignSource) {
    	RequisitionAccessor accessor = m_accessors.get(foreignSource);
    	if (accessor == null) {
    		accessor = new RequisitionAccessor(foreignSource, m_pendingForeignSourceRepository, m_deployedForeignSourceRepository);
    		m_accessors.put(foreignSource, accessor);
    	}
    	return accessor;
    }
    
    // should only be called inside a submitted job on the executor thread - Used for operations that access
    // requisitions for multiple foreignSources
	private void flushAll() {
		for(RequisitionAccessor accessor : m_accessors.values()) {
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
	
	private <T> T submitAndWait(Callable<T> callable) {
		try {
			
			return m_executor.submit(callable).get();
			
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			if (e.getCause() instanceof RuntimeException) {
				throw ((RuntimeException)e.getCause());
			} else {
				throw new RuntimeException(e.getCause());
			}
		}
		
	}
	
	private Future<?> submitWriteOp(Runnable r) {
		return m_executor.submit(r);
	}
	
	// GLOBAL
	public int getDeployedCount() {
		return submitAndWait(new Callable<Integer>() {

			@Override
			public Integer call() throws Exception {
				flushAll();
				return getDeployedForeignSourceRepository().getRequisitions().size();
			}
			
		});
	}

	// GLOBAL
	public RequisitionCollection getDeployedRequisitions() {
		return submitAndWait(new Callable<RequisitionCollection>() {

			@Override
			public RequisitionCollection call() throws Exception {
				flushAll();
		        return new RequisitionCollection(getDeployedForeignSourceRepository().getRequisitions());
			}
			
		});
	}

	// GLOBAL
	public RequisitionCollection getRequisitions() {
		return submitAndWait(new Callable<RequisitionCollection>() {

			@Override
			public RequisitionCollection call() throws Exception {
				flushAll();

				final Set<Requisition> reqs = new TreeSet<Requisition>();
				Set<String> fsNames = getPendingForeignSourceRepository().getActiveForeignSourceNames();
				fsNames.addAll(getDeployedForeignSourceRepository().getActiveForeignSourceNames());
				Set<String> activeForeignSourceNames = fsNames;
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
	public int getPendingCount() {
		return submitAndWait(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				flushAll();
		        return getPendingForeignSourceRepository().getRequisitions().size();
			}

		});
	}

	public Requisition getRequisition(final String foreignSource) {
		return submitAndWait(new Callable<Requisition>() {

			@Override
			public Requisition call() throws Exception {
				return getAccessor(foreignSource).getRequisition();
			}

		});
	}

	public RequisitionNodeCollection getNodes(final String foreignSource) {

		return submitAndWait(new Callable<RequisitionNodeCollection>() {

			@Override
			public RequisitionNodeCollection call() throws Exception {
				return getAccessor(foreignSource).getNodes();
			}
			
		});
	}

	public RequisitionNode getNode(final String foreignSource, final String foreignId) {
		
		return submitAndWait(new Callable<RequisitionNode>() {

			@Override
			public RequisitionNode call() throws Exception {
		        RequisitionAccessor accessor = getAccessor(foreignSource);
		        return accessor.getNode(foreignId);
			}
			
		});
	}

	public RequisitionInterfaceCollection getInterfacesForNode(final String foreignSource, final String foreignId) {
		return submitAndWait(new Callable<RequisitionInterfaceCollection>() {

			@Override
			public RequisitionInterfaceCollection call() throws Exception {
				return getAccessor(foreignSource).getInterfacesForNode(foreignId);
			}
			
		});
	}

	public RequisitionInterface getInterfaceForNode(final String foreignSource, final String foreignId, final String ipAddress) {
	
		return submitAndWait(new Callable<RequisitionInterface>() {

			@Override
			public RequisitionInterface call() throws Exception {
				return getAccessor(foreignSource).getInterfaceForNode(foreignId, ipAddress);
			}
			
		});
	}

	public RequisitionMonitoredServiceCollection getServicesForInterface(final String foreignSource, final String foreignId, final String ipAddress) {
		return submitAndWait(new Callable<RequisitionMonitoredServiceCollection>() {

			@Override
			public RequisitionMonitoredServiceCollection call() throws Exception {
		        return getAccessor(foreignSource).getServicesForInterface(foreignId, ipAddress);
			}
		});
	}

	public RequisitionMonitoredService getServiceForInterface(final String foreignSource, final String foreignId, final String ipAddress, final String service) {
		return submitAndWait(new Callable<RequisitionMonitoredService>() {

			@Override
			public RequisitionMonitoredService call() throws Exception {
		        return getAccessor(foreignSource).getServiceForInterface(foreignId, ipAddress, service);
			}
			
		});
	}

	public RequisitionCategoryCollection getCategories(final String foreignSource, final String foreignId) {
		return submitAndWait(new Callable<RequisitionCategoryCollection>() {

			@Override
			public RequisitionCategoryCollection call() throws Exception {
		        return getAccessor(foreignSource).getCategories(foreignId);
			}
		});
	}

	public RequisitionCategory getCategory(final String foreignSource, final String foreignId, final String category) {
		return submitAndWait(new Callable<RequisitionCategory>() {

			@Override
			public RequisitionCategory call() throws Exception {
				return getAccessor(foreignSource).getCategory(foreignId, category);
			}
		});
	}

	public RequisitionAssetCollection getAssetParameters(final String foreignSource, final String foreignId) {
		return submitAndWait(new Callable<RequisitionAssetCollection>() {

			@Override
			public RequisitionAssetCollection call() throws Exception {
		        return getAccessor(foreignSource).getAssetParameters(foreignId);
			}
		});
	}

	public RequisitionAsset getAssetParameter(final String foreignSource, final String foreignId, final String parameter) {
		return submitAndWait(new Callable<RequisitionAsset>() {

			@Override
			public RequisitionAsset call() throws Exception {
		        return getAccessor(foreignSource).getAssetParameter(foreignId, parameter);
			}
			
		});
	}

	public void addOrReplaceRequisition(final Requisition requisition) {
		submitWriteOp(new Runnable() {

			@Override
			public void run() {
				getAccessor(requisition.getForeignSource()).save(requisition);
			}
			
		});
	}

	public void addOrReplaceNode(final String foreignSource, final RequisitionNode node) {
		submitWriteOp(new Runnable() {

			@Override
			public void run() {
		        getAccessor(foreignSource).addOrReplaceNode(node);
			}
			
		});
	}

	public void addOrReplaceInterface(final String foreignSource, final String foreignId, final RequisitionInterface iface) {
		submitWriteOp(new Runnable() {

			@Override
			public void run() {
		    	getAccessor(foreignSource).addOrReplaceInterface(foreignId, iface);
			}
			
		});
	}

	public void addOrReplaceService(final String foreignSource, final String foreignId, final String ipAddress, final RequisitionMonitoredService service) {
		submitWriteOp(new Runnable() {

			@Override
			public void run() {
		    	getAccessor(foreignSource).addOrReplaceService(foreignId, ipAddress, service);
			}
			
		});
	}

	public void addOrReplaceNodeCategory(final String foreignSource, final String foreignId, final RequisitionCategory category) {
		submitWriteOp(new Runnable() {

			@Override
			public void run() {
		    	getAccessor(foreignSource).addOrReplaceNodeCategory(foreignId, category);
			}
			
		});
	}

	public void addOrReplaceNodeAssetParameter(final String foreignSource, final String foreignId, final RequisitionAsset asset) {
		submitWriteOp(new Runnable() {

			@Override
			public void run() {
				getAccessor(foreignSource).addOrReplaceNodeAssetParameter(foreignId, asset);
			}
			
		});
	}

	public void importRequisition(final String foreignSource, final Boolean rescanExisting) {
		URL activeUrl = createSnapshot(foreignSource);

		final String url = activeUrl.toString();
		debug("importRequisition: Sending import event with URL %s", url);
		final EventBuilder bldr = new EventBuilder(EventConstants.RELOAD_IMPORT_UEI, "Web");
		bldr.addParam(EventConstants.PARM_URL, url);
		if (rescanExisting != null) {
			bldr.addParam(EventConstants.PARM_IMPORT_RESCAN_EXISTING, rescanExisting);
		}

		try {
			getEventProxy().send(bldr.getEvent());
		} catch (final EventProxyException e) {
			throw new DataAccessResourceFailureException("Unable to send event to import group "+foreignSource, e);
		}

	}


	private URL createSnapshot(final String foreignSource) {
		return submitAndWait(new Callable<URL>() {

			@Override
			public URL call() throws Exception {
				return getAccessor(foreignSource).createSnapshot();
			}
			
		});
	}

	public void updateRequisition(final String foreignSource, final MultivaluedMapImpl params) {
		submitWriteOp(new Runnable() {

			@Override
			public void run() {
		    	getAccessor(foreignSource).updateRequisition(params);
			}
			
		});
	}

	public void updateNode(final String foreignSource, final String foreignId, final MultivaluedMapImpl params) {
		submitWriteOp(new Runnable() {

			@Override
			public void run() {
		    	getAccessor(foreignSource).updateNode(foreignId, params);
			}
			
		});
	}

	public void updateInterface(final String foreignSource, final String foreignId, final String ipAddress, final MultivaluedMapImpl params) {
		submitWriteOp(new Runnable() {

			@Override
			public void run() {
		    	getAccessor(foreignSource).updateInterface(foreignId, ipAddress, params);
			}
			
		});
	}

	public void deletePendingRequisition(final String foreignSource) {
		submitWriteOp(new Runnable() {

			@Override
			public void run() {
		        getAccessor(foreignSource).deletePending();
			}
			
		});
	}

	public void deleteDeployedRequisition(final String foreignSource) {
		submitWriteOp(new Runnable() {

			@Override
			public void run() {
		        getAccessor(foreignSource).deleteDeployed();
			}
			
		});
	}

	public void deleteNode(final String foreignSource, final String foreignId) {
		submitWriteOp(new Runnable() {

			@Override
			public void run() {
		    	getAccessor(foreignSource).deleteNode(foreignId);
			}
			
		});
	}

	public void deleteInterface(final String foreignSource, final String foreignId, final String ipAddress) {
		submitWriteOp(new Runnable() {

			@Override
			public void run() {
		    	getAccessor(foreignSource).deleteInterface(foreignId, ipAddress);
			}
			
		});
	}

	public void deleteInterfaceService(final String foreignSource, final String foreignId, final String ipAddress, final String service) {
		submitWriteOp(new Runnable() {

			@Override
			public void run() {
		    	getAccessor(foreignSource).deleteInterfaceService(foreignId, ipAddress, service);
			}
			
		});
	}

	public void deleteCategory(final String foreignSource, final String foreignId, final String category) {
		submitWriteOp(new Runnable() {

			@Override
			public void run() {
		    	getAccessor(foreignSource).deleteCategory(foreignId, category);
			}
			
		});
	}

	public void deleteAssetParameter(final String foreignSource, final String foreignId, final String parameter) {
		submitWriteOp(new Runnable() {

			@Override
			public void run() {
		    	getAccessor(foreignSource).deleteAssetParameter(foreignId, parameter);
			}
			
		});
	}

	void debug(final String format, final Object... args) {
        LogUtils.debugf(this, format, args);
    }
    
    void warn(Throwable t, String format, Object... args) {
    	LogUtils.warnf(this, t, format, args);
    }

}
