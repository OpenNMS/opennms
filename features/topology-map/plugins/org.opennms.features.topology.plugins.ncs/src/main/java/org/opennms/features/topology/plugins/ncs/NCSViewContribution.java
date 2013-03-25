package org.opennms.features.topology.plugins.ncs;

import java.util.Collection;

import org.opennms.features.topology.api.IViewContribution;
import org.opennms.features.topology.api.WidgetContext;
import org.opennms.features.topology.api.support.FilterableHierarchicalContainer;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Tree;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;

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
		tree.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		tree.setItemCaptionPropertyId("name");
		tree.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = -7443836886894714291L;

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
