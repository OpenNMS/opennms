package org.opennms.web.element;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.support.TransactionTemplate;

public interface NetworkElementFactoryInterface {

	String getNodeLabel(int nodeId);

	/**
	 * Translate a node id into a human-readable ipaddress. Note these values
	 * are not cached.
	 *
	 * @return A human-readable node name or null if the node id given does not
	 *         specify a real node.
	 * @param nodeId a int.
	 */
	String getIpPrimaryAddress(int nodeId);

	Node getNode(int nodeId);

	/**
	 * Returns all non-deleted nodes.
	 *
	 * @return an array of {@link org.opennms.web.element.Node} objects.
	 */
	Node[] getAllNodes();

	/**
	 * Returns all non-deleted nodes that have the given nodeLabel substring
	 * somewhere in their nodeLabel.
	 *
	 * @param nodeLabel a {@link java.lang.String} object.
	 * @return an array of {@link org.opennms.web.element.Node} objects.
	 */
	Node[] getNodesLike(String nodeLabel);

	Node[] getNodesWithIpLike(String iplike);

	/**
	 * Returns all non-deleted nodes that have the given service.
	 *
	 * @param serviceId a int.
	 * @return an array of {@link org.opennms.web.element.Node} objects.
	 */
	Node[] getNodesWithService(int serviceId);

	/**
	 * Returns all non-deleted nodes that have the given mac.
	 *
	 * @param macAddr a {@link java.lang.String} object.
	 * @return an array of {@link org.opennms.web.element.Node} objects.
	 */
	Node[] getNodesWithPhysAddr(String macAddr);

	/**
	 * Returns all non-deleted nodes with a MAC address like the rule given from AtInterface.
	 *
	 * @param macAddr a {@link java.lang.String} object.
	 * @return an array of {@link org.opennms.web.element.Node} objects.
	 */
	Node[] getNodesWithPhysAddrAtInterface(String macAddr);

	/**
	 * Returns all non-deleted nodes with a MAC address like the rule given from SnmpInterface.
	 *
	 * @param macAddr a {@link java.lang.String} object.
	 * @return an array of {@link org.opennms.web.element.Node} objects.
	 */
	Node[] getNodesWithPhysAddrFromSnmpInterface(String macAddr);

	/**
	 * Returns all non-deleted nodes that contain the given string in an ifAlias
	 *
	 * @Param ifAlias
	 *               the ifAlias string we are looking for
	 * @return nodes
	 *               the nodes with a matching ifAlias on one or more interfaces
	 * @param ifAlias a {@link java.lang.String} object.
	 */
	Node[] getNodesWithIfAlias(String ifAlias);

	/**
	 * Resolve an IP address to a DNS hostname via the database. If no hostname
	 * can be found, the given IP address is returned.
	 *
	 * @param ipAddress a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	String getHostname(String ipAddress);

	/**
	 * <p>getInterface</p>
	 *
	 * @param ipInterfaceId a int.
	 * @return a {@link org.opennms.web.element.Interface} object.
	 */
	Interface getInterface(int ipInterfaceId);

	/**
	 * <p>getInterface</p>
	 *
	 * @param nodeId a int.
	 * @param ipAddress a {@link java.lang.String} object.
	 * @return a {@link org.opennms.web.element.Interface} object.
	 */
	Interface getInterface(int nodeId, String ipAddress);

	/**
	 * <p>getInterface</p>
	 *
	 * @param nodeId a int.
	 * @param ipAddress a {@link java.lang.String} object.
	 * @param ifindex a int.
	 * @return a {@link org.opennms.web.element.Interface} object.
	 */
	Interface getInterface(int nodeId, String ipAddress,
			int ifIndex);

	/**
	 * Get interface from snmpinterface table. Intended for use with non-ip interfaces.
	 *
	 * @return Interface
	 * @param nodeId a int.
	 * @param ifIndex a int.
	 */
	Interface getSnmpInterface(int nodeId, int ifIndex);

