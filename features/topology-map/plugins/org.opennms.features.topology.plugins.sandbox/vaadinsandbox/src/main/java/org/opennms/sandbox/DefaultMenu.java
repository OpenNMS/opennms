package org.opennms.sandbox;

import java.net.MalformedURLException;

import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.Application;

public class DefaultMenu extends Application{

    private static final long serialVersionUID = 1L;
    private HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
    private VerticalLayout mainLayout = null;
    private VerticalLayout leftLayout = new VerticalLayout();
    private VerticalLayout rightLayout = new VerticalLayout();
    private EventsAlarmsWindow EA_Window = null;
    private ResourceGraphsWindow RG_Window = null;
    private PingWindow Ping_Window = null;
    private Label cartman = new Label("Cartman");
    private Label butters = new Label("Butters");
    private ContextMenu cartmanMenu = new ContextMenu();
    private ContextMenu buttersMenu = new ContextMenu();

    public void init() {
        buildMainLayout();

    }

    private void buildMainLayout() {
        setMainWindow(new Window("Topology Maps"));
        getMainWindow().setImmediate(true);
        mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.addComponent(createMenuBar());
        mainLayout.addComponent(horizontalSplit);
        leftLayout.setSizeFull();
        rightLayout.setSizeFull();
        leftLayout.addComponent(cartman);
        rightLayout.addComponent(butters);
        horizontalSplit.setFirstComponent(leftLayout);
        horizontalSplit.setSecondComponent(rightLayout);
        buildContextMenus();
        leftLayout.addListener(new LayoutClickListener() {

            public void layoutClick(LayoutClickEvent event) {
                if (LayoutClickEvent.BUTTON_RIGHT == event.getButton()) {
                    cartmanMenu.show(event.getClientX(), event.getClientY());
                }
            }

        });
        rightLayout.addListener(new LayoutClickListener() {

            public void layoutClick(LayoutClickEvent event) {
                if (LayoutClickEvent.BUTTON_RIGHT == event.getButton()) {
                    buttersMenu.show(event.getClientX(), event.getClientY());
                }
            }

        });
        /* Allocate all available extra space to the horizontal split panel */
        mainLayout.setExpandRatio(horizontalSplit, 1);

        getMainWindow().setContent(mainLayout);
        getMainWindow().addComponent(cartmanMenu);
        getMainWindow().addComponent(buttersMenu);
    }

    private MenuBar createMenuBar() {
        final MenuBar menubar = new MenuBar();

        MenuBar.Command EA_Select = new MenuBar.Command() {
            public void menuSelected(MenuItem selectedItem) {
                try {
                    showEventsAlarmsWindow();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        };

        MenuBar.Command RG_Select = new MenuBar.Command() {
            public void menuSelected(MenuItem selectedItem) {
                try {
                    showResourceGraphsWindow();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        };

        MenuBar.Command Ping_Select = new MenuBar.Command() {
            public void menuSelected(MenuItem selectedItem) {
                try {
                    showPingWindow();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        };

        MenuBar.MenuItem file = menubar.addItem("File", null);
        MenuBar.MenuItem view = menubar.addItem("View", null);
        MenuBar.MenuItem node = menubar.addItem("Node Options", null);
        node.addItem("Events/Alarms", EA_Select);
        node.addItem("Ping", Ping_Select);
        node.addItem("Resource Graphs", RG_Select);
        menubar.setWidth("100%");

        return menubar;
    }

    private void buildContextMenus() {
        buildCartmanMenu();
        buildButtersMenu();
    }

    private void buildCartmanMenu() {
        ContextMenuItem nodeInfo = cartmanMenu.addItem("Node Info");
        final ContextMenuItem ping = cartmanMenu.addItem("Ping");
        ContextMenuItem traceroute = cartmanMenu.addItem("Traceroute");
        final ContextMenuItem eventsAlarms = cartmanMenu.addItem("Events/Alarms");
        final ContextMenuItem resourceGraphs = cartmanMenu.addItem("Resource Graphs");
        cartmanMenu.addListener(new ContextMenu.ClickListener() {

            public void contextItemClick(ClickEvent event) {
                try {
                    if (eventsAlarms == event.getClickedItem()){
                        showEventsAlarmsWindow();
                    } else if (resourceGraphs == event.getClickedItem()){
                        showResourceGraphsWindow();
                    }else if(ping == event.getClickedItem()) {
                        showPingWindow();
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        });
        nodeInfo.setEnabled(false);
    }

    private void buildButtersMenu() {
        ContextMenuItem nodeInfo = buttersMenu.addItem("Node Info");
        ContextMenuItem ping = buttersMenu.addItem("Ping");
        ContextMenuItem traceroute = buttersMenu.addItem("Traceroute");
        ContextMenuItem eventsAlarms = buttersMenu.addItem("Events/Alarms");
        ContextMenuItem resourceGraphs = buttersMenu.addItem("Resource Graphs");

    }

    private EventsAlarmsWindow getEventsAlarmsWindow() throws MalformedURLException{
        if (EA_Window == null)
            EA_Window = new EventsAlarmsWindow(getMainWindow().getWidth(), getMainWindow().getHeight());
        return EA_Window;
    }

    private void showEventsAlarmsWindow() throws IllegalArgumentException, NullPointerException, MalformedURLException{
        getMainWindow().addWindow(getEventsAlarmsWindow());
    }

    private void showPingWindow(){
        getMainWindow().addWindow(getPingWindow());
    }
    
    private Window getPingWindow() {
        if (Ping_Window == null) {
            Ping_Window = new PingWindow(getMainWindow().getWidth(), getMainWindow().getHeight());
        }
        return  Ping_Window;
    }

    private ResourceGraphsWindow getResourceGraphsWindow() throws MalformedURLException{
        if (RG_Window == null)
            RG_Window = new ResourceGraphsWindow(getMainWindow().getWidth(), getMainWindow().getHeight());
        return RG_Window;
    }

    private void showResourceGraphsWindow() throws IllegalArgumentException, NullPointerException, MalformedURLException{
        getMainWindow().addWindow(getResourceGraphsWindow());
    }

    private void showButtersMenu() {

    }

}
