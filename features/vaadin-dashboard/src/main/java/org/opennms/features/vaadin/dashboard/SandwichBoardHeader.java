package org.opennms.features.vaadin.dashboard;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.*;
import org.opennms.features.vaadin.dashboard.sandwichboard.SandwhichMap;
import org.opennms.features.vaadin.dashboard.sandwichboard.SandwichBoard;
import org.opennms.features.vaadin.dashboard.sandwichboard.SandwichSpec;

/**
 * @author Marcus Hellberg (marcus@vaadin.com)
 */
public class SandwichBoardHeader extends HorizontalLayout {


    private final FieldGroup fieldGroup;
    private final Button pauseButton;
    private BeanItem<SandwichSpec> item;
    private final Button addButton;
    private TextField duration;
    private ComboBox sandwichClass;
    private ComboBox priority;
    private CheckBox pausable;
    private boolean paused = false;


    public SandwichBoardHeader(final SandwichBoard sandwichBoard) {
        setSpacing(true);
        setWidth("100%");

        fieldGroup = new FieldGroup();
        item = new BeanItem<SandwichSpec>(new SandwichSpec());
        fieldGroup.setItemDataSource(item);

        addSandwichSelector();
        addDelayField();
        addPrioritySelector();
        addPauseCheckBox();

        addButton = new Button("Add", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    fieldGroup.commit();
                    sandwichBoard.addSandwich(item.getBean());
                    item = new BeanItem<SandwichSpec>(new SandwichSpec());
                    fieldGroup.setItemDataSource(item);
                } catch (FieldGroup.CommitException e) {
                    e.printStackTrace();
                }
            }
        });
        addComponent(addButton);

        setComponentAlignment(addButton, Alignment.BOTTOM_RIGHT);
        fieldGroup.bindMemberFields(this);

        pauseButton = new Button("Pause", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                paused = !paused;

                if(paused){
                    pauseButton.setCaption("Resume");
                } else {
                    pauseButton.setCaption("Pause");
                }
                sandwichBoard.setPaused(paused);
            }
        });
        addComponent(pauseButton);
        setComponentAlignment(pauseButton, Alignment.BOTTOM_RIGHT);
        setExpandRatio(pauseButton, 1.0f);
    }

    private void addPauseCheckBox() {
        pausable = new CheckBox("Can pause");
        addComponent(pausable);
        setComponentAlignment(pausable, Alignment.BOTTOM_RIGHT);
    }

    private void addDelayField() {
        duration = new TextField("Duration");
        duration.setRequired(true);
        addComponent(duration);
    }

    private void addPrioritySelector() {
        priority = new ComboBox("Priority");
        priority.setRequired(true);
        priority.setNullSelectionAllowed(false);
        priority.addItem(1);
        priority.addItem(2);
        priority.addItem(3);
        addComponent(priority);
    }

    private void addSandwichSelector() {
        sandwichClass = new ComboBox("View");
        sandwichClass.setNullSelectionAllowed(false);
        sandwichClass.setRequired(true);
        sandwichClass.addItem(SandwhichMap.class);
        sandwichClass.addItem(AlertListDashlet.class);

        sandwichClass.setItemCaption(SandwhichMap.class, "Map View");
        sandwichClass.setItemCaption(AlertListDashlet.class, "Alert List View");
        sandwichClass.select(SandwhichMap.class);

        addComponent(sandwichClass);

    }
}
