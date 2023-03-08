package dhyces.janksonfixerupper.serialization.comments;


import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;

import java.util.function.BiFunction;

public interface Comment extends BiFunction<JsonElement, JsonElement, String> {
    String DEFAULT_VALUE = "";

    @Override
    String apply(JsonElement container, JsonElement key);

    default String orEmpty(JsonElement container, JsonElement key) {
        String ret = apply(container, key);
        return ret != null ? ret : "";
    }

    static Comment constant(String comment) {
        return (container, key) -> comment;
    }

    static Comment ifListElseEmpty(BiFunction<JsonArray, JsonElement, String> function) {
        return ifListElse(function, "");
    }

    static Comment ifListElse(BiFunction<JsonArray, JsonElement, String> function, String defaultVal) {
        return (container, key) -> {
            if (container instanceof JsonArray array) {
                return function.apply(array, key);
            }
            return defaultVal;
        };
    }

    static Comment ifMapElseEmpty(BiFunction<JsonObject, String, String> function) {
        return ifMapElse(function, "");
    }

    static Comment ifMapElse(BiFunction<JsonObject, String, String> function, String defaultVal) {
        return (container, key) -> {
            if (container instanceof JsonObject object && key instanceof JsonPrimitive primitive) {
                return function.apply(object, primitive.asString());
            }
            return defaultVal;
        };
    }


}
