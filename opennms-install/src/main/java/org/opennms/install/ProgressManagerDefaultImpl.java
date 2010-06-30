package org.opennms.install;

/**
 * <p>ProgressManagerDefaultImpl class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ProgressManagerDefaultImpl<T> implements ProgressManager<T> {
    /**
     * <p>clearItems</p>
     *
     * @param <T> a T object.
     */
    public void clearItems() {}
    /** {@inheritDoc} */
    public void addItem(T key, String name) {}
    /**
     * <p>setIndeterminate</p>
     *
     * @param key a T object.
     */
    public void setIndeterminate(T key) {}
    /**
     * <p>setIncomplete</p>
     *
     * @param key a T object.
     */
    public void setIncomplete(T key) {}
    /**
     * <p>setInProgress</p>
     *
     * @param key a T object.
     */
    public void setInProgress(T key) {}
    /**
     * <p>setComplete</p>
     *
     * @param key a T object.
     */
    public void setComplete(T key) {}
}
