package org.opennms.install;

/**
 * <p>ProgressManager interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface ProgressManager<T> {
    /**
     * <p>clearItems</p>
     *
     * @param <T> a T object.
     */
    public void clearItems();
    /**
     * <p>addItem</p>
     *
     * @param key a T object.
     * @param name a {@link java.lang.String} object.
     */
    public void addItem(T key, String name);
    /**
     * <p>setIndeterminate</p>
     *
     * @param key a T object.
     */
    public void setIndeterminate(T key);
    /**
     * <p>setIncomplete</p>
     *
     * @param key a T object.
     */
    public void setIncomplete(T key);
    /**
     * <p>setInProgress</p>
     *
     * @param key a T object.
     */
    public void setInProgress(T key);
    /**
     * <p>setComplete</p>
     *
     * @param key a T object.
     */
    public void setComplete(T key);
}
