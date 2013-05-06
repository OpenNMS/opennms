/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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
package org.opennms.features.jmxconfiggenerator.webui.ui;

import java.util.Arrays;

public enum UiState {
	IntroductionView("Introduction", true), 
	ServiceConfigurationView("Service Configuration", true), 
	MbeansDetection("Determine MBeans information", false), 
	MbeansView("MBeans Configuration", true), 
	ResultConfigGeneration("Generate OpenNMS Configuration snippets", false), 
	ResultView("OpenNMS Configuration", true);

	private final String description;
	private boolean ui;

	private UiState(String description, boolean ui) {
		this.description = description;
		this.ui = ui;
	}

	boolean hasUi() {
		return ui;
	}

	public String getDescription() {
		return this.description;
	}

	public boolean hasPrevious() {
		return !isFirst();
	}

	public boolean hasNext() {
		return !isLast();
	}

	private boolean isFirst() {
		return UiState.values()[0].equals(this);
	}

	private boolean isLast() {
		return UiState.values()[UiState.values().length - 1].equals(this);
	}

	public UiState getPrevious() {
		if (hasPrevious()) {
			int currentIndex = Arrays.asList(UiState.values()).indexOf(this);
			return UiState.values()[currentIndex - 1];
		}
		return null; // no previous element
	}

	public UiState getNext() {
		if (hasNext()) {
			int currentIndex = Arrays.asList(UiState.values()).indexOf(this);
			return UiState.values()[currentIndex + 1];
		}
		return null; // no next element
	}
}
