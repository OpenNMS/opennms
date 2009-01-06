package org.opennms.protocols.wmi.test.stubs;

import org.opennms.protocols.wmi.wbem.OnmsWbemObject;
import org.opennms.protocols.wmi.wbem.OnmsWbemObjectSet;
import org.opennms.protocols.wmi.WmiException;

public class OnmsWbemObjectSetBiosStub implements OnmsWbemObjectSet {
    public OnmsWbemObject objStub;

    public OnmsWbemObjectSetBiosStub(OnmsWbemObject obj) {
        objStub = obj;
    }

    public Integer count() throws WmiException {
        return 1;
    }

    public OnmsWbemObject get(Integer idx) throws WmiException {
        if (idx == 0) {
            return objStub;
        } else {
            throw new WmiException("Failed to enumerate WbemObject variant: Incorrect function. [0x00000001]");
        }
    }
}
