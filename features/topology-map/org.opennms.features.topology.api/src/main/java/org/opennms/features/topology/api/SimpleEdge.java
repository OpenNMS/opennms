package org.opennms.features.topology.api;

import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.Connector;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;

@XmlRootElement(name="edge")
public class SimpleEdge extends AbstractEdge {
	
	private final Connector m_source;
	private final Connector m_target;
	
	/**
	 * @param namespace
	 * @param id
	 * @param source
	 * @param target
	 */
	public SimpleEdge(String namespace, String id, Connector source, Connector target) {
		super(namespace, id);
		m_source = source;
		m_target = target;
	}
	
	@Override
	public Item getItem() {
		return new BeanItem<SimpleEdge>(this);
	}

	@Override
	@XmlIDREF
	public Connector getSource() {
		return m_source;
	}

	@Override
	@XmlIDREF
	public Connector getTarget() {
		return m_target;
	}
}
