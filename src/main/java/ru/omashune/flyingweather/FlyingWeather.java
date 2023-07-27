package ru.omashune.flyingweather;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class FlyingWeather extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (getConfig().getBoolean("action-bar.enabled")) {
            BaseComponent[] badWeatherMessage = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',
                    getConfig().getString("action-bar.message")));

            Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.getWorld().hasStorm()) return;

                    ItemStack chestplate = player.getInventory().getChestplate();
                    if (chestplate == null || chestplate.getType() != Material.ELYTRA) return;

                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, badWeatherMessage);
                }
            }, 0, 20);
        }

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!getConfig().getBoolean("toggle-off-gliding.on-join")) return;

        Player player = event.getPlayer();
        if (isGlidingInStorm(player))
            player.setGliding(false);
    }

    @EventHandler
    public void onGlideToggle(EntityToggleGlideEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player && entity.getWorld().hasStorm() && event.isGliding()))
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (!getConfig().getBoolean("toggle-off-gliding.on-weather-change")) return;

        if (event.toWeatherState())
            for (Player player : Bukkit.getOnlinePlayers())
                if (player.isGliding()) player.setGliding(false);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (!getConfig().getBoolean("toggle-off-gliding.on-world-change")) return;

        Player player = event.getPlayer();
        if (isGlidingInStorm(player))
            player.setGliding(false);
    }

    private boolean isGlidingInStorm(Player player) {
        return player.isGliding() && player.getWorld().hasStorm();
    }

}
