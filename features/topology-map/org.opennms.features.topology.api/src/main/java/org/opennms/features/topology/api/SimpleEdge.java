package org.opennms.features.topology.api;

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.features.topology.api.topo.AbstractEdge;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;

@XmlRootElement(name="edge")
public class SimpleEdge extends AbstractEdge {
	/**
	 * @param namespace
	 * @param id
	 * @param source
	 * @param target
	 */
	public SimpleEdge(String namespace, String id, SimpleConnector source, SimpleConnector target) {
		super(namespace, id, source, target);
	}
	
	@Override
	public Item getItem() {
		return new BeanItem<SimpleEdge>(this);
	}

}
