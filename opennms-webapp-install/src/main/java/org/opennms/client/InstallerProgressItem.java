package org.opennms.client;

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BeanModelTag;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Instances of this class are used to communicate the names and statuses
 * of various phases of the database schema upgrade process to the UI.
 *
 * @author ranger
 * @version $Id: $
 */
public class InstallerProgressItem implements BeanModelTag, Serializable, IsSerializable {
    private static final long serialVersionUID = 3439201512922358422L;

    /**
     * Should be final, but cannot be to satisfy the need for a zero-argument
     * constructor for serialization.
     */
    private String m_name;
    private Progress m_progress;

    /**
     * Zero-argument constructor is necessary for GWT serialization.
     *
     * @deprecated Only for use by GWT serialization.
     */
    public InstallerProgressItem() {}

    /**
     * Constructor that automatically sets the progress to {@link Progress#INDETERMINATE}.
     *
     * @param name User-friendly name of the progress phase.
     */
    public InstallerProgressItem(String name) {
        this(name, Progress.INDETERMINATE);
    }

    /**
     * <p>Constructor for InstallerProgressItem.</p>
     *
     * @param name User-friendly name of the progress phase.
     * @param progress The state of progress of the phase.
     */
    public InstallerProgressItem(String name, Progress progress) {
        m_name = name;
        m_progress = progress;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }

    /**
     * <p>getProgress</p>
     *
     * @return a {@link org.opennms.client.Progress} object.
     */
    public Progress getProgress() {
        return m_progress;
    }

    /**
     * <p>setProgress</p>
     *
     * @param progress a {@link org.opennms.client.Progress} object.
     */
    public void setProgress(Progress progress) {
        m_progress = progress;
    }
}
