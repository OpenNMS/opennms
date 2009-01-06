package org.opennms.protocols.wmi.test.stubs;

import org.opennms.protocols.wmi.wbem.OnmsWbemPropertySet;
import org.opennms.protocols.wmi.wbem.OnmsWbemProperty;
import org.opennms.protocols.wmi.WmiException;

public class OnmsWbemPropSetBiosStub implements OnmsWbemPropertySet {
    public OnmsWbemProperty releaseDate;
    public OnmsWbemPropSetBiosStub(OnmsWbemProperty prop) {
        releaseDate = prop;
    }
    public Integer count() throws WmiException {
        return null;
    }

    public OnmsWbemProperty get(Integer idx) throws WmiException {
        return null;
    }

    public OnmsWbemProperty getByName(String name) throws WmiException {      
        if(name.equals("ReleaseDate")) return releaseDate;
        throw new WmiException("Failed to perform WMI operation: Unknown name. [0x80020006]");
    }
}
