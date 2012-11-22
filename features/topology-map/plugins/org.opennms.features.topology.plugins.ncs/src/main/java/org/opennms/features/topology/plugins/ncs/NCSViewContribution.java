package org.opennms.features.topology.plugins.ncs;

import java.util.Collection;

import org.opennms.features.topology.api.IViewContribution;
import org.opennms.features.topology.api.WidgetContext;
import org.opennms.features.topology.api.support.FilterableHierarchicalContainer;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Tree;

public class NCSViewContribution implements IViewContribution {
	
	private NCSComponentRepository m_ncsComponentRepository;

	public void setNcsComponentRepository(NCSComponentRepository ncsComponentRepository) {
		m_ncsComponentRepository = ncsComponentRepository;
	}
	
	@Override
	public Component getView(final WidgetContext widgetContext) {
		
		Tree tree = new Tree("Services", new FilterableHierarchicalContainer(new NCSServiceContainer(m_ncsComponentRepository)));
		tree.setMultiSelect(true);
		tree.setImmediate(true);
		tree.setItemCaptionMode(Tree.ITEM_CAPTION_MODE_PROPERTY);
		tree.setItemCaptionPropertyId("name");
		tree.addListener(new ValueChangeListener() {
			
			@Override
			public void valueChange(ValueChangeEvent event) {
				Collection<Long> selectedIds = (Collection<Long>) event.getProperty().getValue();
				
				Criteria criteria = NCSEdgeProvider.createCriteria(selectedIds);
				
				widgetContext.getGraphContainer().setCriteria(criteria);
			}
		});
		
		return tree;
	}

	@Override
	public String getTitle() {
		return "Services";
	}

	@Override
	public Resource getIcon() {
		return null;
	}

}
