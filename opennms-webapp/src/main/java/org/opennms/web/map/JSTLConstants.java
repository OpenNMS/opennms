package org.opennms.web.map;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;


/**
 * Class to reveal java constants to JSTL Expression Language
 * Uses reflection to scan the declared fields of a Constants class
 * Adds these fields to the Map.
 * Map is unmodifiable after initialization.
 *
 */
public class JSTLConstants extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;
    
    private boolean initialised = false;
     
	public JSTLConstants() {
		Category log = ThreadCategory.getInstance(MapsConstants.LOG4J_CATEGORY);
		Class c = this.getClass();
		Field[] fields = c.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
 
			Field field = fields[i];
			int modifier = field.getModifiers();
			if (Modifier.isFinal(modifier) && !Modifier.isPrivate(modifier))
				try {
					this.put(field.getName(), field.get(this));
				}
				catch (IllegalAccessException e) {
					log.error("Error while instantiating JSTLConstants!",e);
				}
		}
		initialised = true;
	}
 
	public void clear() {
		if (!initialised) {
			super.clear();
        } else {
			throw new UnsupportedOperationException("Cannot modify this map");
        }
	}
 
	public Object put(String key, Object value) {
		if (!initialised) {
			return super.put(key, value);
        } else {
			throw new UnsupportedOperationException("Cannot modify this map");
        }
	}
 
	public void putAll(Map<? extends String, ? extends Object> m) {
		if (!initialised) {
			super.putAll(m);
        } else {
			throw new UnsupportedOperationException("Cannot modify this map");
        }
	}
 
	public Object remove(String key) {
		if (!initialised) {
			return super.remove(key);
        } else {
			throw new UnsupportedOperationException("Cannot modify this map");
        }
	}
}
 