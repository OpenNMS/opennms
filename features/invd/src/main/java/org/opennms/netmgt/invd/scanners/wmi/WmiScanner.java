//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.invd.scanners.wmi;

import org.opennms.netmgt.invd.InventoryScanner;
import org.opennms.netmgt.invd.ScanningClient;
import org.opennms.netmgt.invd.InventorySet;
import org.opennms.netmgt.invd.exceptions.InventoryException;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.WmiPeerFactory;
import org.opennms.netmgt.config.invd.wmi.WmiAsset;
import org.opennms.netmgt.config.invd.wmi.WmiAssetProperty;
import org.opennms.netmgt.config.invd.wmi.WmiCategory;
import org.opennms.netmgt.config.invd.wmi.WmiInventory;
import org.opennms.netmgt.dao.WmiInvScanConfigDao;
import org.opennms.protocols.wmi.WmiClient;
import org.opennms.protocols.wmi.WmiException;
import org.opennms.protocols.wmi.WmiManager;
import org.opennms.protocols.wmi.WmiParams;
import org.opennms.protocols.wmi.WmiResult;
import org.opennms.protocols.wmi.wbem.OnmsWbemObjectSet;
import org.opennms.protocols.wmi.wbem.OnmsWbemObject;
import org.opennms.protocols.wmi.wbem.OnmsWbemProperty;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.ThreadCategory;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;
import java.beans.PropertyVetoException;

public class WmiScanner implements InventoryScanner {
    private final HashMap<Integer, WmiClientState> m_scheduledNodes = new HashMap<Integer, WmiClientState>();
    
    private WmiInvScanConfigDao m_wmiInvScanConfigDao;

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    
    public WmiInvScanConfigDao getWmiInvScanConfigDao() {
		return m_wmiInvScanConfigDao;
	}

	public void setWmiInvScanConfigDao(WmiInvScanConfigDao wmiInvScanConfigDao) {
		m_wmiInvScanConfigDao = wmiInvScanConfigDao;
	}

	public void initialize(Map<String, String> parameters) {
        log().debug("initialize: Initializing WmiScanner.");
        m_scheduledNodes.clear();
        
        // Retrieve the DAO for our configuration file.
        m_wmiInvScanConfigDao = BeanUtils.getBean("daoContext", "wmiInvScanConfigDao", WmiInvScanConfigDao.class);
        
        initWMIPeerFactory();
        initDatabaseConnectionFactory();
    }

    public void initialize(ScanningClient client, Map<String, String> parameters) {
        log().debug("initialize: Initializing WMI scanning for client: " + client);
        Integer scheduledNodeKey = client.getNodeId();
        WmiClientState nodeState = m_scheduledNodes.get(scheduledNodeKey);

        if (nodeState != null) {
            log().info("initialize: Not scheduling interface for WMI inventory scanning: " + nodeState.getAddress());
            final StringBuffer sb = new StringBuffer();
            sb.append("initialize service: ");

            sb.append(" for address: ");
            sb.append(nodeState.getAddress());
            sb.append(" already scheduled for scanning on node: ");
            sb.append(client);
            log().debug(sb.toString());
            throw new IllegalStateException(sb.toString());
        } else {
            nodeState = new WmiClientState(client.getInetAddress(), parameters);
            log().info("initialize: Scheduling interface for inventory scan: " + nodeState.getAddress());
            m_scheduledNodes.put(scheduledNodeKey, nodeState);
        }
    }

    public void release() {
        m_scheduledNodes.clear();
    }

    public void release(ScanningClient agent) {
        WmiClientState nodeState = m_scheduledNodes.get(agent.getNodeId());
        if (nodeState != null) {
            m_scheduledNodes.remove(agent.getNodeId());
        }
    }

