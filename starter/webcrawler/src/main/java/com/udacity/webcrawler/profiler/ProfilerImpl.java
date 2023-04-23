package com.udacity.webcrawler.profiler;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.lang.reflect.Proxy;

import static com.udacity.webcrawler.json.CrawlResultWriter.write;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * Concrete implementation of the {@link Profiler}.
 */
final class ProfilerImpl implements Profiler {

  private final Clock clock;
  private final ProfilingState state = new ProfilingState();
  private final ZonedDateTime startTime;


  @Inject
  ProfilerImpl(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
    this.startTime = ZonedDateTime.now(clock);
  }

  @Override
  public <T> T wrap(Class<T> klass, T delegate) {
    Objects.requireNonNull(klass);
    if (!ProfiledClass(klass)) {
      throw new IllegalArgumentException();
    }
    ProfilingMethodInterceptor interceptor = new ProfilingMethodInterceptor(clock);
    return (T) Proxy.newProxyInstance(
            delegate.getClass().getClassLoader(),
            new Class[]{klass},
            interceptor);
  }

  private <T> boolean ProfiledClass(Class<T> klass) {
    boolean profiledClass = Arrays.stream(Klass.getDeclaredMethods()).anyMatch(
            result -> result.getAnnotation(Profiled.class) != null);
    return profiledClass;
  }

  @Override
  public void writeData(Path path) throws IOException{
    // TODO: Write the ProfilingState data to the given file path. If a file already exists at that
    //       path, the new data should be appended to the existing file.
    if (Files.deleteIfExists(path)) {
      Files.createDirectory(path);
    }
    try {
      Writer writer = Files.newBufferedWriter(path);
      write(writer);
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void writeData(Writer writer) throws IOException {
    writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
    writer.write(System.lineSeparator());
    state.write(writer);
    writer.write(System.lineSeparator());
  }

}
