package me.aglerr.donations.libs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.net.ssl.HttpsURLConnection;
import java.awt.Color;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DiscordWebhook {

    private final String url;
    private String content;
    private String username;
    private String avatarUrl;
    private boolean tts;
    private final List<EmbedObject> embeds = new ArrayList<>();

    public DiscordWebhook(String url) {
        this.url = url;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setTts(boolean tts) {
        this.tts = tts;
    }

    public void addEmbed(EmbedObject embed) {
        this.embeds.add(embed);
    }

    public void execute() throws Exception {
        if (this.url == null || this.url.trim().isEmpty()) {
            return;
        }

        JsonObject json = new JsonObject();
        if (this.content != null && !this.content.isEmpty()) {
            json.addProperty("content", this.content);
        }
        if (this.username != null && !this.username.isEmpty()) {
            json.addProperty("username", this.username);
        }
        if (this.avatarUrl != null && !this.avatarUrl.isEmpty()) {
            json.addProperty("avatar_url", this.avatarUrl);
        }
        json.addProperty("tts", this.tts);

        if (!this.embeds.isEmpty()) {
            JsonArray embedArray = new JsonArray();
            for (EmbedObject embed : this.embeds) {
                embedArray.add(embed.toJson());
            }
            json.add("embeds", embedArray);
        }

        URL urlObj = new URL(this.url);
        HttpURLConnection connection;
        if (this.url.startsWith("https://")) {
            connection = (HttpsURLConnection) urlObj.openConnection();
        } else {
            connection = (HttpURLConnection) urlObj.openConnection();
        }

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("User-Agent", "TheOnlyDonations-Webhook");
        connection.setDoOutput(true);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        byte[] payloadBytes = json.toString().getBytes(StandardCharsets.UTF_8);
        connection.setFixedLengthStreamingMode(payloadBytes.length);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(payloadBytes);
            os.flush();
        }

        int responseCode = connection.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            throw new RuntimeException("Discord Webhook returned response code: " + responseCode + " - " + connection.getResponseMessage());
        }
        connection.disconnect();
    }

    public static class EmbedObject {
        private String title;
        private String description;
        private String url;
        private Integer color;
        private Footer footer;
        private Thumbnail thumbnail;
        private Image image;
        private Author author;
        private String timestamp;
        private final List<Field> fields = new ArrayList<>();

        public EmbedObject setTitle(String title) {
            this.title = title;
            return this;
        }

        public EmbedObject setDescription(String description) {
            this.description = description;
            return this;
        }

        public EmbedObject setUrl(String url) {
            this.url = url;
            return this;
        }

        public EmbedObject setColor(Color color) {
            if (color != null) {
                int rgb = color.getRed();
                rgb = (rgb << 8) + color.getGreen();
                rgb = (rgb << 8) + color.getBlue();
                this.color = rgb;
            }
            return this;
        }

        public EmbedObject setColor(int decimalColor) {
            this.color = decimalColor;
            return this;
        }

        public EmbedObject setFooter(String text, String iconUrl) {
            this.footer = new Footer(text, iconUrl);
            return this;
        }

        public EmbedObject setThumbnail(String url) {
            this.thumbnail = new Thumbnail(url);
            return this;
        }

        public EmbedObject setImage(String url) {
            this.image = new Image(url);
            return this;
        }

        public EmbedObject setAuthor(String name, String url, String iconUrl) {
            this.author = new Author(name, url, iconUrl);
            return this;
        }

        public EmbedObject setTimestamp(String isoTimestamp) {
            this.timestamp = isoTimestamp;
            return this;
        }

        public EmbedObject addField(String name, String value, boolean inline) {
            this.fields.add(new Field(name, value, inline));
            return this;
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            if (title != null && !title.isEmpty()) {
                json.addProperty("title", title);
            }
            if (description != null && !description.isEmpty()) {
                json.addProperty("description", description);
            }
            if (url != null && !url.isEmpty()) {
                json.addProperty("url", url);
            }
            if (color != null) {
                json.addProperty("color", color);
            }
            if (footer != null && footer.getText() != null && !footer.getText().isEmpty()) {
                JsonObject footerJson = new JsonObject();
                footerJson.addProperty("text", footer.getText());
                if (footer.getIconUrl() != null && !footer.getIconUrl().isEmpty()) {
                    footerJson.addProperty("icon_url", footer.getIconUrl());
                }
                json.add("footer", footerJson);
            }
            if (thumbnail != null && thumbnail.getUrl() != null && !thumbnail.getUrl().isEmpty()) {
                JsonObject thumbnailJson = new JsonObject();
                thumbnailJson.addProperty("url", thumbnail.getUrl());
                json.add("thumbnail", thumbnailJson);
            }
            if (image != null && image.getUrl() != null && !image.getUrl().isEmpty()) {
                JsonObject imageJson = new JsonObject();
                imageJson.addProperty("url", image.getUrl());
                json.add("image", imageJson);
            }
            if (author != null && author.getName() != null && !author.getName().isEmpty()) {
                JsonObject authorJson = new JsonObject();
                authorJson.addProperty("name", author.getName());
                if (author.getUrl() != null && !author.getUrl().isEmpty()) {
                    authorJson.addProperty("url", author.getUrl());
                }
                if (author.getIconUrl() != null && !author.getIconUrl().isEmpty()) {
                    authorJson.addProperty("icon_url", author.getIconUrl());
                }
                json.add("author", authorJson);
            }
            if (timestamp != null && !timestamp.isEmpty()) {
                json.addProperty("timestamp", timestamp);
            }
            if (!fields.isEmpty()) {
                JsonArray fieldsArray = new JsonArray();
                for (Field field : fields) {
                    JsonObject fieldJson = new JsonObject();
                    fieldJson.addProperty("name", field.getName());
                    fieldJson.addProperty("value", field.getValue());
                    fieldJson.addProperty("inline", field.isInline());
                    fieldsArray.add(fieldJson);
                }
                json.add("fields", fieldsArray);
            }
            return json;
        }

        private static class Footer {
            private final String text;
            private final String iconUrl;

            public Footer(String text, String iconUrl) {
                this.text = text;
                this.iconUrl = iconUrl;
            }

            public String getText() {
                return text;
            }

            public String getIconUrl() {
                return iconUrl;
            }
        }

        private static class Thumbnail {
            private final String url;

            public Thumbnail(String url) {
                this.url = url;
            }

            public String getUrl() {
                return url;
            }
        }

        private static class Image {
            private final String url;

            public Image(String url) {
                this.url = url;
            }

            public String getUrl() {
                return url;
            }
        }

        private static class Author {
            private final String name;
            private final String url;
            private final String iconUrl;

            public Author(String name, String url, String iconUrl) {
                this.name = name;
                this.url = url;
                this.iconUrl = iconUrl;
            }

            public String getName() {
                return name;
            }

            public String getUrl() {
                return url;
            }

            public String getIconUrl() {
                return iconUrl;
            }
        }

        private static class Field {
            private final String name;
            private final String value;
            private final boolean inline;

            public Field(String name, String value, boolean inline) {
                this.name = name;
                this.value = value;
                this.inline = inline;
            }

            public String getName() {
                return name;
            }

            public String getValue() {
                return value;
            }

            public boolean isInline() {
                return inline;
            }
        }
    }
}
