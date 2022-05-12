package heyn.rplace;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Rplace.scheduler.runTaskAsynchronously(Rplace.plugin, () -> {
            JSONObject resp = null;
            try {
                resp = (JSONObject) Rplace.parser.parse(updateCanvas.post_request("https://place.heyn.live/api/addp", "uuid="+e.getPlayer().getUniqueId()+"&serverUUID="+Rplace.serverUUID));
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
            if (resp != null) {
                Rplace.authTokens.putIfAbsent(e.getPlayer().getUniqueId(), URLEncoder.encode((String) resp.get("auth"), StandardCharsets.UTF_8));
            }
            try {
                resp = (JSONObject) Rplace.parser.parse(updateCanvas.get_request("https://place.heyn.live/api/checkcd?auth="+Rplace.authTokens.getOrDefault(e.getPlayer().getUniqueId(), null)));
                if (((String) resp.get("status")).equals("On Cooldown!")) {
                    Rplace.cooldowns.remove(e.getPlayer().getUniqueId());
                    Rplace.cooldowns.putIfAbsent(e.getPlayer().getUniqueId(), Math.toIntExact((Long) resp.get("time_remaining")));
                }
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        });
    }
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
            Rplace.scheduler.runTaskAsynchronously(Rplace.plugin, () -> {
            updateCanvas.post_request("https://place.heyn.live/api/removep", "auth="+Rplace.authTokens.getOrDefault(e.getPlayer().getUniqueId(), null));
            Rplace.authTokens.remove(e.getPlayer().getUniqueId());
            Rplace.cooldowns.remove(e.getPlayer().getUniqueId());
            }
        );
    }
}
