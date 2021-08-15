package firstofferztask;

import com.google.gson.Gson;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


public class MainVerticle extends AbstractVerticle {
  private CityIdMapper[] arrayOfJsons;


  @Override
  public void start() throws Exception {
    // read json for future reference
//    String filePath = "D:\\Coding\\firstofferz\\src\\main\\java\\firstofferztask\\city.json";
//    FileSystem file = vertx.fileSystem().readFile(filePath, ar -> {
//      if (ar.failed()) {
//
//      }
//      else {
//
//      }
//    });

    // create a runnable to read from city list file
    String filePath = System.getProperty("user.dir") + "\\src\\main\\java\\firstofferztask\\city.json";
    Gson gson = new Gson();
    Runnable runnable =
      () -> {
        try {
          Reader input = new FileReader(filePath);
          arrayOfJsons = gson.fromJson(input, CityIdMapper[].class);
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        }
      };
    Thread thread = new Thread(runnable);
    thread.start();


    // Create a Router
    Router router = Router.router(vertx);


    // The first one is for : http://localhost:8080/healthcheck which returns constant string. "I'm alive!!!"
    Route route1 = router.route().path("/healthcheck");
    route1.handler(context -> {
      // This handler will be called for the following request paths:
      // `/healthcheck`
      // `/healthcheck/`
      // `/healthcheck//`
      // but not:
      // `/healthcheck/subdir`
      context.json(
        new JsonObject().put("response", "I'm alive!!!"));
    });


    // The second is for :http://localhost:8080/hello?name=XXX which returns "Hello XXX!"
    Route route2 = router.route().path("/hello");
    route2.handler(context -> {
      // Get the query parameter "name"
      MultiMap queryParams = context.queryParams();
      String name = queryParams.contains("name") ? queryParams.get("name") : "unknown";
      // Write a json response
      context.json(
        new JsonObject()
          .put("response", "Hello " + name + "!")
      );
    });


    //-----------------------------------------------------------------------------//
    // The third is for http://localhost:8080/currentforecasts?city=XXX&country=cc
    // which returns current weather for the selected location
    // {
    //  "country": "AU",
    //  "city": "Cairns",
    //  "temp": 10.5,
    //  "humidity": 74,
    //  "date": "04/06/2021"
    // }
    // The temperature should be in Celsius degree.
    //-----------------------------------------------------------------------------//
    Route route3 = router.route().path("/currentforecasts");
    route3.handler(context -> {
      // Get the query parameters "city" and "country"
      MultiMap queryParams = context.queryParams();
      String city = queryParams.contains("city") ? queryParams.get("city") : "unknown";
      String country = queryParams.contains("country") ? queryParams.get("country") : "unknown";

      // function to make the API call,  get the relevant info, and return a response to the user
      fetchCurrentForecast(city, country, context);
    });


    //-----------------------------------------------------------------------------//
    // The fourth is for http://localhost:8080/forecasts?city=XXX&country=cc&days=YYY
    // which returns a Json object with the weather for the selected city.
    // {
    //  "forecasts": [
    //     {
    //      "date": "01/06/2021",
    //       "dayTemp": 33.2,
    //       "minTemp": 24,
    //       "maxTemp": 37
    //     },
    //     {
    //       /Next day data here
    //     }
    //  ]
    //}
    //-----------------------------------------------------------------------------//
    Route route4 = router.route().path("/forecasts");
    route4.handler(context -> {
      // Get the query parameter "city", "country", and "days"
      MultiMap queryParams = context.queryParams();
      String city = queryParams.contains("city") ? queryParams.get("city") : "unknown";
      String country = queryParams.contains("country") ? queryParams.get("country") : "unknown";
      String daysStr = queryParams.contains("days") ? queryParams.get("days") : "unknown";
      int days = Integer.parseInt(daysStr);

      // read json city file and extract the relevant city id
      Integer cityID = getCityIDFromJson(city, country);
      if (cityID != null)
        fetchForecasts(cityID, days, context);
    });



    //------ Create the HTTP server and listen ------//
    vertx.createHttpServer()
      // Handle every request using the router
      .requestHandler(router)
      // Start listening
      .listen(8080)
      // Print the port
      .onSuccess(server ->
        System.out.println(
          "HTTP server started on port " + server.actualPort()
        )
      );
  }


  /**
   * get the cityID of the provided city's and country's names
   * @param city name as string
   * @param country 2-letter code as string
   * @return
   */
  private Integer getCityIDFromJson(String city, String country) {
    for (CityIdMapper jsonObj : arrayOfJsons) {
      // insensitive compare
      if (jsonObj.getCity().equalsIgnoreCase(city) && jsonObj.getCountry().equalsIgnoreCase(country)) {
        return jsonObj.getId();
      }
    }
    return null;
  }


