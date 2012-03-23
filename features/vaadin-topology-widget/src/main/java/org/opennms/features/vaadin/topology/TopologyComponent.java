package org.opennms.features.vaadin.topology;

import org.opennms.features.vaadin.topology.gwt.client.VTopologyComponent;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;

@ClientWidget(VTopologyComponent.class)
public class TopologyComponent extends AbstractComponent {

    private Integer[] m_dataArray = new Integer[] { 10 };

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        // TODO Auto-generated method stub
        super.paintContent(target);
        target.addAttribute("dataArray", getDataArray());
    }

    public void setDataArray(Integer[] arr) {
        m_dataArray = arr;
        
        requestRepaint();
        
    }
    
    public Integer[] getDataArray() {
        return m_dataArray;
    }

}
