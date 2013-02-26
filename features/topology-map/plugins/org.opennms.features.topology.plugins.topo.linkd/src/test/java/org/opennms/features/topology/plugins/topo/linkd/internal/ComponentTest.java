package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.util.Collection;

import org.junit.Test;
import org.opennms.features.topology.api.topo.AbstractVertex;

import com.vaadin.data.util.BeanItem;

public class ComponentTest {
	@Test
	public void testComponentBeanProperties() throws Exception {
		Collection<?> ids = new BeanItem<AbstractVertex>(new AbstractVertex(LinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD, "fakeId")).getItemPropertyIds();
		for (Object id : ids) {
			System.out.println(id);
		}
	}
}