  /**
   * perform a GET request to the weather API for the current weather, and return a response to the user
   * @param city name as string
   * @param country 2-letter code as string
   * @param context to write the response to
   */
  private void fetchCurrentForecast(String city, String country, RoutingContext context) {
    // initializing const params
    String key = "793dc133790da68ae9b59470bb584f69"; //TODO: use env variables for keys
    String host = "api.openweathermap.org";
    String path = String.format("/data/2.5/weather?q=%s,%s&appid=%s", city, country, key);

    // creating HTTP request
    HttpRequest<JsonObject> request = WebClient.create(vertx)
      .get(443, host, path)  // on port 443 (HTTPS)
      .ssl(true)  // enable SSL encryption
      .putHeader("Accept", "application/json")
      .as(BodyCodec.jsonObject())
      .expect(ResponsePredicate.SC_OK);

    request
      .send()
      .onSuccess(asyncResult -> {
        JSONObject jsonResponse = new JSONObject(asyncResult.body()).getJSONObject("map");
        int humidity = jsonResponse.getJSONObject("main").getInt("humidity");
        double temp = jsonResponse.getJSONObject("main").getDouble("temp");

        // Write a json response
        context.json(
          new JsonObject()
            .put("country", country)
            .put("city", city)
            .put("temp", convertFromKelvinToCelsius(temp))
            .put("humidity", humidity)
            .put("date", getDate())
        );
      });
  }


  /**
   * perform a GET request to the weather API for the future forecasts, and return a response to the user
   * @param cityID as integer
   * @param days to indicate the number of forecasts
   * @param context to write the response to
   */
  private void fetchForecasts(Integer cityID, int days, RoutingContext context) {
    // initializing const params
    String key = "793dc133790da68ae9b59470bb584f69";
    String host = "api.openweathermap.org";
    String path = String.format("/data/2.5/forecast?id=%s&appid=%s", cityID, key);

    // creating HTTP request
    HttpRequest<JsonObject> request = WebClient.create(vertx)
      .get(443, host, path)  // on port 443 (HTTPS)
      .ssl(true)  // enable SSL encryption
      .putHeader("Accept", "application/json")
      .as(BodyCodec.jsonObject())
      .expect(ResponsePredicate.SC_OK);

    request
      .send()
      .onSuccess(asyncResult -> {
        JSONObject jsonResponse = new JSONObject(asyncResult.body()).getJSONObject("map");
        JSONArray jsonArray = jsonResponse.getJSONArray("list");

        // handle dates in received response json array
//        String dt_txt = jsonArray.getJSONObject(0).getString("dt_txt");
//        String[] dtArr = dt_txt.split(" ", 2);
//        String currentDt = dtArr[0];
//        JsonObject[] resultArr = new JsonObject[days];
//        int currentDay = 0;
//        double temp = 0, tempMin = 0, tempMax = 0;
//        int dayCount = 0;
//
//        for (int n = 0; n < jsonArray.length(); n++) {
//          if (currentDay >= days)
//            break;
//
//          dt_txt = jsonArray.getJSONObject(n).getString("dt_txt");
//          dtArr = dt_txt.split(" ", 2);
//
//          if(currentDt.equals(dtArr[0])) { // same day
//            temp += convertFromKelvin(jsonArray.getJSONObject(n).getJSONObject("main").getDouble("temp"));
//            tempMin += convertFromKelvin(jsonArray.getJSONObject(n).getJSONObject("main").getDouble("temp_min"));
//            tempMax += convertFromKelvin(jsonArray.getJSONObject(n).getJSONObject("main").getDouble("temp_max"));
//          }
//          else {
//            // calculate mean values
//            double resultTemp = temp / dayCount;
//            double resultTempMin = tempMin / dayCount;
//            double resultTempMax = tempMax / dayCount;
//            // reset for next date
//            dayCount = 0;
//            temp = 0;
//            tempMax = 0;
//            tempMin = 0;
//            currentDt = dtArr[0];
//
//            // create day json
//            JsonObject jsonDay = new JsonObject();
//            jsonDay.put("date", currentDt);
//            jsonDay.put("dayTemp", String.valueOf(new DecimalFormat("##.##").format(resultTemp)));
//            jsonDay.put("minTemp", String.valueOf(new DecimalFormat("##.##").format(resultTempMin)));
//            jsonDay.put("maxTemp", String.valueOf(new DecimalFormat("##.##").format(resultTempMax)));
//
//            // place json in result array
//            resultArr[currentDay] = jsonDay;
//            currentDay++;
//
//            // still handle new day
//            temp += convertFromKelvin(jsonArray.getJSONObject(n).getJSONObject("main").getDouble("temp"));
//            tempMin += convertFromKelvin(jsonArray.getJSONObject(n).getJSONObject("main").getDouble("temp_min"));
//            tempMax += convertFromKelvin(jsonArray.getJSONObject(n).getJSONObject("main").getDouble("temp_max"));
//          }
//          dayCount++;
//        }


        // Write a json response
        context.json(
          new JsonObject()
            .put("forecasts", extractForecastData(jsonArray, days))
        );
      });

  }


