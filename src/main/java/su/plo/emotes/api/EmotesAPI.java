package su.plo.emotes.api;

import net.kyori.adventure.text.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

public interface EmotesAPI {
    /**
     * Zip emotes
     */
    void pack(ZipOutputStream out) throws IOException;

    /**
     * Zip emotes
     */
    void pack(FileOutputStream out) throws IOException;

    /**
     * Zip emotes
     */
    void pack(File file) throws IOException;

    /**
     * Replace plain emotes text to TranslatableComponent
     */
    Component process(Component component);

    EmoteManager getEmoteManager();
}
