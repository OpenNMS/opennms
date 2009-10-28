package org.opennms.client;

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BeanModelTag;
import com.google.gwt.user.client.rpc.IsSerializable;

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
     */
    public InstallerProgressItem() {}

    public InstallerProgressItem(String name) {
        this(name, Progress.INDETERMINATE);
    }

    public InstallerProgressItem(String name, Progress progress) {
        m_name = name;
        m_progress = progress;
    }

    public String getName() {
        return m_name;
    }

    public Progress getProgress() {
        return m_progress;
    }

    public void setProgress(Progress progress) {
        m_progress = progress;
    }
}
