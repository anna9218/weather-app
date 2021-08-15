package firstofferztask;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class SanityTest {
  Vertx vertx = Vertx.vertx();


  @Before
  public void deployVerticle(TestContext testContext) {
    vertx.deployVerticle(new MainVerticle(), testContext.asyncAssertSuccess());
  }

  @Test
  public void startServer(TestContext testContext) {
    vertx.createHttpServer()
      .requestHandler(req -> req.response().end("Ok"))
      .listen(8080, ar -> {
      });
  }

  @Test
  public void testHealthChecApi(TestContext testContext) {
    HttpClient client = vertx.createHttpClient();

    client.request(HttpMethod.GET, 8080, "localhost", "/healthcheck")
      .compose(req -> req.send().compose(HttpClientResponse::body))
      .onComplete(testContext.asyncAssertSuccess(buffer -> testContext.verify((res) -> {
        Assertions.assertEquals(buffer.toString(), "{\"response\":\"I'm alive!!!\"}");
      })));
  }

  @Test
  public void testHelloApi(TestContext testContext) {
    HttpClient client = vertx.createHttpClient();

    client.request(HttpMethod.GET, 8080, "localhost", "/hello?name=Sydney")
      .compose(req -> req.send().compose(HttpClientResponse::body))
      .onComplete(testContext.asyncAssertSuccess(buffer -> testContext.verify((res) -> {
        Assertions.assertEquals(buffer.toString(), "{\"response\":\"Hello Sydney!\"}");
      })));
  }

  @Test
  public void testCurrentForecastsApi(TestContext testContext) {
    HttpClient client = vertx.createHttpClient();

    client.request(HttpMethod.GET, 8080, "localhost", "/forecasts?city=Sydney&country=AU")
      .compose(req -> req.send().compose(HttpClientResponse::body))
      .onComplete(testContext.asyncAssertSuccess(buffer -> testContext.verify((res) ->{
        Assertions.assertEquals(buffer.toString(), "Ok");
      })));
  }

  @Test
  public void testCurrentForecastDaysApi(TestContext testContext) {
    HttpClient client = vertx.createHttpClient();

    client.request(HttpMethod.GET, 8080, "localhost", "/forecasts?city=Sydney&country=AU&days=1")
      .compose(req -> req.send().compose(HttpClientResponse::body))
      .onComplete(testContext.asyncAssertSuccess(buffer -> testContext.verify((res) -> {
        Assertions.assertEquals(buffer.toString(), "Ok");
      })));
  }
}

