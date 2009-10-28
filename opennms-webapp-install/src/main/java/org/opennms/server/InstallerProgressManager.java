package org.opennms.server;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.opennms.client.InstallerProgressItem;
import org.opennms.client.Progress;
import org.opennms.install.Installer;
import org.opennms.install.ProgressManager;

public class InstallerProgressManager implements ProgressManager<Installer.ProgressItemKey> {
    /**
     * This map holds a set of progress indicators that will be exposed via
     * the {@link #getProgressItems()} call.
     */
    private final Map<Installer.ProgressItemKey,InstallerProgressItem> m_progressItems = Collections.synchronizedSortedMap(new TreeMap<Installer.ProgressItemKey,InstallerProgressItem>());

    public synchronized void addItem(Installer.ProgressItemKey key, String name){
        m_progressItems.put(key, new InstallerProgressItem(name));
    }

    public synchronized void setIndeterminate(Installer.ProgressItemKey key){
        m_progressItems.get(key).setProgress(Progress.INDETERMINATE);
    }

    public synchronized void setIncomplete(Installer.ProgressItemKey key){
        m_progressItems.get(key).setProgress(Progress.INCOMPLETE);
    }

    public synchronized void setInProgress(Installer.ProgressItemKey key){
        m_progressItems.get(key).setProgress(Progress.IN_PROGRESS);
    }

    public synchronized void setComplete(Installer.ProgressItemKey key){
        m_progressItems.get(key).setProgress(Progress.COMPLETE);
    }

    public synchronized void clearItems() {
        m_progressItems.clear();
    }
    
    public synchronized Collection<InstallerProgressItem> getProgressItems() {
        return m_progressItems.values();
    }
}
