package dhyces.janksonfixerupper.serialization.comments;

import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;

import java.util.HashMap;
import java.util.Map;

public class MapSensitiveComment implements Comment {
    private final Map<String, String> map;
    private MapSensitiveComment(Map<String, String> map) {
        this.map = map;
    }

    @Override
    public String apply(JsonElement container, JsonElement key) {
        if (container instanceof JsonObject && key instanceof JsonPrimitive primitive) {
            return map.get(primitive.asString());
        }
        return DEFAULT_VALUE;
    }

    public static class Builder {
        private MapSensitiveComment comment = new MapSensitiveComment(new HashMap<>());

        public MapSensitiveComment.Builder whenKeyIs(String key, String comment) {
            this.comment.map.put(key, comment);
            return this;
        }

        public MapSensitiveComment build() {
            MapSensitiveComment temp = comment;
            comment = new MapSensitiveComment(new HashMap<>());
            return temp;
        }
    }
}
