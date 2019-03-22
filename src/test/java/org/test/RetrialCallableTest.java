package org.test;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Callable;

public class RetrialCallableTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetrialCallableTest.class);

    @Test
    public void testNormal() throws Exception {
        Callable<String> delegate = Mockito.mock(CallableString.class);

        Mockito.when(delegate.call())
                .thenReturn("abc");

        Callable<String> callable = new RetrialCallable<>(3, delegate);

        Assert.assertEquals("abc", callable.call());
    }

    @Test
    public void testAllExceptions() throws Exception {
        Callable<String> delegate = Mockito.mock(CallableString.class);

        RetrialCallableExceptionStrategy strategy = Mockito.mock(RetrialCallableExceptionStrategy.class);

        Mockito.when(delegate.call())
                .thenThrow(new IOException("exception 1"))
                .thenThrow(new IOException("exception 2"))
                .thenThrow(new IOException("exception 3"));

        Callable<String> callable = new RetrialCallable<>(3, strategy, delegate);
        
        try {
            callable.call();
            Assert.fail("Exception is expected");
        } catch (IOException e) {
            LOGGER.debug("Expected exception", e);
        }

        Mockito.verify(strategy, Mockito.times(1)).handleException(
                Mockito.anyObject(), Mockito.eq(0), Mockito.eq(3));
        Mockito.verify(strategy, Mockito.times(1)).handleException(
                Mockito.anyObject(), Mockito.eq(1), Mockito.eq(3));
        Mockito.verify(strategy, Mockito.times(1)).handleException(
                Mockito.anyObject(), Mockito.eq(2), Mockito.eq(3));

        Mockito.verify(strategy, Mockito.times(3)).handleException(
                Mockito.anyObject(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    public void testSuccessfullyEnds() throws Exception {
        Callable<String> delegate = Mockito.mock(CallableString.class);

        Mockito.when(delegate.call())
                .thenThrow(new IOException("exception 1"))
                .thenThrow(new IOException("exception 2"))
                .thenReturn("abc");

        Callable<String> callable = new RetrialCallable<>(3, delegate);

        Assert.assertEquals("abc", callable.call());
    }

    @Test
    public void testStaticCall() throws Exception {
        Callable<String> delegate = Mockito.mock(CallableString.class);

        Mockito.when(delegate.call())
                .thenThrow(new IOException("exception 1"))
                .thenThrow(new IOException("exception 2"))
                .thenReturn("abc");

        String result = RetrialCallable.call(3, RetrialCallableExceptionStrategy.NONE, delegate);

        Assert.assertEquals("abc", result);
    }

    private interface CallableString extends Callable<String> {

    }

}