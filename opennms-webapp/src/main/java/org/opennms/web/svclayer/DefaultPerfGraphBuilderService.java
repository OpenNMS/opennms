package org.opennms.web.svclayer;

import java.util.Collection;
import java.util.Iterator;

import org.opennms.netmgt.dao.AttributeSecretDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsSecretAttribute;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.secret.model.GraphDefinition;

public class DefaultPerfGraphBuilderService implements PerfGraphBuilderService {

	private NodeDao m_nodeDao;
	private AttributeSecretDao m_attributeDao;

	public GraphDefinition createGraphDefinition() {
		return new GraphDefinition();
	}

	public void addAttributeToGraphDefinition(String attributeId, String graphDefId) {
		// TODO Auto-generated method stub
		
	}

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

	public byte[] getGraph(String graphDefId) {
		// TODO Auto-generated method stub
		return null;
	}

	public GraphDefinition getGraphDefinition(String graphDefId) {
		// TODO Auto-generated method stub
		return null;
	}

	public void saveGraphDefinition(String graphDefId) {
		// TODO Auto-generated method stub
		
	}

	public void setNodeDao(NodeDao nodeDao) {
		m_nodeDao = nodeDao;
	}

	public void setAttributeDao(AttributeSecretDao attributeDao) {
		m_attributeDao = attributeDao;
	}



}
