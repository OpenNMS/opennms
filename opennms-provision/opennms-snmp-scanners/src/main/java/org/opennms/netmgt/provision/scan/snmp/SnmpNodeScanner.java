package org.opennms.netmgt.provision.scan.snmp;


public class SnmpNodeScanner extends AbstractSnmpScanner {
    
    public SnmpNodeScanner() {
        super("Node Scanner");
    }

    @Override
    public void onInit() {
        getSingleInstance(".1.3.6.1.2.1.1.2", "0").andStoreIn(sysObjectId());
    }

    public Storer sysObjectId() {
        return new Storer() {
            public void storeResult(ScanContext context, SnmpObjId base, SnmpObject instId, SnmpValue val) {
                context.updateSysObjectId(val.toDisplayString());
            }
        }
    }
}
