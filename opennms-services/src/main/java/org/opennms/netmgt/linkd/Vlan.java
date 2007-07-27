package org.opennms.netmgt.linkd;

public class Vlan {

		int vlanIndex;
		
		String vlanName;
		
		int vlanStatus = -1;
		
		int vlanType = -1;
		
		Vlan(int index, String name, int status,int type) {
			vlanIndex = index;
			vlanName = name;
			vlanStatus = status;
			vlanType = type;
		}

		Vlan(int index, String name, int status) {
			vlanIndex = index;
			vlanName = name;
			vlanStatus = status;

		}

		public int getVlanIndex() {
			return vlanIndex;
		}

		public String getVlanName() {
			return vlanName;
		}

		public int getVlanStatus() {
			return vlanStatus;
		}

		public int getVlanType() {
			return vlanType;
		}
	
}
