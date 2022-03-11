package su.plo.emotes.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import su.plo.emotes.Emotes;

public class ChatListener implements Listener {
    private final Emotes plugin;

    public ChatListener(Emotes plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onChat(AsyncChatEvent event) {
        event.message(plugin.process(event.message()));
    }
}
