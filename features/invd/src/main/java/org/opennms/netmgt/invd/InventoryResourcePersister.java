package org.opennms.netmgt.invd;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.dao.InventoryAssetDao;
import org.opennms.netmgt.dao.InventoryAssetPropertyDao;
import org.opennms.netmgt.dao.InventoryCategoryDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.inventory.OnmsInventoryAsset;
import org.opennms.netmgt.model.inventory.OnmsInventoryAssetProperty;
import org.opennms.netmgt.model.inventory.OnmsInventoryCategory;

public class InventoryResourcePersister {
	private InventoryCategoryDao m_invCategoryDao;
	private InventoryAssetDao m_invAssetDao;
	private InventoryAssetPropertyDao m_invAssetPropDao;
	private NodeDao m_nodeDao;
	
	public InventoryResourcePersister() {
		
	}
	
	public void persist(InventorySet invSet) {
		// Don't attempt to persist failed scans.
		if(invSet.getStatus() != InventoryScanner.SCAN_SUCCEEDED)
			return;
		
		// Cycle through each resource and attempt to persist it.
		for(InventoryResource res : invSet.getInventoryResources()) {
			persistResource(res);
		}
	}
	
	private void persistResource(InventoryResource res) {
		Boolean assetModified = false;
		OnmsNode ownerNode = getNodeDao().get(res.getOwnerNodeId());
		OnmsInventoryCategory cat = getInvCategoryDao().findByName(res.getResourceCategory());
		
		// Verify whether the category already exists or not.
		if(cat == null) {
			cat = new OnmsInventoryCategory(res.getResourceCategory());
		}
		
		// Attempt to retrieve an existing asset.
		OnmsInventoryAsset asset = getInvAssetDao().findByNameNodeAndCategory(
				res.getResourceName(), 
				ownerNode, 
				cat);		
		
		// Update an existing asset.
		if(asset != null) {
			
			
			// Change the source if applicable.
			if(!asset.getAssetSource().equalsIgnoreCase(res.getResourceSource())) {
				asset.setAssetSource(res.getResourceSource());
				assetModified = true;
			}
			
			// Revive cleaned up assets if re-found.
			if(asset.getEffStatus() == false) {
				asset.setEffStatus(true);
				assetModified = true;
			}
		// Create a new asset.
		} else {
			asset = new OnmsInventoryAsset(cat, 
					res.getResourceName(), 
					res.getResourceSource(), 
					ownerNode, 
					new Date(), 
					true);
		}
		
		// Update the last modified date.
		asset.setDateUpdated(new Date());
		
		// Cycle through the resource properties and persist them.
		List<String> propsUpdated = persistResourceProps(res, asset);
		
		// Check and see if any properties were updated.
		if(propsUpdated.size() > 0) {
			assetModified = true;
		}
		
		if(assetModified) {
			// Here's where we would emit an asset changed event.
		}
		
		// Save the asset.
		getInvAssetDao().save(asset);
	}
	
	private List<String> persistResourceProps(InventoryResource res, OnmsInventoryAsset asset) {
		Map<String, String> resourceProps = res.getResourceProperties();
		List<String> propsUpdated = new ArrayList<String>();
		
		// Cycle through all of the key/value property pairs scanned.
		for(String key : resourceProps.keySet()) {
			String value = resourceProps.get(key);
			
			// Retrieve the existing property if applicable.
			OnmsInventoryAssetProperty prop = asset.getPropertyByName(key);
			
			// Check to see if this is an existing property or a new one.
			if(prop != null) {
				if(!prop.getAssetValue().equals(value)) {
					prop.setAssetValue(value);
					
					// Add this property to the list of changes.
					propsUpdated.add(key);
				}
				
			// New property.
			} else {
				prop = new OnmsInventoryAssetProperty(key, value);
				
				propsUpdated.add(key);
			}
		}
		
		return propsUpdated;
	}
	
	// DAO methods.

	public InventoryCategoryDao getInvCategoryDao() {
		return m_invCategoryDao;
	}

	public void setInvCategoryDao(InventoryCategoryDao invCategoryDao) {
		m_invCategoryDao = invCategoryDao;
	}

	public InventoryAssetDao getInvAssetDao() {
		return m_invAssetDao;
	}

	public void setInvAssetDao(InventoryAssetDao invAssetDao) {
		m_invAssetDao = invAssetDao;
	}

	public InventoryAssetPropertyDao getInvAssetPropDao() {
		return m_invAssetPropDao;
	}

	public void setInvAssetPropDao(InventoryAssetPropertyDao invAssetPropDao) {
		m_invAssetPropDao = invAssetPropDao;
	}

	public NodeDao getNodeDao() {
		return m_nodeDao;
	}

	public void setNodeDao(NodeDao nodeDao) {
		m_nodeDao = nodeDao;
	}
	
	
}
