package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Clock clock;


  ProfilingMethodInterceptor(Clock clock) {
    this.clock = clock;
  }


  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Profiled annotation = method.getAnnotation(Profiled.class);
    if (annotation != null) {
      Instant start = clock.instant();
      Object result = method.invoke(proxy, args);
      Instant end = clock.instant();
      Duration duration = Duration.between(start, end);
      ProfilingState.record(annotation.getClass(), duration);
      return result;
    } else {
      return method.invoke(proxy, args);
    }
  }
}
