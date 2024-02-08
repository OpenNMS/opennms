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
package org.opennms.features.topology.api.topo;

import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.BeanItem;

public class AbstractVertex extends DefaultVertexRef implements Vertex {

	private String m_tooltipText;
	private String m_iconKey;
	private String m_styleName;
	private Integer m_x;
	private Integer m_y;
	private boolean m_selected;
	private boolean m_locked = false;
	private String m_ipAddr;
	private Integer m_nodeID;
	private Integer m_edgePathOffset;

	@Deprecated
	public AbstractVertex(String namespace, String id) {
		super(namespace, id);
	}

    public AbstractVertex(String namespace, String id, String label){
        super(namespace, id, label);
    }
	/**
	 * @deprecated Use namespace/id tuple
	 */
	@Override
	public final String getKey() {
		return getNamespace() + ":" + getId();
	}

	@Override
	public Item getItem() {
		return new BeanItem<AbstractVertex>(this);
	}

	@Override
	public String getTooltipText() {
		return m_tooltipText != null ? m_tooltipText : getLabel();
	}

	public final void setTooltipText(String tooltpText) {
		m_tooltipText = tooltpText;
	}

	@Override
	public String getIconKey() {
		return m_iconKey;
	}

	public final void setIconKey(String iconKey) {
		m_iconKey = iconKey;
	}

	@Override
	public String getStyleName() {
		return m_styleName;
	}

	public final void setStyleName(String styleName) {
		m_styleName = styleName;
	}

	@Override
	public final Integer getX() {
		return m_x;
	}

	public final void setX(Integer x) {
		m_x = x;
	}

	@Override
	public final Integer getY() {
		return m_y;
	}

	public final void setY(Integer y) {
		m_y = y;
	}

	@Override
	public final boolean isLocked() {
		return m_locked;
	}

	public final void setLocked(boolean locked) {
		m_locked = locked;
	}

	@Override
	public final boolean isSelected() {
		return m_selected;
	}

	public final void setSelected(boolean selected) {
		m_selected = selected;
	}

	@Override
	public String getIpAddress() {
		return m_ipAddr;
	}

	public final void setIpAddress(String ipAddr){
		m_ipAddr = ipAddr;
	}

	@Override
	public final Integer getNodeID() {
		return m_nodeID;
	}

	public final void setNodeID(Integer nodeID) {
		m_nodeID = nodeID;
	}

	@Override
	public Integer getEdgePathOffset() {
		return m_edgePathOffset;
	}

	public void setEdgePathOffset(Integer edgePathOffset) {
		m_edgePathOffset = edgePathOffset;
	}

	 @Override
	 public String toString() { return "Vertex:"+getNamespace()+":"+getId() + "[label="+getLabel()+", styleName="+getStyleName()+"]"; }


}
