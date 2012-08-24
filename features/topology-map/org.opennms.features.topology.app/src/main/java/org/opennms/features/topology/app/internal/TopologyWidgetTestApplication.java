package org.opennms.features.topology.app.internal;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.opennms.features.topology.api.DisplayState;
import org.opennms.features.topology.api.TopologyProvider;
import org.opennms.features.topology.api.VertexContainer;
import org.opennms.features.topology.app.internal.SimpleGraphContainer.GVertex;
import org.opennms.features.topology.app.internal.TopoContextMenu.TopoContextMenuItem;
import org.opennms.features.topology.app.internal.jung.KKLayoutAlgorithm;
import org.opennms.features.topology.app.internal.support.IconRepositoryManager;

import com.github.wolfie.refresher.Refresher;
import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

public class TopologyWidgetTestApplication extends Application implements CommandUpdateListener, MenuItemUpdateListener, ContextMenuHandler{

	private Window m_window;
	private TopologyComponent m_topologyComponent;
	private Tree m_tree;
	private SimpleGraphContainer m_graphContainer;
	private CommandManager m_commandManager;
	private MenuBar m_menuBar;
	private TopoContextMenu m_contextMenu;
	private AbsoluteLayout m_layout;
	private IconRepositoryManager m_iconRepositoryManager;

	public TopologyWidgetTestApplication(CommandManager commandManager, TopologyProvider topologyProvider, IconRepositoryManager iconRepoManager) {
		m_commandManager = commandManager;
		m_commandManager.addMenuItemUpdateListener(this);
		m_graphContainer = new SimpleGraphContainer();
		m_graphContainer.setDataSource(topologyProvider);
		m_iconRepositoryManager = iconRepoManager;
	}


	@Override
	public void init() {
	    setTheme("topo_default");
	    
		m_layout = new AbsoluteLayout();
		m_layout.setSizeFull();

		m_window = new Window("Topology Widget Test");
		m_window.setContent(m_layout);
		setMainWindow(m_window);

		Refresher refresher = new Refresher();
		refresher.setRefreshInterval(5000);
		getMainWindow().addComponent(refresher);

		m_graphContainer.setLayoutAlgorithm(new KKLayoutAlgorithm());

		m_topologyComponent = new TopologyComponent(m_graphContainer);
		m_topologyComponent.setIconRepoManager(m_iconRepositoryManager);
		m_topologyComponent.setSizeFull();
		m_topologyComponent.addMenuItemStateListener(this);
		m_topologyComponent.setContextMenuHandler(this);

		final Property scale = m_graphContainer.getProperty(DisplayState.SCALE);
		final Slider slider = new Slider(0, 4);
		slider.setResolution(2);
		slider.setHeight("300px");
		slider.setOrientation(Slider.ORIENTATION_VERTICAL);
		scale.setValue(1.0);

		slider.addListener(new ValueChangeListener(){

			public void valueChange(ValueChangeEvent event) {
				scale.setValue((Double) slider.getValue());
			}
		});

		slider.setImmediate(true);

		Label semanticZoomLabel = new Label();
		final Property zoomLevel = m_graphContainer.getProperty(DisplayState.SEMANTIC_ZOOM_LEVEL);
		semanticZoomLabel.setPropertyDataSource(zoomLevel);

		NativeButton zoomInBtn = new NativeButton("+");
		zoomInBtn.addListener(new ClickListener() {

			public void buttonClick(ClickEvent event) {
				int szl = (Integer) zoomLevel.getValue();
				szl++;
				zoomLevel.setValue(szl);
				m_graphContainer.redoLayout();
			}
		});

		NativeButton zoomOutBtn = new NativeButton("-");
		zoomOutBtn.addListener(new ClickListener() {

			public void buttonClick(ClickEvent event) {
				int szl = (Integer) zoomLevel.getValue();
				szl--;
				zoomLevel.setValue(szl);
				m_graphContainer.redoLayout();
			}
		});

		AbsoluteLayout mapLayout = new AbsoluteLayout();

		mapLayout.addComponent(m_topologyComponent, "top:0px; left: 0px; right: 0px; bottom: 0px;");
		mapLayout.addComponent(slider, "top: 20px; left: 20px; z-index:1000;");
		mapLayout.addComponent(semanticZoomLabel, "bottom: 10px; right: 10px; z-index: 2000;");
		mapLayout.addComponent(zoomInBtn, "top: 0px; left: 0px;");
		mapLayout.addComponent(zoomOutBtn, "top: 0px; left: 25px");
		mapLayout.setSizeFull();

		HorizontalSplitPanel treeMapSplitPanel = new HorizontalSplitPanel();
		treeMapSplitPanel.setFirstComponent(createWestLayout());
		treeMapSplitPanel.setSecondComponent(mapLayout);
		treeMapSplitPanel.setSplitPosition(200, Sizeable.UNITS_PIXELS);
		treeMapSplitPanel.setSizeFull();


		VerticalSplitPanel bottomLayoutBar = new VerticalSplitPanel();
		bottomLayoutBar.setFirstComponent(treeMapSplitPanel);

		VerticalLayout zoomLayout = new VerticalLayout();
//		zoomLayout.addComponent(zoomInBtn);
//		zoomLayout.addComponent(zoomOutBtn);

		bottomLayoutBar.setSecondComponent(zoomLayout);
		bottomLayoutBar.setSplitPosition(99, Sizeable.UNITS_PERCENTAGE);
		bottomLayoutBar.setSizeFull();


		m_commandManager.addActionHandlers(m_topologyComponent, m_graphContainer, getMainWindow());
		m_commandManager.addCommandUpdateListener(this);


		menuBarUpdated(m_commandManager);
		m_layout.addComponent(bottomLayoutBar, "top: 23px; left: 0px; right:0px; bottom:0px;");

		m_graphContainer.redoLayout();
	}


