package org.opennms.web.rest.support;

import java.beans.PropertyEditorSupport;
import java.net.InetAddress;

import org.opennms.core.utils.InetAddressUtils;

public class InetAddressTypeEditor extends PropertyEditorSupport {

    @Override
    public String getAsText() {
        return InetAddressUtils.str((InetAddress)getValue());
    }

    @Override
    public void setAsText(String text) {
        InetAddress addr = InetAddressUtils.addr(text);
        setValue(addr);
    }
}
