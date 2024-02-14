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

import org.opennms.features.topology.api.topo.simple.SimpleConnector;

import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.BeanItem;

public class AbstractEdge extends AbstractEdgeRef implements Edge {

	private final SimpleConnector m_source;
	private final SimpleConnector m_target;

	private String m_tooltipText;
	private String m_styleName;

	public AbstractEdge(String namespace, String id, VertexRef source, VertexRef target) {
		super(namespace, id);
		if (source == null) {
			throw new IllegalArgumentException("Source is null");
		} else if (target == null) {
			throw new IllegalArgumentException("Target is null");
		}
		m_source = new SimpleConnector(namespace, id + "::" + source.getId(), source.getLabel() + " Connector", source, this);
		m_target = new SimpleConnector(namespace, id + "::" + target.getId(), target.getLabel() + " Connector", target, this);
		m_styleName = "edge";
	}

	public AbstractEdge(String namespace, String id, SimpleConnector source, SimpleConnector target) {
		super(namespace, id);
		m_source = source;
		m_target = target;
		m_styleName = "edge";
	}

	// Constructor to make cloneable easier for sub classes
	protected AbstractEdge(AbstractEdge edgeToClone) {
		this(edgeToClone.getNamespace(), edgeToClone.getId(), edgeToClone.getSource().clone(), edgeToClone.getTarget().clone());
		setLabel(edgeToClone.getLabel());
		setStyleName(edgeToClone.getStyleName());
		setTooltipText(edgeToClone.getTooltipText());
	}

	@Override
	public AbstractEdge clone() {
		return new AbstractEdge(this);
	}

	@Override
	public void setId(String id) {
		m_id = id;
	}

	/**
	 * @deprecated Use namespace/id tuple
	 */
	@Override
	public String getKey() {
		return getNamespace() + ":" + getId();
	}

	@Override
	public String getTooltipText() {
		return m_tooltipText;
	}

	@Override
	public final String getStyleName() {
		return m_styleName;
	}

	@Override
	public final void setTooltipText(String tooltipText) {
		m_tooltipText = tooltipText;
	}

	@Override
	public final void setStyleName(String styleName) {
		m_styleName = styleName;
	}

	@Override
	public Item getItem() {
		return new BeanItem<AbstractEdge>(this);
	}

	@Override
	public final SimpleConnector getSource() {
		return m_source;
	}

	@Override
	public final SimpleConnector getTarget() {
		return m_target;
	}

	@Override
	public String toString() {
		final StringBuilder retval = new StringBuilder();
		retval.append("Edge:").append(getNamespace()).append(":").append(getId());
		retval.append("[");
		retval.append("label=").append(getLabel());
		retval.append(",");
		retval.append("styleName=").append(getStyleName());
		retval.append(",");
		retval.append("source=").append(getSource().getVertex().getLabel());
		retval.append(",");
		retval.append("target=").append(getTarget().getVertex().getLabel());
		retval.append("]");
		return retval.toString();
	}
}
