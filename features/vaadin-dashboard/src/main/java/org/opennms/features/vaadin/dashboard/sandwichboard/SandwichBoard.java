package org.opennms.features.vaadin.dashboard.sandwichboard;

import com.vaadin.server.VaadinSession;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;

import java.util.*;

/**
 * @author Marcus Hellberg (marcus@vaadin.com)
 */
public class SandwichBoard extends CssLayout {

    private final CssLayout contentLayout;
    private List<SandwichSpec> sandwiches = new LinkedList<SandwichSpec>();
    int currentIndex = -1;
    long currentIteration = 0;
    private Label progressPausedLabel;
    private boolean paused;
    private ProgressIndicator progressIndicator;
    private long startTime = 0;
    private int currentViewDuration;
    private Timer progressTimer;
    private Timer transitionTimer;


    public SandwichBoard() {
        addStyleName("sandwhich-board");
        contentLayout = new CssLayout();
        contentLayout.addComponent(new Label("Nothing to display"));
        addComponent(contentLayout);
        setSizeFull();
    }


    public void addSandwich(SandwichSpec sandwichHolder) {
        sandwiches.add(sandwichHolder);

        // Immediately show first
        if (getSandwiches().size() == 1) {
            showNext();
            startProgressTimer();
        }
    }

    private void startProgressTimer() {
        progressTimer = new Timer();
        progressTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateProgress();
            }
        }, 200, 200);
    }

    private synchronized void showNext() {
        if (getSandwiches().isEmpty()) {
            return;
        }
        getNextIndex();
        contentLayout.removeAllComponents();
        resetProgressIndicator();
        Sandwich sandwich = getSandwiches().get(currentIndex).getSandwichInstance();
        sandwich.addStyleName("sandwich");
        contentLayout.addComponent(sandwich);
        scheduleNext();
    }

    private void resetProgressIndicator() {
        if (progressIndicator == null) {
            progressIndicator = new ProgressIndicator();
            progressIndicator.setWidth("100%");
            progressIndicator.setPollingInterval(200);
            addComponent(progressIndicator, 0);
        }

        progressIndicator.setValue(0.0f);
    }

    private void getNextIndex() {
        int tries = 0;
        while (true) {
            currentIndex++;
            if (currentIndex > getSandwiches().size() - 1) {
                currentIndex = 0;
                currentIteration++;
            }

            if (currentIteration % getSandwiches().get(currentIndex).getPriority() == 0) {
                break;
            }
            tries++;

            if (tries > getSandwiches().size()) {
                throw new IllegalStateException("Could not find a Sandwich to show, at least one Sandwich must have a priority of 1 ");
            }
        }

    }

    private void scheduleNext() {
        // Schedule next
        transitionTimer = new Timer();
        currentViewDuration = getSandwiches().get(currentIndex).getDuration() * 1000;

        if (currentViewDuration > 0) {
            startTime = System.currentTimeMillis();
            transitionTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // Show next if it's ok, otherwise wait again.
                    if (canAdvance()) {
                        VaadinSession.getCurrent().lock();
                        try {
                            showNext();
                        } finally {
                           VaadinSession.getCurrent().unlock();
                        }
                    } else {
                        scheduleNext();
                    }
                }
            }, currentViewDuration);
        } else {
            throw new IllegalStateException("Delay for " + getSandwiches().get(currentIndex).getSandwichClass().getSimpleName() + " is <= 0");
        }
    }

    private boolean canAdvance() {
        if (paused) {
            return false;
        }

        SandwichSpec sandwichSpec = getSandwiches().get(currentIndex);

        if (!sandwichSpec.isPausable()) {
            return true;
        } else {
            boolean canAdvance = sandwichSpec.getSandwichInstance().allowAdvance();

            if (!canAdvance) {
                VaadinSession.getCurrent().lock();
                try {
                    showProgressPaused();
                } finally {
                    VaadinSession.getCurrent().unlock();
                }
            }
            return canAdvance;
        }
    }

    private void showProgressPaused() {
        if (progressPausedLabel == null) {
            progressPausedLabel = new Label("Unacknowledged Alerts, cannot proceed");
            progressPausedLabel.addStyleName("progress-paused");
            progressPausedLabel.setSizeUndefined();
        }
        contentLayout.addComponent(progressPausedLabel, 0);
    }

    public List<SandwichSpec> getSandwiches() {
        return Collections.unmodifiableList(sandwiches);
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }


    private void updateProgress() {
        if(progressIndicator != null && startTime > 0) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            float elapsedPercentage = elapsedTime / (float) currentViewDuration;
            progressIndicator.setValue(elapsedPercentage > 1 ? 1f : elapsedPercentage);
        }
    }

    @Override
    public void detach() {
        if (progressTimer != null) {
            progressTimer.cancel();
        }

        if (transitionTimer != null) {
            transitionTimer.cancel();
        }
        super.detach();
    }
}
