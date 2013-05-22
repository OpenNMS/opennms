package org.opennms.features.topology.api;

import java.util.HashMap;
import java.util.Map;

import org.opennms.features.topology.api.WidgetUpdateListener.WidgetUpdateEvent;
import org.osgi.service.blueprint.container.BlueprintContainer;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;

public class BrowserWidget extends Widget {

    private TabSheet createTabSheet() {
        final TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        // Add a TabSheet.SelectedTabChangeListener to show or hide the extra controls
        tabSheet.addListener(new SelectedTabChangeListener() {
            private static final long serialVersionUID = 6370347645872323830L;

            @Override
            public void selectedTabChange(SelectedTabChangeEvent event) {
                if (event.getSource() == tabSheet) {
                    // Bizarrely enough, getSelectedTab() returns the contained
                    // Component, not the Tab itself.
                    //
                    // If the tab is not selected...
                    Layout extras = extraControlsMap.get(tabSheet.getSelectedTab());
                    if (extras == null) {
                        extraControls.setVisible(false);
                    } else {
                        extraControls = extras;
                        extraControls.setVisible(true);
                    }
                }
            }
        });
        return tabSheet;
    }
    
    private final TabSheet tabSheet = createTabSheet();
    private final Map<Component, Layout> extraControlsMap = new HashMap<Component, Layout>();
    private Layout extraControls = new HorizontalLayout(); // Dummy
    
    public BrowserWidget(BlueprintContainer container, String beanName) {
        super(container, beanName);
    }
    
    @Override
    protected void updateWidget(WidgetUpdateEvent updateEvent) {
        if (updateEvent == null) return;
        if (updateEvent.getType().isBind()) {
            addComponent(updateEvent);
            // TODO MVR notify all listeners
            
        }
        if (updateEvent.getType().isUnbind()) {
            removeComponent(updateEvent);
//            tabSheet.removeComponent(updateEvent.getChangedElement());
            // TODO MVR notify all listeners
            
        }
    }
    
    private void removeComponent(WidgetUpdateEvent event) {
        tabSheet.removeComponent(event.getChangedElement());
        if (extraControlsMap.get(event.getChangedElement()) == extraControls) {
            extraControls = new HorizontalLayout(); // Dummy
        }
        extraControlsMap.remove(event.getChangedElement());
    }
    
    private void addComponent(WidgetUpdateEvent event) {
        final Component view = event.getChangedElement();
        tabSheet.addTab(view, event.getViewData().getTitle(), event.getViewData().getIcon()); // Icon can be null 

        // If the component supports the HasExtraComponents interface, then add the extra 
        // components to the tab bar
        try {
            Component[] extras = ((HasExtraComponents)event.getChangedElement()).getExtraComponents();
            if (extras != null && extras.length > 0) {
                // For any extra controls, add a horizontal layout that will float
                // on top of the right side of the tab panel
                final HorizontalLayout extraControls = new HorizontalLayout();
                extraControls.setHeight(32, Sizeable.UNITS_PIXELS);
                extraControls.setSpacing(true);

                // Add the extra controls to the layout
                for (Component component : extras) {
                    extraControls.addComponent(component);
                    extraControls.setComponentAlignment(component, Alignment.MIDDLE_RIGHT);
                }
                extraControlsMap.put(view, extraControls);
            }
        } catch (ClassCastException e) {}
        view.setSizeFull();
    }

    @Override
    public Component getView(WidgetContext widgetContext) {
        // Use an absolute layout for the bottom panel
        AbsoluteLayout bottomLayout = new AbsoluteLayout();
        bottomLayout.setSizeFull();
        bottomLayout.addComponent(tabSheet);
        bottomLayout.addComponent(extraControls, "top:0px;right:5px;z-index:100"); // Place the extra controls on the absolute layout
        return bottomLayout;
    }
}
