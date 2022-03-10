package su.plo.emotes.data;

import java.util.List;

public record FontProvider(String file, List<String> chars, int height, int ascent, String type) {
}
