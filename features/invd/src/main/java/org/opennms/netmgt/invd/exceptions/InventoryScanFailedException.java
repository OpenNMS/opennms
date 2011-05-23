package org.opennms.netmgt.invd.exceptions;

public class InventoryScanFailedException extends InventoryException {
	private static final long serialVersionUID = -8887131469914354570L;

	public InventoryScanFailedException(int code) {
        super("Inventory scan failed for an unknown reason (code " + code + ".  Please review previous logs for this thread for details.  You can also open up an enhancement bug report (include your logs) to request that failure messages are logged for this type of error.");
    }
}
