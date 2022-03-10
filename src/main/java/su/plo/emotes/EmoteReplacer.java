package su.plo.emotes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import su.plo.emotes.api.Emote;
import su.plo.emotes.api.EmoteManager;

import java.util.List;

public class EmoteReplacer {
    private final TextReplacementConfig replacementConfig;

    EmoteReplacer(EmoteManager emotes) {
        if (emotes.getAll().size() == 0) {
            this.replacementConfig = null;
            return;
        }

        List<String> list = emotes.getAll()
                .stream()
                .map(Emote::name)
                .toList();

        this.replacementConfig = TextReplacementConfig.builder()
                .match("\\b(" + String.join("|", list) + ")\\b")
                .replacement((match, _b) -> Component.translatable(_b.content()))
                .build();
    }

    public Component process(Component component) {
        if (replacementConfig == null) {
            return component;
        }

        return component.replaceText(this.replacementConfig);
    }
}
