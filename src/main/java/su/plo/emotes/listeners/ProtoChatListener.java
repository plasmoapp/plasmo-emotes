package su.plo.emotes.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.kyori.adventure.text.Component;
import su.plo.emotes.Emotes;

public class ProtoChatListener {
    private final Emotes plugin;

    public ProtoChatListener(Emotes plugin) {
        this.plugin = plugin;
    }

    public void listen() {
        final Emotes emotes = this.plugin;

        this.plugin.protocolManager.addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(plugin).listenerPriority(ListenerPriority.HIGH).types(PacketType.Play.Server.CHAT)) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.isPlayerTemporary() || !event.isFiltered() || event.isCancelled() || !event.getPacketType().equals(PacketType.Play.Server.CHAT)) {
                    return;
                }

                PacketContainer packet = event.getPacket();
                EnumWrappers.ChatType type = packet.getChatTypes().read(0);
                if (type == null || type.equals(EnumWrappers.ChatType.GAME_INFO)) {
                    return;
                }

                for (int i = 0; i < packet.getModifier().size(); i++) {
                    String name = packet.getModifier().getField(i).getType().getName();
                    if (name.contains("net.kyori.adventure.text")) {
                        Component component = (Component) packet.getModifier().read(i);
                        if (component != null) {
                            component = emotes.process(component);
                            packet.getModifier().write(i, component);
                        }
                    }
                }
            }
        });
    }
}
