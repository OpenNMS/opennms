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

package org.opennms.features.topology.app.internal.gwt.client.d3;

public enum D3Events {
	
	CLICK("click"),
	MOUSE_DOWN("mousedown"),
	KEY_DOWN("keydown"), 
	CONTEXT_MENU("contextmenu"),
	DRAG_START("dragstart"),
	DRAG("drag"),
	DRAG_END("dragend"), 
	MOUSE_OVER("mouseover"), 
	MOUSE_OUT("mouseout"),
	DOUBLE_CLICK("dblclick");
	
	private String m_event;
	
	D3Events(String event){
		m_event = event;
	}
	
	public String event() {
		return m_event;
	}
	
	public interface Handler <T>{
		public void call(T t, int index);
	}
	
	public interface XMLHandler<T>{
	    public void call(T t);
	}
	
	public interface AnonymousHandler{
	    public void call();
	}
}
