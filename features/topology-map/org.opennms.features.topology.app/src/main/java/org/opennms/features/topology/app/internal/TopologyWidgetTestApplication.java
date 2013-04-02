/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.app.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.HistoryManager;
import org.opennms.features.topology.api.IViewContribution;
import org.opennms.features.topology.api.MapViewManager;
import org.opennms.features.topology.api.MapViewManagerListener;
import org.opennms.features.topology.api.SelectionListener;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.SelectionNotifier;
import org.opennms.features.topology.api.WidgetContext;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.app.internal.TopoContextMenu.TopoContextMenuItem;
import org.opennms.features.topology.app.internal.TopologyComponent.VertexUpdateListener;
import org.opennms.features.topology.app.internal.jung.FRLayoutAlgorithm;
import org.opennms.features.topology.app.internal.support.IconRepositoryManager;
import org.opennms.web.api.OnmsHeaderProvider;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.data.Property;
import com.vaadin.server.Page;
import com.vaadin.server.Page.UriFragmentChangedEvent;
import com.vaadin.server.Page.UriFragmentChangedListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.slider.SliderOrientation;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

@SuppressWarnings("serial")
@Theme("topo_default")
@PreserveOnRefresh
public class TopologyWidgetTestApplication extends UI implements CommandUpdateListener, MenuItemUpdateListener, ContextMenuHandler, WidgetUpdateListener, WidgetContext, UriFragmentChangedListener, GraphContainer.ChangeListener, MapViewManagerListener, VertexUpdateListener {

	private static final long serialVersionUID = 6837501987137310938L;
	private static int HEADER_HEIGHT = 100;
	private static final int MENU_BAR_HEIGHT = 23;

	private static final String LABEL_PROPERTY = "label";
	private TopologyComponent m_topologyComponent;
	private VertexSelectionTree m_tree;
	private final GraphContainer m_graphContainer;
	private final CommandManager m_commandManager;
	private MenuBar m_menuBar;
	private TopoContextMenu m_contextMenu;
	private AbsoluteLayout m_layout;
	private AbsoluteLayout m_rootLayout;
	private final IconRepositoryManager m_iconRepositoryManager;
	private WidgetManager m_widgetManager;
	private WidgetManager m_treeWidgetManager;
	private Accordion m_treeAccordion;
    private HorizontalSplitPanel m_treeMapSplitPanel;
    private VerticalSplitPanel m_bottomLayoutBar;
    private final Label m_zoomLevelLabel = new Label("0"); 
    private final HistoryManager m_historyManager;
    private final SelectionManager m_selectionManager;
    private String m_headerHtml;
    private boolean m_showHeader = true;
    private OnmsHeaderProvider m_headerProvider = null; 
    
	public TopologyWidgetTestApplication(CommandManager commandManager, HistoryManager historyManager, GraphProvider topologyProvider, ProviderManager providerManager, IconRepositoryManager iconRepoManager, SelectionManager selectionManager) {
	    
	    m_commandManager = commandManager;
		m_commandManager.addMenuItemUpdateListener(this);
		m_historyManager = historyManager;
		m_iconRepositoryManager = iconRepoManager;
		m_selectionManager = selectionManager;

		// Create a per-session GraphContainer instance
		m_graphContainer = new VEProviderGraphContainer(topologyProvider, providerManager);
		m_graphContainer.addChangeListener(this);
		m_graphContainer.getMapViewManager().addListener(this);
	}

    private String getHeader(HttpServletRequest request) {
        if(m_headerProvider == null) {
            return "";
        } else {
            return m_headerProvider.getHeaderHtml(request);
        }
    }

