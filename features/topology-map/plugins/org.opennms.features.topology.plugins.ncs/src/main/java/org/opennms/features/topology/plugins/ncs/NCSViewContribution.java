package org.opennms.features.topology.plugins.ncs;

import com.google.common.collect.Lists;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.Resource;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Tree;
import org.opennms.features.topology.api.*;
import org.opennms.features.topology.api.support.AbstractSearchSelectionOperation;
import org.opennms.features.topology.api.support.FilterableHierarchicalContainer;
import org.opennms.features.topology.api.topo.*;
import org.opennms.features.topology.plugins.ncs.internal.NCSCriteriaServiceManager;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;
import org.opennms.osgi.VaadinApplicationContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class NCSViewContribution implements IViewContribution, SearchProvider {
	
	private NCSComponentRepository m_ncsComponentRepository;
	private NCSEdgeProvider m_ncsEdgeProvider;
    private NCSCriteriaServiceManager m_serviceManager;
    private int m_serviceCount = 0;
    NCSServiceContainer m_container;

    public void setNcsComponentRepository(NCSComponentRepository ncsComponentRepository) {
		m_ncsComponentRepository = ncsComponentRepository;
        m_container = new NCSServiceContainer(m_ncsComponentRepository);
	}

    @Override
    public Component getView(final VaadinApplicationContext applicationContext, final WidgetContext widgetContext) {

        final Tree tree = new Tree("Services", new FilterableHierarchicalContainer(m_container));
		tree.setMultiSelect(true);
		tree.setImmediate(true);
		tree.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		tree.setItemCaptionPropertyId("name");
		tree.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = -7443836886894714291L;
			
			public void valueChange(ValueChangeEvent event) {
				Collection<Long> selectedIds = new HashSet<Long>( (Collection<Long>) event.getProperty().getValue() );
				
				Collection<Long> nonSelectableIds = new ArrayList<Long>();
				
				for(Long id : selectedIds) {
				    boolean isRoot = (Boolean) tree.getItem(id).getItemProperty("isRoot").getValue();
				    if(id < 0 && isRoot) {
				        nonSelectableIds.add(id);
				    }
				}
				selectedIds.removeAll(nonSelectableIds);
				for(Long id : nonSelectableIds) {
				    tree.unselect(id);
				}
				
				Criteria criteria = NCSEdgeProvider.createCriteria(selectedIds);
				
				m_serviceManager.registerCriteria(criteria, widgetContext.getGraphContainer().getSessionId());
                if(m_serviceManager.isCriteriaRegistered("ncsPath", widgetContext.getGraphContainer().getSessionId())) {
                    m_serviceManager.unregisterCriteria("ncsPath", widgetContext.getGraphContainer().getSessionId());
                }
				selectVerticesForEdge(criteria, widgetContext.getGraphContainer().getSelectionManager());
			}
		});
		
		
		
		m_serviceManager.addCriteriaServiceListener(new ServiceListener() {

            @Override
            public void serviceChanged(ServiceEvent event) {
                if(event.getType() == ServiceEvent.UNREGISTERING) {
                    //tree.setValue( tree.getNullSelectionItemId() );
                }
            }
            
		}, widgetContext.getGraphContainer().getSessionId(), "ncs");
		
		return tree;
	}

    protected void selectVerticesForEdge(Criteria criteria, SelectionManager selectionManager) {
	    List<VertexRef> vertexRefs = new ArrayList<VertexRef>();
	    List<Edge> edges = m_ncsEdgeProvider.getEdges(criteria);
	    for(Edge ncsEdge : edges) {
	        vertexRefs.add(ncsEdge.getSource().getVertex());
	        vertexRefs.add(ncsEdge.getTarget().getVertex());
	    }
	    selectionManager.setSelectedVertexRefs(vertexRefs);
	    
    }

    @Override
	public String getTitle() {
		return "Services";
	}

	@Override
	public Resource getIcon() {
		return null;
	}
	
	public void setNcsEdgeProvider(NCSEdgeProvider ncsEdgeProvider) {
        m_ncsEdgeProvider = ncsEdgeProvider;
    }
	
	public void setNcsCriteriaServiceManager(NCSCriteriaServiceManager manager) {
	    m_serviceManager = manager;
	}

    @Override
    public List<VertexRef> query(SearchQuery searchQuery) {
        List<VertexRef> vertexRefs = Lists.newArrayList();

        List<NCSComponent> components = m_ncsComponentRepository.findByType("Service");
        for (NCSComponent component : components) {
            VertexRef vRef = new AbstractVertexRef("ncs service", String.valueOf(component.getId()), component.getName());
            if(searchQuery.matches(vRef)) {
                vertexRefs.add(vRef);
            }

        }
        return vertexRefs;
    }

    @Override
    public AbstractSearchSelectionOperation getSelectionOperation() {
        return new AbstractSearchSelectionOperation() {
            @Override
            public Undoer execute(List<VertexRef> targets, OperationContext operationContext) {

                if(targets != null && targets.size() > 0){
                    Criteria criteria = NCSEdgeProvider.createCriteria(Lists.newArrayList(Long.parseLong(targets.get(0).getId())));

                    m_serviceManager.registerCriteria(criteria, operationContext.getGraphContainer().getSessionId());
                    if(m_serviceManager.isCriteriaRegistered("ncsPath", operationContext.getGraphContainer().getSessionId())) {
                        m_serviceManager.unregisterCriteria("ncsPath", operationContext.getGraphContainer().getSessionId());
                    }
                    selectVerticesForEdge(criteria, operationContext.getGraphContainer().getSelectionManager());
                }

                return null;
            }
        };
    }

    @Override
    public boolean supportsPrefix(String searchPrefix) {
        return searchPrefix.equals("service=");
    }

}
