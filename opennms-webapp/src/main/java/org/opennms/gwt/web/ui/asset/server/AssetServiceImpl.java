/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.gwt.web.ui.asset.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.gwt.web.ui.asset.client.AssetService;
import org.opennms.gwt.web.ui.asset.shared.AssetCommand;
import org.opennms.gwt.web.ui.asset.shared.AssetSuggCommand;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.api.SecurityContextService;
import org.opennms.web.springframework.security.Authentication;
import org.opennms.web.springframework.security.SpringSecurityContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * @author <a href="mailto:ronny@opennms.org">Ronny Trommer</a>
 */
@Transactional(readOnly = false)
public class AssetServiceImpl extends RemoteServiceServlet implements AssetService {
    private static final long serialVersionUID = -6571388103047893262L;

    private static final String AUTOENABLE = "A";
    private static final String SSH_CONNECTION = "ssh";
    private static final String TELNET_CONNECTION = "telnet";
    private static final String RSH_CONNECTION = "rsh";

    private static ArrayList<String> s_autoenableOptions = new ArrayList<String>();
    private static ArrayList<String> s_connectionOptions = new ArrayList<String>();

    static {
        s_autoenableOptions.add(AUTOENABLE);
        //TODO added "" to be able to remove AUTOENABLE again. this could cause problems at the AUTOENABLE reading code.
        s_autoenableOptions.add("");

        s_connectionOptions.add(TELNET_CONNECTION);
        s_connectionOptions.add(SSH_CONNECTION);
        s_connectionOptions.add(RSH_CONNECTION);
        //TODO added "" to be able to remove connection again. this could cause problems at the connection reading code.
        s_connectionOptions.add("");
    }

    private final Logger logger = LoggerFactory.getLogger("OpenNMS.WEB." + AssetServiceImpl.class.getName());

    /**
     * asset data access object for asset records
     */
    private AssetRecordDao m_assetRecordDao;

    /**
     * node data access object for nodes
     */
    private NodeDao m_nodeDao;

    /**
     * node object with asset record
     */
    private OnmsNode m_onmsNode;

    /**
     * asset record object
     */
    private OnmsAssetRecord m_onmsAssetRecord;

    /**
     * web security context service for user name and role
     */
    private SecurityContextService m_securityContext;

    private HashSet<String> m_allowHtmlFields;

