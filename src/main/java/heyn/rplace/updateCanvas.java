package heyn.rplace;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class updateCanvas implements Runnable{
    public void run() {
        try {
            URL url = new URL("http://localhost:8000/api/pixels");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int status = con.getResponseCode();
            if (status == 200) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                //Bukkit.getLogger().info(content.toString());
                JSONParser parser = new JSONParser();
                JSONObject js = (JSONObject) parser.parse(content.toString());
                replace((JSONArray) js.get("pixels"));
            }
            con.disconnect();
        }
        catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
    private static void replace(JSONArray content) {
        World w = Bukkit.getServer().getWorlds().get(0);
        content.forEach(item -> {
            JSONObject pixel = (JSONObject) item;
            long x = (long) pixel.get("pos_x");
            long z = (long) pixel.get("pos_z");
            int color = Math.toIntExact((long) pixel.get("color"));
            w.getBlockAt(Math.toIntExact(x), 69, Math.toIntExact(z)).setType(concrete.colors.get(color));
        });
    }
    public static void forcerun() {
        try {
            URL url = new URL("http://localhost:8000/api/pixels");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int status = con.getResponseCode();
            if (status == 200) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                //Bukkit.getLogger().info(content.toString());
                JSONParser parser = new JSONParser();
                JSONObject js = (JSONObject) parser.parse(content.toString());
                replace((JSONArray) js.get("pixels"));
            }
            con.disconnect();
        }
        catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
