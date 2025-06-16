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
 * <p>StatusCategory class.</p>
 *
 * @author <a href="mailto:jason.aras@opennms.org">Jason Aras</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class StatusCategory {

	private String m_label;
	private String m_comment;
	private Float m_normal;
	private Integer m_warning;
	private Boolean m_outagestatus;
	private String  m_filter;
	private Collection<StatusNode> m_nodelist;
	
	
	/**
	 * <p>Constructor for StatusCategory.</p>
	 */
	public StatusCategory(){
	
		m_nodelist = new ArrayList<>();
	
	}
	
	/**
	 * <p>getFilter</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getFilter() {
		
		return m_filter;
		
	}
	
	/**
	 * <p>setFilter</p>
	 *
	 * @param filter a {@link java.lang.String} object.
	 */
	public void setFilter(String filter) {
		
		m_filter = filter;
				
	}
	
	
	
	
	/**
	 * <p>getNodes</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<StatusNode> getNodes() {
	
		return m_nodelist;
	
	}
	
	
	/**
	 * <p>addNode</p>
	 *
	 * @param NewNode a {@link org.opennms.web.svclayer.catstatus.model.StatusNode} object.
	 */
	public void addNode(StatusNode NewNode){
		
		m_nodelist.add(NewNode);
		
	}
	

	/**
	 * <p>getOutageStatus</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	public Boolean getOutageStatus() {
		return m_outagestatus;
	}

	/**
	 * <p>setOutageStatus</p>
	 *
	 * @param OutageStatus a {@link java.lang.Boolean} object.
	 */
	public void setOutageStatus(Boolean OutageStatus) {
		m_outagestatus = OutageStatus;
	}
	
	/**
	 * <p>getComment</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getComment() {
		return m_comment;
	}


	/**
	 * <p>setComment</p>
	 *
	 * @param m_comment a {@link java.lang.String} object.
	 */
	public void setComment(String m_comment) {
		this.m_comment = m_comment;
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
	 * <p>getNormal</p>
	 *
	 * @return a {@link java.lang.Float} object.
	 */
	public Float getNormal() {
		return m_normal;
	}


	/**
	 * <p>setNormal</p>
	 *
	 * @param m_normal a {@link java.lang.Float} object.
	 */
	public void setNormal(Float m_normal) {
		this.m_normal = m_normal;
	}


	/**
	 * <p>getWarning</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getWarning() {
		return m_warning;
	}


	/**
	 * <p>setWarning</p>
	 *
	 * @param m_warning a {@link java.lang.Integer} object.
	 */
	public void setWarning(Integer m_warning) {
		this.m_warning = m_warning;
	}
	
}
