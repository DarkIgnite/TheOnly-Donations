# TheOnly-Donations (AvatarDonation) - Forked Edition 🚀

> **Forked from:** [mdaffa48/TheOnly-Donations](https://github.com/mdaffa48/TheOnly-Donations)  
> **Version:** `4.0.4-FORKED-Update-5`  
> **Platform:** Spigot / Paper / Purpur (1.16 - 1.21+)  
> **Java:** 21  

An enhanced Minecraft server donation and store notification plugin. Displays donor avatars in-game via chat, plays celebration effects, and sends rich Discord Webhook embeds upon purchase.

---

## ✨ What's New in this Fork?

This fork introduces several requested modern features, visual improvements, and Discord integrations:

### 1. 🔔 Discord Webhook Integration
- Automatically sends a customized Discord embed notification whenever a purchase/donation is performed via `/donations send <player> <product>`.
- Displays donor head/skin avatar thumbnail rendered using `mc-heads.net`, `minotar.net`, or `SkinsRestorer`.
- Embed includes donor name, package/rank name, formatted price (`Rp 150.000`), server information, customizable footer, and timestamp.
- Asynchronous dispatching ensures zero server lag or tick delays.

### 2. 🎨 Dynamic Webhook Colors per Product Tier
- Support for `webhookColor` per product in `product.yml` (e.g. Hex `#FFD700`, `#B300FF`, `#00FF88`, `#00D2FF`, `#E0E0E0`).
- Embed color dynamically changes depending on the rank tier purchased.

### 3. 💰 Thousand Separator Price Formatting
- Added automatic price formatting with dot thousand separators (e.g. `Rp 150.000` instead of `Rp 150000.0`).
- Available as `{product_price}` placeholder across chat messages, title bars, and Discord Webhooks.

### 4. 🎆 In-Game Fireworks Celebration Effect
- Automatically launches multiple colored fireworks above the buyer's head upon transaction completion.
- Configurable firework amount, delay, shapes (`BALL`, `BURST`, `STAR`), and hex color palettes in `config.yml`.

### 5. 💬 Dynamic Screen Titles & Subtitles
- Supports `{player}`, `{product_name}`, `{product_displayname}`, and `{product_price}` placeholders in title and subtitle announcements.

### 6. ⚙️ Rank-to-Rank Upgrade System
- Pre-configured product upgrade architecture with price differences and deducted rewards (e.g., `SemiToSemi+`, `SemiToMember+`, `LuxuryToLuxury+`).

---

## 📦 Installation & Compilation

### Requirements
- **Java 21** or higher
- **Maven 3.8+**
- **Spigot / Paper 1.16 - 1.21+**

### Compile from Source:
```bash
git clone https://github.com/DarkIgnite/TheOnly-Donations.git
cd TheOnly-Donations
mvn clean package
```
The compiled jar will be generated inside the `target/` directory:
`target/TheOnly-Donations-4.0.4-FORKED.jar`

---

## 🔧 Commands & Permissions

| Command | Permission | Description |
|---|---|---|
| `/donations help` | `donations.admin` | Display help message |
| `/donations reload` | `donations.admin` | Reload configuration files |
| `/donations reset` | `donations.admin` | Reset current donation goal progress |
| `/donations send <player> <product>` | `donations.admin` | Send a package/rank to a player |

---

## 📜 Configuration Example

### `config.yml` (Discord Webhook & Fireworks)
```yaml
discordWebhook:
  enabled: true
  webhookUrl: "https://discord.com/api/webhooks/..."
  username: "LeftyCraft • Store"
  avatarUrl: "https://mc-heads.net/avatar/MHF_Chest/100"
  embed:
    enabled: true
    title: "✨ NOTIFIKASI PEMBELIAN STORE ✨"
    description: "Pemain **`{player}`** baru saja menyelesaikan pembelian di store!"
    color: "#FFD700"
    thumbnailUrl: "https://mc-heads.net/avatar/{player}/100"
    fields:
      - name: "👤 Pembeli"
        value: "> ```{player}```"
        inline: true
      - name: "📦 Paket / Rank"
        value: "> ```{product_displayname}```"
        inline: true
      - name: "💵 Total Pembayaran"
        value: "> ```Rp {product_price}```"
        inline: true

events:
  fireworks:
    enabled: true
    amount: 4
    delay: 6
    colors:
      - "#FFAA00"
      - "#FFD700"
      - "#00FF88"
      - "#00D2FF"
```

### `product.yml` (Dynamic Colors & Upgrades)
```yaml
products:
  Semi:
    displayName: "&fSemi Rank"
    price: 10000
    webhookColor: "#E0E0E0"
    command:
      - "lp user {player} parent set semi"
      - "eco give {player} 100000"

  SemiToSemi+:
    displayName: "&fSemi &7➔ &bSemi+ &e(Upgrade)"
    price: 15000
    webhookColor: "#00D2FF"
    command:
      - "lp user {player} parent set semi+"
      - "eco give {player} 100000"
```

---

## 👥 Credits
- **Original Author:** [aglerr / mdaffa48](https://github.com/mdaffa48)
- **Contributors:** starfruit2210, [DarkIgnite](https://github.com/DarkIgnite)