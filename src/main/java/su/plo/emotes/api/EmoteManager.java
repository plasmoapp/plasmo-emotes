package su.plo.emotes.api;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface EmoteManager {
    void register(String name, Emote emote);

    void unregister(String name);

    @Nullable Emote get(String name);

    List<Emote> getAll();
}
