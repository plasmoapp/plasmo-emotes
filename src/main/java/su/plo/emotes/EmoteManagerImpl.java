package su.plo.emotes;

import su.plo.emotes.api.Emote;
import su.plo.emotes.api.EmoteManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmoteManagerImpl implements EmoteManager {
    private final Map<String, Emote> emotes = new HashMap<>();

    @Override
    public synchronized void register(String name, Emote emote) {
        emotes.put(name, emote);
    }

    @Override
    public synchronized void unregister(String name) {
        emotes.remove(name);
    }

    @Override
    public synchronized Emote get(String name) {
        return emotes.get(name);
    }

    @Override
    public List<Emote> getAll() {
        return new ArrayList<>(emotes.values());
    }
}
