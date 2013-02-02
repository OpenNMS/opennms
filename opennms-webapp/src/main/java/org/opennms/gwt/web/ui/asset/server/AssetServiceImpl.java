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

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.gwt.web.ui.asset.client.AssetService;
import org.opennms.gwt.web.ui.asset.shared.AssetCommand;
import org.opennms.gwt.web.ui.asset.shared.AssetSuggCommand;
import org.opennms.netmgt.dao.AssetRecordDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.api.SecurityContextService;
import org.opennms.web.springframework.security.Authentication;
import org.opennms.web.springframework.security.SpringSecurityContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * @author <a href="mailto:ronny@opennms.org">Ronny Trommer</a>
 */
@Transactional(readOnly = false)
public class AssetServiceImpl extends RemoteServiceServlet implements AssetService {

    /**
     * generated serial
     */
    private static final long serialVersionUID = 3847574674959207209L;

    /**
     * Constant <code>AUTOENABLE="A"</code>
     */
    private static final String AUTOENABLE = "A";

    /**
     * Constant <code>AUTOENABLES="new ArrayList<String> { AUTOENABLE }"</code>
     */
    private static final ArrayList<String> s_autoenableOptions = new ArrayList<String>();

    /**
     * Constant <code>SSH_CONNECTION="ssh"</code>
     */
    private static final String SSH_CONNECTION = "ssh";

    /**
     * Constant <code>TELNET_CONNECTION="ssh"</code>
     */
    private static final String TELNET_CONNECTION = "telnet";

    /**
     * Constant <code>RSH_CONNECTION="rsh"</code>
     */
    private static final String RSH_CONNECTION = "rsh";

    /**
     * ROLE_ADMIN is allowed to edit
     */
    private static final String ALLOW_EDIT_ROLE_ADMIN = Authentication.ROLE_ADMIN;

    /**
     * ROLE_PROVISIONING is allowed to edit
     */
    private static final String ALLOW_EDIT_ROLE_PROVISION = Authentication.ROLE_PROVISION;

    /**
     * Constant
     * <code>CONNECTIONS="new ArrayList<String>{ TELNET_CONNECTION,SSH_CO"{trunked}</code>
     */
    private static final ArrayList<String> s_connectionOptions = new ArrayList<String>();

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

    private HashSet<String> s_allowHtmlFields;

