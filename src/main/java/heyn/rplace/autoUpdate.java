package heyn.rplace;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class autoUpdate {
    public static void autoUpdate(){
        try {
            String version = Rplace.version;
            String parseVersion = version.replace(".", "");

            String tagname = null;
            URL api = new URL("https://api.github.com/repos/heyngra/rplace/releases/latest");
            URLConnection con = api.openConnection();
            con.setConnectTimeout(15000);
            con.setReadTimeout(15000);

            JsonObject json = new JsonParser().parse(new InputStreamReader(con.getInputStream())).getAsJsonObject();
            tagname = json.get("tag_name").getAsString();

            String parsedTagName = tagname.replace(".", "");

            int latestVersion = Integer.parseInt(parsedTagName.substring(1, parsedTagName.length()));
            URL download = new URL("https://github.com/heyngra/rplace/releases/download/" + tagname + "/rplace.jar");

            if(latestVersion > Integer.parseInt(parseVersion)) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN +"[rplace] Found a new version " +ChatColor.RED+ tagname +ChatColor.LIGHT_PURPLE+ " downloading now!!");

                new BukkitRunnable(){

                    @Override
                    public void run() {
                        try {

                            InputStream in = download.openStream();
                            File temp = new File("plugins/update");
                            if (!temp.exists()) {
                                temp.mkdir();
                            }
                            Path path = new File("plugins/update" + File.separator + "rplace.jar").toPath();
                            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
                            Bukkit.getLogger().warning("You need to start the server again to install newest update.");

                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }.runTaskLaterAsynchronously(Rplace.plugin, 0);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
