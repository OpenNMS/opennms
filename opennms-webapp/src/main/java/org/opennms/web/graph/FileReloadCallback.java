package org.opennms.web.graph;

import java.io.File;

public interface FileReloadCallback<T> {
    public T reload(T object, File file);
}
