package org.opennms.sandbox;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class PingWindow extends Window{

   
    private final double sizePercentage = 0.80; // Window size proportionate to main window
    private boolean numOutput = false;
    private NativeSelect ipDropdown = null;
    private NativeSelect packetSizeDropdown = null;
    private Node testNode = null;
    public PingWindow (float width, float height){
        
        //Test Data
        testNode = new Node("172.0.1.234","Test Node");
        //End Test Data
        
        
        int windowWidth = (int)(sizePercentage * width), windowHeight = (int)(sizePercentage * height);

        setCaption("Ping");
        setImmediate(true);
        setResizable(false);
        setWidth("" + windowWidth + "px");
        setHeight("" + windowHeight + "px");
        setPositionX((int)((1.0 - windowWidth/width)/2.0 * width));
        setPositionY((int)((1.0 - windowHeight/height)/2.0 * height));

        VerticalLayout layout = new VerticalLayout();
        GridLayout grid = new GridLayout(2,5);
        ipDropdown = new NativeSelect();
        packetSizeDropdown = new NativeSelect();
        CheckBox numericalDataCheckBox = new CheckBox("Use Numerical Node Names");
        numericalDataCheckBox.setImmediate(true);
        packetSizeDropdown.addItem("16");
        packetSizeDropdown.addItem("32");
        packetSizeDropdown.addItem("64");
        packetSizeDropdown.addItem("128");
        packetSizeDropdown.addItem("256");
        packetSizeDropdown.addItem("512");
        packetSizeDropdown.addItem("1024");
        
        //Test Data
        ipDropdown.addItem(testNode.getDisplayedName());
        ipDropdown.select(testNode.getDisplayedName());
        //End Test Data
        
        grid.addComponent(new Label("IP Address: "));
        grid.addComponent(ipDropdown);
        grid.addComponent(new Label("Number of Requests: "));
        grid.addComponent(new TextField());
        grid.addComponent(new Label("Time-Out (seconds): "));
        grid.addComponent(new TextField());
        grid.addComponent(new Label("Packet Size: "));
        grid.addComponent(packetSizeDropdown);
        packetSizeDropdown.select("16");
        
        numericalDataCheckBox.setValue(false);
        grid.addComponent(numericalDataCheckBox);
        
        numericalDataCheckBox.addListener(new ValueChangeListener() {

			public void valueChange(ValueChangeEvent event) {
				switchNumOutput();
			}
        	
        });
       
        final Button pingButton = new Button("Ping");        
        pingButton.addListener(new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                if(event.getButton() == pingButton){
                  getWindow().showNotification("I HAS NOTIFICATZ UR DISLAI!!!111");
                }
            }
        }); 
        layout.addComponent(pingButton);
        addComponent(grid);
        addComponent(layout);

    }
    
	private void switchNumOutput() {
		ipDropdown.removeAllItems();
		if (numOutput == false){
			testNode.setDisplayedName(testNode.ip);
	        ipDropdown.addItem(testNode.getDisplayedName());
	        numOutput = true;
		} else {
			testNode.setDisplayedName(testNode.name);
            ipDropdown.addItem(testNode.getDisplayedName());
            numOutput = false;
		}
		ipDropdown.select(testNode.getDisplayedName());
		ipDropdown.requestRepaint();
	}
}
