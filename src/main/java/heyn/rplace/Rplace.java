package heyn.rplace;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.net.http.HttpClient;

public final class Rplace extends JavaPlugin {

    public static Rplace plugin;
    private BukkitScheduler scheduler;
    public BukkitTask updater;
    public static HttpClient client;
    @Override
    public void onEnable() {
        plugin = this;
        client = HttpClient.newHttpClient();
        this.scheduler = plugin.getServer().getScheduler();
        updater = scheduler.runTaskTimer(plugin, new updateCanvas(), 0L, 30L);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
