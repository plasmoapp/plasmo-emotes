package su.plo.emotes;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import net.kyori.adventure.text.Component;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import su.plo.emotes.api.Emote;
import su.plo.emotes.api.EmoteManager;
import su.plo.emotes.api.EmotesAPI;
import su.plo.emotes.commands.EmotesCommand;
import su.plo.emotes.data.Font;
import su.plo.emotes.data.FontProvider;
import su.plo.emotes.listeners.ChatListener;
import su.plo.emotes.listeners.ProtoChatListener;

import java.io.*;
import java.lang.reflect.Type;
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
    private final String conflictPattern = "___[0-9]";
    private final List<String> languages = List.of("af_za", "ar_sa", "ast_es", "az_az", "ba_ru", "bar", "be_by", "bg_bg", "br_fr", "brb", "bs_ba", "ca_es", "cs_cz", "cy_gb", "da_dk", "de_at", "de_ch", "de_de", "el_gr", "en_au", "en_ca", "en_gb", "en_nz", "en_pt", "en_ud", "en_us", "enp", "enws", "eo_uy", "es_ar", "es_cl", "es_ec", "es_es", "es_mx", "es_uy", "es_ve", "esan", "et_ee", "eu_es", "fa_ir", "fi_fi", "fil_ph", "fo_fo", "fr_ca", "fr_fr", "fra_de", "fur_it", "fy_nl", "ga_ie", "gd_gb", "gl_es", "haw_us", "he_il", "hi_in", "hr_hr", "hu_hu", "hy_am", "id_id", "ig_ng", "io_en", "is_is", "isv", "it_it", "ja_jp", "jbo_en", "ka_ge", "kk_kz", "kn_in", "ko_kr", "ksh", "kw_gb", "la_la", "lb_lu", "li_li", "lmo", "lol_us", "lt_lt", "lv_lv", "lzh", "mk_mk", "mn_mn", "ms_my", "mt_mt", "nds_de", "nl_be", "nl_nl", "nn_no", "no_no???[JE only] nb_no???[BE only]", "oc_fr", "ovd", "pl_pl", "pt_br", "pt_pt", "qya_aa", "ro_ro", "rpr", "ru_ru", "se_no", "sk_sk", "sl_si", "so_so", "sq_al", "sr_sp", "sv_se", "sxu", "szl", "ta_in", "th_th", "tl_ph", "tlh_aa", "tok", "tr_tr", "tt_ru", "uk_ua", "val_es", "vec_it", "vi_vn", "yi_de", "yo_ng", "zh_cn", "zh_hk", "zh_tw");

    private final Gson gson = new Gson();
    private final EmoteManager manager = new EmoteManagerImpl();

    public ProtocolManager protocolManager;
    private EmoteReplacer replacer;

    @Override
    public void onEnable() {
        // Plugin metrics
        new Metrics(this, 14583);

        saveDefaultConfig();
        registerListeners();
        savePackMeta();
        loadEmotes();

        getCommand("emotes").setExecutor(new EmotesCommand(this));
        Bukkit.getServicesManager().register(EmotesAPI.class, this, this, ServicePriority.Normal);
    }

    @Override
    public void onDisable() {
        if (protocolManager != null) {
            protocolManager.removePacketListeners(this);
        }
    }

    private void registerListeners() {
        if (getConfig().getBoolean("enable-chat-handler")) {
            if (getConfig().getBoolean("use-protocol-lib") &&
                    getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
                protocolManager = ProtocolLibrary.getProtocolManager();
                new ProtoChatListener(this).listen();
            } else {
                getServer().getPluginManager().registerEvents(new ChatListener(this), this);
            }
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
                    if (mimeType == null || (!mimeType.equals("image/png") && !mimeType.equals("image/jpeg"))) {
                        getLogger().warning(file.getName() + " is not png/jpg; skipping");
                        continue;
                    }

                    String emoteName = file.getName();
                    emoteName = emoteName.replaceAll(conflictPattern, "");
                    emoteName = emoteName.substring(0, emoteName.lastIndexOf('.'));

                    manager.register(emoteName, new Emote(emoteName, file));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.replacer = new EmoteReplacer(manager);

        if (getConfig().getBoolean("generate-pack")) {
            try {
                this.pack(new File(getDataFolder(), "emotes.zip"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Map<String, String> loadPackInfo() throws IOException, JsonParseException {
        File file = new File(getDataFolder(), "emotes_lang.json");
        if (!file.exists()) {
            return null;
        }

        Type LANG_TYPE = new TypeToken<Map<String, String>>() {
        }.getType();

        try (JsonReader reader = new JsonReader(new FileReader(file, StandardCharsets.UTF_8))) {
            return gson.fromJson(reader, LANG_TYPE);
        }
    }

    @Override
    public void pack(ZipOutputStream out) throws IOException {
        Map<String, String> lang = new HashMap<>();
        Font font = new Font(new ArrayList<>());

        String nextChar = "\ue000";

        Map<String, String> packInfo = loadPackInfo();
        if (packInfo != null) {
            for (String character : packInfo.values()) {
                if (character.charAt(0) >= nextChar.charAt(0)) {
                    nextChar = Character.toString((character.charAt(0) + 1));
                }
            }
        } else {
            packInfo = new HashMap<>();
        }

        for (Emote emote : manager.getAll()) {
            String currentChar = packInfo.get(emote.name());
            if (currentChar == null) {
                currentChar = nextChar;
                nextChar = Character.toString(nextChar.charAt(0) + 1);
            }

            File file = emote.file();
            HashCode hash = com.google.common.io.Files
                    .hash(file, Hashing.md5());
            String fileName = file.getName().substring(0, file.getName().lastIndexOf('.'));
            fileName = fileName.replaceAll(conflictPattern, "");
            fileName += "_";
            fileName += hash;
            fileName += file.getName().substring(file.getName().lastIndexOf('.'));
            fileName = fileName.toLowerCase();

            out.putNextEntry(new ZipEntry("assets/plasmo_emotes/textures/font/emotes/" + fileName));
            out.write(Files.readAllBytes(file.toPath()));

            font.providers().add(new FontProvider(
                    "plasmo_emotes:font/emotes/" + fileName,
                    List.of(currentChar),
                    11,
                    9,
                    "bitmap"
            ));

            lang.put(emote.name(), currentChar);
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

        // save all languages
        json = gson.toJson(lang).getBytes(StandardCharsets.UTF_8);
        for (String language : languages) {
            out.putNextEntry(new ZipEntry("assets/minecraft/lang/" + language + ".json"));
            out.write(json, 0, json.length);
        }

        // save pack info file
        try (OutputStream os = new FileOutputStream(new File(getDataFolder(), "emotes_lang.json"))) {
            os.write(json);
        }
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
