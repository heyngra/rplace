package heyn.rplace;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static heyn.rplace.Rplace.parser;

public class updateCanvas implements Runnable{

    public static String get_request(String url1) {
        try {
            URL url = new URL(url1);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
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
                return content.toString();
            }
            con.disconnect();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return url1;
    }
    public static String post_request(String url1, String jsonInputString) {
        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL(url1);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            //Bukkit.getLogger().info(jsonInputString);
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            int length = input.length;
            con.setFixedLengthStreamingMode(length);
            con.connect();
            try (OutputStream os = con.getOutputStream()) {
                os.write(input, 0, input.length);
            }
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
            con.disconnect();
        } catch(IOException e) {
            e.printStackTrace();
        }
        return (response.toString());
    }
    public void run() {
        JSONObject js = null;
        try {
            if (Rplace.latest_history == -1) {
                js = (JSONObject) parser.parse(updateCanvas.get_request("https://place.heyn.live/api/pixels"));
                //System.out.println(js.get("newest_history").toString());
                Rplace.latest_history = Math.toIntExact((Long) js.get("newest_history"));
            } else {
                try {
                    js = (JSONObject) parser.parse(updateCanvas.get_request("https://place.heyn.live/api/after?hist=" + Rplace.latest_history));
                } catch (Exception e) {
                    e.printStackTrace();
                    Bukkit.getLogger().warning("https://place.heyn.live/api/after?hist=" + Rplace.latest_history + "; Consider checking: https://github.com/heyngra/rplace/issues/1");
                }
                Rplace.latest_history = Math.toIntExact((Long) js.get("newest_history"));
                if (((String) js.getOrDefault("status", "")).equals("Reload")) {
                    Rplace.latest_history = -1;
                    run();
                }
        }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (js != null) {
            JSONObject finalJs = js;
            Rplace.scheduler.runTask(Rplace.plugin, () -> updateCanvas.replace((JSONArray) finalJs.get("pixels")));
        }
    }
    private static Runnable replace(JSONArray content) {
        World w = Bukkit.getServer().getWorlds().get(0);
        content.forEach(item -> {
            JSONObject pixel = (JSONObject) item;
            long x = (long) pixel.get("pos_x");
            long z = (long) pixel.get("pos_z");
            int color = Math.toIntExact((long) pixel.get("color"));
            w.getBlockAt(Math.toIntExact(x), 69, Math.toIntExact(z)).setType(concrete.colors.get(color));
        });
        return null;
    }
    public static void forcerun() {
        Rplace.scheduler.runTaskAsynchronously(Rplace.plugin, () -> {
            JSONObject js = null;
            try {
                js = (JSONObject) parser.parse(updateCanvas.get_request("https://place.heyn.live/api/after?hist="+Rplace.latest_history+"Consider checking: https://github.com/heyngra/rplace/issues/1"));
                Rplace.latest_history = Math.toIntExact((Long) js.get("newest_history"));
                if (((String) js.getOrDefault("status", "")).equals("Reload")) {
                    Rplace.latest_history = -1;
                    updateCanvas.forcerun();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            assert js != null;
            JSONObject finalJs = js;
            Rplace.scheduler.runTask(Rplace.plugin, () -> updateCanvas.replace((JSONArray) finalJs.get("pixels")));
        });
    }
}
