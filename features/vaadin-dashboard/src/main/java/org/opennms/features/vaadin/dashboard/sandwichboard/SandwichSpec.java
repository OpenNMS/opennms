package org.opennms.features.vaadin.dashboard.sandwichboard;

/**
* @author Marcus Hellberg (marcus@vaadin.com)
*/
public class SandwichSpec {

    private Class<? extends Sandwich> sandwichClass;
    private int duration = 10;
    private int priority = 1;
    private boolean pausable = true;
    private Sandwich sandwichInstance;

    public boolean isPausable() {
        return pausable;
    }

    public void setPausable(boolean pausable) {
        this.pausable = pausable;
    }


    public Class<? extends Sandwich> getSandwichClass() {
        return sandwichClass;
    }

    public void setSandwichClass(Class<? extends Sandwich> sandwichClass) {
        this.sandwichClass = sandwichClass;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Returns an instance of the type of Sanwich that was initialized.
     *
     * Creates a new instance when called for the first time,
     * other calls will return the same instance that was created.
     * @return
     */
    public Sandwich getSandwichInstance(){
        if (sandwichInstance == null) {
            try {
                sandwichInstance = sandwichClass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return sandwichInstance;
    }
}
