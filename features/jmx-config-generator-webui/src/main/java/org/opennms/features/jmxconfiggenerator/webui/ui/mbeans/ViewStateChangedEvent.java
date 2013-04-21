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

package org.opennms.features.jmxconfiggenerator.webui.ui.mbeans;

/**
 *
 * @author Markus von Rüden
 */
public class ViewStateChangedEvent {

	/**
	 * old view state
	 */
	private ViewState oldState;
	/**
	 * new view state
	 */
	private ViewState newState;
	/**
	 * which object is responsible for the view state change. Notice: Usually it is not the button or the element which
	 * invokes the view state change.
	 */
	private Object source;

	public ViewStateChangedEvent(ViewState oldState, ViewState newState, Object source) {
		this.oldState = oldState;
		this.newState = newState;
		this.source = source;
	}

	public ViewState getOldState() {
		return oldState;
	}

	public ViewState getNewState() {
		return newState;
	}

	public Object getSource() {
		return source;
	}
}
