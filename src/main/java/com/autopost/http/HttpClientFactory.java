package com.autopost.http;

import com.autopost.util.Redactor;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Factory for creating configured HTTP clients with timeouts, retries, and proper logging. All
 * external HTTP calls should use clients created by this factory.
 */
@Component
public class HttpClientFactory {

  private static final Logger log = LoggerFactory.getLogger(HttpClientFactory.class);

  private final Redactor redactor;

  public HttpClientFactory(Redactor redactor) {
    this.redactor = redactor;
  }

  /**
   * Creates a standard HTTP client with default timeouts and retry logic.
   *
   * @return configured OkHttpClient
   */
  public OkHttpClient createClient() {
    return createClient(Duration.ofSeconds(30), Duration.ofSeconds(60), Duration.ofSeconds(60), 3);
  }

  /**
   * Creates an HTTP client with custom timeouts and retry configuration.
   *
   * @param connectTimeout connection timeout
   * @param readTimeout read timeout
   * @param writeTimeout write timeout
   * @param maxRetries maximum number of retries for failed requests
   * @return configured OkHttpClient
   */
  public OkHttpClient createClient(
      Duration connectTimeout, Duration readTimeout, Duration writeTimeout, int maxRetries) {
    return new OkHttpClient.Builder()
        .connectTimeout(connectTimeout)
        .readTimeout(readTimeout)
        .writeTimeout(writeTimeout)
        .addInterceptor(new RetryInterceptor(maxRetries))
        .addInterceptor(new LoggingInterceptor(redactor))
        .build();
  }

  /**
   * Creates an HTTP client optimized for large file downloads/uploads.
   *
   * @return configured OkHttpClient for large transfers
   */
  public OkHttpClient createLargeTransferClient() {
    return createClient(
        Duration.ofSeconds(60), // Longer connect timeout
        Duration.ofMinutes(10), // Longer read timeout for large files
        Duration.ofMinutes(10), // Longer write timeout for uploads
        2 // Fewer retries for large transfers
        );
  }

  /** Interceptor that handles retries with exponential backoff for transient failures. */
  private static class RetryInterceptor implements Interceptor {
    private static final Logger log = LoggerFactory.getLogger(RetryInterceptor.class);

    private final int maxRetries;

    public RetryInterceptor(int maxRetries) {
      this.maxRetries = maxRetries;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
      Request request = chain.request();
      IOException lastException = null;

      for (int attempt = 0; attempt <= maxRetries; attempt++) {
        try {
          if (attempt > 0) {
            // Calculate backoff delay with jitter
            long baseDelay = (long) Math.pow(2, attempt - 1) * 1000; // Exponential backoff
            long jitter = ThreadLocalRandom.current().nextLong(0, baseDelay / 2);
            long delay = Math.min(baseDelay + jitter, 30000); // Cap at 30 seconds

            log.debug(
                "Retrying request to {} after {}ms (attempt {}/{})",
                request.url().host(),
                delay,
                attempt,
                maxRetries);

            Thread.sleep(delay);
          }

          Response response = chain.proceed(request);

          // Check if we should retry based on response code
          if (shouldRetry(response, attempt)) {
            response.close();
            continue;
          }

          if (attempt > 0) {
            log.info("Request to {} succeeded on attempt {}", request.url().host(), attempt + 1);
          }

          return response;

        } catch (IOException e) {
          lastException = e;
          if (!shouldRetryOnException(e, attempt)) {
            throw e;
          }
          log.warn(
              "Request to {} failed (attempt {}/{}): {}",
              request.url().host(),
              attempt + 1,
              maxRetries + 1,
              e.getMessage());
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new IOException("Interrupted during retry backoff", e);
        }
      }

      // All retries exhausted
      log.error("Request to {} failed after {} attempts", request.url().host(), maxRetries + 1);
      throw lastException != null ? lastException : new IOException("All retries exhausted");
    }

    private boolean shouldRetry(Response response, int attempt) {
      if (attempt >= maxRetries) {
        return false;
      }

      int code = response.code();
      return code == 429 // Rate limited
          || code == 502 // Bad Gateway
          || code == 503 // Service Unavailable
          || code == 504; // Gateway Timeout
    }

    private boolean shouldRetryOnException(IOException e, int attempt) {
      if (attempt >= maxRetries) {
        return false;
      }

      String message = e.getMessage();
      if (message == null) {
        return false;
      }

      // Retry on connection timeouts and network errors
      return message.contains("timeout")
          || message.contains("connection")
          || message.contains("network")
          || message.contains("socket");
    }
  }

  /** Interceptor that logs HTTP requests and responses without exposing sensitive information. */
  private static class LoggingInterceptor implements Interceptor {
    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);

    private final Redactor redactor;

    public LoggingInterceptor(Redactor redactor) {
      this.redactor = redactor;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
      Request request = chain.request();
      long startTime = System.currentTimeMillis();

      log.debug("→ {} {}", request.method(), request.url());

      // Log headers (redacted)
      if (log.isTraceEnabled()) {
        for (int i = 0; i < request.headers().size(); i++) {
          String name = request.headers().name(i);
          String value = request.headers().value(i);
          String redactedValue = redactor.redactHeader(name, value);
          log.trace("→ {}: {}", name, redactedValue);
        }
      }

      Response response;
      try {
        response = chain.proceed(request);
      } catch (IOException e) {
        long duration = System.currentTimeMillis() - startTime;
        log.warn(
            "← {} {} failed after {}ms: {}",
            request.method(),
            request.url(),
            duration,
            redactor.redactException(e));
        throw e;
      }

      long duration = System.currentTimeMillis() - startTime;
      log.debug("← {} {} {} ({}ms)", request.method(), request.url(), response.code(), duration);

      // Log response headers (redacted) if trace enabled
      if (log.isTraceEnabled()) {
        for (int i = 0; i < response.headers().size(); i++) {
          String name = response.headers().name(i);
          String value = response.headers().value(i);
          String redactedValue = redactor.redactHeader(name, value);
          log.trace("← {}: {}", name, redactedValue);
        }
      }

      return response;
    }
  }
}
