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

package org.opennms.features.topology.api.support;

import java.net.URL;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


/**
* The InfoWindow class constructs a custom Window component that contains an embedded 
* browser displaying the embeddedURL.
* @author Leonardo Bell
* @author Philip Grenon
* @version 1.0
*/
public class InfoWindow extends Window {
    /**
     * Is used to determine the label/caption for this pop up window.
     * @author Markus von Rüden
     *
     */
    public static interface LabelCreator {
            String getLabel();
    }
    
    private static final long serialVersionUID = -510407825043696244L;
	
    private final double sizePercentage = 0.80; // Window size ratio to the main window
    private final int widthCushion = 50; //Border cushion for width of window;
    private final int heightCushion = 110; //Border cushion for height of window
    private Embedded infoBrowser = null; //Browser component which is directed at the Resource Graphs page

    
    public InfoWindow(final URL embeddedURL, LabelCreator labelCreator) {
        infoBrowser = new Embedded("", new ExternalResource(embeddedURL));

        String label = labelCreator == null ? "" : labelCreator.getLabel();
        setCaption(label);
        setImmediate(true);
        setResizable(false);
        setModal(true);
        
        /*Adds the browser to the main layout*/
        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(infoBrowser);

        setContent(layout);
    }
    
    @Override
    public void attach() {
    	super.attach();
    	
    	int width = getUI().getPage().getBrowserWindowWidth();
    	int height = getUI().getPage().getBrowserWindowHeight();
    	
    	/*Sets the browser and window sizes based on the main window*/
        int browserWidth = (int)(sizePercentage * width), browserHeight = (int)(sizePercentage * height);
        int windowWidth = browserWidth + widthCushion, windowHeight = browserHeight + heightCushion;
        setWidth("" + windowWidth + "px");
        setHeight("" + windowHeight + "px");
        setPositionX((width - windowWidth)/2);
		setPositionY((height - windowHeight)/2);
        
        /*Sets the size of the browser to fit within the sub-window*/
        infoBrowser.setType(Embedded.TYPE_BROWSER);
        infoBrowser.setWidth("" + browserWidth + "px");
        infoBrowser.setHeight("" + browserHeight + "px");
    }
}
