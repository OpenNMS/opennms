/*
 * Created on 12-dic-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.netmgt.linkd.snmp;

/**
 * @author antonio
 *
 * See CISCO-VTP-MIB for definition
 */

public interface VlanCollectorEntry {
	
	public final static String VLAN_INDEX = "vtpVlanIndex";
	public final static String VLAN_NAME = "vtpVlanName";
	public final static String VLAN_STATUS = "vtpVlanStatus";
	public final static String VLAN_TYPE = "vtpVlanType";

	public final static int VLAN_TYPE_ETHERNET = 1;
	public final static int VLAN_TYPE_FDDI = 2;
	public final static int VLAN_TYPE_TOKENRING = 3;
	public final static int VLAN_TYPE_FDDINET = 4;
	public final static int VLAN_TYPE_TRNET = 5;
	public final static int VLAN_TYPE_DEPRECATED = 6;
	
	public final static int VLAN_STATUS_OPERATIONAL = 1;
	public final static int VLAN_STATUS_SUSPENDED = 2;
	public final static int VLAN_STATUS_mtuTooBigForDevice = 3;
	public final static int VLAN_STATUS_mtuTooBigForTrunk = 4;
	
}
