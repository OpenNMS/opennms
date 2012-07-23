package org.opennms.features.vaadin.datacollection;

import java.util.Arrays;

import org.opennms.features.vaadin.datacollection.model.DataCollectionGroupDTO;
import org.opennms.features.vaadin.datacollection.model.GroupDTO;
import org.opennms.features.vaadin.mibcompiler.api.Logger;

import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;

@SuppressWarnings("serial")
public class GroupPanel extends VerticalLayout {

    private final GroupForm form;

    private final GroupTable table;

    public GroupPanel(final DataCollectionGroupDTO source, final Logger logger) {
        addStyleName(Runo.PANEL_LIGHT);

        form = new GroupForm(source) {
            @Override
            public void saveGroup(GroupDTO group) {
                // TODO Auto-generated method stub
            }
            @Override
            public void deleteGroup(GroupDTO group) {
                // TODO Auto-generated method stub
            }
        };

        table = new GroupTable(source) {
            @Override
            public void updateExternalSource(BeanItem<GroupDTO> item) {
                form.setItemDataSource(item, Arrays.asList(GroupForm.FORM_ITEMS));
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
