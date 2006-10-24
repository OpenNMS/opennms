package org.opennms.web.graph;

public interface FileReloadCallback<K, T> {
    public T reload(FileReloadContainer<K, T> container) throws Throwable;
}
