package heyn.rplace;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class Cooldowner implements Runnable {
    public void run() {
        for (Map.Entry<UUID, Integer > i : Rplace.cooldowns.entrySet()) {
            if (i.getValue() <= 1) {
                Rplace.cooldowns.remove(i.getKey());
                Player p = Bukkit.getPlayer(i.getKey());
                if (p == null) {continue;}
                Audience a = Audience.audience(p);
                a.sendActionBar(Component.text(""));
                continue;
            }
            Rplace.cooldowns.replace(i.getKey(), i.getValue()-1);
            Player p = Bukkit.getPlayer(i.getKey());
            if (p == null) {continue;}
            int seconds = (int) Math.ceil((i.getValue()-1)/20);
            TextComponent tc = Component.text("You need to wait "+seconds+" seconds to place another pixel.");
            Audience a = Audience.audience(p);
            a.sendActionBar(tc);
        }
    }
}
