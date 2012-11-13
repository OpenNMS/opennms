package org.opennms.features.topology.app.internal;

import java.util.Collection;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.IViewContribution;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.SelectionManager.SelectionListener;
import org.opennms.features.topology.api.WidgetContext;
import org.opennms.features.topology.api.support.FilterableHierarchicalContainer;

import com.vaadin.ui.Component;
import com.vaadin.ui.Tree;

@SuppressWarnings({"serial"})
public abstract class VertexSelectionTree extends Tree implements SelectionListener, IViewContribution {

    private final GraphContainer m_graphContainer;

    public VertexSelectionTree(FilterableHierarchicalContainer container, GraphContainer graphContainer) {
        super(null, container);
        
        m_graphContainer = graphContainer;
        
        this.addListener(new ValueChangeListener() {
            
            @Override
            public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {

            	Collection<?> vertices = m_graphContainer.getVertexForest((Collection<?>)event.getProperty().getValue());
            	m_graphContainer.getSelectionManager().setSelectedVertices(vertices);
            	
            	getContainerDataSource().fireItemUpdated();
                
            }
        });

    }

    /**
     * When a user clicks on a vertex or edge in the UI, update the selection in the tree view.
     */
    @Override
    public void selectionChanged(SelectionManager selectionManager) {
        setValue(selectionManager.getSelectedVertices());
    }

    @Override
    public FilterableHierarchicalContainer getContainerDataSource() {
        return (FilterableHierarchicalContainer)super.getContainerDataSource();
    }

    @Override
    public abstract String getTitle();

    @Override
    public Component getView(WidgetContext widgetContext) {
        return this;
    }
}
