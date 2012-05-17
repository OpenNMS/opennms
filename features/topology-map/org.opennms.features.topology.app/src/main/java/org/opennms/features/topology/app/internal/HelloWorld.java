package org.opennms.features.topology.app.internal;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window;

public class HelloWorld extends Application {

	@Override
	public void init() {
		
		Button hello = new Button("Say Hello Stupid");
		hello.addListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				HelloWorld.this.getMainWindow().showNotification("Hello Stupid!");
			}
			
		});
		
		Window mainWindow = new Window("Hello World");
		
		mainWindow.addComponent(hello);
		
		
		
		
		setMainWindow(mainWindow);
		
		
	}

}
