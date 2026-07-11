package com.balugaq.flb.implementation;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.OptionalMap;
import lombok.Getter;
import net.guizhanss.guizhanlibplugin.updater.GuizhanUpdater;
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
            "骨头",
            "LSDGZ",
            "LENGSHANG_LSDGZ",
            "FR1"
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

        if (Bukkit.getPluginManager().isPluginEnabled("GuizhanLibPlugin")) {
            tryUpdate();
        }
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

                Slimefun.getItemCfg().setValue(sf.getId() + ".enabled", false);
                Slimefun.getItemCfg().save();

                getLogger().info("成功禁用该物品！");
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

    /**
     * Attempts to update the plugin if auto-update is enabled.
     */
    private void tryUpdate() {
        try {
            if (configManager.isAutoUpdate() && getDescription().getVersion().startsWith("Build")) {
                GuizhanUpdater.start(this, getFile(), username, repo, branch);
            }
        } catch (NoClassDefFoundError | NullPointerException | UnsupportedClassVersionError e) {
            getLogger().info("自动更新失败: " + e.getMessage());
            Debug.trace(e);
        }
    }
}
