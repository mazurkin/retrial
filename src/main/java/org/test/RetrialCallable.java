package org.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Callable;

/**
 * Allows to automate retries to java.util.concurrent.Callable
 * @param <T> Result type
 */
public class RetrialCallable<T> implements Callable<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetrialCallable.class);

    private final int trials;

    private final Callable<T> delegate;

    private final RetrialCallableExceptionStrategy exceptionStrategy;

    public RetrialCallable(int trials, Callable<T> delegate) {
        this(trials, RetrialCallableExceptionStrategy.NONE, delegate);
    }

    public RetrialCallable(int trials, RetrialCallableExceptionStrategy exceptionStrategy, Callable<T> delegate) {
        if (trials <= 0) {
            throw new IllegalArgumentException("Trial count must be greater than 0: " + trials);
        }

        if (delegate == null) {
            throw new IllegalArgumentException("Delegate is null");
        }

        if (exceptionStrategy == null) {
            throw new IllegalArgumentException("Exception strategy is null");
        }

        this.trials = trials;
        this.delegate = delegate;
        this.exceptionStrategy = exceptionStrategy;
    }

    public static <T> T call(int trials,
                             RetrialCallableExceptionStrategy exceptionStrategy,
                             Callable<T> delegate) throws Exception
    {
        Callable<T> retrial = new RetrialCallable<>(trials, exceptionStrategy, delegate);

        return retrial.call();
    }

    @Override
    public T call() throws Exception {
        Deque<Exception> exceptions = null;

        for (int trial = 0, left = trials - 1; trial < trials; trial++, left--) {
            try {
                try {
                    return delegate.call();
                } catch (InterruptedException e) {
                    // don't try to repeat execution when the thread is interrupted
                    throw e;
                } catch (Exception e) {
                    LOGGER.debug("Exception occurred on trial {} from {}", new Object[] { trial + 1, trials, e });

                    if (left > 0) {
                        if (exceptions == null) {
                            exceptions = new ArrayDeque<>(trials);
                        }

                        exceptions.addLast(e);

                        exceptionStrategy.handleException(e, trial, trials);
                    } else {
                        throw e;
                    }
                }
            } catch (Exception e) {
                throw enrichException(e, exceptions);
            }
        }

        throw new IllegalStateException("Should never happen. Blame the developer");
    }

    private static Exception enrichException(Exception e, Deque<Exception> exceptions) {
        if (exceptions != null) {
            while (!exceptions.isEmpty()) {
                Exception suppressed = exceptions.removeFirst();
                if (e != suppressed) {
                    e.addSuppressed(suppressed);
                }
            }
        }

        return e;
    }
    
}
