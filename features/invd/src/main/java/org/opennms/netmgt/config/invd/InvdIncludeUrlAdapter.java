package org.opennms.netmgt.config.invd;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class InvdIncludeUrlAdapter extends XmlAdapter<String,InvdIncludeUrl> {
	public InvdIncludeUrl unmarshal(String val) throws Exception {
        return InvdIncludeUrl.getInstance(val);
    }
    public String marshal(InvdIncludeUrl val) throws Exception {
        return val.getIncludeUrl();
    }
}
