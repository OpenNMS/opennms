package org.opennms.netmgt.invd.scanners;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.invd.InventoryResource;
import org.opennms.netmgt.invd.InventoryScanner;
import org.opennms.netmgt.invd.InventorySet;
import org.opennms.netmgt.invd.ScanningClient;
import org.opennms.netmgt.invd.exceptions.InventoryException;
import org.opennms.netmgt.model.events.EventProxy;

public class InventoryScannerStub implements InventoryScanner {

	public void initialize(Map<String, String> parameters) {
		// TODO Auto-generated method stub

	}

	public void release() {
		// TODO Auto-generated method stub

	}

	public void initialize(ScanningClient agent, Map<String, String> parameters) {
		// TODO Auto-generated method stub

	}

	public void release(ScanningClient agent) {
		// TODO Auto-generated method stub

	}

	public InventorySet collect(ScanningClient client, EventProxy eproxy,
			Map<String, String> parameters) throws InventoryException {
		InventorySet collectionSetResult=new InventorySet() {

            public int getStatus() {
                return InventoryScanner.SCAN_SUCCEEDED;
            }

            /*public void visit(CollectionSetVisitor visitor) {
                visitor.visitCollectionSet(this);
                visitor.completeCollectionSet(this);
            } */

			@SuppressWarnings("unused")
			public boolean ignorePersist() {
				return false;
			}

            public List<InventoryResource> getInventoryResources() {
                return new ArrayList<InventoryResource>();
            }
        };
		return collectionSetResult;
	}

}