  /**
   * Extracts data from the received JsonArray
   * @param jsonArray contains all the weather data for the number of given days, in 3-hour intervals
   * @param days represents the number of desired forecasts
   * @return result JsonObject array, where each JsonObject represents a single forecast
   */
  private JsonObject[] extractForecastData(JSONArray jsonArray, int days) {
    JsonObject[] resultArr = new JsonObject[days];

    String dt_txt = jsonArray.getJSONObject(0).getString("dt_txt");
    String[] dtArr = dt_txt.split(" ", 2);
    String currentDt = dtArr[0];

    int currentDay = 0;
    double temp = 0, tempMin = 0, tempMax = 0;
    int dayCount = 0;

    for (int n = 0; n < jsonArray.length(); n++) {
      if (currentDay >= days)
        break;

      dt_txt = jsonArray.getJSONObject(n).getString("dt_txt");
      dtArr = dt_txt.split(" ", 2);

      if(currentDt.equals(dtArr[0])) { // same day
        temp = aggregateTempValues(jsonArray, n, temp);
        tempMin = aggregateTempMinValues(jsonArray, n, tempMin);
        tempMax = aggregateTempMaxValues(jsonArray, n, tempMax);
      }
      else {
        // calculate mean values
        double resultTemp = temp / dayCount;
        double resultTempMin = tempMin / dayCount;
        double resultTempMax = tempMax / dayCount;

        // reset for next date
        dayCount = 0;
        temp = 0;
        tempMax = 0;
        tempMin = 0;
        currentDt = dtArr[0];

        // create day json and place json in result array
        resultArr[currentDay] = createDayJson(currentDt, resultTemp, resultTempMin, resultTempMax);
        currentDay++;

        // still handle new day
        temp = aggregateTempValues(jsonArray, n, temp);
        tempMin = aggregateTempMinValues(jsonArray, n, tempMin);
        tempMax = aggregateTempMaxValues(jsonArray, n, tempMax);
      }
      dayCount++;
    }

    return resultArr;
  }


  /**
   * Extract the temp value from the given JsonArray
   * @param jsonArray
   * @param n
   * @param temp
   * @return the temp value
   */
  private double aggregateTempValues(JSONArray jsonArray, int n, double temp) {
    temp += convertFromKelvin(jsonArray.getJSONObject(n).getJSONObject("main").getDouble("temp"));
    return temp;
  }

  /**
   * Extract the min temp value from the given JsonArray
   * @param jsonArray
   * @param n
   * @param tempMin
   * @return the min temp value
   */
  private double aggregateTempMinValues(JSONArray jsonArray, int n, double tempMin) {
    tempMin += convertFromKelvin(jsonArray.getJSONObject(n).getJSONObject("main").getDouble("temp_min"));
    return tempMin;
  }

  /**
   * Extract the max temp value from the given JsonArray
   * @param jsonArray
   * @param n
   * @param tempMax
   * @return the max temp value
   */
  private double aggregateTempMaxValues(JSONArray jsonArray, int n, double tempMax) {
    tempMax += convertFromKelvin(jsonArray.getJSONObject(n).getJSONObject("main").getDouble("temp_max"));
    return tempMax;
  }


  /**
   * Create a JsonObject which represents a single day in a forecast
   * @param currentDt of type string, the current date
   * @param resultTemp
   * @param resultTempMin
   * @param resultTempMax
   * @return JsonObject with the provided data
   */
  private JsonObject createDayJson(String currentDt, double resultTemp, double resultTempMin, double resultTempMax) {
    JsonObject jsonDay = new JsonObject();

    jsonDay.put("date", currentDt);
    jsonDay.put("dayTemp", String.valueOf(new DecimalFormat("##.##").format(resultTemp)));
    jsonDay.put("minTemp", String.valueOf(new DecimalFormat("##.##").format(resultTempMin)));
    jsonDay.put("maxTemp", String.valueOf(new DecimalFormat("##.##").format(resultTempMax)));

    return jsonDay;
  }


  /**
   * Function to convert the temperature from Kelvin to Celsius
   * @param temp as double, in Kelvin units
   * @return temp as string, in Celsius units
   */
  private String convertFromKelvinToCelsius(double temp) {
    final double KELVIN = 273.15;
    temp -= KELVIN;
    return String.valueOf(new DecimalFormat("##.##").format(temp));
  }


  /**
   * Function to convert the temperature from Kelvin to Celsius
   * @param temp as double, in Kelvin units
   * @return temp as double, in Celsius units
   */
  private double convertFromKelvin(double temp) {
    final double KELVIN = 273.15;
    temp -= KELVIN;
    return temp;
  }


  /**
   * @return a string date in a readable format
   */
  private String getDate() {
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    Date date = new Date();
    return formatter.format(date);
  }




  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle());
  }
}






