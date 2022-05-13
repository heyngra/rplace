package heyn.rplace;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.units.qual.C;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.UUID;

public final class Rplace extends JavaPlugin {

    public static Rplace plugin;
    public static String version = "1.0.0";
    public static BukkitScheduler scheduler;
    public BukkitTask updater;
    public static int latest_history = -1;
    public BukkitTask cooldowner;
    public static HttpClient client;
    public static JSONParser parser = new JSONParser();
    public static UUID serverUUID;
    public static HashMap<UUID, String> authTokens = new HashMap<>();
    public static HashMap<UUID, Integer> cooldowns = new HashMap<>();
    public static int cooldown;


    @Override
    public void onEnable() {
        plugin = this;
        autoUpdate.autoUpdate();
        client = HttpClient.newHttpClient();
        scheduler = plugin.getServer().getScheduler();
        updater = scheduler.runTaskTimerAsynchronously(plugin, new updateCanvas(), 0L, 30L);
        cooldowner = scheduler.runTaskTimer(plugin, new Cooldowner(), 0L, 1L);
        scheduler.runTaskAsynchronously(Rplace.plugin, () -> {
            UUID serverUUID1;
            int cooldown1;
            try {
                serverUUID1 = UUID.fromString((String) (((JSONObject) parser.parse(updateCanvas.get_request("https://place.heyn.live/api/serverid"))).get("uuid")));
                cooldown1 = Math.toIntExact((Long) ((JSONObject) parser.parse(updateCanvas.get_request("https://place.heyn.live/api/getcooldown"))).getOrDefault("cooldown", 60));
            } catch (ParseException e) {
                serverUUID1 = null;
                cooldown1 = 60;
                e.printStackTrace();
            }
            serverUUID = serverUUID1;
            cooldown = cooldown1;
        });
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
