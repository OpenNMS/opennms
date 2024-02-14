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
package org.opennms.web.svclayer.catstatus.model;


import java.util.Collection;
import java.util.ArrayList;

/**
 * <p>StatusNode class.</p>
 *
 * @author <a href="mailto:jason.aras@opennms.org">Jason Aras</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class StatusNode {
	private String m_label;
	private Boolean m_outagestatus;
	private Collection<StatusInterface> m_ipinterfaces;
	private Integer m_nodeid;
	
	
	/**
	 * <p>Constructor for StatusNode.</p>
	 */
	public StatusNode(){
		
		m_ipinterfaces = new ArrayList<>();
		
	}
	
	/**
	 * <p>addIpInterface</p>
	 *
	 * @param ipInterface a {@link org.opennms.web.svclayer.catstatus.model.StatusInterface} object.
	 */
	public void addIpInterface(StatusInterface ipInterface){
		
		m_ipinterfaces.add(ipInterface);
		
	}
	
	/**
	 * <p>getIpInterfaces</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<StatusInterface> getIpInterfaces(){
		
		return m_ipinterfaces;
		
	}
	
	
	/**
	 * <p>getLabel</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getLabel() {
		return m_label;
	}
	/**
	 * <p>setLabel</p>
	 *
	 * @param m_label a {@link java.lang.String} object.
	 */
	public void setLabel(String m_label) {
		this.m_label = m_label;
	}
	/**
	 * <p>getOutagestatus</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	public Boolean getOutagestatus() {
		return m_outagestatus;
	}
	/**
	 * <p>setOutagestatus</p>
	 *
	 * @param m_outagestatus a {@link java.lang.Boolean} object.
	 */
	public void setOutagestatus(Boolean m_outagestatus) {
		this.m_outagestatus = m_outagestatus;
	}

	/**
	 * <p>getNodeid</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getNodeid() {
		return m_nodeid;
	}

	/**
	 * <p>setNodeid</p>
	 *
	 * @param nodeid a {@link java.lang.Integer} object.
	 */
	public void setNodeid(Integer nodeid) {
		m_nodeid = nodeid;
	}
	

	
}