	/**
	 * <p>getInterfacesWithIpAddress</p>
	 *
	 * @param ipAddress a {@link java.lang.String} object.
	 * @return an array of {@link org.opennms.web.element.Interface} objects.
	 */
	Interface[] getInterfacesWithIpAddress(String ipAddress);

	Interface[] getInterfacesWithIfAlias(int nodeId,
			String ifAlias);

	/**
	 * Returns true if node has any snmpIfAliases
	 *
	 * @Param nodeId
	 *               The nodeId of the node we are looking at
	 *               the ifAlias string we are looking for
	 * @return boolean
	 *               true if node has any snmpIfAliases
	 * @param nodeId a int.
	 */
	boolean nodeHasIfAliases(int nodeId);

	/**
	 * <p>getAllInterfacesOnNode</p>
	 *
	 * @param nodeId a int.
	 * @return an array of {@link org.opennms.web.element.Interface} objects.
	 */
	Interface[] getAllInterfacesOnNode(int nodeId);

	/**
	 * Returns all snmp interfaces on a node
	 *
	 * @Param int nodeId
	 *               The nodeId of the node we are looking at
	 * @return Interface[]
	 * @param nodeId a int.
	 */
	Interface[] getAllSnmpInterfacesOnNode(int nodeId);

	/**
	 * <p>getActiveInterfacesOnNode</p>
	 *
	 * @param nodeId a int.
	 * @return an array of {@link org.opennms.web.element.Interface} objects.
	 */

	Interface[] getActiveInterfacesOnNode(int nodeId);

	/*
	 * Returns all interfaces, including their SNMP information
	 */
	/**
	 * <p>getAllInterfaces</p>
	 *
	 * @return an array of {@link org.opennms.web.element.Interface} objects.
	 */
	Interface[] getAllInterfaces();

	/*
	 * Returns all interfaces, but only includes snmp data if includeSNMP is true
	 * This may be useful for pages that don't need snmp data and don't want to execute
	 * a sub-query per interface!
	 *
	 * @param includeSNMP a boolean.
	 * @return an array of {@link org.opennms.web.element.Interface} objects.
	 */
	Interface[] getAllInterfaces(boolean includeSnmp);

	/**
	 * <p>getAllManagedIpInterfaces</p>
	 *
	 * @param includeSNMP a boolean.
	 * @return an array of {@link org.opennms.web.element.Interface} objects.
	 */
	Interface[] getAllManagedIpInterfaces(boolean includeSNMP);

	/**
	 * Return the service specified by the node identifier, IP address, and
	 * service identifier.
	 *
	 * <p>
	 * Note that if there are both an active service and historically deleted
	 * services with this (nodeid, ipAddress, serviceId) key, then the active
	 * service will be returned. If there are only deleted services, then the
	 * first deleted service will be returned.
	 * </p>
	 *
	 * @param nodeId a int.
	 * @param ipAddress a {@link java.lang.String} object.
	 * @param serviceId a int.
	 * @return a {@link org.opennms.web.element.Service} object.
	 */
	Service getService(int nodeId, String ipAddress,
			int serviceId);

	/**
	 * Return the service specified by the node identifier, IP address, and
	 * service identifier.
	 *
	 * <p>
	 * Note that if there are both an active service and historically deleted
	 * services with this (nodeid, ipAddress, serviceId) key, then the active
	 * service will be returned. If there are only deleted services, then the
	 * first deleted service will be returned.
	 * </p>
	 *
	 * @param ifServiceId a int.
	 * @return a {@link org.opennms.web.element.Service} object.
	 */
	Service getService(int ifServiceId);

	/**
	 * <p>getAllServices</p>
	 *
	 * @return an array of {@link org.opennms.web.element.Service} objects.
	 */
	Service[] getAllServices();

	/**
	 * <p>getServicesOnInterface</p>
	 *
	 * @param nodeId a int.
	 * @param ipAddress a {@link java.lang.String} object.
	 * @return an array of {@link org.opennms.web.element.Service} objects.
	 */
	Service[] getServicesOnInterface(int nodeId,
			String ipAddress);

