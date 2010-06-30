/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 18, 2006
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.svclayer;

import java.util.Collection;
import java.util.Iterator;

import org.opennms.netmgt.dao.AttributeSecretDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsSecretAttribute;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.secret.model.GraphDefinition;

/**
 * <p>DefaultPerfGraphBuilderService class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.6.12
 */
public class DefaultPerfGraphBuilderService implements PerfGraphBuilderService {

	private NodeDao m_nodeDao;
	private AttributeSecretDao m_attributeDao;

	/**
	 * <p>createGraphDefinition</p>
	 *
	 * @return a {@link org.opennms.secret.model.GraphDefinition} object.
	 */
	public GraphDefinition createGraphDefinition() {
		return new GraphDefinition();
	}

	/** {@inheritDoc} */
	public void addAttributeToGraphDefinition(String attributeId, String graphDefId) {
		// TODO Auto-generated method stub
		
	}

	/** {@inheritDoc} */
	public Palette getAttributePalette(int nodeId) {
		OnmsNode node = m_nodeDao.get(nodeId);
		
		PaletteBuilder paletteBuilder = new PaletteBuilder(node.getLabel());

		// add node category
		paletteBuilder.addCategory("Node Attributes");
		populateCategory(paletteBuilder, m_attributeDao.getAttributesForNode(node));
		
		// now add the interface categories
		for (Iterator iter = node.getIpInterfaces().iterator(); iter.hasNext();) {
			OnmsIpInterface ipIface = (OnmsIpInterface) iter.next();
			paletteBuilder.addCategory("Interface: "+ipIface.getIpAddress());			
			populateCategory(paletteBuilder, m_attributeDao.getAttributesForInterface(ipIface));
			
			paletteBuilder.addSpacer();
		}
		
		
		return paletteBuilder.getPalette();
	}

	private void populateCategory(PaletteBuilder paletteBuilder, Collection<OnmsSecretAttribute> attributes) {
		if (attributes == null) return;
		for (OnmsSecretAttribute attribute : attributes) {
			paletteBuilder.addItem(attribute.getId(), attribute.getLabel());
		}
	}

	/** {@inheritDoc} */
	public byte[] getGraph(String graphDefId) {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public GraphDefinition getGraphDefinition(String graphDefId) {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	public void saveGraphDefinition(String graphDefId) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * <p>setNodeDao</p>
	 *
	 * @param nodeDao a {@link org.opennms.netmgt.dao.NodeDao} object.
	 */
	public void setNodeDao(NodeDao nodeDao) {
		m_nodeDao = nodeDao;
	}

	/**
	 * <p>setAttributeDao</p>
	 *
	 * @param attributeDao a {@link org.opennms.netmgt.dao.AttributeSecretDao} object.
	 */
	public void setAttributeDao(AttributeSecretDao attributeDao) {
		m_attributeDao = attributeDao;
	}



}
