package org.opennms.protocols.wmi.test.stubs;

import org.opennms.protocols.wmi.wbem.OnmsWbemObject;
import org.opennms.protocols.wmi.wbem.OnmsWbemMethodSet;
import org.opennms.protocols.wmi.wbem.OnmsWbemObjectPath;
import org.opennms.protocols.wmi.wbem.OnmsWbemPropertySet;
import org.opennms.protocols.wmi.WmiException;

import java.util.List;

public class OnmsWbemObjectBiosStub implements OnmsWbemObject {
    public OnmsWbemPropertySet props;
    public OnmsWbemObjectBiosStub(OnmsWbemPropertySet propset) {
        props = propset;
    }

    public OnmsWbemObject wmiExecMethod(String methodName, List params, List namedValueSet) {
        return null;
    }

    public List<String> wmiInstances() {
        return null;
    }

    public String wmiPut() {
        return null;
    }

    public OnmsWbemMethodSet getWmiMethods() throws WmiException {
        return null;
    }

    public OnmsWbemObjectPath getWmiPath() throws WmiException {
        return null;
    }

    public String getWmiObjectText() throws WmiException {
        return null;
    }

    public OnmsWbemPropertySet getWmiProperties() throws WmiException {
        return props;
    }
}