	/**
	 * <p>getServicesOnInterface</p>
	 *
	 * @param nodeId a int.
	 * @param ipAddress a {@link java.lang.String} object.
	 * @param includeDeletions a boolean.
	 * @return an array of {@link org.opennms.web.element.Service} objects.
	 */
	Service[] getServicesOnInterface(int nodeId,
			String ipAddress, boolean includeDeletions);

	/**
	 * Get the list of all services on a given node.
	 *
	 * @param nodeId a int.
	 * @return an array of {@link org.opennms.web.element.Service} objects.
	 */
	Service[] getServicesOnNode(int nodeId);

	/**
	 * Get the list of all instances of a specific service on a given node.
	 *
	 * @param nodeId a int.
	 * @param serviceId a int.
	 * @return an array of {@link org.opennms.web.element.Service} objects.
	 */
	Service[] getServicesOnNode(int nodeId, int serviceId);

	/**
	 * <p>getServiceNameFromId</p>
	 *
	 * @param serviceId a int.
	 * @return a {@link java.lang.String} object.
	 */
	String getServiceNameFromId(int serviceId);

	/**
	 * <p>getServiceIdFromName</p>
	 *
	 * @param serviceName a {@link java.lang.String} object.
	 * @return a int.
	 */
	int getServiceIdFromName(String serviceName);

	/**
	 * <p>getServiceIdToNameMap</p>
	 *
	 * @return a java$util$Map object.
	 */
	Map<Integer, String> getServiceIdToNameMap();

	/**
	 * <p>getServiceNameToIdMap</p>
	 *
	 * @return a java$util$Map object.
	 */
	Map<String, Integer> getServiceNameToIdMap();

	/**
	 * <p>getNodesLikeAndIpLike</p>
	 *
	 * @param nodeLabel a {@link java.lang.String} object.
	 * @param iplike a {@link java.lang.String} object.
	 * @param serviceId a int.
	 * @return an array of {@link org.opennms.web.element.Node} objects.
	 */
	Node[] getNodesLikeAndIpLike(String nodeLabel,
			String iplike, int serviceId);

	/**
	 * <p>getNodesLike</p>
	 *
	 * @param nodeLabel a {@link java.lang.String} object.
	 * @param serviceId a int.
	 * @return an array of {@link org.opennms.web.element.Node} objects.
	 */
	Node[] getNodesLike(String nodeLabel, int serviceId);

	/**
	 * <p>getNodesWithIpLike</p>
	 *
	 * @param iplike a {@link java.lang.String} object.
	 * @param serviceId a int.
	 * @return an array of {@link org.opennms.web.element.Node} objects.
	 */
	Node[] getNodesWithIpLike(String iplike, int serviceId);

	/**
	 * <p>getAllNodes</p>
	 *
	 * @param serviceId a int.
	 * @return an array of {@link org.opennms.web.element.Node} objects.
	 */
	Node[] getAllNodes(int serviceId);

	/**
	 * <p>getNodesFromPhysaddr</p>
	 *
	 * @param atPhysAddr a {@link java.lang.String} object.
	 * @return an array of {@link org.opennms.web.element.Node} objects.
	 */
	Node[] getNodesFromPhysaddr(String atPhysAddr);

	AtInterface getAtInterface(int nodeId, String ipAddr);

    IpRouteInterface[] getIpRoute(int nodeId);

	/**
	 * <p>isParentNode</p>
	 *
	 * @param nodeID a int.
	 * @return a boolean.
	 */
	boolean isParentNode(int nodeId);

	/**
	 * <p>getDataLinksOnNode</p>
	 *
	 * @param nodeID a int.
	 * @return an array of {@link org.opennms.web.element.DataLinkInterface} objects.
	 * @throws java.sql.SQLException if any.
	 */
	DataLinkInterface[] getDataLinksOnNode(int nodeID);

	/**
	 * <p>getDataLinksOnInterface</p>
	 *
	 * @param nodeID a int.
	 * @param ifindex a int.
	 * @return an array of {@link org.opennms.web.element.DataLinkInterface} objects.
	 */
	DataLinkInterface[] getDataLinksOnInterface(int nodeID,
			int ifindex);