    /**
     *
     */
    public AssetServiceImpl() {
        this.m_securityContext = new SpringSecurityContextService();

		/*
         * Init static strings for autoenable option TODO: Should be
		 * configurable, we take this over from the old JSP version
		 */
        s_autoenableOptions.add(AUTOENABLE);
        //TODO added "" to be able to remove AUTOENABLE again. this could cause problems at the AUTOENABLE reading code.
        s_autoenableOptions.add("");


		/*
         * Init static strings for connection types TODO: Should be
		 * configurable, we take it over from the old JSP version
		 */
        s_connectionOptions.add(TELNET_CONNECTION);
        s_connectionOptions.add(SSH_CONNECTION);
        s_connectionOptions.add(RSH_CONNECTION);
        //TODO added "" to be able to remove connection again. this could cause problems at the connection reading code.
        s_connectionOptions.add("");

		
		/*
         * Init AllowHtmlFields for sanitizing Strings
		 */
        initAllowHtmlFields();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AssetCommand getAssetByNodeId(int nodeId) {
        AssetCommand assetCommand = new AssetCommand();
        this.m_onmsNode = this.m_nodeDao.get(nodeId);
        this.m_onmsAssetRecord = this.m_onmsNode.getAssetRecord();
        logger.debug("onmsAssetRecord '{}'", m_onmsAssetRecord);
        // copy all assetRecord properties to assetCommand for gui
        BeanUtils.copyProperties(this.m_onmsAssetRecord, assetCommand);

        //This manual null to "" settings is to prevent problems caused by different behavior of browsers in null handling in select boxes
        if (assetCommand.getConnection() == null) {
            assetCommand.setConnection("");
        }
        if (assetCommand.getAutoenable() == null) {
            assetCommand.setAutoenable("");
        }


        // set node specific properties for the asset node page
        assetCommand.setSnmpSysContact(this.m_onmsNode.getSysContact());
        assetCommand.setSnmpSysDescription(this.m_onmsNode.getSysDescription());
        assetCommand.setSnmpSysLocation(this.m_onmsNode.getSysLocation());
        assetCommand.setSnmpSysName(this.m_onmsNode.getSysName());
        assetCommand.setSnmpSysObjectId(this.m_onmsNode.getSysObjectId());

        // set static arrays for gui options
        assetCommand.setAutoenableOptions(s_autoenableOptions);
        assetCommand.setConnectionOptions(s_connectionOptions);

        assetCommand.setNodeId(this.m_onmsNode.getNodeId());
        assetCommand.setNodeLabel(this.m_onmsNode.getLabel());

        // set user from web ui session
        assetCommand.setLoggedInUser(this.m_securityContext.getUsername());

        // This is a poor re-implementation of modify permission based on spring
        // roles
        if (this.m_securityContext.hasRole(ALLOW_EDIT_ROLE_ADMIN) || this.m_securityContext.hasRole(ALLOW_EDIT_ROLE_PROVISION)) {
            assetCommand.setAllowModify(true);
        } else {
            assetCommand.setAllowModify(false);
        }

        // assign the asset record back to the node
        this.m_onmsAssetRecord.setNode(this.m_onmsNode);

        logger.debug("assetCommand: '{}'", assetCommand);
        return assetCommand;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AssetSuggCommand getAssetSuggestions() {
        // The suggestion model transfered by RPC between webui and service
        AssetSuggCommand suggestion = new AssetSuggCommand();

        // a list of all asset records which contains all distinct asset
        // properties for suggestion
        List<OnmsAssetRecord> distinctAssetProperties = this.m_assetRecordDao.getDistinctProperties();

        // Map all distinct asset properties
        for (OnmsAssetRecord asset : distinctAssetProperties) {
            suggestion.addAdditionalhardware(asset.getAdditionalhardware());
            suggestion.addAddress1(asset.getGeolocation().getAddress1());
            suggestion.addAddress2(asset.getGeolocation().getAddress2());
            suggestion.addAdmin(asset.getAdmin());
            suggestion.addBuilding(asset.getBuilding());
            suggestion.addCategory(asset.getCategory());
            suggestion.addCircuitId(asset.getCircuitId());
            suggestion.addCity(asset.getGeolocation().getCity());
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
            suggestion.addState(asset.getGeolocation().getState());
            suggestion.addStoragectrl(asset.getStoragectrl());
            suggestion.addSupportPhone(asset.getSupportPhone());
            suggestion.addThresholdCategory(asset.getThresholdCategory());
            suggestion.addVendor(asset.getVendor());
            suggestion.addVendorFax(asset.getVendorFax());
            suggestion.addVendorPhone(asset.getVendorPhone());
            suggestion.addZip(asset.getGeolocation().getZip());
            suggestion.addCoordinates(asset.getGeolocation().getCoordinates());

            // VMware monitoring assets
            suggestion.addVmwareManagedObjectId(asset.getVmwareManagedObjectId());
            suggestion.addVmwareManagedEntityType(asset.getVmwareManagedEntityType());
            suggestion.addVmwareManagementServer(asset.getVmwareManagementServer());

            // VMware topology assets
            suggestion.addVmwareTopologyInfo(asset.getVmwareTopologyInfo());
            suggestion.addVmwareState(asset.getState());

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
        this.m_onmsNode = this.m_nodeDao.get(nodeId);
        this.m_onmsAssetRecord = this.m_onmsNode.getAssetRecord();

        // copy the transfer object for rpc back to the hibernate model
        BeanUtils.copyProperties(WebSecurityUtils.sanitizeBeanStringProperties(assetCommand, s_allowHtmlFields), this.m_onmsAssetRecord);
        logger.debug("OnmsAssetRecord: '{}'", m_onmsAssetRecord);

        // set the last modified user from logged in user
        this.m_onmsAssetRecord.setLastModifiedBy(this.m_securityContext.getUsername());

        // set last modified date and assign the node for the asset record
        this.m_onmsAssetRecord.setLastModifiedDate(new Date());
        this.m_onmsAssetRecord.setNode(this.m_onmsNode);

        // try to persist the asset record from the web ui
        try {
            logger.debug("OnmsNode '{}'", m_onmsNode.toString());
            logger.debug("AssetRecordDao to update '{}'", m_assetRecordDao.toString());
            logger.debug("OnmsAssetRecord to update '{}'", m_onmsAssetRecord.toString());

            this.m_assetRecordDao.saveOrUpdate(this.m_onmsAssetRecord);
            isSaved = true;
        } catch (Exception e) {
            // TODO: Catch exception and show error in web user interface
            isSaved = false;
            logger.error("Problem during saveing or updating assets '{}'", e.getMessage());
            e.printStackTrace();
        }

        // save was successful
        return isSaved;
    }

    /**
     * <p>
     * initAllowHtmlFields
     * </p>
     * setup an list of fieldnames where html is allowed
     */
    private void initAllowHtmlFields() {
        s_allowHtmlFields = new HashSet<String>();
        String allowHtmlFieldNames = System.getProperty("opennms.assets.allowHtmlFields");
        if (allowHtmlFieldNames == null)
            return;
        for (String fieldName : allowHtmlFieldNames.split("\\s*,\\s*")) {
            s_allowHtmlFields.add(fieldName.toLowerCase());
        }
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
    public void setAssetRecordDao(AssetRecordDao m_assetRecordDao) {
        this.m_assetRecordDao = m_assetRecordDao;
    }

    /**
     * <p>
     * getNodeDao
     * </p>
     *
     * @return m_nodeDao a {@link org.opennms.netmgt.dao.NodeDao}
     */
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    /**
     * <p>
     * setNodeDao
     * </p>
     *
     * @param m_nodeDao a {@link org.opennms.netmgt.dao.NodeDao}
     */
    public void setNodeDao(NodeDao m_nodeDao) {
        this.m_nodeDao = m_nodeDao;
    }
}
