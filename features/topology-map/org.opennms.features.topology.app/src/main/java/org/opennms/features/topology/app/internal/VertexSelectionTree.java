package org.opennms.features.topology.app.internal;

import java.util.Collection;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.SelectionContext;
import org.opennms.features.topology.api.SelectionListener;
import org.opennms.features.topology.api.topo.VertexRef;

import com.vaadin.ui.Tree;

@SuppressWarnings({"serial"})
public class VertexSelectionTree extends Tree implements SelectionListener {

	private final String m_title;
    private final GraphContainer m_graphContainer;

    public VertexSelectionTree(String title, GraphContainer graphContainer) {
        super(null, new GCFilterableContainer(graphContainer));
        m_title = title;
        
        m_graphContainer = graphContainer;
        
        this.addListener(new ValueChangeListener() {
            
            @Override
            public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {

            	@SuppressWarnings("unchecked")
				Collection<VertexRef> refs = (Collection<VertexRef>)event.getProperty().getValue();
            	
            	Collection<VertexRef> vertices = m_graphContainer.getVertexRefForest(refs);
            	m_graphContainer.getSelectionManager().setSelectedVertexRefs(vertices);
            	
            }
        });

    }

    /**
     * When a user clicks on a vertex or edge in the UI, update the selection in the tree view.
     */
    @Override
    public void selectionChanged(SelectionContext selectionContext) {
        setValue(selectionContext.getSelectedVertexRefs());
    }

    @Override
    public GCFilterableContainer getContainerDataSource() {
        return (GCFilterableContainer)super.getContainerDataSource();
    }

    public String getTitle() { return m_title; }

}
