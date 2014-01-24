package org.opennms.core.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmptyListAdapter<T> extends XmlAdapter<List<T>, List<T>> {
    private static final Logger LOG = LoggerFactory.getLogger(EmptyListAdapter.class);

    public EmptyListAdapter() {
        super();
        LOG.info("Initializing EmptyListAdapter.");
    }

    @Override
    public List<T> unmarshal(final List<T> in) throws Exception {
        final List<T> ret;
        if (in == null) {
            ret = new ArrayList<T>();
        } else {
            ret = in;
        }
        LOG.info("EmptyListAdapter.unmarshal(): returning {}", ret);
        return ret;
    }

    @Override
    public List<T> marshal(final List<T> out) throws Exception {
        final List<T> ret;
        if (out != null && out.size() == 0) {
            ret = null;
        } else {
            ret = out;
        }
        LOG.info("EmptyListAdapter.marshal(): returning {}", ret);
        return ret;
    }

}
