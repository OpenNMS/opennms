package org.opennms.features.vaadin.topology;

import org.opennms.features.vaadin.topology.gwt.client.Vertex;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;

@ClientWidget(Vertex.class)
public class VaadinVertex extends AbstractComponent{

    @Override
    public void paint(PaintTarget target) throws PaintException {
        super.paint(target);
        getApplication().getMainWindow().showNotification("I am requesting a paint job");
        target.addAttribute("x", 50);
        target.addAttribute("y", 50);
    }

    
}
