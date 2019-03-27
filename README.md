# Retries for java.util.Callable

```
RetrialCallableExceptionStrategy strategy = (e, i, r) -> {
    LOGGER.error("Failed to load a value on trial #{}", i + 1);
    Thread.sleep(1000 + i * 3000);
};

// try to load value 3 times or throw an exception
String value = RetrialCallable.call(3, strategy, () -> loadString());
```