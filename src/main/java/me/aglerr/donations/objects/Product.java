package me.aglerr.donations.objects;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Product {

    private final String name;
    private final String displayName;
    private final double price;
    private final List<String> command;
    private final String webhookColor;

    public Product(String name, String displayName, double price, List<String> command, @Nullable String webhookColor) {
        this.name = name;
        this.displayName = displayName;
        this.price = price;
        this.command = command;
        this.webhookColor = webhookColor;
    }

    public Product(String name, String displayName, double price, List<String> command) {
        this(name, displayName, price, command, null);
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getPrice() {
        return price;
    }

    public List<String> getCommand() {
        return command;
    }

    @Nullable
    public String getWebhookColor() {
        return webhookColor;
    }

    public void execute(OfflinePlayer player) {
        for (String command : this.command) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                    .replace("{player}", player.getName()));
        }
    }
}
