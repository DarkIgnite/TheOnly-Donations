package me.aglerr.donations.managers;

import com.muhammaddaffa.mdlib.utils.Common;
import com.muhammaddaffa.mdlib.utils.Logger;
import me.aglerr.donations.ConfigValue;
import me.aglerr.donations.libs.DiscordWebhook;
import me.aglerr.donations.objects.Product;
import me.aglerr.donations.objects.QueueDonation;
import me.aglerr.donations.utils.Utils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.awt.Color;
import java.time.Instant;
import java.util.Map;

public class WebhookManager {

    public static void sendDonationWebhook(QueueDonation donation) {
        if (!ConfigValue.WEBHOOK_ENABLED) {
            return;
        }

        String webhookUrl = ConfigValue.WEBHOOK_URL;
        if (webhookUrl == null || webhookUrl.trim().isEmpty() || webhookUrl.equalsIgnoreCase("YOUR_DISCORD_WEBHOOK_URL_HERE")) {
            return;
        }

        try {
            OfflinePlayer player = donation.getPlayer();
            Product product = donation.getProduct();

            DiscordWebhook webhook = new DiscordWebhook(webhookUrl);

            if (ConfigValue.WEBHOOK_USERNAME != null && !ConfigValue.WEBHOOK_USERNAME.isEmpty()) {
                webhook.setUsername(parseText(ConfigValue.WEBHOOK_USERNAME, player, product));
            }

            if (ConfigValue.WEBHOOK_AVATAR_URL != null && !ConfigValue.WEBHOOK_AVATAR_URL.isEmpty()) {
                webhook.setAvatarUrl(parseText(ConfigValue.WEBHOOK_AVATAR_URL, player, product));
            }

            if (ConfigValue.WEBHOOK_CONTENT != null && !ConfigValue.WEBHOOK_CONTENT.isEmpty()) {
                webhook.setContent(parseText(ConfigValue.WEBHOOK_CONTENT, player, product));
            }

            if (ConfigValue.WEBHOOK_EMBED_ENABLED) {
                DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();

                if (ConfigValue.WEBHOOK_EMBED_TITLE != null && !ConfigValue.WEBHOOK_EMBED_TITLE.isEmpty()) {
                    embed.setTitle(parseText(ConfigValue.WEBHOOK_EMBED_TITLE, player, product));
                }

                if (ConfigValue.WEBHOOK_EMBED_URL != null && !ConfigValue.WEBHOOK_EMBED_URL.isEmpty()) {
                    embed.setUrl(parseText(ConfigValue.WEBHOOK_EMBED_URL, player, product));
                }

                if (ConfigValue.WEBHOOK_EMBED_DESCRIPTION != null && !ConfigValue.WEBHOOK_EMBED_DESCRIPTION.isEmpty()) {
                    embed.setDescription(parseText(ConfigValue.WEBHOOK_EMBED_DESCRIPTION, player, product));
                }

                int color = parseColor(ConfigValue.WEBHOOK_EMBED_COLOR);
                embed.setColor(color);

                if (ConfigValue.WEBHOOK_EMBED_THUMBNAIL != null && !ConfigValue.WEBHOOK_EMBED_THUMBNAIL.isEmpty()) {
                    embed.setThumbnail(parseText(ConfigValue.WEBHOOK_EMBED_THUMBNAIL, player, product));
                }

                if (ConfigValue.WEBHOOK_EMBED_IMAGE != null && !ConfigValue.WEBHOOK_EMBED_IMAGE.isEmpty()) {
                    embed.setImage(parseText(ConfigValue.WEBHOOK_EMBED_IMAGE, player, product));
                }

                if (ConfigValue.WEBHOOK_EMBED_FOOTER_TEXT != null && !ConfigValue.WEBHOOK_EMBED_FOOTER_TEXT.isEmpty()) {
                    String footerText = parseText(ConfigValue.WEBHOOK_EMBED_FOOTER_TEXT, player, product);
                    String footerIcon = ConfigValue.WEBHOOK_EMBED_FOOTER_ICON != null ?
                            parseText(ConfigValue.WEBHOOK_EMBED_FOOTER_ICON, player, product) : "";
                    embed.setFooter(footerText, footerIcon);
                }

                if (ConfigValue.WEBHOOK_EMBED_TIMESTAMP) {
                    embed.setTimestamp(Instant.now().toString());
                }

                if (ConfigValue.WEBHOOK_EMBED_FIELDS != null) {
                    for (Map<?, ?> fieldMap : ConfigValue.WEBHOOK_EMBED_FIELDS) {
                        String name = fieldMap.containsKey("name") ? String.valueOf(fieldMap.get("name")) : "";
                        String value = fieldMap.containsKey("value") ? String.valueOf(fieldMap.get("value")) : "";
                        boolean inline = !fieldMap.containsKey("inline") || Boolean.parseBoolean(String.valueOf(fieldMap.get("inline")));

                        if (!name.isEmpty() && !value.isEmpty()) {
                            embed.addField(parseText(name, player, product), parseText(value, player, product), inline);
                        }
                    }
                }

                webhook.addEmbed(embed);
            }

            webhook.execute();
        } catch (Exception e) {
            Logger.info("Failed to send Discord webhook: " + e.getMessage());
        }
    }

