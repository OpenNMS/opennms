package org.opennms.web.rest;

public interface GraphNameResolver {
    GraphNameCollection getGraphNames(final String resourceId);
}