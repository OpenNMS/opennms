package org.opennms.features.vaadin.datacollection;

import java.util.Arrays;

import org.opennms.features.vaadin.datacollection.model.DataCollectionGroupDTO;
import org.opennms.features.vaadin.datacollection.model.ResourceTypeDTO;
import org.opennms.features.vaadin.mibcompiler.api.Logger;

import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;

@SuppressWarnings("serial")
public class ResourceTypePanel extends VerticalLayout {

    private final ResourceTypeForm form;
    
    private final ResourceTypeTable table;
    
    public ResourceTypePanel(final DataCollectionGroupDTO source, final Logger logger) {
        addStyleName(Runo.PANEL_LIGHT);
        
        form = new ResourceTypeForm() {
            @Override
            public void saveResourceType(ResourceTypeDTO resourceType) {
                // TODO Auto-generated method stub
            }
            @Override
            public void deleteResourceType(ResourceTypeDTO resourceType) {
                // TODO Auto-generated method stub
            }
        };
        
        table = new ResourceTypeTable(source) {
            @Override
            public void updateExternalSource(BeanItem<ResourceTypeDTO> item) {
                form.setItemDataSource(item, Arrays.asList(ResourceTypeForm.FORM_ITEMS));
                form.setVisible(true);
                form.setReadOnly(true);
            }
        };
        
        setSpacing(true);
        setMargin(true);
        addComponent(table);
        addComponent(form);
    }

}
