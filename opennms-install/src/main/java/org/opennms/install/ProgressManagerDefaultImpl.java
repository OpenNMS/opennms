package org.opennms.install;

public class ProgressManagerDefaultImpl<T> implements ProgressManager<T> {
    public void clearItems() {}
    public void addItem(T key, String name) {}
    public void setIndeterminate(T key) {}
    public void setIncomplete(T key) {}
    public void setInProgress(T key) {}
    public void setComplete(T key) {}
}
