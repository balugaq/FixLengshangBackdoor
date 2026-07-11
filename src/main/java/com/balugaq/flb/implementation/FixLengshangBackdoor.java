package com.balugaq.flb.implementation;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.OptionalMap;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class FixLengshangBackdoor extends JavaPlugin implements Listener {
    @Getter
    private static FixLengshangBackdoor instance;
    @Getter
    private final @NotNull String username;
    @Getter
    private final @NotNull String repo;
    @Getter
    private final @NotNull String branch;
    private ConfigManager configManager;

    private static final List<String> ids = List.of(
            "木棍",
            "书",
            "WHITE_CLOTH",
            "ORANGE_CLOTH",
            "YELLOW_CLOTH",
            "RED_CLOTH",
            "PINK_CLOTH",
            "BLUE_CLOTH",
            "骨头"
    );

    public FixLengshangBackdoor() {
        this.username = "balugaq";
        this.repo = "FixLengshangBackdoor";
        this.branch = "master";
    }

    public static ConfigManager getConfigManager() {
        return getInstance().configManager;
    }

    /**
     * Initializes the plugin and sets up all necessary components.
     */
    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("正在加载配置文件...");
        saveDefaultConfig();
        this.configManager = new ConfigManager(this);

        // delay for lateinit items
        Bukkit.getScheduler().runTaskLater(FixLengshangBackdoor.getInstance(), this::banItems, 10L);
        Bukkit.getPluginManager().registerEvents(this, this);

        getLogger().info("成功启用 " + getName());
    }

    public void banItems() {
        for (String id : ids) {
            SlimefunItem sf = SlimefunItem.getById(id);
            if (sf != null) {
                getLogger().info("检测到 " + sf + " 正在禁用物品");

                // clear item handlers
                ReflectionUtil.setValue(sf, "itemHandlers", new OptionalMap<>(HashMap::new));

                // disable item
                sf.disable();
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void detectJoin(PlayerJoinEvent event) {
        ItemStack[] contents = event.getPlayer().getInventory().getContents();
        for (ItemStack itemStack : contents) {
            SlimefunItem sf = SlimefunItem.getByItem(itemStack);
            if (sf != null) {
                if (ids.contains(sf.getId())) {
                    int amount = itemStack.getAmount();
                    itemStack.setAmount(0);
                    itemStack.setType(Material.AIR);
                    getLogger().info("检测到玩家 " + event.getPlayer().getName() + " 含有 " + amount + " 个 " + sf + " ，已自动检测并清除物品。");
                }
            }
        }
    }

    @Override
    public void onDisable() {
        this.configManager = null;

        getLogger().info("成功禁用 " + getName());

        // Clear instance
        instance = null;
    }

    /**
     * Returns the bug tracker URL for the plugin.
     *
     * @return the bug tracker URL
     */
    @NotNull
    public String getBugTrackerURL() {
        return MessageFormat.format("https://github.com/{0}/{1}/issues/", this.username, this.repo);
    }
}
