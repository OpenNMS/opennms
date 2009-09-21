package org.opennms.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HTML;

import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.*;
import com.extjs.gxt.ui.client.widget.layout.*; 

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Application implements EntryPoint {

  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
    
    // Create a Dock Panel
    DockPanel dock = new DockPanel();
    // dock.setStyleName("cw-DockPanel");
    // dock.setSpacing(4);
    dock.setHorizontalAlignment(DockPanel.ALIGN_CENTER);
    dock.setWidth("100%");
    
    VerticalPanel vertical = new VerticalPanel();
    vertical.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
    
    HTML contents = new HTML("<img src=\"../images/logo.gif\" alt=\"openNMS Installer\"/>");
    vertical.add(contents);
    
    /*
    DisclosurePanel verifyPanel = new DisclosurePanel("Verify ownership");
    verifyPanel.setAnimationEnabled(true);
    verifyPanel.add(new Button("Check"));
    vertical.add(verifyPanel);
    // vertical.add(new DisclosurePanel("Verify ownership"));
    
    vertical.add(new DisclosurePanel("Set admin password"));
    
    vertical.add(new DisclosurePanel("Connect to database"));
    
    vertical.add(new DisclosurePanel("Check stored procedures"));
    
    vertical.add(new DisclosurePanel("Additional tips"));
    
    vertical.add(new Button("Continue &#0187;"));
    */
    
    VerticalPanel gxtWrapper = new VerticalPanel();
    gxtWrapper.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
    
    ContentPanel gxtPanel = new ContentPanel();
    AccordionLayout gxtPanelLayout = new AccordionLayout();
    // gxtPanelLayout.setAutoWidth(true);
    gxtPanel.setLayout(gxtPanelLayout);
    gxtPanel.setHeaderVisible(false);
    gxtPanel.setBodyBorder(false);
    gxtPanel.setSize(300, 300);
    gxtPanel.setBodyStyleName("transparent-background");
    // gxtPanel.setHorizontalAlignment(ContentPanel.);
    
    ContentPanel verifyOwnership = new ContentPanel();
    verifyOwnership.setHeading("Verify ownership");
    // verifyOwnership.setLayout(new FitLayout());
    verifyOwnership.setIconStyle("check-success-icon");
    verifyOwnership.setBodyStyleName("accordion-panel");
    verifyOwnership.addText("Add form controls here.");
    
    ContentPanel setAdminPassword = new ContentPanel();
    setAdminPassword.setHeading("Set administrator password");
    setAdminPassword.setIconStyle("check-success-icon");
    setAdminPassword.setBodyStyleName("accordion-panel");
    setAdminPassword.addText("Add form controls here.");
    
    ContentPanel connectToDatabase = new ContentPanel();
    connectToDatabase.setHeading("Connect to database");
    connectToDatabase.setIconStyle("check-failure-icon");
    connectToDatabase.setBodyStyleName("accordion-panel");
    connectToDatabase.addText("Add form controls here.");
    
    ContentPanel checkStoredProcedures = new ContentPanel();
    checkStoredProcedures.setHeading("Check stored procedures");
    checkStoredProcedures.setIconStyle("check-failure-icon");
    checkStoredProcedures.setBodyStyleName("accordion-panel");
    checkStoredProcedures.addText("Add form controls here.");
    
    ContentPanel tips = new ContentPanel();
    tips.setHeading("Additional tips");
    tips.setBodyStyleName("accordion-panel");
    tips.addText("Add additional tips here.");

    gxtPanel.add(verifyOwnership);
    gxtPanel.add(setAdminPassword);
    gxtPanel.add(connectToDatabase);
    gxtPanel.add(checkStoredProcedures);
    gxtPanel.add(tips);
    
    // ExtJS button
    Button continueButton = new Button("Continue &#0187;");
    continueButton.disable();
    gxtPanel.addButton(continueButton);
    // vertical.add(continueButton);
    
    gxtWrapper.add(gxtPanel);
    vertical.add(gxtWrapper);
    
    //dock.add(new HTML(), DockPanel.WEST);
    dock.add(vertical, DockPanel.CENTER);
    //dock.add(new HTML(), DockPanel.EAST);
    
    RootPanel.get().add(dock);
  }
}
