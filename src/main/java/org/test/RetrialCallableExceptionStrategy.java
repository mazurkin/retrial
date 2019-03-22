package org.test;

/**
 * Allows to implement some logic (pauses for example) when an exception occurs
 */
public interface RetrialCallableExceptionStrategy {

    /**
     * Exception strategy that does nothing
     */
    RetrialCallableExceptionStrategy NONE = (e, i, n) -> { };

    /**
     * Handle the exception
     *
     * @param exception Exception that has been thrown
     * @param trial The index of trial in the range: {@code 0 <= trial < total}
     * @param total The total number of trials to be made
     *
     * @throws InterruptedException Exception on thread interruption
     */
    void handleException(Exception exception, int trial, int total) throws InterruptedException;

}
