package org.opennms.netmgt.dao.jdbc.category;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsCategory;

public class LazyCategory extends OnmsCategory {
	
	private static final long serialVersionUID = -7788675055201613651L;

	private DataSource m_dataSource;
	private boolean m_loaded = false;
    private boolean m_dirty = false;
	
	public LazyCategory(DataSource dataSource) {
		m_dataSource = dataSource;
	}
	
	public String getDescription() {
        load();
        return super.getDescription();
    }
	
	public String getName() {
		load();
		return super.getName();
	}
	
	public boolean isLoaded() {
		return m_loaded;
	}
	
	public void setDescription(String description) {
        load();
        setDirty(true);
        super.setDescription(description);
    }

	public void setLoaded(boolean loaded) {
		m_loaded = loaded;
	}

    public void setName(String name) {
		load();
        setDirty(true);
		super.setName(name);
	}

    private void load() {
		if (!m_loaded) {
			// this loads data into the object cache
			new FindByCategoryId(m_dataSource).findUnique(getId());
		}
	}

    public boolean isDirty() {
        return m_dirty;
    }

    public void setDirty(boolean dirty) {
        m_dirty = dirty;
    }
}
