package su.plo.emotes;

import com.google.gson.Gson;
import net.kyori.adventure.text.Component;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import su.plo.emotes.api.Emote;
import su.plo.emotes.api.EmoteManager;
import su.plo.emotes.api.EmotesAPI;
import su.plo.emotes.data.Font;
import su.plo.emotes.data.FontProvider;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class Emotes extends JavaPlugin implements EmotesAPI {
    private final Gson gson = new Gson();
    private final EmoteManager manager = new EmoteManagerImpl();
    private EmoteReplacer replacer;

    @Override
    public void onEnable() {
        // Plugin metrics
        new Metrics(this, 14583);

        saveDefaultConfig();
        registerListeners();
        savePackMeta();
        loadEmotes();

        Bukkit.getServicesManager().register(EmotesAPI.class, this, this, ServicePriority.Normal);
    }

    @Override
    public void onDisable() {
    }

    private void registerListeners() {
        if (getConfig().getBoolean("enable-chat-handler")) {
            getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        }
    }

    private void savePackMeta() {
        File packFolder = new File(getDataFolder(), "pack");
        if (!packFolder.exists()) {
            packFolder.mkdir();
        }

        File mcMeta = new File(packFolder, "pack.mcmeta");
        File icon = new File(packFolder, "pack.png");

        // write mcmeta
        if (!mcMeta.exists()) {
            try (InputStream is = getResource("pack.mcmeta")) {
                try (FileOutputStream out = new FileOutputStream(mcMeta)) {
                    out.write(is.readAllBytes());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // write png
        if (!icon.exists()) {
            try (InputStream is = getResource("pack.png")) {
                try (FileOutputStream out = new FileOutputStream(icon)) {
                    out.write(is.readAllBytes());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadEmotes() {
        File emotesFolder = new File(getDataFolder(), "emotes");
        if (!emotesFolder.exists()) {
            emotesFolder.mkdir();
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(emotesFolder.toPath())) {
            for (Path path : stream) {
                if (!Files.isDirectory(path)) {
                    File file = path.toFile();
                    String mimeType = Files.probeContentType(path);
                    if (!mimeType.equals("image/png") && !mimeType.equals("image/jpeg")) {
                        getLogger().warning(file.getName() + " is not png/jpg; skipping");
                        continue;
                    }

                    String emoteName = file.getName();
                    emoteName = emoteName.substring(0, emoteName.lastIndexOf('.'));

                    manager.register(emoteName, new Emote(emoteName, file));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.replacer = new EmoteReplacer(manager);

        try {
            this.pack(new File(getDataFolder(), "emotes.zip"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pack(ZipOutputStream out) throws IOException {
        Map<String, String> lang = new HashMap<>();
        Font font = new Font(new ArrayList<>());
        String nextChar = "\ue000";

        for (Emote emote : manager.getAll()) {
            File file = emote.file();
            out.putNextEntry(new ZipEntry("assets/plasmo_emotes/textures/font/emotes/" + file.getName().toLowerCase()));
            out.write(Files.readAllBytes(file.toPath()));

            font.providers().add(new FontProvider(
                    "plasmo_emotes:font/emotes/" + file.getName().toLowerCase(),
                    List.of(nextChar),
                    11,
                    9,
                    "bitmap"
            ));

            lang.put(emote.name(), nextChar);

            nextChar = Character.toString(nextChar.charAt(0) + 1);
        }

        File packFolder = new File(getDataFolder(), "pack");

        try (InputStream is = new FileInputStream(new File(packFolder, "pack.mcmeta"))) {
            out.putNextEntry(new ZipEntry("pack.mcmeta"));
            out.write(is.readAllBytes());
        }

        try (InputStream is = new FileInputStream(new File(packFolder, "pack.png"))) {
            out.putNextEntry(new ZipEntry("pack.png"));
            out.write(is.readAllBytes());
        }

        out.putNextEntry(new ZipEntry("assets/minecraft/font/default.json"));
        byte[] json = gson.toJson(font).getBytes(StandardCharsets.UTF_8);
        out.write(json, 0, json.length);

        out.putNextEntry(new ZipEntry("assets/minecraft/lang/en_us.json"));
        json = gson.toJson(lang).getBytes(StandardCharsets.UTF_8);
        out.write(json, 0, json.length);
    }

    @Override
    public void pack(FileOutputStream out) throws IOException {
        try (ZipOutputStream zipOut = new ZipOutputStream(out)) {
            this.pack(zipOut);
        }
    }

    @Override
    public void pack(File file) throws IOException {
        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            this.pack(fileOut);
        }
    }

    @Override
    public Component process(Component component) {
        return replacer.process(component);
    }

    @Override
    public EmoteManager getEmoteManager() {
        return manager;
    }
}