    public InventorySet collect(ScanningClient client, EventProxy eproxy, Map<String, String> parameters) throws InventoryException {
        String inventoryName = parameters.get("collection");

        // Find attributes to collect - check groups in configuration. For each,
        // check scheduled nodes to see if that group should be collected
        WmiInventory inventory = getWmiInvScanConfigDao().getWmiInventoryByName(inventoryName);
        WmiClientState clientState = m_scheduledNodes.get(client.getNodeId());

        // Load the attribute group types.
        //loadAttributeGroupList(collection);

        // Load the attribute types.
        //loadAttributeTypeList(collection);

        // Load the category objects.
        //loadCategoryObjectList(collection);

        // Create a new collection set.
        WmiInventorySet inventorySet = new WmiInventorySet(/*agent*/);

        // Iterate through the WMI inventory categories.
        for (WmiCategory category : inventory.getWmiCategories()) {
            // Iterate through each asset in the category.
            for (WmiAsset asset : category.getWmiAssets()) {
                // Check to see if we should use WMI to verify the existance of this asset on the
                // client.
                if (clientState.shouldCheckAvailability(asset.getName(), asset.getRecheckInterval())) {
                    // And if the check shows it's not available, skip to the next asset.
                    if (!isGroupAvailable(clientState, asset)) continue;
                }

                if (clientState.assetIsAvailable(asset.getName())) {
                    try {
                        clientState.connect();

                        WmiClient wmiClient = (WmiClient) clientState.getWmiClient();

                        OnmsWbemObjectSet wOS;
                        wOS = wmiClient.performInstanceOf(asset.getWmiClass());

                        if (wOS != null) {
                            //  Go through each object (class instance) in the object set.
                            for (int i = 0; i < wOS.count(); i++) {
                                //OnmsInventoryAsset invAsset = new OnmsInventoryAsset();
                                WmiInventoryResource resource = new WmiInventoryResource();

                                // Fetch our WBEM Object
                                OnmsWbemObject obj = wOS.get(i);

                                OnmsWbemProperty prop = obj.getWmiProperties().getByName(asset.getNameProperty());
                                Object propVal = prop.getWmiValue();
                                String instance;
                                if (propVal instanceof String) {
                                    instance = (String) propVal;
                                } else {
                                    instance = propVal.toString();
                                }
                                resource.setResourceName(instance);
                                resource.setResourceSource("WMI");
                                resource.setResourceCategory(category.getName());
                                resource.setOwnerNodeId(client.getNodeId());
                                resource.setResourceDate(new Date());

                                for(WmiAssetProperty assetProp : asset.getWmiAssetProperties()) {
                                    String assetName = assetProp.getName();
                                    String assetPropName = assetProp.getWmiProperty();
                                    String assetPropValue;

                                    OnmsWbemProperty wobjProp = obj.getWmiProperties().getByName(assetPropName);
                                    Object objPropVal = wobjProp.getWmiValue();

                                    // We have to check this because some asset properties may be null.
                                    // If it's null, default the value, otherwise we ened to check the type
                                    // of the object to determine how to convert it to a string.
                                    if (objPropVal == null) {
                                        assetPropValue = "";
                                    } else {

                                        if (objPropVal instanceof String) {
                                            assetPropValue = (String) objPropVal;
                                        } else {
                                            assetPropValue = objPropVal.toString();
                                        }
                                    }
                                    resource.getResourceProperties().put(assetName, assetPropValue);
                                }
                                inventorySet.getInventoryResources().add(resource);
                            }
                        }

                        wmiClient.disconnect();
                    } catch (WmiException e) {
                        log().info("unable to collect inventory for asset '" + asset.getName() + "'", e);
                    }
                }
            }
        }

        inventorySet.setStatus(InventoryScanner.SCAN_SUCCEEDED);
        return inventorySet;

    }

    private boolean isGroupAvailable(WmiClientState clientState, WmiAsset asset) {
        log().debug("Checking availability of group " + asset.getName());
        WmiManager manager;

        /*
         * We provide a bogus comparison value and use an operator of "NOOP"
         * to ensure that, regardless of results, we receive a result and perform
         * no logic. We're only validating that the agent is reachable and gathering
         * the result objects.
         */
        try {
            // Get and initialize the WmiManager
            manager = clientState.getManager();
            manager.init();

            WmiParams params = new WmiParams(WmiParams.WMI_OPERATION_INSTANCEOF, "not-applicable", "NOOP",
                    asset.getWmiClass(), asset.getNameProperty());

            WmiResult result = manager.performOp(params);
            manager.close();
            boolean isAvailable = (result.getResultCode() == WmiResult.RES_STATE_OK);
            clientState.setAssetIsAvailable(asset.getName(), isAvailable);
            log().debug("Group " + asset.getName() + " is " + (isAvailable ? "" : "not") + "available ");
        } catch (WmiException e) {
            //throw new WmiCollectorException("Error checking group (" + wpm.getName() + ") availability", e);
            // Log a warning signifying that this group is unavailable.
            log().warn("Error checking group (" + asset.getName() + ") availability", e);
            // Set the group as unavailable.
            clientState.setAssetIsAvailable(asset.getName(), false);
            // And then continue on to check the next wpm entry.
            return false;
        }
        return true;
    }

    private void initDatabaseConnectionFactory() {
        try {
            DataSourceFactory.init();
        } catch (IOException e) {
            log().fatal("initDatabaseConnectionFactory: IOException getting database connection", e);
            throw new UndeclaredThrowableException(e);
        } catch (MarshalException e) {
            log().fatal("initDatabaseConnectionFactory: Marshall Exception getting database connection", e);
            throw new UndeclaredThrowableException(e);
        } catch (ValidationException e) {
            log().fatal("initDatabaseConnectionFactory: Validation Exception getting database connection", e);
            throw new UndeclaredThrowableException(e);
        } catch (SQLException e) {
            log().fatal("initDatabaseConnectionFactory: Failed getting connection to the database.", e);
            throw new UndeclaredThrowableException(e);
        } catch (PropertyVetoException e) {
            log().fatal("initDatabaseConnectionFactory: Failed getting connection to the database.", e);
            throw new UndeclaredThrowableException(e);
        } catch (ClassNotFoundException e) {
            log().fatal("initDatabaseConnectionFactory: Failed loading database driver.", e);
            throw new UndeclaredThrowableException(e);
        }
    }

    private void initWMIPeerFactory() {
        log().debug("initialize: Initializing WmiPeerFactory");
        try {
            WmiPeerFactory.init();
        } catch (MarshalException e) {
            log().fatal("initialize: Error marshalling configuration.", e);
            throw new UndeclaredThrowableException(e);
        } catch (ValidationException e) {
            log().fatal("initialize: Error validating configuration.", e);
            throw new UndeclaredThrowableException(e);
        } catch (IOException e) {
            log().fatal("initialize: Error reading configuration", e);
            throw new UndeclaredThrowableException(e);
        }
    }
}
