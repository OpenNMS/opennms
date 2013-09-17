/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.tags;

import org.apache.commons.lang.StringUtils;
import org.opennms.web.tags.select.SelectTagHandler;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Creates a HTML select-element for a given number of objects (called elements).
 * For each object an option-section is created.
 * There is also an empty option for a "no selection made" element.
 *
 * @param <T> The type of the elements (e.g. String)
 */
public class SelectTag<T> extends SimpleTagSupport {

    private static class DefaultSelectTagHandler implements SelectTagHandler<Object> {

        @Override
        public String getValue(Object input) {
            if (input == null) return "";
            if (input instanceof String && StringUtils.isEmpty((String)input)) return "";
            return input.toString();
        }

        @Override
        public String getDescription(Object input) {
            return getValue(input);
        }

        @Override
        public boolean isSelected(Object currentElement, Object selectedElement) {
            if (currentElement == selectedElement) return true;
            if (currentElement != null) return currentElement.equals(selectedElement);
            return false;
        }
    }

    private static final String TEMPLATE = "<select {ONCHANGE}>\n{OPTIONS}\n</select>";
    private static final String OPTION_TEMPLATE = "<option value='{VALUE}' {SELECTED}>{DESCRIPTION}</option>\n";


    private List<T> elements;
    private T selected;
    private SelectTagHandler selectTagHandler;
    private Comparator comparator;
    private String onChange;

    public void setOnChange(String onChange) {
        this.onChange = onChange;
    }

    public void setElements(T[] elements) {
        List<T> elementsToAdd = new ArrayList<T>();
        for (T eachElement : elements) {
            elementsToAdd.add(eachElement);
        }
        setElements(elementsToAdd);
    }

    public void setElements(List<T> elements) {
        this.elements = new ArrayList<T>();
        if (elements == null) return;
        this.elements.addAll(elements);
    }

    public void setSelected(T selected) {
        this.selected = selected;
    }

    public void setHandler(SelectTagHandler selectTagHandler) {
        this.selectTagHandler = selectTagHandler;
    }

    public void setComparator(Comparator comparator) {
        this.comparator = comparator;
    }

    @Override
    public void doTag() throws JspException, IOException {


        // prepare output
        List<T> viewElements = new ArrayList<T>();
        if (elements != null) {
            viewElements.addAll(elements);
        }
        if (comparator != null) {
            Collections.sort(viewElements, comparator);
        }
        viewElements.add(0, null); // "" empty at the beginning of the line

        // create output
        StringBuffer optionsBuffer = new StringBuffer();
        for (T eachElement : viewElements) {
            optionsBuffer.append(getOption(eachElement, selected));
        }
        String output = TEMPLATE
                .replace("{ONCHANGE}", StringUtils.isEmpty(onChange) ? "" : "onChange=\"" + onChange + "\"")
                .replace("{OPTIONS}", optionsBuffer.toString());

        // write output
        getJspContext().getOut().write(output);
    }

    private String getOption(T element, T selected) {
        SelectTagHandler handler = getSelectTagHandler();
        return OPTION_TEMPLATE
                .replace("{VALUE}", handler.getValue(element))
                .replace("{DESCRIPTION}", handler.getDescription(element))
                .replace("{SELECTED}", handler.isSelected(element, selected) ? "selected" : "");
    }

    private SelectTagHandler getSelectTagHandler() {
        if (selectTagHandler == null) return new DefaultSelectTagHandler();
        return selectTagHandler;
    }
}
