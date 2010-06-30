package org.opennms.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.opennms.client.InstallerProgressItem;
import org.opennms.client.Progress;
import org.opennms.install.Installer;
import org.opennms.install.ProgressManager;

/**
 * <p>InstallerProgressManager class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class InstallerProgressManager implements ProgressManager<Installer.ProgressItemKey> {
    /**
     * This map holds a set of progress indicators that will be exposed via
     * the {@link #getProgressItems()} call.
     */
    private final Map<Installer.ProgressItemKey,InstallerProgressItem> m_progressItems = new TreeMap<Installer.ProgressItemKey,InstallerProgressItem>();

    /** {@inheritDoc} */
    public synchronized void addItem(Installer.ProgressItemKey key, String name){
        m_progressItems.put(key, new InstallerProgressItem(name));
    }

    /**
     * <p>setIndeterminate</p>
     *
     * @param key a {@link org.opennms.install.Installer.ProgressItemKey} object.
     */
    public synchronized void setIndeterminate(Installer.ProgressItemKey key){
        m_progressItems.get(key).setProgress(Progress.INDETERMINATE);
    }

    /**
     * <p>setIncomplete</p>
     *
     * @param key a {@link org.opennms.install.Installer.ProgressItemKey} object.
     */
    public synchronized void setIncomplete(Installer.ProgressItemKey key){
        m_progressItems.get(key).setProgress(Progress.INCOMPLETE);
    }

    /**
     * <p>setInProgress</p>
     *
     * @param key a {@link org.opennms.install.Installer.ProgressItemKey} object.
     */
    public synchronized void setInProgress(Installer.ProgressItemKey key){
        m_progressItems.get(key).setProgress(Progress.IN_PROGRESS);
    }

    /**
     * <p>setComplete</p>
     *
     * @param key a {@link org.opennms.install.Installer.ProgressItemKey} object.
     */
    public synchronized void setComplete(Installer.ProgressItemKey key){
        m_progressItems.get(key).setProgress(Progress.COMPLETE);
    }

    /**
     * <p>clearItems</p>
     */
    public synchronized void clearItems() {
        m_progressItems.clear();
    }

    /**
     * <p>getProgressItems</p>
     *
     * @return a {@link java.util.List} object.
     */
    public synchronized List<InstallerProgressItem> getProgressItems() {
        // We have to add all of the items to a new ArrayList so that GWT
        // serialization works. It fails when just returning {@link TreeMap.values()}.
        List<InstallerProgressItem> retval = new ArrayList<InstallerProgressItem>();
        retval.addAll(m_progressItems.values());
        return retval;
    }
}
