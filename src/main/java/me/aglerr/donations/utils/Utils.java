package me.aglerr.donations.utils;

import com.google.common.base.Strings;
import com.muhammaddaffa.mdlib.utils.Common;
import com.muhammaddaffa.mdlib.utils.Logger;
import me.aglerr.donations.ConfigValue;
import me.aglerr.donations.DonationPlugin;
import me.aglerr.donations.libs.ImageChar;
import me.aglerr.donations.libs.ImageMessage;
import me.aglerr.donations.libs.ImageMessageHex;
import me.aglerr.donations.managers.DependencyManager;
import me.aglerr.donations.objects.QueueDonation;
import net.md_5.bungee.api.chat.TextComponent;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.property.SkinIdentifier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class Utils {

    public static String[] getStartupLogo() {
        return new String[]{
                "  _______ _             ____        _       ",
                " |__   __| |           / __ \\      | |      ",
                "    | |  | |__   ___  | |  | |_ __ | |_   _ ",
                "    | |  | '_ \\ / _ \\ | |  | | '_ \\| | | | |",
                "    | |  | | | |  __/ | |__| | | | | | |_| |",
                "    |_|  |_| |_|\\___|  \\____/|_| |_|_|\\__, |",
                "                                       __/ |",
                "                                      |___/ ",
                "  ",
                "                Donations edition",
                "            Thanks for using and enjoy!",
                "  "
        };
    }

    public static void sendStartupLogo() {
        for (String message : getStartupLogo()) {
            System.out.println(message);
        }
    }

    public static String getMinepicURL(OfflinePlayer player) {
        String name = player.getName() != null ? player.getName() : "Steve";
        // Handle Bedrock / Floodgate player names (prefixed with . or *)
        String cleanName = name.startsWith(".") || name.startsWith("*") ? name.substring(1) : name;

        if (DependencyManager.SKINS_RESTORER_ENABLED) {
            try {
                SkinsRestorer api = DonationPlugin.getSkinsApi();
                if (api != null && api.getPlayerStorage() != null) {
                    Optional<SkinIdentifier> optional = api.getPlayerStorage().getSkinIdOfPlayer(player.getUniqueId());
                    if (optional.isPresent()) {
                        return "https://mc-heads.net/avatar/" + optional.get().getIdentifier() + "/100";
                    }
                }
            } catch (Throwable ignored) {
                // SkinsRestorer API not ready or proxy mode without database
            }
        }

        if (ConfigValue.USE_UUID && player.getUniqueId() != null) {
            return "https://mc-heads.net/avatar/" + player.getUniqueId() + "/100";
        }
        return "https://mc-heads.net/avatar/" + cleanName + "/100";
    }

    /**
     * Note: this method should be run in async
     */
    public static void broadcastDonation(QueueDonation donation) {
        // If avatar broadcast is disabled, immediately broadcast without avatar
        if (!ConfigValue.BROADCAST_AVATAR_ENABLED) {
            broadcastNoAvatar(donation);
            return;
        }

        BufferedImage image = null;

        try {
            URL url = new URL(getMinepicURL(donation.getPlayer()));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            try (InputStream in = conn.getInputStream()) {
                image = ImageIO.read(in);
            }
        } catch (Throwable e) {
            try {
                URL fallbackUrl = new URL("https://minotar.net/helm/Steve/100.png");
                HttpURLConnection conn = (HttpURLConnection) fallbackUrl.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                try (InputStream in = conn.getInputStream()) {
                    image = ImageIO.read(in);
                }
            } catch (Throwable ignored) {}
        }

        // If downloading image failed completely (e.g. offline server, no internet), fallback to no-avatar broadcast
        if (image == null) {
            broadcastNoAvatar(donation);
            return;
        }

        try {
            if (DonationPlugin.HEX_AVAILABLE) {
                ImageMessageHex imageMessageHex = new ImageMessageHex(image, 8, ImageChar.BLOCK.getChar())
                        .appendText(ConfigValue.donationAvatar(donation));
                imageMessageHex.sendToPlayers();
            } else {
                ImageMessage imageMessage = new ImageMessage(image, 8, ImageChar.BLOCK.getChar())
                        .appendText(ConfigValue.donationAvatar(donation));
                imageMessage.sendToPlayers();
            }
        } catch (Throwable t) {
            // Fallback on any rendering error
            broadcastNoAvatar(donation);
        }
    }

    public static void broadcastNoAvatar(QueueDonation donation) {
        try {
            List<String> messages = ConfigValue.donationNoAvatar(donation);
            for (String message : messages) {
                if (message == null || message.isEmpty()) {
                    Bukkit.broadcastMessage("");
                    continue;
                }
                Bukkit.broadcastMessage(Common.color(message));
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static String getProgressBar(int current, int max, int totalBars, char symbol, String completedColor, String notCompletedColor){
        float percent = (float) current/max;
        int progressBars = (int) (totalBars * percent);

        return Strings.repeat(Common.color(completedColor) + symbol, progressBars) +
                Strings.repeat(Common.color(notCompletedColor) + symbol, totalBars - progressBars);
    }

    public static String formatPrice(double price) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("id", "ID"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat df;
        if (price == (long) price) {
            df = new DecimalFormat("#,###", symbols);
        } else {
            df = new DecimalFormat("#,###.##", symbols);
        }
        return df.format(price);
    }

}
