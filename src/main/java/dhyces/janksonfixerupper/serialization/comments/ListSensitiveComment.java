package dhyces.janksonfixerupper.serialization.comments;

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonElement;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class ListSensitiveComment implements Comment {
    private final Int2ObjectMap<String> map;
    private ListSensitiveComment(Int2ObjectMap<String> map) {
        this.map = map;
    }

    @Override
    public String apply(JsonElement container, JsonElement key) {
        if (container instanceof JsonArray array) {
            int index = array.indexOf(key);
            if (map.containsKey(index)) {
                return map.get(index);
            }
        }
        return DEFAULT_VALUE;
    }

    public static class Builder {
        private ListSensitiveComment comment = new ListSensitiveComment(new Int2ObjectOpenHashMap<>());

        public ListSensitiveComment.Builder whenIndexIs(int index, String comment) {
            this.comment.map.put(index, comment);
            return this;
        }

        public ListSensitiveComment build() {
            ListSensitiveComment temp = comment;
            comment = new ListSensitiveComment(new Int2ObjectOpenHashMap<>());
            return temp;
        }
    }
}
