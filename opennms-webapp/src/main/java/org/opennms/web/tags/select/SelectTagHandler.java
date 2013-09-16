package org.opennms.web.tags.select;

import org.opennms.netmgt.model.OnmsFilterFavorite;

public interface SelectTagHandler<T> {
    String getValue(T input);

    String getDescription(T input);

    boolean isSelected(T currentElement, T selectedElement);

}
