package org.opennms.web.svclayer;

import java.util.Iterator;

import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.secret.model.GraphDefinition;

public class DefaultPerfGraphBuilderService implements PerfGraphBuilderService {

	private NodeDao m_nodeDao;

	public GraphDefinition createGraphDefinition() {
		return new GraphDefinition();
	}

	public void addAttributeToGraphDefinition(String attributeId, String graphDefId) {
		// TODO Auto-generated method stub
		
	}

	public Palette getAttributePalette(int nodeId) {
		OnmsNode node = m_nodeDao.get(nodeId);
		Palette palette = new Palette(node.getLabel());
		palette.setLabel(node.getLabel());

		// add node category
		PaletteCategory nodeCategory = new PaletteCategory("Node Attributes");
		palette.addCategory(nodeCategory);
		
		// now add the interface categories
		for (Iterator iter = node.getIpInterfaces().iterator(); iter.hasNext();) {
			OnmsIpInterface ipIface = (OnmsIpInterface) iter.next();
			PaletteCategory ifCategory = new PaletteCategory("Interface: "+ipIface.getIpAddress());
			palette.addCategory(ifCategory);
		}
		
		
		return palette;
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



}
