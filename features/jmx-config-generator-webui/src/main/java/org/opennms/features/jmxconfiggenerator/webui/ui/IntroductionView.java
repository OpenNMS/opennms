/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc. OpenNMS(R) is Copyright (C)
 * 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 *
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 * *****************************************************************************
 */
package org.opennms.features.jmxconfiggenerator.webui.ui;

import org.opennms.features.jmxconfiggenerator.webui.JmxConfigGeneratorApplication;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class IntroductionView extends Panel implements ClickListener {

	private final Button next;
	private JmxConfigGeneratorApplication app;
	
	public IntroductionView(JmxConfigGeneratorApplication app) {
		this.app = app;
		next = UIHelper.createButton("next",  IconProvider.BUTTON_NEXT, this);
		
		setSizeFull();
		setContent(new VerticalLayout());
		getContent().setSizeFull();
		
		
		addComponent(new Label(UIHelper.loadContentFromFile(getClass(), "/descriptions/IntroductionView.html"),
				Label.CONTENT_RAW));
		addComponent(next);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		app.updateView(UiState.ServiceConfigurationView);
	}
}
