package org.opennms.features.topology.api;

import com.vaadin.data.Container;
import com.vaadin.data.util.BeanContainer;

public abstract class VertexContainer<K, T> extends BeanContainer<K,T> implements Container.Hierarchical {

    public VertexContainer(Class<? super T> type) {
        super(type);
    }
    
    public void fireItemSetChange() {
        super.fireItemSetChange();
    }

}
