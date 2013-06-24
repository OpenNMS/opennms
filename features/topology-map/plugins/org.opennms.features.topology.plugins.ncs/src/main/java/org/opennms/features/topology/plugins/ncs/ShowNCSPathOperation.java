package org.opennms.features.topology.plugins.ncs;

import java.net.ConnectException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.camel.component.http.HttpOperationFailedException;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.ncs.NCSEdgeProvider.NCSServiceCriteria;
import org.opennms.features.topology.plugins.ncs.NCSPathEdgeProvider.NCSServicePathCriteria;
import org.opennms.features.topology.plugins.ncs.internal.NCSCriteriaServiceManager;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.FormFieldFactory;
import com.vaadin.ui.Select;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

public class ShowNCSPathOperation implements Operation {
    
    
    private NCSEdgeProvider m_ncsEdgeProvider;
    private NCSPathProviderService m_ncsPathProvider;
    private NCSComponentRepository m_dao;
    private NodeDao m_nodeDao;
    private NCSServiceCriteria m_storedCriteria;
    private NCSCriteriaServiceManager m_serviceManager;
    
    @Override
    public Undoer execute(List<VertexRef> targets, final OperationContext operationContext) {
        //Get the current NCS criteria from here you can get the foreignIds foreignSource and deviceA and Z
        final NCSServiceCriteria criteria = (NCSServiceCriteria) operationContext.getGraphContainer().getCriteria(m_ncsEdgeProvider.getEdgeNamespace());
        if(criteria.size() > 0) {
            m_storedCriteria = criteria;
        }
        
        
        final VertexRef defaultVertRef = targets.get(0);
        final SelectionManager selectionManager = operationContext.getGraphContainer().getSelectionManager();
        final Collection<VertexRef> vertexRefs = getVertexRefsForNCSService(m_storedCriteria); //selectionManager.getSelectedVertexRefs();
        
        final Window mainWindow = operationContext.getMainWindow();
        
        final Window ncsPathPrompt = new Window("Show NCS Path");
        ncsPathPrompt.setModal(true);
        ncsPathPrompt.setResizable(false);
        ncsPathPrompt.setWidth("300px");
        ncsPathPrompt.setHeight("220px");
        
        //Items used in form field
        final PropertysetItem item = new PropertysetItem();
        item.addItemProperty("Device A", new ObjectProperty<String>("", String.class));
        item.addItemProperty("Device Z", new ObjectProperty<String>("", String.class));
        
        
        FormFieldFactory fieldFactory = new FormFieldFactory() {
            private static final long serialVersionUID = 1L;

            @Override
            public Field createField(Item item, Object propertyId, Component uiContext) {
                String pid = (String) propertyId;

                Select select = new Select();
                for(VertexRef vertRef : vertexRefs) {
                    select.addItem(vertRef.getId());
                    select.setItemCaption(vertRef.getId(), vertRef.getLabel());
                }
                select.setNewItemsAllowed(false);
                select.setNullSelectionAllowed(false);
                select.setImmediate(true);
                select.setScrollToSelectedItem(true);
                
                if("Device A".equals(pid)) {
                    select.setCaption("Device A");
                } else {
                    select.setCaption("Device Z");
                    
                }
                
                return select;
            }
            
        };
        
        final Form promptForm = new Form() {


            @Override
            public void commit() {
                String deviceA = (String)getField("Device A").getValue();
                String deviceZ = (String)getField("Device Z").getValue();
                
                OnmsNode nodeA = m_nodeDao.get(Integer.valueOf(deviceA));
                String deviceANodeForeignId = nodeA.getForeignId();
                //Use nodeA's foreignSource, deviceZ should have the same foreignSource. It's an assumption
                // which might need to changed in the future. Didn't want to hard code it it "space" if they
                // change it in the future
                String nodeForeignSource = nodeA.getForeignSource();
                
                String deviceZNodeForeignId = m_nodeDao.get(Integer.valueOf(deviceZ)).getForeignId();
                
                NCSComponent ncsComponent = m_dao.get(m_storedCriteria.get(0));
                String foreignSource = ncsComponent.getForeignSource();
                String foreignId = ncsComponent.getForeignId();
                String serviceName = ncsComponent.getName();
                try {
                    NCSServicePath path = getNcsPathProvider().getPath(foreignId, foreignSource, deviceANodeForeignId, deviceZNodeForeignId, nodeForeignSource, serviceName);
                    
                    if(path.getStatusCode() == 200) {
                        NCSServicePathCriteria criteria = new NCSServicePathCriteria(path.getEdges());
                        m_serviceManager.registerCriteria(criteria, operationContext.getGraphContainer().getSessionId());
                    
                        //Select only the vertices in the path
                        selectionManager.setSelectedVertexRefs(path.getVertices());
                    } else {
                        LoggerFactory.getLogger(this.getClass()).warn("An error occured while retrieving the NCS Path, Juniper NetworkAppsApi send error code: " + path.getStatusCode());
                        mainWindow.showNotification("An error occurred while retrieving the NCS Path\nStatus Code: " + path.getStatusCode(), Notification.TYPE_ERROR_MESSAGE);
                    }
                    
                }  catch (Exception e) {
                    
                    if(e.getCause() instanceof ConnectException ) {
                        LoggerFactory.getLogger(this.getClass()).warn("Connection Exception Occurred while retreiving path {}", e);
                        mainWindow.showNotification("Connection Refused when attempting to reach the NetworkAppsApi", Notification.TYPE_ERROR_MESSAGE);
                    } else if(e.getCause() instanceof HttpOperationFailedException) {
                        HttpOperationFailedException httpException = (HttpOperationFailedException) e.getCause();
                        if(httpException.getStatusCode() == 401) {
                            LoggerFactory.getLogger(this.getClass()).warn("Authentication error when connecting to NetworkAppsApi {}", httpException);
                            mainWindow.showNotification("Authentication error when connecting to NetworkAppsApi, please check the username and password", Notification.TYPE_ERROR_MESSAGE);
                        } else {
                            LoggerFactory.getLogger(this.getClass()).warn("An error occured while retrieving the NCS Path {}", httpException);
                            mainWindow.showNotification("An error occurred while retrieving the NCS Path\n" + httpException.getMessage(), Notification.TYPE_ERROR_MESSAGE);
                        }
                    } else {
                    
                        LoggerFactory.getLogger(this.getClass()).warn("Exception Occurred while retreiving path {}", e);
                        mainWindow.showNotification("An error occurred while calculating the path please check the karaf.log file for the exception: \n" + e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
                    }
                }
            }
            
        };
        
        promptForm.setWriteThrough(false);
        promptForm.setFormFieldFactory(fieldFactory);
        promptForm.setItemDataSource(item);
        
        Button ok = new Button("OK");
        ok.addListener(new ClickListener() {

            private static final long serialVersionUID = -2742886456007926688L;

            @Override
            public void buttonClick(ClickEvent event) {
                promptForm.commit();
                mainWindow.removeWindow(ncsPathPrompt);
            }
            
        });
        promptForm.getFooter().addComponent(ok);
        
        Button cancel = new Button("Cancel");
        cancel.addListener(new ClickListener(){
            private static final long serialVersionUID = -9026067481179449095L;

            @Override
            public void buttonClick(ClickEvent event) {
                mainWindow.removeWindow(ncsPathPrompt);
            }
            
        });
        promptForm.getFooter().addComponent(cancel);
        ncsPathPrompt.addComponent(promptForm);
        mainWindow.addWindow(ncsPathPrompt);
        promptForm.getField("Device A").setValue(defaultVertRef.getId());
        return null;
    }

    private Collection<VertexRef> getVertexRefsForNCSService( NCSServiceCriteria storedCriteria ) {
        List<Edge> edges = m_ncsEdgeProvider.getEdges(storedCriteria);
        Set<VertexRef> vertRefList = new HashSet<VertexRef>();
        for(Edge edge : edges) {
            vertRefList.add(edge.getSource().getVertex());
            vertRefList.add(edge.getTarget().getVertex());
        }
        return vertRefList;
    }

    protected void highlightEdgePaths(NCSServicePath path, GraphProvider graphProvider) {
        // TODO Auto-generated method stub
        Edge edge = graphProvider.getEdge("nodes", path.getEdges().iterator().next().getId());
        edge.setStyleName("ncsSetPath");
    }

    @Override
    public boolean display(List<VertexRef> targets, OperationContext operationContext) {
        
        for(VertexRef targetRef : targets) {
            String namespace = targetRef.getNamespace();
            if(!namespace.equals("nodes")) {
                return false;
            }else {
                NCSServiceCriteria criteria = (NCSServiceCriteria) operationContext.getGraphContainer().getCriteria("ncs");
                return criteria != null && criteria.size() == 1;
            }
        }
        
        return false;
    }

    @Override
    public boolean enabled(List<VertexRef> targets, OperationContext operationContext) {
        for(VertexRef targetRef : targets) {
            String namespace = targetRef.getNamespace();
            if(!namespace.equals("nodes")) {
                return false;
            }else {
                NCSServiceCriteria criteria = (NCSServiceCriteria) operationContext.getGraphContainer().getCriteria("ncs");
                return criteria != null && criteria.size() == 1;
            }
        }
        
        return false;
    }

    @Override
    public String getId() {
        return null;
    }

    public NCSEdgeProvider getNcsEdgeProvider() {
        return m_ncsEdgeProvider;
    }

    public void setNcsEdgeProvider(NCSEdgeProvider ncsEdgeProvider) {
        m_ncsEdgeProvider = ncsEdgeProvider;
    }

    public NCSPathProviderService getNcsPathProvider() {
        return m_ncsPathProvider;
    }

    public void setNcsPathProvider(NCSPathProviderService ncsPathProvider) {
        m_ncsPathProvider = ncsPathProvider;
    }

    public NCSComponentRepository getDao() {
        return m_dao;
    }

    public void setDao(NCSComponentRepository dao) {
        m_dao = dao;
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }
    
    public void setNcsCriteriaServiceManager(NCSCriteriaServiceManager manager) {
        m_serviceManager = manager;
    }

}
