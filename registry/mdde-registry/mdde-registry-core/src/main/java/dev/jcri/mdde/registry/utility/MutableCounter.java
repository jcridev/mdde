package dev.jcri.mdde.registry.utility;

import static java.lang.Math.toIntExact;

/**
 * A simple counter class wrapping a long value into a mutable container. For example for use in Maps, where Atomic
 * counters make no sense. Class provides no thread-safety.
 */
public class MutableCounter {
    private long _cnt;

    /**
     * Default constructor. Counter starts at zero.
     */
    public MutableCounter() {
        this(0);
    }

    /**
     * Constructor.
     * @param start Intial value of the counter
     */
    public MutableCounter(long start) {
        this._cnt = start;
    }

    /**
     * Increment the current value by 1.
     */
    public void increment(){
        this._cnt++;
    }

    /**
     * Decrement the current value by 1.
     */
    public void decrement(){
        this._cnt--;
    }

    /**
     * Get current value.
     * @return Long value
     */
    public long get() {
        return this._cnt;
    }

    /**
     * Get an int value.
     * @return Integer value if the underlying long fits.
     * @throws ArithmeticException if the underlying counter can't fit into an Integer value.
     */
    public int getInt() throws ArithmeticException{
        // https://docs.oracle.com/javase/8/docs/api/java/lang/Math.html#toIntExact-long-
        return toIntExact(this._cnt);
    }

    /**
     * Override current value.
     * @param newVal New exact counter value.
     */
    public void set(long newVal) {
        this._cnt = newVal;
    }
}
