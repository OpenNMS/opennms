package org.opennms.sandbox;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;

/**
 * The PingWindow class creates a Vaadin Sub-window with a form and results section
 * for the Ping functionality of a Node.
 * @author Leonardo Bell
 * @author Philip Grenon
 * @version 1.0
 */
@SuppressWarnings("serial")
public class PingWindow extends Window{

	private final double sizePercentage = 0.80; // Window size proportionate to main window
    private boolean numOutput = false; //Flag for Numerical output
    private NativeSelect ipDropdown = null; //Dropdown component for IP Address
    private NativeSelect packetSizeDropdown = null; //Dropdown component for Packet Size
    private Node testNode = null; //Node object containing all of its relative information.
    private Label nodeLabel = null; //Label displaying the name of the Node at the top of the window
    
    /**
     * The PingWindow method constructs a PingWindow component with a size proportionate to the 
     * width and height of the main window.
     * @param testNode 
     * @param width Width of Main window
     * @param height Height of Main window
     */
	public PingWindow (Node testNode, float width, float height){
       
	    
    	/*Sets up window settings*/
	this.testNode = testNode;
    	setCaption("Ping");
        setImmediate(true);
        setResizable(false);
        int windowWidth = (int)(sizePercentage * width), windowHeight = (int)(sizePercentage * height);
        setWidth("" + windowWidth + "px");
        setHeight("" + windowHeight + "px");
        setPositionX((int)((1.0 - windowWidth/width)/2.0 * width));
        setPositionY((int)((1.0 - windowHeight/height)/2.0 * height));
        

        /*Initialize the header of the Sub-window with the name of the selected Node*/
        String nodeName = "<div style=\"text-align: center; font-size: 18pt; font-weight:bold;\">" + testNode.getName() + "</div>";
        nodeLabel = new Label(nodeName);
        nodeLabel.setContentMode(Label.CONTENT_XHTML);
        
        /*Creating various layouts to encapsulate all of the components*/
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        VerticalSplitPanel vSplit = new VerticalSplitPanel();
        VerticalLayout topLayout = new VerticalLayout();
        VerticalLayout bottomLayout = new VerticalLayout();
        VerticalLayout form = new VerticalLayout();
        GridLayout grid = new GridLayout(2,4);
        grid.setWidth("420");
        grid.setHeight("120");
        
        /*Sets up IP Address dropdown with the Name as default*/
        ipDropdown = new NativeSelect();
        ipDropdown.addItem(testNode.getDisplayedName());
        ipDropdown.select(testNode.getDisplayedName());
        
        /*Sets up Packet Size dropdown with different values*/
        packetSizeDropdown = new NativeSelect();
        packetSizeDropdown.addItem("16");
        packetSizeDropdown.addItem("32");
        packetSizeDropdown.addItem("64");
        packetSizeDropdown.addItem("128");
        packetSizeDropdown.addItem("256");
        packetSizeDropdown.addItem("512");
        packetSizeDropdown.addItem("1024");
        packetSizeDropdown.select("16");
        
        /*Creates the Numerical Output Check box and sets up the listener*/
        CheckBox numericalDataCheckBox = new CheckBox("Use Numerical Node Names");
        numericalDataCheckBox.setImmediate(true);
        numericalDataCheckBox.setValue(false);
        numericalDataCheckBox.addListener(new ValueChangeListener() {
			public void valueChange(ValueChangeEvent event) {
				switchNumOutput();
			}
        });
        
        /*Creates the form labels and text fields*/
        Label ipLabel = new Label("IP Address: ");
        Label requestsLabel = new Label("Number of Requests: ");
        Label timeoutLabel = new Label("Time-Out (seconds): ");
        Label packetLabel = new Label("Packet Size: ");
        TextField requestsField = new TextField();
        TextField timeoutField = new TextField();
     
        /*Add all of the components to the GridLayout*/
        grid.addComponent(ipLabel);
        grid.setComponentAlignment(ipLabel, Alignment.MIDDLE_LEFT);
        grid.addComponent(ipDropdown);
        grid.setComponentAlignment(ipDropdown, Alignment.MIDDLE_LEFT);
        grid.addComponent(requestsLabel);
        grid.setComponentAlignment(requestsLabel, Alignment.MIDDLE_LEFT);
        grid.addComponent(requestsField);
        grid.setComponentAlignment(requestsField, Alignment.MIDDLE_LEFT);
        grid.addComponent(timeoutLabel);
        grid.setComponentAlignment(timeoutLabel, Alignment.MIDDLE_LEFT);
        grid.addComponent(timeoutField);
        grid.setComponentAlignment(timeoutField, Alignment.MIDDLE_LEFT);
        grid.addComponent(packetLabel);
        grid.setComponentAlignment(packetLabel, Alignment.MIDDLE_LEFT);
        grid.addComponent(packetSizeDropdown);
        grid.setComponentAlignment(packetSizeDropdown, Alignment.MIDDLE_LEFT);
        
        /*Creates the Ping button and sets up the listener*/
        final Button pingButton = new Button("Ping"); 
        pingButton.addListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                if(event.getButton() == pingButton){
                  getWindow().showNotification("I HAS NOTIFICATZ UR DISLAI!!!111");
                }
            }
        }); 
        
        /*Adds components to the form and sets the width and spacing*/
        form.addComponent(grid);
        form.addComponent(numericalDataCheckBox);
        form.addComponent(pingButton);
        form.setWidth("100%");
        form.setSpacing(true);
        
        /*Adds components to the Top Layout and sets the width and margins*/
        topLayout.addComponent(nodeLabel);
        topLayout.setComponentAlignment(nodeLabel, Alignment.MIDDLE_CENTER);
        topLayout.addComponent(form);
        topLayout.setSizeFull();
        topLayout.setMargin(true, true, false, true);
        
        /*Adds components to the Bottom Layout and sets the width and margins*/
        bottomLayout.addComponent(new Label("RESULTS GO HERE"));
        bottomLayout.setSizeFull();
        bottomLayout.setMargin(true);
        
        /*Setting first and second components for the split panel and setting the panel divider position*/
        vSplit.setFirstComponent(topLayout);
        vSplit.setSecondComponent(bottomLayout);
        vSplit.setSplitPosition(33, UNITS_PERCENTAGE);
        vSplit.setLocked(true);
        
        /*Adds split panel to the main layout and expands the split panel to 100% of the layout space*/
        mainLayout.addComponent(vSplit);
        mainLayout.setExpandRatio(vSplit, 1);
        
        setContent(mainLayout);
    }
    
    /**
     * The switchNumOutput method changes the displayed values in the IP Address dropdown depending on
     * whether the Numerical Output checkbox is selected.
     */
	private void switchNumOutput() {
		ipDropdown.removeAllItems();
		if (numOutput == false){ 
			/*Switching to IP Address format*/
			testNode.setDisplayedName(testNode.getIPAddress());
	        ipDropdown.addItem(testNode.getDisplayedName());
	        numOutput = true;
		} else { 
			/*Switching to Name format*/
			testNode.setDisplayedName(testNode.getName());
            ipDropdown.addItem(testNode.getDisplayedName());
            numOutput = false;
		}
		ipDropdown.select(testNode.getDisplayedName());
		ipDropdown.requestRepaint();
	}
}
