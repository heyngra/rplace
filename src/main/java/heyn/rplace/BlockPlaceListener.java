package heyn.rplace;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class BlockPlaceListener implements Listener {
    private final Inventory inv;
    private HashMap<Player, Block> placer = new HashMap<>();
    public BlockPlaceListener() {
        this.inv = Bukkit.createInventory(null, 18, Component.text("Choose your color!"));
        initializeItems();
    }
    public void initializeItems() {
        inv.addItem(createGuiItem(Material.WHITE_CONCRETE, Component.text("§fWhite"), Component.text("Choose your color: §fWhite")));
        inv.addItem(createGuiItem(Material.ORANGE_CONCRETE, Component.text("§fOrange"), Component.text("Choose your color: §fOrange")));
        inv.addItem(createGuiItem(Material.MAGENTA_CONCRETE, Component.text("§Magenta"), Component.text("Choose your color: §fMagenta")));
        inv.addItem(createGuiItem(Material.LIGHT_BLUE_CONCRETE, Component.text("§fLight Blue"), Component.text("Choose your color: §fLight Blue")));
        inv.addItem(createGuiItem(Material.YELLOW_CONCRETE, Component.text("§fYellow"), Component.text("Choose your color: §fYellow")));
        inv.addItem(createGuiItem(Material.LIME_CONCRETE, Component.text("§fLime"), Component.text("Choose your color: §fLime")));
        inv.addItem(createGuiItem(Material.PINK_CONCRETE, Component.text("§fPink"), Component.text("Choose your color: §fPink")));
        inv.addItem(createGuiItem(Material.GRAY_CONCRETE, Component.text("§fGray"), Component.text("Choose your color: §fGray")));
        inv.addItem(createGuiItem(Material.LIGHT_GRAY_CONCRETE, Component.text("§fLight Gray"), Component.text("Choose your color: §fLight Gray")));
        inv.addItem(createGuiItem(Material.CYAN_CONCRETE, Component.text("§fCyan"), Component.text("Choose your color: §fCyan")));
        inv.addItem(createGuiItem(Material.PURPLE_CONCRETE, Component.text("§fPurple"), Component.text("Choose your color: §fPurple")));
        inv.addItem(createGuiItem(Material.BLUE_CONCRETE, Component.text("§fBlue"), Component.text("Choose your color: §fBlue")));
        inv.addItem(createGuiItem(Material.BROWN_CONCRETE, Component.text("§fBrown"), Component.text("Choose your color: §fBrown")));
        inv.addItem(createGuiItem(Material.GREEN_CONCRETE, Component.text("§fGreen"), Component.text("Choose your color: §fGreen")));
        inv.addItem(createGuiItem(Material.RED_CONCRETE, Component.text("§fRed"), Component.text("Choose your color: §fRed")));
        inv.addItem(createGuiItem(Material.BLACK_CONCRETE, Component.text("§fBlack"), Component.text("Choose your color: §fBlack")));
    }
    protected ItemStack createGuiItem(final Material material, final net.kyori.adventure.text.Component name, final net.kyori.adventure.text.Component lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        // Set the name of the item
        meta.displayName(name);

        // Set the lore of the item
        meta.lore(List.of(lore));

        item.setItemMeta(meta);

        return item;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Block b1 = e.getPlayer().getWorld().getBlockAt(e.getBlock().getLocation().getBlockX(), e.getBlock().getLocation().getBlockY()-1, e.getBlock().getLocation().getBlockZ());
        if (e.getBlock().getType() == Material.STONE) {
            e.setCancelled(true);
            if (Rplace.cooldowns.containsKey(e.getPlayer().getUniqueId())) {
                return;
            }
            e.getPlayer().openInventory(inv);
            if (placer.containsKey(e.getPlayer())) {
                placer.remove(e.getPlayer());
            }
            placer.put(e.getPlayer(), b1);
        }
    }
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!e.getInventory().equals(inv)) return;
        e.setCancelled(true);
        final ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;
        final Player p = (Player) e.getWhoClicked();
        HashMap<Material, Integer> reversedHashMap = new HashMap<>();

        for (Integer i : concrete.colors.keySet()) {
            reversedHashMap.put(concrete.colors.get(i), i);
        }
        String resp = updateCanvas.post_request("https://place.heyn.live/api/add", "color="+reversedHashMap.get(clickedItem.getType())+"&pos_x="+placer.get(((Player) e.getWhoClicked()).getPlayer()).getX()+"&pos_z="+placer.get(((Player) e.getWhoClicked()).getPlayer()).getZ()+"&auth="+Rplace.authTokens.get(Objects.requireNonNull(((Player) e.getWhoClicked()).getPlayer()).getUniqueId()));
        try {
            JSONObject jsresp = (JSONObject) Rplace.parser.parse(resp);
            String status = (String) jsresp.get("status");
            if (Objects.equals(status, "success")) {
                Rplace.cooldowns.putIfAbsent(e.getWhoClicked().getUniqueId(), 1200);
            } else if (Objects.equals(status, "On Cooldown!")) {
                Rplace.cooldowns.putIfAbsent(e.getWhoClicked().getUniqueId(), Math.toIntExact((Long) jsresp.getOrDefault("time_remaining", 1200)));
            }
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        updateCanvas.forcerun();
        inv.close();
    }
}
