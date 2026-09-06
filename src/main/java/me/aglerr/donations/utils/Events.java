package me.aglerr.donations.utils;

import com.muhammaddaffa.mdlib.utils.Common;
import com.muhammaddaffa.mdlib.xseries.XSound;
import me.aglerr.donations.DonationPlugin;
import me.aglerr.donations.objects.Product;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Events {

    private static final DonationPlugin plugin = JavaPlugin.getPlugin(DonationPlugin.class);

    public static void playAllEvents(OfflinePlayer player, Product product){
        eventEffects();
        eventSound();
        eventTitleBar(player, product);
        eventCommand(player);
        eventFireworks(player);
    }

    public static void playAllEvents(OfflinePlayer player){
        playAllEvents(player, null);
    }

    public static void eventEffects(){
        FileConfiguration config = DonationPlugin.DEFAULT_CONFIG.getConfig();
        // Return if the effect event is disabled
        if(!config.getBoolean("events.effect.enabled")) {
            return;
        }
        // Get all the effects
        List<String> effects = config.getStringList("events.effect.effects");
        // Loop through all online players
        Bukkit.getOnlinePlayers().forEach(player -> {
            // Loop through all effects
            for(String effectString : effects){
                String[] effectSection = effectString.split(";");
                // Get the effect type
                PotionEffectType effectType = PotionEffectType.getByName(effectSection[0]);
                // Skip if the effect type is doesn't exist
                if(effectType == null) continue;
                // Get the amplifier of the effect
                int amplifier = Integer.parseInt(effectSection[1]) + 1;
                // Get the duration in seconds
                int duration = Integer.parseInt(effectSection[2]) * 20;
                // Create the potion effect
                PotionEffect potionEffect = new PotionEffect(effectType, duration, amplifier);
                // First, remove all potion effect if exist
                player.removePotionEffect(effectType);
                // Add the potion effect to the player
                player.addPotionEffect(potionEffect);
            }
        });
    }

    public static void eventSound(){
        FileConfiguration config = DonationPlugin.DEFAULT_CONFIG.getConfig();
        // Get all the sounds
        List<String> sounds = config.getStringList("events.sounds");
        // Loop through all online players
        Bukkit.getOnlinePlayers().forEach(player -> {
            // Loop through all sounds
            for(String soundString : sounds){
                String[] section = soundString.split(";");
                // Get the sound type
                Optional<XSound> xSound = XSound.matchXSound(section[0]);
                // Skip if the sound type doesn't exist
                if (!xSound.isPresent()) {
                    continue;
                }
                Sound sound = xSound.get().parseSound();
                // If the sound still not exist, skip
                if (sound == null) {
                    continue;
                }
                // Get the volume of the sound
                double volume = Double.parseDouble(section[1]);
                // Get the pitch of the sound
                double pitch = Double.parseDouble(section[2]);
                // Play the sound
                player.playSound(player.getLocation(), sound, (float) volume, (float) pitch);
            }
        });
    }

    public static void eventTitleBar(OfflinePlayer offlinePlayer, Product product){
        FileConfiguration config = DonationPlugin.DEFAULT_CONFIG.getConfig();
        // Return if the title bar event is disabled
        if(!config.getBoolean("events.titleBar.enabled")) return;

        String titleRaw = config.getString("events.titleBar.title", "");
        String subTitleRaw = config.getString("events.titleBar.subTitle", "");

        String playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : "Unknown";
        String productName = product != null ? product.getName() : "";
        String productDisplayName = product != null ? product.getDisplayName() : "";
        String productPrice = product != null ? Utils.formatPrice(product.getPrice()) : "";

        // Get the title
        String title = Common.color(titleRaw
                .replace("{player}", playerName)
                .replace("{product_name}", productName)
                .replace("{product_displayname}", productDisplayName)
                .replace("{product_price}", productPrice));

        // Get the subtitle
        String subTitle = Common.color(subTitleRaw
                .replace("{player}", playerName)
                .replace("{product_name}", productName)
                .replace("{product_displayname}", productDisplayName)
                .replace("{product_price}", productPrice));

        // Get animations duration
        int fadeIn = config.getInt("events.titleBar.fadeIn", 20);
        int stay = config.getInt("events.titleBar.stay", 60);
        int fadeOut = config.getInt("events.titleBar.fadeOut", 20);

        // Loop through all online players
        Bukkit.getOnlinePlayers().forEach(player -> {
            Common.sendTitle(player, title, subTitle, fadeIn, stay, fadeOut);
        });
    }

    public static void eventCommand(OfflinePlayer offlinePlayer){
        FileConfiguration config = DonationPlugin.DEFAULT_CONFIG.getConfig();
        // Return if the command event is disabled
        if(!config.getBoolean("events.command.enabled")) return;
        // Loop through all the commands
        config.getStringList("events.command.commands").forEach(command ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                        .replace("{player}", offlinePlayer.getName() != null ? offlinePlayer.getName() : "")));
    }

    public static void eventFireworks(OfflinePlayer offlinePlayer) {
        FileConfiguration config = DonationPlugin.DEFAULT_CONFIG.getConfig();
        if (!config.getBoolean("events.fireworks.enabled", true)) {
            return;
        }

        Player player = offlinePlayer.getPlayer();
        if (player == null || !player.isOnline()) {
            return;
        }

        int amount = config.getInt("events.fireworks.amount", 3);
        int delay = config.getInt("events.fireworks.delay", 6);
        List<String> colorHexes = config.getStringList("events.fireworks.colors");
        List<String> types = config.getStringList("events.fireworks.types");

        List<Color> colors = new ArrayList<>();
        if (colorHexes.isEmpty()) {
            colors.add(Color.ORANGE);
            colors.add(Color.YELLOW);
            colors.add(Color.AQUA);
            colors.add(Color.FUCHSIA);
            colors.add(Color.LIME);
        } else {
            for (String hex : colorHexes) {
                try {
                    java.awt.Color awtColor = java.awt.Color.decode(hex.trim());
                    colors.add(Color.fromRGB(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue()));
                } catch (Exception ignored) {
                    colors.add(Color.ORANGE);
                }
            }
        }

        Random random = new Random();
        for (int i = 0; i < amount; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;

                Location loc = player.getLocation().clone().add(
                        (random.nextDouble() - 0.5) * 2.0,
                        0.5,
                        (random.nextDouble() - 0.5) * 2.0
                );

                Firework firework = player.getWorld().spawn(loc, Firework.class);
                FireworkMeta meta = firework.getFireworkMeta();

                FireworkEffect.Type type = FireworkEffect.Type.BALL;
                if (!types.isEmpty()) {
                    try {
                        String typeName = types.get(random.nextInt(types.size()));
                        type = FireworkEffect.Type.valueOf(typeName.toUpperCase());
                    } catch (Exception ignored) {}
                }

                Color primary = colors.get(random.nextInt(colors.size()));
                Color secondary = colors.get(random.nextInt(colors.size()));

                FireworkEffect effect = FireworkEffect.builder()
                        .with(type)
                        .withColor(primary)
                        .withFade(secondary, Color.WHITE)
                        .flicker(true)
                        .trail(true)
                        .build();

                meta.addEffect(effect);
                meta.setPower(1);
                firework.setFireworkMeta(meta);
            }, (long) i * delay);
        }
    }

}
