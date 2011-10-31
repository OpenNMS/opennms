package org.opennms.core.schema;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import liquibase.resource.ResourceAccessor;

import org.springframework.core.io.Resource;

public class ExistingResourceAccessor implements ResourceAccessor {
    private final Resource m_resource;

    public ExistingResourceAccessor() {
        m_resource = null;
    }

    public ExistingResourceAccessor(final Resource resource) {
        m_resource = resource;
    }

    @Override
    public InputStream getResourceAsStream(final String file) throws IOException {
        if (m_resource == null) return null;
        return m_resource.createRelative(file).getInputStream();
    }

    @Override
    public Enumeration<URL> getResources(final String packageName) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented!");
        /*
        final Vector<URL> resources = new Vector<URL>();
        if (m_resource != null) {
            resources.add(m_resource.getURI().toURL());
        }
        return resources.elements();
        */
    }

    @Override
    public ClassLoader toClassLoader() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
