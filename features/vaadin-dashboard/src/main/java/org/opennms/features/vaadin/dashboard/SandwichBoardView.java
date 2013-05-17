package org.opennms.features.vaadin.dashboard;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.VerticalLayout;
import org.opennms.features.vaadin.dashboard.sandwichboard.SandwichBoard;

/**
 * @author Marcus Hellberg (marcus@vaadin.com)
 */
public class SandwichBoardView extends VerticalLayout implements View {

    private final SandwichBoardHeader sandwichBoardHeader;
    private final SandwichBoard sandwichBoard;

    public SandwichBoardView() {
        setSizeFull();
        sandwichBoard = new SandwichBoard();
        sandwichBoardHeader = new SandwichBoardHeader(sandwichBoard);


        addComponents(sandwichBoardHeader, sandwichBoard);
        setExpandRatio(sandwichBoard, 1.0f);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

    }
}
