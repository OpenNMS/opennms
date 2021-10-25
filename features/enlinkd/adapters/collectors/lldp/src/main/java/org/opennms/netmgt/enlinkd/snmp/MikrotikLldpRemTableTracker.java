package org.opennms.netmgt.enlinkd.snmp;

import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpRowResult;

public class MikrotikLldpRemTableTracker extends LldpRemTableTracker{

    public MikrotikLldpRemTableTracker() {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public SnmpRowResult createRowResult(final int columnCount, final SnmpInstId instance) {
        return new MikrotikLldpRemRow(columnCount, instance);
    }

    public static class MikrotikLldpRemRow extends LldpRemRow {

        public MikrotikLldpRemRow(int columnCount, SnmpInstId instance) {
            super(columnCount, instance);
        }
        @Override
        public Integer getLldpRemLocalPortNum() {
            return getInstance().getSubIdAt(2);
        }

    }

}
