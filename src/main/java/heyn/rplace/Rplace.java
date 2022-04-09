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
    public BukkitScheduler scheduler;
    public BukkitTask updater;
    public BukkitTask cooldowner;
    public static HttpClient client;
    public static JSONParser parser = new JSONParser();
    public static final UUID serverUUID;
    public static HashMap<UUID, String> authTokens = new HashMap<>();
    public static HashMap<UUID, Integer> cooldowns = new HashMap<>();
    static {
        UUID serverUUID1;
        try {
            serverUUID1 = UUID.fromString((String) (((JSONObject) parser.parse(updateCanvas.get_request("http://localhost:8000/api/serverid"))).get("uuid")));
        } catch (ParseException e) {
            serverUUID1 = null;
            e.printStackTrace();
        }
        serverUUID = serverUUID1;
    }

    @Override
    public void onEnable() {
        plugin = this;
        client = HttpClient.newHttpClient();
        this.scheduler = plugin.getServer().getScheduler();
        updater = scheduler.runTaskTimer(plugin, new updateCanvas(), 0L, 30L);
        cooldowner = scheduler.runTaskTimer(plugin, new Cooldowner(), 0L, 1L);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