    /**
     *
     */
    public AssetServiceImpl() {
        m_securityContext = new SpringSecurityContextService();

        /*
         * Init AllowHtmlFields for sanitizing Strings
         */
        m_allowHtmlFields = new HashSet<String>();
        final String allowHtmlFieldNames = System.getProperty("opennms.assets.allowHtmlFields");
        
        if (allowHtmlFieldNames != null) {
            for (String fieldName : allowHtmlFieldNames.split("\\s*,\\s*")) {
                m_allowHtmlFields.add(fieldName.toLowerCase());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AssetCommand getAssetByNodeId(int nodeId) {
        AssetCommand assetCommand = new AssetCommand();
        m_onmsNode = m_nodeDao.get(nodeId);
        m_onmsAssetRecord = m_onmsNode.getAssetRecord();
        logger.debug("onmsAssetRecord '{}'", m_onmsAssetRecord);
        // copy all assetRecord properties to assetCommand for gui
        BeanUtils.copyProperties(m_onmsAssetRecord, assetCommand);

        //This manual null to "" settings is to prevent problems caused by different behavior of browsers in null handling in select boxes
        if (assetCommand.getConnection() == null) {
            assetCommand.setConnection("");
        }
        if (assetCommand.getAutoenable() == null) {
            assetCommand.setAutoenable("");
        }


        // set node specific properties for the asset node page
        assetCommand.setSnmpSysContact(m_onmsNode.getSysContact());
        assetCommand.setSnmpSysDescription(m_onmsNode.getSysDescription());
        assetCommand.setSnmpSysLocation(m_onmsNode.getSysLocation());
        assetCommand.setSnmpSysName(m_onmsNode.getSysName());
        assetCommand.setSnmpSysObjectId(m_onmsNode.getSysObjectId());

        // set static arrays for gui options
        assetCommand.setAutoenableOptions(s_autoenableOptions);
        assetCommand.setConnectionOptions(s_connectionOptions);

        assetCommand.setNodeId(m_onmsNode.getNodeId());
        assetCommand.setNodeLabel(m_onmsNode.getLabel());

        // set user from web ui session
        assetCommand.setLoggedInUser(m_securityContext.getUsername());

        // This is a poor re-implementation of modify permission based on spring
        // roles
        if (m_securityContext.hasRole(Authentication.ROLE_ADMIN) || m_securityContext.hasRole(Authentication.ROLE_PROVISION)) {
            assetCommand.setAllowModify(true);
        } else {
            assetCommand.setAllowModify(false);
        }

        // assign the asset record back to the node
        m_onmsAssetRecord.setNode(m_onmsNode);

        logger.debug("assetCommand: '{}'", assetCommand);
        return assetCommand;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AssetSuggCommand getAssetSuggestions() {
        // The suggestion model transfered by RPC between webui and service
        final AssetSuggCommand suggestion = new AssetSuggCommand();

        // a list of all asset records which contains all distinct asset properties for suggestion
        final List<OnmsAssetRecord> distinctAssetProperties = m_assetRecordDao.getDistinctProperties();

        // Map all distinct asset properties
        for (final OnmsAssetRecord asset : distinctAssetProperties) {
            suggestion.addAdditionalhardware(asset.getAdditionalhardware());
            final OnmsGeolocation geolocation = asset.getGeolocation();
            suggestion.addAddress1(geolocation.getAddress1());
            suggestion.addAddress2(geolocation.getAddress2());
            suggestion.addAdmin(asset.getAdmin());
            suggestion.addBuilding(asset.getBuilding());
            suggestion.addCategory(asset.getCategory());
            suggestion.addCircuitId(asset.getCircuitId());
            suggestion.addCity(geolocation.getCity());
            suggestion.addCountry(geolocation.getCountry());
            suggestion.addCpu(asset.getCpu());
            suggestion.addDepartment(asset.getDepartment());
            suggestion.addDescription(asset.getDescription());
            suggestion.addDisplayCategory(asset.getDisplayCategory());
            suggestion.addDivision(asset.getDivision());
            suggestion.addFloor(asset.getFloor());
            suggestion.addHdd1(asset.getHdd1());
            suggestion.addHdd2(asset.getHdd2());
            suggestion.addHdd3(asset.getHdd3());
            suggestion.addHdd4(asset.getHdd4());
            suggestion.addHdd5(asset.getHdd5());
            suggestion.addHdd6(asset.getHdd6());
            suggestion.addInputpower(asset.getInputpower());
            suggestion.addLease(asset.getLease());
            suggestion.addMaintcontract(asset.getMaintcontract());
            suggestion.addManufacturer(asset.getManufacturer());
            suggestion.addModelNumber(asset.getModelNumber());
            suggestion.addNotifyCategory(asset.getNotifyCategory());
            suggestion.addNumpowersupplies(asset.getNumpowersupplies());
            suggestion.addOperatingSystem(asset.getOperatingSystem());
            suggestion.addPollerCategory(asset.getPollerCategory());
            suggestion.addRack(asset.getRack());
            suggestion.addRam(asset.getRam());
            suggestion.addRegion(asset.getRegion());
            suggestion.addRoom(asset.getRoom());
            suggestion.addSnmpcommunity(asset.getSnmpcommunity());
            suggestion.addState(geolocation.getState());
            suggestion.addStoragectrl(asset.getStoragectrl());
            suggestion.addSupportPhone(asset.getSupportPhone());
            suggestion.addThresholdCategory(asset.getThresholdCategory());
            suggestion.addVendor(asset.getVendor());
            suggestion.addVendorFax(asset.getVendorFax());
            suggestion.addVendorPhone(asset.getVendorPhone());
            suggestion.addZip(geolocation.getZip());

            // VMware monitoring assets
            suggestion.addVmwareManagedObjectId(asset.getVmwareManagedObjectId());
            suggestion.addVmwareManagedEntityType(asset.getVmwareManagedEntityType());
            suggestion.addVmwareManagementServer(asset.getVmwareManagementServer());

            // VMware topology assets
            suggestion.addVmwareTopologyInfo(asset.getVmwareTopologyInfo());
            suggestion.addVmwareState(geolocation.getState());

        }
        return suggestion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean saveOrUpdateAssetByNodeId(int nodeId, AssetCommand assetCommand) {

        logger.debug("nodeId: '{}' assetCommand: '{}'", nodeId, assetCommand);

        Boolean isSaved = false;
        m_onmsNode = m_nodeDao.get(nodeId);
        m_onmsAssetRecord = m_onmsNode.getAssetRecord();
        OnmsGeolocation geolocation = m_onmsAssetRecord.getGeolocation();

        logger.debug("gelocation before: {}", geolocation);

        // copy the transfer object for rpc back to the hibernate model
        final AssetCommand sanitizeBeanStringProperties = WebSecurityUtils.sanitizeBeanStringProperties(assetCommand, m_allowHtmlFields);
        
        // logger.debug("nodeId: '{}' sanitized assetCommand: '{}'", nodeId, sanitizeBeanStringProperties);

        BeanUtils.copyProperties(sanitizeBeanStringProperties, m_onmsAssetRecord);

        geolocation = m_onmsAssetRecord.getGeolocation();
        logger.debug("gelocation after: {}", geolocation);

        // logger.debug("copyProperties finished");

        if (geolocation == null) {
            geolocation = new OnmsGeolocation();
            m_onmsAssetRecord.setGeolocation(geolocation);
        }
        
        // logger.debug("geolocation: {}", geolocation);

        geolocation.setLongitude(sanitizeBeanStringProperties.getLongitude());
        geolocation.setLatitude(sanitizeBeanStringProperties.getLatitude());

        logger.debug("OnmsAssetRecord: '{}'", m_onmsAssetRecord);

        // set the last modified user from logged in user
        m_onmsAssetRecord.setLastModifiedBy(m_securityContext.getUsername());

        // set last modified date and assign the node for the asset record
        m_onmsAssetRecord.setLastModifiedDate(new Date());
        m_onmsAssetRecord.setNode(m_onmsNode);

        // try to persist the asset record from the web ui
        try {
            logger.debug("OnmsNode '{}'", m_onmsNode.toString());
            logger.debug("AssetRecordDao to update '{}'", m_assetRecordDao.toString());
            logger.debug("OnmsAssetRecord to update '{}'", m_onmsAssetRecord.toString());

            m_assetRecordDao.saveOrUpdate(m_onmsAssetRecord);
            isSaved = true;
        } catch (Exception e) {
            // TODO: Catch exception and show error in web user interface
            isSaved = false;
            logger.error("Problem during saving or updating assets '{}'", e.getMessage());
            e.printStackTrace();
        }

        // save was successful
        return isSaved;
    }

    /**
     * <p>
     * getAssetRecordDao
     * </p>
     *
     * @return assetRecordDao a {@link org.opennms.netmgt.model.OnmsAssetRecord}
     */
    public AssetRecordDao getAssetRecordDao() {
        return m_assetRecordDao;
    }

    /**
     * <p>
     * setAssetRecordDao
     * </p>
     *
     * @param m_assetRecordDao a {@link org.opennms.netmgt.model.OnmsAssetRecord}
     */
    public void setAssetRecordDao(AssetRecordDao assetRecordDao) {
        m_assetRecordDao = assetRecordDao;
    }

    /**
     * <p>
     * getNodeDao
     * </p>
     *
     * @return m_nodeDao a {@link org.opennms.netmgt.dao.api.NodeDao}
     */
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    /**
     * <p>
     * setNodeDao
     * </p>
     *
     * @param m_nodeDao a {@link org.opennms.netmgt.dao.api.NodeDao}
     */
    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }
}
