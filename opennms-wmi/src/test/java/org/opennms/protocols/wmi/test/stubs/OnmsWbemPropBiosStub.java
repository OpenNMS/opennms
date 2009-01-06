package org.opennms.protocols.wmi.test.stubs;

import org.opennms.protocols.wmi.wbem.OnmsWbemProperty;
import org.opennms.protocols.wmi.WmiException;

public class OnmsWbemPropBiosStub implements OnmsWbemProperty {
    public String getWmiName() throws WmiException {
        return "ReleaseDate";
    }

    public String getWmiOrigin() throws WmiException {
        return null;
    }

    public Boolean getWmiIsArray() throws WmiException {
        return false;
    }

    public Boolean getWmiIsLocal() throws WmiException {
        return null;
    }

    public Object getWmiValue() throws WmiException {
        return "2/12/2004 00:00:00";
    }

    public Integer getWmiCIMType() throws WmiException {
        return null;
    }
}