	/**
	 * <p>getDataLinks</p>
	 *
	 * @param nodeID a int.
	 * @param ifindex a int.
	 * @return an array of {@link org.opennms.web.element.DataLinkInterface} objects.
	 */
	DataLinkInterface[] getDataLinks(int nodeId, int ifIndex);

	/**
	 * <p>getDataLinksFromNodeParent</p>
	 *
	 * @param nodeID a int.
	 * @param ifindex a int.
	 * @return an array of {@link org.opennms.web.element.DataLinkInterface} objects.
	 */
	DataLinkInterface[] getDataLinksFromNodeParent(int nodeId,
			int ifIndex);

	/**
	 * <p>getAllDataLinks</p>
	 *
	 * @return an array of {@link org.opennms.web.element.DataLinkInterface} objects.
	 */
	DataLinkInterface[] getAllDataLinks();

	/**
	 * Returns all non-deleted nodes with an IP address like the rule given.
	 *
	 * @param iplike a {@link java.lang.String} object.
	 * @return a {@link java.util.List} object.
	 */
	List<Integer> getNodeIdsWithIpLike(String iplike);

	/**
	 * <p>getNodesWithCategories</p>
	 *
	 * @param transTemplate a {@link org.springframework.transaction.support.TransactionTemplate} object.
	 * @param nodeDao a {@link org.opennms.netmgt.dao.NodeDao} object.
	 * @param categoryDao a {@link org.opennms.netmgt.dao.CategoryDao} object.
	 * @param categories1 an array of {@link java.lang.String} objects.
	 * @param onlyNodesWithDownAggregateStatus a boolean.
	 * @return an array of {@link org.opennms.web.element.Node} objects.
	 */
	Node[] getNodesWithCategories(
			TransactionTemplate transTemplate, final String[] categories1,
			final boolean onlyNodesWithDownAggregateStatus);

	/**
	 * <p>getNodesWithCategories</p>
	 *
	 * @param nodeDao a {@link org.opennms.netmgt.dao.NodeDao} object.
	 * @param categoryDao a {@link org.opennms.netmgt.dao.CategoryDao} object.
	 * @param categories1 an array of {@link java.lang.String} objects.
	 * @param onlyNodesWithDownAggregateStatus a boolean.
	 * @return an array of {@link org.opennms.web.element.Node} objects.
	 */
	Node[] getNodesWithCategories(String[] categories,
			boolean onlyNodesWithDownAggregateStatus);

	/**
	 * <p>getNodesWithCategories</p>
	 *
	 * @param transTemplate a {@link org.springframework.transaction.support.TransactionTemplate} object.
	 * @param nodeDao a {@link org.opennms.netmgt.dao.NodeDao} object.
	 * @param categoryDao a {@link org.opennms.netmgt.dao.CategoryDao} object.
	 * @param categories1 an array of {@link java.lang.String} objects.
	 * @param categories2 an array of {@link java.lang.String} objects.
	 * @param onlyNodesWithDownAggregateStatus a boolean.
	 * @return an array of {@link org.opennms.web.element.Node} objects.
	 */
	Node[] getNodesWithCategories(
			TransactionTemplate transTemplate, final String[] categories1,
			final String[] categories2,
			final boolean onlyNodesWithDownAggregateStatus);

	/**
	 * <p>getNodesWithCategories</p>
	 *
	 * @param nodeDao a {@link org.opennms.netmgt.dao.NodeDao} object.
	 * @param categoryDao a {@link org.opennms.netmgt.dao.CategoryDao} object.
	 * @param categories1 an array of {@link java.lang.String} objects.
	 * @param categories2 an array of {@link java.lang.String} objects.
	 * @param onlyNodesWithDownAggregateStatus a boolean.
	 * @return an array of {@link org.opennms.web.element.Node} objects.
	 */
	Node[] getNodesWithCategories(String[] categories1,
			String[] categories2, boolean onlyNodesWithDownAggregateStatus);

}