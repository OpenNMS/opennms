package org.opennms.features.vaadin.datacollection;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.opennms.features.vaadin.datacollection.model.DataCollectionGroupDTO;
import org.opennms.features.vaadin.mibcompiler.api.Logger;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;

@SuppressWarnings("serial")
public abstract class DataCollectionGroupPanel extends Panel implements TabSheet.SelectedTabChangeListener {

    public DataCollectionGroupPanel(final DatacollectionGroup group, final Logger logger) {
        setCaption("Data Collection");
        addStyleName(Runo.PANEL_LIGHT);

        // Creating DTO Object
        
        final DataCollectionGroupDTO dto = getDataCollectionGroupDTO(group);

        // Data Collection Group - Main Fields

        final TextField groupName = new TextField("Data Collection Group Name");
        groupName.setPropertyDataSource(new ObjectProperty<String>(group.getName()));
        groupName.setNullSettingAllowed(false);

        final TextField fileName = new TextField("XML File Name");
        fileName.setNullSettingAllowed(false);
        fileName.setValue(group.getName() + ".xml");

        // Button Toolbar

        final HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.addComponent(new Button("Cancel Processing", new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                cancelProcessing();
                logger.info("Data collection processing has been canceled");
            }
        }));
        toolbar.addComponent(new Button("Generate Data Collection File", new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                generateDataCollectionFile(getOnmsDataCollection(dto));
                logger.info("The data collection have been saved.");
            }
        }));

        // Tab Panel

        final TabSheet tabs = new TabSheet();
        tabs.setStyleName(Runo.TABSHEET_SMALL);
        tabs.setSizeFull();
        tabs.addTab(new ResourceTypePanel(dto, logger), "Resource Types");
        tabs.addTab(new GroupPanel(dto, logger), "MIB Groups");
        tabs.addTab(new SystemDefPanel(dto, logger), "System Definitions");

        // Main Layout

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.addComponent(toolbar);
        mainLayout.addComponent(groupName);
        mainLayout.addComponent(fileName);
        mainLayout.addComponent(tabs);
        mainLayout.setComponentAlignment(toolbar, Alignment.MIDDLE_RIGHT);
        setContent(mainLayout);
    }

    public void selectedTabChange(SelectedTabChangeEvent event) {
        TabSheet tabsheet = event.getTabSheet();
        Tab tab = tabsheet.getTab(tabsheet.getSelectedTab());
        if (tab != null) {
            getWindow().showNotification("Selected tab: " + tab.getCaption());
        }
    }    

    public DatacollectionGroup getOnmsDataCollection(DataCollectionGroupDTO dto) {
        MapperFacade mapper = new DefaultMapperFactory.Builder().build().getMapperFacade();
        return mapper.map(dto, DatacollectionGroup.class);
    }

    public DataCollectionGroupDTO getDataCollectionGroupDTO(DatacollectionGroup group) {
        MapperFacade mapper = new DefaultMapperFactory.Builder().build().getMapperFacade();
        return mapper.map(group, DataCollectionGroupDTO.class);
    }

    /**
     * Cancel processing.
     */
    public abstract void cancelProcessing();

    /**
     * Generate event file.
     *
     * @param group the OpenNMS Data Collection Group
     */
    public abstract void generateDataCollectionFile(DatacollectionGroup group);

}
