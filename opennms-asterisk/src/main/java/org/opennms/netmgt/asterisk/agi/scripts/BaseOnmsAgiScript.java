/**
 * 
 */
package org.opennms.netmgt.asterisk.agi.scripts;

import org.apache.log4j.Category;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.opennms.core.utils.ThreadCategory;

/**
 * @author jeffg
 *
 */
public abstract class BaseOnmsAgiScript extends BaseAgiScript {

    protected Category log() {
        return ThreadCategory.getInstance(getClass());
    }

}
