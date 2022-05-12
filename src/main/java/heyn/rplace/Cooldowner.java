package heyn.rplace;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Cooldowner implements Runnable {
    public void run() {
        for (Map.Entry<UUID, Integer > i : Rplace.cooldowns.entrySet()) {
            if (i.getValue() <= 1) {
                Rplace.cooldowns.remove(i.getKey());
                Player p = Bukkit.getPlayer(i.getKey());
                if (p == null) {continue;}
                Objects.requireNonNull(Bukkit.getPlayer(i.getKey())).spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
                continue;
            }
            Rplace.cooldowns.replace(i.getKey(), i.getValue()-1);
            Player p = Bukkit.getPlayer(i.getKey());
            if (p == null) {continue;}
            int seconds = (int) Math.ceil((i.getValue()-1)/20);
            TextComponent tc = new TextComponent("You need to wait "+seconds+" seconds to place another pixel.");
            Objects.requireNonNull(Bukkit.getPlayer(i.getKey())).spigot().sendMessage(ChatMessageType.ACTION_BAR, tc);
        }
    }
}
