package org.opennms.web.tags.select;

import org.opennms.netmgt.model.OnmsFilterFavorite;

public class FilterFavoriteSelectTagHandler implements SelectTagHandler<OnmsFilterFavorite> {
    @Override
    public String getValue(OnmsFilterFavorite input) {
        if (input == null) return "";
        return input.getId() + ";" + input.getFilter();
    }

    @Override
    public String getDescription(OnmsFilterFavorite input) {
        if (input == null) return "";
        return "" + input.getName();
    }

    @Override
    public boolean isSelected(OnmsFilterFavorite currentElement, OnmsFilterFavorite selectedElement) {
        if (currentElement == selectedElement) return true;
        if (currentElement != null) return currentElement.equals(selectedElement);
        return false;
    }
}
