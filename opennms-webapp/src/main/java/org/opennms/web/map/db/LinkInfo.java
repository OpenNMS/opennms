package org.opennms.web.map.db;

public class LinkInfo {
	int nodeid;
	int ifindex;
	int nodeparentid;
	int parentifindex;
	int snmpiftype;
	long snmpifspeed;
	int snmpifoperstatus;


	
	LinkInfo(int nodeid, int ifindex, int nodeparentid, int parentifindex, int snmpiftype, long snmpifspeed, int snmpifoperstatus) {
		super();
		this.nodeid = nodeid;
		this.ifindex = ifindex;
		this.nodeparentid = nodeparentid;
		this.parentifindex = parentifindex;
		this.snmpiftype = snmpiftype;
		this.snmpifspeed = snmpifspeed;
		this.snmpifoperstatus = snmpifoperstatus;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof LinkInfo ) {
			LinkInfo ol = (LinkInfo) obj;
			return 
			(ol.nodeid == this.nodeid 
					&& ol.ifindex == this.ifindex 
					&& ol.nodeparentid== this.nodeparentid 
					&& ol.parentifindex == this.parentifindex
					&& ol.snmpiftype == this.snmpiftype
					&& ol.snmpifspeed == this.snmpifspeed
					&& ol.snmpifoperstatus==this.snmpifoperstatus);
			
		} 
		return false;
	}
	
	public int hashCode() {
		return (3*nodeid)+(5*ifindex)+(7*nodeparentid)+(11*parentifindex)+(13*snmpiftype)+(17*snmpifoperstatus);
	}

}
