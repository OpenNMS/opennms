package org.opennms.features.vaadin.datacollection;

import java.util.Arrays;

import org.opennms.features.vaadin.datacollection.model.DataCollectionGroupDTO;
import org.opennms.features.vaadin.datacollection.model.SystemDefDTO;
import org.opennms.features.vaadin.mibcompiler.api.Logger;

import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;

@SuppressWarnings("serial")
public class SystemDefPanel extends VerticalLayout {

    private final SystemDefForm form;

    private final SystemDefTable table;

    public SystemDefPanel(final DataCollectionGroupDTO source, final Logger logger) {
        addStyleName(Runo.PANEL_LIGHT);
        
        form = new SystemDefForm(source) {
            @Override
            public void saveSystemDef(SystemDefDTO group) {
                // TODO Auto-generated method stub
            }
            @Override
            public void deleteSystemDef(SystemDefDTO group) {
                // TODO Auto-generated method stub
            }
        };

        table = new SystemDefTable(source) {
            @Override
            public void updateExternalSource(BeanItem<SystemDefDTO> item) {
                form.setItemDataSource(item, Arrays.asList(SystemDefForm.FORM_ITEMS));
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
