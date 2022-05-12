package heyn.rplace;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
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
        this.inv = Bukkit.createInventory(null, 18, "Choose your color!");
        initializeItems();
    }
    public void initializeItems() {
        inv.addItem(createGuiItem(Material.WHITE_CONCRETE, new TextComponent("§fWhite"), new TextComponent("Choose your color: §fWhite")));
        inv.addItem(createGuiItem(Material.ORANGE_CONCRETE, new TextComponent("§fOrange"), new TextComponent("Choose your color: §fOrange")));
        inv.addItem(createGuiItem(Material.MAGENTA_CONCRETE, new TextComponent("§fMagenta"), new TextComponent("Choose your color: §fMagenta")));
        inv.addItem(createGuiItem(Material.LIGHT_BLUE_CONCRETE, new TextComponent("§fLight Blue"), new TextComponent("Choose your color: §fLight Blue")));
        inv.addItem(createGuiItem(Material.YELLOW_CONCRETE, new TextComponent("§fYellow"), new TextComponent("Choose your color: §fYellow")));
        inv.addItem(createGuiItem(Material.LIME_CONCRETE, new TextComponent("§fLime"), new TextComponent("Choose your color: §fLime")));
        inv.addItem(createGuiItem(Material.PINK_CONCRETE, new TextComponent("§fPink"), new TextComponent("Choose your color: §fPink")));
        inv.addItem(createGuiItem(Material.GRAY_CONCRETE, new TextComponent("§fGray"), new TextComponent("Choose your color: §fGray")));
        inv.addItem(createGuiItem(Material.LIGHT_GRAY_CONCRETE, new TextComponent("§fLight Gray"), new TextComponent("Choose your color: §fLight Gray")));
        inv.addItem(createGuiItem(Material.CYAN_CONCRETE, new TextComponent("§fCyan"), new TextComponent("Choose your color: §fCyan")));
        inv.addItem(createGuiItem(Material.PURPLE_CONCRETE, new TextComponent("§fPurple"), new TextComponent("Choose your color: §fPurple")));
        inv.addItem(createGuiItem(Material.BLUE_CONCRETE, new TextComponent("§fBlue"), new TextComponent("Choose your color: §fBlue")));
        inv.addItem(createGuiItem(Material.BROWN_CONCRETE, new TextComponent("§fBrown"), new TextComponent("Choose your color: §fBrown")));
        inv.addItem(createGuiItem(Material.GREEN_CONCRETE, new TextComponent("§fGreen"), new TextComponent("Choose your color: §fGreen")));
        inv.addItem(createGuiItem(Material.RED_CONCRETE, new TextComponent("§fRed"), new TextComponent("Choose your color: §fRed")));
        inv.addItem(createGuiItem(Material.BLACK_CONCRETE, new TextComponent("§fBlack"), new TextComponent("Choose your color: §fBlack")));
    }
    protected ItemStack createGuiItem(final Material material, final TextComponent name, final TextComponent lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        // Set the name of the item
        meta.setDisplayName(name.getText());

        // Set the lore of the item
        meta.setLore(List.of(lore.getText()));

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
        Rplace.scheduler.runTaskAsynchronously(Rplace.plugin, () -> {
            String resp = updateCanvas.post_request("https://place.heyn.live/api/add", "color="+reversedHashMap.get(clickedItem.getType())+"&pos_x="+placer.get(((Player) e.getWhoClicked()).getPlayer()).getX()+"&pos_z="+placer.get(((Player) e.getWhoClicked()).getPlayer()).getZ()+"&auth="+Rplace.authTokens.get(Objects.requireNonNull(((Player) e.getWhoClicked()).getPlayer()).getUniqueId()));
            try {
                JSONObject jsresp = (JSONObject) Rplace.parser.parse(resp);
                String status = (String) jsresp.get("status");
                if (Objects.equals(status, "success")) {
                    Rplace.cooldowns.putIfAbsent(e.getWhoClicked().getUniqueId(), Rplace.cooldown*20);
                } else if (Objects.equals(status, "On Cooldown!")) {
                    Rplace.cooldowns.putIfAbsent(e.getWhoClicked().getUniqueId(), Math.toIntExact((Long) jsresp.getOrDefault("time_remaining", 1200)));
                }
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
            Rplace.scheduler.runTask(Rplace.plugin, updateCanvas::forcerun);
        });

        ((Player) e.getWhoClicked()).getPlayer().closeInventory();
    }
    @EventHandler
    public void preventBreakingCanvas(BlockBreakEvent e) {
        if (e.getBlock().getType().toString().contains("CONCRETE")) {
            e.setCancelled(true);
        }
    }
}
