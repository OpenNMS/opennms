package org.opennms.netmgt.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class SnmpInterfaceIdAdapter extends XmlAdapter<List<Integer>, Set<OnmsIpInterface>> {

    @Override
    public List<Integer> marshal(final Set<OnmsIpInterface> ifaces) throws Exception {
        final ArrayList<Integer> ret = new ArrayList<Integer>();
        for (final OnmsIpInterface iface : ifaces) {
            ret.add(iface.getId());
        }
        return ret;
    }

    @Override
    public Set<OnmsIpInterface> unmarshal(final List<Integer> ids) throws Exception {
        final Set<OnmsIpInterface> ret = new TreeSet<OnmsIpInterface>();
        for (final Integer id : ids) {
            final OnmsIpInterface iface = new OnmsIpInterface();
            iface.setId(id);
            ret.add(iface);
        }
        return ret;
    }

}
