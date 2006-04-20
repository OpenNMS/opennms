package org.opennms.netmgt.scheduler;

public interface Scheduler extends ScheduleTimer {

	/**
	 * This method is used to schedule a ready runnable in the system. The
	 * interval is used as the key for determining which queue to add the
	 * runnable.
	 * @param interval
	 *            The queue to add the runnable to.
	 * @param runnable
	 *            The element to run when interval expires.
	 * 
	 * @throws java.lang.RuntimeException
	 *             Thrown if an error occurs adding the element to the queue.
	 */
	public abstract void schedule(long interval, final ReadyRunnable runnable);

	/**
	 * This returns the current time for the scheduler
	 */
	public abstract long getCurrentTime();

	/**
	 * Starts the fiber.
	 * 
	 * @throws java.lang.IllegalStateException
	 *             Thrown if the fiber is already running.
	 */
	public abstract void start();

	/**
	 * Stops the fiber. If the fiber has never been run then an exception is
	 * generated.
	 * 
	 * @throws java.lang.IllegalStateException
	 *             Throws if the fiber has never been started.
	 */
	public abstract void stop();

	/**
	 * Pauses the scheduler if it is current running. If the fiber has not been
	 * run or has already stopped then an exception is generated.
	 * 
	 * @throws java.lang.IllegalStateException
	 *             Throws if the operation could not be completed due to the
	 *             fiber's state.
	 */
	public abstract void pause();

	/**
	 * Resumes the scheduler if it has been paused. If the fiber has not been
	 * run or has already stopped then an exception is generated.
	 * 
	 * @throws java.lang.IllegalStateException
	 *             Throws if the operation could not be completed due to the
	 *             fiber's state.
	 */
	public abstract void resume();

	/**
	 * Returns the current of this fiber.
	 * 
	 * @return The current status.
	 */
	public abstract int getStatus();

}