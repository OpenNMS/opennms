package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.util.Collection;

import org.junit.Test;

import com.vaadin.data.util.BeanItem;

public class ComponentTest {
	@Test
	public void testComponentBeanProperties() throws Exception {
		Collection<?> ids = new BeanItem<LinkdVertex>(new LinkdVertex()).getItemPropertyIds();
		for (Object id : ids) {
			System.out.println(id);
		}
	}
}