    private Layout createWestLayout() {
        m_tree = createTree();
        
        TextField filterField = new TextField("Filter");
        filterField.setTextChangeEventMode(TextChangeEventMode.LAZY);
        filterField.setTextChangeTimeout(200);
        filterField.addListener(new TextChangeListener() {
            
            @Override
            public void textChange(TextChangeEvent event) {
                VertexContainer<Object, GVertex> container = (VertexContainer<Object, GVertex>) m_tree.getContainerDataSource();
                container.removeAllContainerFilters();
                container.addContainerFilter(Vertex.LABEL_PROPERTY, event.getText(), true, false);
                
            }
        });
        
        Panel scrollPanel = new Panel("Vertices");
        scrollPanel.setHeight("100%");
        scrollPanel.setWidth("100%");
        scrollPanel.setStyleName(Reindeer.PANEL_LIGHT);
        scrollPanel.addComponent(m_tree);
        
        AbsoluteLayout absLayout = new AbsoluteLayout();
        absLayout.setWidth("100%");
        absLayout.setHeight("100%");
        absLayout.addComponent(filterField, "top: 25px; left: 0px;");
        absLayout.addComponent(scrollPanel, "top: 75px; left: 0px; bottom:0px;"); 
        
        return absLayout;
    }

	private Tree createTree() {

		final Tree tree = new Tree();
		tree.setMultiSelect(true);
		tree.setContainerDataSource(m_graphContainer.getVertexContainer());
        
		tree.setImmediate(true);
		tree.setItemCaptionPropertyId(Vertex.LABEL_PROPERTY);
		for (Iterator<?> it = tree.rootItemIds().iterator(); it.hasNext();) {
			tree.expandItemsRecursively(it.next());
		}
		
		tree.addListener(new ValueChangeListener() {
            
            @Override
            public void valueChange(ValueChangeEvent event) {
                m_topologyComponent.selectVerticesByItemId((Collection<Object>) event.getProperty().getValue());
            }
        });
		
		return tree;
	}


	public void updateMenuItems() {
		updateMenuItems(m_menuBar.getItems());
		m_menuBar.requestRepaint();
	}

	private void updateContextMenuItems(Object target, List<TopoContextMenuItem> items) {
		for(TopoContextMenuItem contextItem : items) {
			if(contextItem.hasChildren()) {
				updateContextMenuItems(target, contextItem.getChildren());
			} else {
				m_commandManager.updateContextMenuItem(target, contextItem, m_graphContainer, getMainWindow());
			}
		}
	}


	private void updateMenuItems(List<MenuItem> menuItems) {
		for(MenuItem menuItem : menuItems) {
			if(menuItem.hasChildren()) {
				updateMenuItems(menuItem.getChildren());
			}else {
				m_commandManager.updateMenuItem(menuItem, m_graphContainer, getMainWindow());
			}
		}
	}

	@Override
	public void menuBarUpdated(CommandManager commandManager) {
		if(m_menuBar != null) {
			m_layout.removeComponent(m_menuBar);
		}

		if(m_contextMenu != null) {
			getMainWindow().removeComponent(m_contextMenu);
		}

		m_menuBar = commandManager.getMenuBar(m_graphContainer, getMainWindow());
		m_menuBar.setWidth("100%");
		m_layout.addComponent(m_menuBar, "top: 0px; left: 0px; right:0px;");
		//TODO: Finish implementing the context menu
		m_contextMenu = commandManager.getContextMenu(m_graphContainer, getMainWindow());
		getMainWindow().addComponent(m_contextMenu);
		updateMenuItems();
	}


	@Override
	public void show(Object target, int left, int top) {
		updateContextMenuItems(target, m_contextMenu.getItems());
		updateSubMenuDisplay(m_contextMenu.getItems());
		m_contextMenu.setTarget(target);
		m_contextMenu.show(left, top);
	}


	private void updateSubMenuDisplay(List<TopoContextMenuItem> items) {
		for (TopoContextMenuItem item : items) {
			if (!item.hasChildren()) continue;
			else updateSubMenuDisplay(item.getChildren());
			boolean shouldDisplay = false;
			for (TopoContextMenuItem child : item.getChildren()) {
				if (child.getItem().isVisible()) {
					shouldDisplay = true;
					break;
				}
			}
			item.getItem().setVisible(shouldDisplay);
		}
	}

}
