package firstofferztask;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonReader {

  public static void readJson() throws ParseException {
    JSONParser parser = new JSONParser();
    String filePath = "D:\\Coding\\firstofferz\\src\\main\\java\\firstofferztask\\city.json";
    Object obj = parser.parse(filePath);
    JSONObject jsonObject = (JSONObject)obj;

  }
}
