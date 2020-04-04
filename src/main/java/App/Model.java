package App;

/*
 * Apache 2.0 license
 *
 * Subject to the terms and conditions of this License, each Contributor hereby
 * grants to You a perpetual, worldwide, non-exclusive, no-charge, royalty-free,
 * irrevocable copyright license to reproduce, prepare Derivative Works of,
 * publicly display, publicly perform, sublicense, and distribute the Work and such
 * Derivative Works in Source or Object form.
 */

import org.apache.commons.io.*;
import org.json.*;

import javax.imageio.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;

enum Heading {
    N1 (360f-22.5f, 0),
    N2 (0f, 22.5f),
    NE(22.5f, 67.5f),
    E(67.5f, 67+45f),
    SE(22.5f+90f, 67.5f+90f),
    S(67.5f+90f, 67+45f+90f),
    SW (22.5f+180f, 67.5f+180f),
    W(67.5f+180f, 67+45f+180f),
    NW(22.5f+270f, 67.5f+270f);

    public final float lower;
    public final float upper;

    private Heading(float lower, float upper) {
        this.lower = lower;
        this.upper = upper;
    }
}

enum TimeZone{
     Europe_London("Europe/London"),
     Europe_Rome("Europe/Rome"),
     EST("EST"),
     Australia_Sydney("Australia/Sydney");

     public final String ZoneId;

     private TimeZone(String ZoneId) {
         this.ZoneId = ZoneId;
     }

}
public class Model {


    // Weather vars
    final private String APPID = "b2a8bc257fa01634206954930e5a6301"; // provided by OpenWeatherMap

    String weatherURL = "http://api.openweathermap.org/data/2.5/weather?q={LOCATION}&APPID={APPID}&units=metric".replace("{APPID}", APPID);

    long freq = 10*60*1000L;
    int delta = 1000; // update interval [ms]

    JSONObject json_weather;
    JSONObject tempObj;
    JSONObject windObj;
    JSONObject mainObj;
    JSONArray  weatherdetails;
    JSONObject weatherobj;
    String     weathericoncode;
    String     location;

    public void setLocation(String location) {
        this.location = location;
    }

    public JSONObject getJson_weather() throws IOException {
        json_weather = new JSONObject(IOUtils.toString(new URL(getWeatherURL(location)), Charset.forName("UTF-8")));
        return json_weather;
    }

    public JSONArray getWeatherdetails() {
        weatherdetails = (JSONArray) json_weather.get("weather");
        return  weatherdetails;
    }

    public JSONObject getWeatherobj() {
        weatherobj = weatherdetails.getJSONObject(0);
        return weatherobj;
    }

    public String getWeathericoncode() {
        weathericoncode = weatherobj.get("icon").toString();
        return weathericoncode;
    }

    public JSONObject getMainObj() {
        mainObj =(JSONObject)json_weather.get("main");
        return mainObj;
    }

    public JSONObject getWindObj() {
        windObj =(JSONObject)json_weather.get("wind");
        return windObj;
    }

    public int getDelta(String formatString) {
        int delta=0;

        switch(formatString) {
            case "dd MMM YYYY":
                delta = 1000;
                break;
            case "HH mm a":
                delta = 1000;
                break;
            case "E dd MMM":
                delta = 1000;
                break;
            case "HH mm ss SSS":
                delta = 10;
                break;
            default:
                delta=1000;
        }
        return delta;
    }

    public void setDelta(int delta) {
        this.delta = delta;
    }

    public String getWeatherURL(String location) {
        return weatherURL.replace("{LOCATION}", location);
    }

    public Image getWeatherImage() throws IOException {
        return ImageIO.read(new URL("http://openweathermap.org/img/wn/"+weathericoncode+"@2x.png"));
    }

    public String getWeatherDescription() {
        return weatherobj.get("description").toString();
    }

    public String getTemp() {
        return mainObj.get("temp").toString();
    }

    public String getFeels_like() {
        return mainObj.get("feels_like").toString();
    }

    public String getTemp_min() {
        return mainObj.get("temp_min").toString();
    }

    public String getTemp_max() {
        return  mainObj.get("temp_max").toString();
    }

    public long getFreq() {
        return freq;
    }

    String getCompassHeading(String wind_direction) {

        String compassHeading = "";
        for (Heading heading: Heading.values()) {
            if (Integer.parseInt(wind_direction) >= heading.lower && Integer.parseInt(wind_direction) < heading.upper) {
                compassHeading = heading.name();
            }
        }
        if(compassHeading.equals("N1") || compassHeading.equals("N2")) compassHeading = "N";

        return compassHeading;
    }
}

