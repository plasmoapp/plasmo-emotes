package su.plo.emotes.commands;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.plo.emotes.Emotes;
import su.plo.emotes.api.Emote;

public class EmotesCommand implements CommandExecutor {
    private final Emotes plugin;

    public EmotesCommand(Emotes plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        Book.Builder builder = Book.builder();

        int pageLines = 0;
        Component page = Component.empty();
        for (Emote emote : plugin.getEmoteManager().getAll()) {
            pageLines++;
            page = page.append(Component.translatable(emote.name()).color(NamedTextColor.WHITE))
                    .append(Component.text(" "))
                    .append(Component.text(emote.name()))
                    .append(Component.text("\n"));

            if (pageLines >= 13) {
                builder.addPage(page);
                pageLines = 0;
                page = Component.empty();
            }
        }

        if (pageLines > 0) {
            builder.addPage(page);
        }

        player.openBook(builder.build());

        return true;
    }
}