    public static String parseText(String text, OfflinePlayer player, Product product) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String playerName = player.getName() != null ? player.getName() : "Unknown";
        String playerUuid = player.getUniqueId() != null ? player.getUniqueId().toString() : "";
        String playerAvatar = Utils.getMinepicURL(player);

        String result = text
                .replace("{player}", playerName)
                .replace("{player_uuid}", playerUuid)
                .replace("{player_avatar}", playerAvatar)
                .replace("{product_name}", product.getName())
                .replace("{product_displayname}", ChatColor.stripColor(Common.color(product.getDisplayName())))
                .replace("{product_price}", Utils.formatPrice(product.getPrice()))
                .replace("{product_price_raw}", String.valueOf(product.getPrice()))
                .replace("{goal_progress_bar}", ChatColor.stripColor(Common.color(DonationGoal.getProgressBar())))
                .replace("{goal_percentage}", DonationGoal.getDonationPercentage())
                .replace("{goal_donation_goal}", DonationGoal.getDonationGoal())
                .replace("{goal_current_donation}", DonationGoal.getCurrentDonation());

        if (DependencyManager.PLACEHOLDER_API_ENABLED) {
            result = PlaceholderAPI.setPlaceholders(player, result);
        }

        // Strip any residual Minecraft color codes (§ or &) for Discord text formatting
        return ChatColor.stripColor(Common.color(result));
    }

    private static int parseColor(String colorStr) {
        if (colorStr == null || colorStr.trim().isEmpty()) {
            return 0x5865F2; // Discord Blurple default
        }
        colorStr = colorStr.trim();
        try {
            if (colorStr.startsWith("#")) {
                return Integer.parseInt(colorStr.substring(1), 16);
            }
            if (colorStr.startsWith("0x")) {
                return Integer.parseInt(colorStr.substring(2), 16);
            }
            // Check if purely numeric integer
            if (colorStr.matches("\\d+")) {
                return Integer.parseInt(colorStr);
            }
            // Check hex without #
            if (colorStr.matches("[0-9a-fA-F]{6}")) {
                return Integer.parseInt(colorStr, 16);
            }
            // Color name mapping
            switch (colorStr.toUpperCase()) {
                case "GOLD":
                case "YELLOW":
                    return 0xFFAA00;
                case "GREEN":
                    return 0x55FF55;
                case "DARK_GREEN":
                    return 0x00AA00;
                case "RED":
                    return 0xFF5555;
                case "DARK_RED":
                    return 0xAA0000;
                case "BLUE":
                    return 0x5555FF;
                case "DARK_BLUE":
                    return 0x0000AA;
                case "AQUA":
                    return 0x55FFFF;
                case "DARK_AQUA":
                    return 0x00AAAA;
                case "PURPLE":
                case "DARK_PURPLE":
                    return 0xAA00AA;
                case "LIGHT_PURPLE":
                case "PINK":
                    return 0xFF55FF;
                case "WHITE":
                    return 0xFFFFFF;
                case "GRAY":
                    return 0xAAAAAA;
                case "DARK_GRAY":
                    return 0x555555;
                case "BLACK":
                    return 0x000000;
                default:
                    return 0x5865F2;
            }
        } catch (Exception e) {
            return 0x5865F2;
        }
    }
}
