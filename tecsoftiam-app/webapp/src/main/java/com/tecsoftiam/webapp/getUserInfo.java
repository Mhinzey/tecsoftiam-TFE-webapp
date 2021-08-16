package com.tecsoftiam.webapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Scanner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class getUserInfo {

    public getUserInfo(){

    }
        public void getInfo() throws IOException, ParseException{
          
          URL url = new URL("https://api.github.com/users/mhinzey");
           HttpURLConnection con = (HttpURLConnection) url.openConnection();
          con.setRequestMethod("GET");
        
        int responsecode = con.getResponseCode();
        if (responsecode != 200) {
          throw new RuntimeException("HttpResponseCode: " + responsecode);
      } else {
          String inline = "";
          Scanner scanner = new Scanner(url.openStream());   
         //Write all the JSON data into a string using a scanner
          while (scanner.hasNext()) {
             inline += scanner.nextLine();
          }
          scanner.close();
      
          //Using the JSON simple library parse the string into a json object
          JSONParser parse = new JSONParser();
          JSONObject data_obj;         
            data_obj = (JSONObject) parse.parse(inline);         
          //Get the required object from the above created object
          JSONObject obj = (JSONObject) data_obj.get("Global");
          System.out.println(obj.get("login"));
      }

    }
         public String readUrl(String urlString) throws Exception {
    BufferedReader reader = null;
    try {
        URL url = new URL(urlString);
        reader = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuffer buffer = new StringBuffer();
        int read;
        char[] chars = new char[1024];
        while ((read = reader.read(chars)) != -1)
            buffer.append(chars, 0, read);

        return buffer.toString();
    } finally {
        if (reader != null)
            reader.close();
    }
      }
}
