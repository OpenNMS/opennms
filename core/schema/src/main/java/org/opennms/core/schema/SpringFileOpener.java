package org.opennms.core.schema;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import liquibase.FileOpener;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class SpringFileOpener implements FileOpener {
    private ResourceLoader m_resourceLoader = new DefaultResourceLoader();

    public InputStream getResourceAsStream(String file) throws IOException {
        Resource resource = getResource(file);

        return resource.getInputStream();
    }

    public Enumeration<URL> getResources(String packageName) throws IOException {
        Vector<URL> tmp = new Vector<URL>();
        tmp.add(getResource(packageName).getURL());
        return tmp.elements();
    }

    public Resource getResource(String file) {
        File f = new File(file);
        if (f.exists()) {
            return getResourceLoader().getResource(file);
        } else {
            return getResourceLoader().getResource(adjustClasspath(file));
        }
    }

    private String adjustClasspath(String file) {
        return !isClasspathPrefixPresent(file) ? ResourceLoader.CLASSPATH_URL_PREFIX + file : file;
    }

    public boolean isClasspathPrefixPresent(String file) {
        return file.startsWith(ResourceLoader.CLASSPATH_URL_PREFIX);
    }

    public ClassLoader toClassLoader() {
        return getResourceLoader().getClassLoader();
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        m_resourceLoader = resourceLoader;
    }

    public ResourceLoader getResourceLoader() {
        return m_resourceLoader;
    }
}
