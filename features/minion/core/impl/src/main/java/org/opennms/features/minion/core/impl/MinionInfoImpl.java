package org.opennms.features.minion.core.impl;

import java.util.Objects;

import org.opennms.features.minion.core.api.MinionInfo;

public class MinionInfoImpl implements MinionInfo {

    private final String m_location;

    public MinionInfoImpl(String location) {
        m_location = Objects.requireNonNull(location);
    }

    @Override
    public String getLocation() {
        return m_location;
    }
}