	@Override
    protected void init(VaadinRequest request) {
        m_headerHtml =  getHeader(new HttpServletRequestVaadinImpl(request));

        m_rootLayout = new AbsoluteLayout();
        m_rootLayout.setSizeFull();
        
        setContent(m_rootLayout);
        
        Page.getCurrent().addUriFragmentChangedListener(this);
        
        m_layout = new AbsoluteLayout();
        m_layout.setSizeFull();
        m_rootLayout.addComponent(m_layout);
        
        if(m_showHeader && m_headerHtml != null) {
            HEADER_HEIGHT = 100;
            Panel header = new Panel("header");
            header.setCaption(null);
            header.setSizeUndefined();
            header.addStyleName("onmsheader");
            m_rootLayout.addComponent(header, "top: 0px; left: 0px; right:0px;");
            
            try {
                CustomLayout customLayout = new CustomLayout(getHeaderLayout());
                header.setContent(customLayout);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            HEADER_HEIGHT = 0;
        }
        
        //Refresher refresher = new Refresher();
        //refresher.setRefreshInterval(5000);
        //getMainWindow().addComponent(refresher);

        m_graphContainer.setLayoutAlgorithm(new FRLayoutAlgorithm());

        final Property<Double> scale = m_graphContainer.getScaleProperty();

        m_topologyComponent = new TopologyComponent(m_graphContainer, m_iconRepositoryManager, m_selectionManager, this);
        m_topologyComponent.setSizeFull();
        m_topologyComponent.addMenuItemStateListener(this);
        m_topologyComponent.addVertexUpdateListener(this);
        
        final Slider slider = new Slider(0, 1);
        
        slider.setPropertyDataSource(scale);
        slider.setResolution(1);
        slider.setHeight("300px");
        slider.setOrientation(SliderOrientation.VERTICAL);

        slider.setImmediate(true);

        final Button zoomInBtn = new Button();
        zoomInBtn.setIcon(new ThemeResource("images/plus.png"));
        zoomInBtn.setDescription("Expand Semantic Zoom Level");
        zoomInBtn.setStyleName("semantic-zoom-button");
        zoomInBtn.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                int szl = (Integer) m_graphContainer.getSemanticZoomLevel();
                szl++;
                m_graphContainer.setSemanticZoomLevel(szl);
                setSemanticZoomLevel(szl);
                saveHistory();
            }
        });

        Button zoomOutBtn = new Button();
        zoomOutBtn.setIcon(new ThemeResource("images/minus.png"));
        zoomOutBtn.setDescription("Collapse Semantic Zoom Level");
        zoomOutBtn.setStyleName("semantic-zoom-button");
        zoomOutBtn.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                int szl = (Integer) m_graphContainer.getSemanticZoomLevel();
                if(szl > 0) {
                    szl--;
                    m_graphContainer.setSemanticZoomLevel(szl);
                    setSemanticZoomLevel(szl);
                    saveHistory();
                } 
                
            }
        });
        
        
        final Button panBtn = new Button();
        panBtn.setIcon(new ThemeResource("images/cursor_drag_arrow.png"));
        panBtn.setDescription("Pan Tool");
        panBtn.setStyleName("toolbar-button down");
        
        final Button selectBtn = new Button();
        selectBtn.setIcon(new ThemeResource("images/selection.png"));
        selectBtn.setDescription("Selection Tool");
        selectBtn.setStyleName("toolbar-button");
        selectBtn.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                selectBtn.setStyleName("toolbar-button down");
                panBtn.setStyleName("toolbar-button");
                m_topologyComponent.setActiveTool("select");
            }
        });
        
        panBtn.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                panBtn.setStyleName("toolbar-button down");
                selectBtn.setStyleName("toolbar-button");
                m_topologyComponent.setActiveTool("pan");
            }
        });

        
        VerticalLayout toolbar = new VerticalLayout();
        toolbar.setWidth("31px");
        toolbar.addComponent(panBtn);
        toolbar.addComponent(selectBtn);
        
        HorizontalLayout semanticLayout = new HorizontalLayout();
        semanticLayout.addComponent(zoomInBtn);
        semanticLayout.addComponent(m_zoomLevelLabel);
        semanticLayout.addComponent(zoomOutBtn);
        semanticLayout.setComponentAlignment(m_zoomLevelLabel, Alignment.MIDDLE_CENTER);
        
        AbsoluteLayout mapLayout = new AbsoluteLayout();

        mapLayout.addComponent(m_topologyComponent, "top:0px; left: 0px; right: 0px; bottom: 0px;");
        mapLayout.addComponent(slider, "top: 5px; left: 20px; z-index:1000;");
        mapLayout.addComponent(toolbar, "top: 324px; left: 12px;");
        mapLayout.addComponent(semanticLayout, "top: 380px; left: 2px;");
        mapLayout.setSizeFull();

        m_treeMapSplitPanel = new HorizontalSplitPanel();
        m_treeMapSplitPanel.setFirstComponent(createWestLayout());
        m_treeMapSplitPanel.setSecondComponent(mapLayout);
        m_treeMapSplitPanel.setSplitPosition(222, Unit.PIXELS);
        m_treeMapSplitPanel.setSizeFull();

        m_commandManager.addCommandUpdateListener(this);

        menuBarUpdated(m_commandManager);
        if(m_widgetManager.widgetCount() != 0) {
            updateWidgetView(m_widgetManager);
        }else {
            m_layout.addComponent(m_treeMapSplitPanel, getBelowMenuPosition());
        }
        
        if(m_treeWidgetManager.widgetCount() != 0) {
            updateAccordionView(m_treeWidgetManager);
        }
    }
	

    /**
	 * Update the Accordion View with installed widgets
	 * @param treeWidgetManager
	 */
    private void updateAccordionView(WidgetManager treeWidgetManager) {
        if (m_treeAccordion != null) {
            m_treeAccordion.removeAllComponents();
            
            m_treeAccordion.addTab(m_tree, m_tree.getTitle());
            for(IViewContribution widget : treeWidgetManager.getWidgets()) {
                if(widget.getIcon() != null) {
                    m_treeAccordion.addTab(widget.getView(this), widget.getTitle(), widget.getIcon());
                }else {
                    m_treeAccordion.addTab(widget.getView(this), widget.getTitle());
                }
            }
        }
    }

    /**
     * Updates the bottom widget area with the registered widgets
     * 
     * Any widget with the service property of 'location=bottom' are
     * included.
     * 
     * @param widgetManager
     */
    private void updateWidgetView(WidgetManager widgetManager) {
        if (m_layout != null) {
            if(widgetManager.widgetCount() == 0) {
                m_layout.removeAllComponents();
                m_layout.addComponent(m_treeMapSplitPanel, getBelowMenuPosition());
                m_layout.requestRepaint();
            } else {
                if(m_bottomLayoutBar == null) {
                    m_bottomLayoutBar = new VerticalSplitPanel();
                    m_bottomLayoutBar.setFirstComponent(m_treeMapSplitPanel);
                    // Split the screen 70% top, 30% bottom
                    m_bottomLayoutBar.setSplitPosition(70, Unit.PERCENTAGE);
                    m_bottomLayoutBar.setSizeFull();
                    m_bottomLayoutBar.setSecondComponent(getTabSheet(widgetManager, this));
                }

                m_layout.removeAllComponents();
                m_layout.addComponent(m_bottomLayoutBar, getBelowMenuPosition());
                m_layout.requestRepaint();
                
            }
        }
        
        // TODO: Integrate contextmenu with the Connector/Extension pattern
        if(m_contextMenu != null && m_contextMenu.getParent() == null) {
            //getMainWindow().addComponent(m_contextMenu);
        }
    }


    private String getBelowMenuPosition() {
        return "top: " + (HEADER_HEIGHT + MENU_BAR_HEIGHT) + "px; left: 0px; right:0px; bottom:0px;";
    }

    /**
     * Gets a {@link TabSheet} view for all widgets in this manager.
     * 
     * @return TabSheet
     */
    private TabSheet getTabSheet(WidgetManager manager, WidgetContext widgetContext) {
        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();

        for(IViewContribution viewContrib : manager.getWidgets()) {
            // Create a new view instance
            Component view = viewContrib.getView(widgetContext);
            try {
                m_selectionManager.addSelectionListener((SelectionListener)view);
            } catch (ClassCastException e) {}
            try {
                ((SelectionNotifier)view).addSelectionListener(m_selectionManager);
            } catch (ClassCastException e) {}
            if(viewContrib.getIcon() != null) {
                tabSheet.addTab(view, viewContrib.getTitle(), viewContrib.getIcon());
            } else {
                tabSheet.addTab(view, viewContrib.getTitle());
            }
            view.setSizeFull();
        }

        return tabSheet;
    }
    

    /**
     * Creates the west area layout including the
     * accordion and tree views.
     * 
     * @return
     */
	private Layout createWestLayout() {
        m_tree = createTree();
        
        
        final TextField filterField = new TextField("Filter");
        filterField.setTextChangeTimeout(200);
        
        
        final Button filterBtn = new Button("Filter");
        filterBtn.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
            	GCFilterableContainer container = m_tree.getContainerDataSource();
                container.removeAllContainerFilters();
                
                String filterString = (String) filterField.getValue();
                if(!filterString.equals("") && filterBtn.getCaption().toLowerCase().equals("filter")) {
                    container.addContainerFilter(LABEL_PROPERTY, (String) filterField.getValue(), true, false);
                    filterBtn.setCaption("Clear");
                } else {
                    filterField.setValue("");
                    filterBtn.setCaption("Filter");
                }
                
            }
        });
        
        
        HorizontalLayout filterArea = new HorizontalLayout();
        filterArea.addComponent(filterField);
        filterArea.addComponent(filterBtn);
        filterArea.setComponentAlignment(filterBtn, Alignment.BOTTOM_CENTER);
        
        m_treeAccordion = new Accordion();
        m_treeAccordion.addTab(m_tree, m_tree.getTitle());
        m_treeAccordion.setWidth("100%");
        m_treeAccordion.setHeight("100%");
        
        AbsoluteLayout absLayout = new AbsoluteLayout();
        absLayout.setWidth("100%");
        absLayout.setHeight("100%");
        absLayout.addComponent(filterArea, "top: 25px; left: 15px;");
        absLayout.addComponent(m_treeAccordion, "top: 75px; left: 15px; right: 15px; bottom:25px;"); 
        
        return absLayout;
    }

    private VertexSelectionTree createTree() {
	    //final FilterableHierarchicalContainer container = new FilterableHierarchicalContainer(m_graphContainer.getVertexContainer());
	    
		VertexSelectionTree tree = new VertexSelectionTree("Nodes", m_graphContainer, m_selectionManager);
		tree.setMultiSelect(true);
		tree.setImmediate(true);
		tree.setItemCaptionPropertyId(LABEL_PROPERTY);

		for (Iterator<?> it = tree.rootItemIds().iterator(); it.hasNext();) {
			Object item = it.next();
			tree.expandItemsRecursively(item);
		}
		
		m_selectionManager.addSelectionListener(tree);

		return tree;
	}

	@Override
	public void updateMenuItems() {
		updateMenuItems(m_menuBar.getItems());
		m_menuBar.requestRepaint();
	}

	private void updateContextMenuItems(Object target, List<TopoContextMenuItem> items) {
		for(TopoContextMenuItem contextItem : items) {
			if(contextItem.hasChildren()) {
				updateContextMenuItems(target, contextItem.getChildren());
			} else {
				m_commandManager.updateContextMenuItem(target, contextItem, m_graphContainer, this);
			}
		}
	}


	private void updateMenuItems(List<MenuItem> menuItems) {
		for(MenuItem menuItem : menuItems) {
			if(menuItem.hasChildren()) {
				updateMenuItems(menuItem.getChildren());
			}else {
				m_commandManager.updateMenuItem(menuItem, m_graphContainer, this, m_selectionManager);
			}
		}
	}

	@Override
	public void menuBarUpdated(CommandManager commandManager) {
		if(m_menuBar != null) {
			m_rootLayout.removeComponent(m_menuBar);
		}

		if(m_contextMenu != null) {
			m_contextMenu.detach();

		}

		m_menuBar = commandManager.getMenuBar(m_graphContainer, this, m_selectionManager);
		m_menuBar.setWidth("100%");
		m_rootLayout.addComponent(m_menuBar, "top: " + HEADER_HEIGHT +"px; left: 0px; right:0px;");

		m_contextMenu = commandManager.getContextMenu(m_graphContainer, this);
		m_contextMenu.setAsContextMenuOf(this);
		updateMenuItems();
	}

	@Override
	public void show(Object target, int left, int top) {
		updateContextMenuItems(target, m_contextMenu.getItems());
		updateSubMenuDisplay(m_contextMenu.getItems());
		m_contextMenu.setTarget(target);
		m_contextMenu.open(left, top);
	}


	private static void updateSubMenuDisplay(List<TopoContextMenuItem> items) {
		for (TopoContextMenuItem item : items) {
			if (!item.hasChildren()) continue;
			else updateSubMenuDisplay(item.getChildren());
			// TODO: Figure out how to do this in the new contextmenu
			/*
			boolean shouldDisplay = false;
			for (TopoContextMenuItem child : item.getChildren()) {
				if (child.getItem().isVisible()) {
					shouldDisplay = true;
					break;
				}
			}
			item.getItem().setVisible(shouldDisplay);
			*/
		}
	}


    public WidgetManager getWidgetManager() {
        return m_widgetManager;
    }


    public void setWidgetManager(WidgetManager widgetManager) {
        if(m_widgetManager != null) {
            m_widgetManager.removeUpdateListener(this);
        }
        m_widgetManager = widgetManager;
        m_widgetManager.addUpdateListener(this);
    }


    @Override
    public void widgetListUpdated(WidgetManager widgetManager) {
        if(!isClosing()) {
            if(widgetManager == m_widgetManager) {
                updateWidgetView(widgetManager);
            }else if(widgetManager == m_treeWidgetManager) {
                updateAccordionView(widgetManager);
            }
        }
    }


    public WidgetManager getTreeWidgetManager() {
        return m_treeWidgetManager;
    }


    public void setTreeWidgetManager(WidgetManager treeWidgetManager) {
        if(m_treeWidgetManager != null) {
            m_treeWidgetManager.removeUpdateListener(this);
        }
        
        m_treeWidgetManager = treeWidgetManager;
        m_treeWidgetManager.addUpdateListener(this);
    }


    @Override
    public GraphContainer getGraphContainer() {
        return m_graphContainer;
    }


    int m_settingFragment = 0;
    
    @Override
    public void uriFragmentChanged(UriFragmentChangedEvent event) {
        m_settingFragment++;
        String fragment = event.getUriFragment();
        m_historyManager.applyHistory(fragment, m_graphContainer);
        m_settingFragment--;
    }


    private void saveHistory() {
        if (m_settingFragment == 0) {
            String fragment = m_historyManager.create(m_graphContainer);
            Page.getCurrent().setUriFragment(fragment, false);
        }
    }


    @Override
    public void graphChanged(GraphContainer graphContainer) {
        m_zoomLevelLabel.setValue("" + graphContainer.getSemanticZoomLevel());
    }


    private void setSemanticZoomLevel(int szl) {
        m_zoomLevelLabel.setValue(String.valueOf(szl));
        m_graphContainer.redoLayout();
    }


    @Override
    public void boundingBoxChanged(MapViewManager viewManager) {
        saveHistory();
    }


    @Override
    public void onVertexUpdate() {
        saveHistory();
    }
    
    private InputStream getHeaderLayout() {
        return new ByteArrayInputStream(m_headerHtml.getBytes());
    }

    public void setHeaderProvider(OnmsHeaderProvider headerProvider) {
        m_headerProvider = headerProvider;
    }

    /**
     * Parameter is a String because config has String values
     * @param boolVal
     */
    //@Override
    public void setShowHeader(String boolVal) {
        m_showHeader = "true".equals(boolVal);
    }
}
