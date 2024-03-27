/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.topology.app.internal.gwt.client.d3;

public enum D3Events {
	
	CLICK("click"),
	MOUSE_DOWN("mousedown"),
	KEY_DOWN("keydown"), 
	CONTEXT_MENU("contextmenu"),
	DRAG_START("dragstart"),
	DRAG("drag"),
	DRAG_END("dragend"),
	MOUSE_WHEEL("mousewheel"),
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
