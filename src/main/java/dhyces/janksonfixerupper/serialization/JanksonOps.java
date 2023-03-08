package dhyces.janksonfixerupper.serialization;

import blue.endless.jankson.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import dhyces.janksonfixerupper.serialization.wrappers.Commented;

import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class JanksonOps implements DynamicOps<JsonElement> {
    public static final JanksonOps INSTANCE = new JanksonOps();
    private JanksonOps() {}

    @Override
    public JsonElement empty() {
        return JsonNull.INSTANCE;
    }

    @Override
    public <U> U convertTo(DynamicOps<U> outOps, JsonElement input) {
        if (input instanceof JsonObject) {
            return convertMap(outOps, input);
        }
        if (input instanceof JsonArray) {
            return convertList(outOps, input);
        }
        if (input.equals(JsonNull.INSTANCE)) {
            return outOps.empty();
        }

        JsonPrimitive primitive = (JsonPrimitive) input;
        Object value = primitive.getValue();
        if (value instanceof Byte b) {
            return outOps.createByte(b);
        }
        if (value instanceof Short s) {
            return outOps.createShort(s);
        }
        if (value instanceof Integer i) {
            return outOps.createInt(i);
        }
        if (value instanceof Float f) {
            return outOps.createFloat(f);
        }
        if (value instanceof Double d) {
            return outOps.createDouble(d);
        }
        if (value instanceof Boolean bool) {
            return outOps.createBoolean(bool);
        }
        return outOps.createString(primitive.asString());
    }

    @Override
    public DataResult<Number> getNumberValue(JsonElement input) {
        if (!(input instanceof JsonPrimitive primitive) || Double.isNaN(primitive.asDouble(Double.NaN))) {
            return DataResult.error("Cannot be accessed as a double: " + input);
        }
        return DataResult.success(primitive.asDouble(0));
    }

    @Override
    public JsonElement createNumeric(Number num) {
        if (num instanceof Byte b) {
            return JsonPrimitive.of(b.longValue());
        }
        if (num instanceof Short s) {
            return JsonPrimitive.of(s.longValue());
        }
        if (num instanceof Integer i) {
            return JsonPrimitive.of(i.longValue());
        }
        if (num instanceof Float f) {
            return JsonPrimitive.of(Double.parseDouble(f.toString()));
        }
        if (num instanceof Double d) {
            return JsonPrimitive.of(d);
        }
        return new JsonPrimitive(num);
    }

    @Override
    public DataResult<String> getStringValue(JsonElement input) {
        if (!(input instanceof JsonPrimitive primitive)) {
            return DataResult.error("Cannot be accessed as a string: " + input);
        }
        return DataResult.success(primitive.asString());
    }

    @Override
    public JsonElement createString(String value) {
        return JsonPrimitive.of(value);
    }

    @Override
    public DataResult<JsonElement> mergeToList(JsonElement list, JsonElement value) {
        if (!(list instanceof JsonArray array)) {
            return DataResult.error("Cannot be accessed as a list: " + list);
        }

        JsonArray retArray = new JsonArray();
        if (!array.isEmpty()) {
            retArray.addAll(array);
        }
        retArray.add(value);
        if (value instanceof Commented commented) {
            retArray.setComment(retArray.indexOf(value), commented.getComment());
        }
        return DataResult.success(retArray);
    }

    @Override
    public DataResult<JsonElement> mergeToMap(JsonElement map, JsonElement key, JsonElement value) {
        if (!(map instanceof JsonObject obj)) {
            return DataResult.error("Cannot be accessed as a json object: " + map);
        }
        if (!(key instanceof JsonPrimitive primitive)) {
            return DataResult.error("Key: " + key + " is not a primitive value.");
        }

        JsonObject retMap = new JsonObject();
        if (!obj.isEmpty()) {
            retMap.putAll(obj);
        }
        retMap.put(primitive.asString(), value);
        if (primitive instanceof Commented commented) {
            retMap.setComment(primitive.asString(), commented.getComment());
        }
        return DataResult.success(retMap);
    }

    @Override
    public DataResult<Stream<Pair<JsonElement, JsonElement>>> getMapValues(JsonElement input) {
        if (!(input instanceof JsonObject map)) {
            return DataResult.error("Cannot be accessed as a json object: " + input);
        }
        return DataResult.success(map.entrySet().stream().map(entry -> Pair.of(JsonPrimitive.of(entry.getKey()), entry.getValue())));
    }

    @Override
    public JsonElement createMap(Stream<Pair<JsonElement, JsonElement>> map) {
        JsonObject obj = new JsonObject();
        map.forEach(pair -> {
            if (!(pair.getFirst() instanceof JsonPrimitive primitive)) {
                throw new ClassCastException("Key: " + pair.getFirst() + " is not a json primitive");
            }
            obj.put(primitive.asString(), pair.getSecond());
        });
        return obj;
    }

    @Override
    public DataResult<Stream<JsonElement>> getStream(JsonElement input) {
        if (!(input instanceof JsonArray array)) {
            return DataResult.error("Cannot be accessed as a json object: " + input);
        }
        return DataResult.success(array.stream());
    }

    @Override
    public JsonElement createList(Stream<JsonElement> input) {
        return input.collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
    }

    @Override
    public JsonElement remove(JsonElement input, String key) {
        if (input instanceof JsonObject obj) {
            JsonObject retObj = obj.clone();
            retObj.remove(key);
            return retObj;
        }
        return input;
    }

    @Override
    public ListBuilder<JsonElement> listBuilder() {
        return new JanksonArrayBuilder();
    }

    @Override
    public RecordBuilder<JsonElement> mapBuilder() {
        return new JanksonMapBuilder();
    }

    static class JanksonArrayBuilder implements ListBuilder<JsonElement> {
        private DataResult<JsonArray> builder = DataResult.success(new JsonArray(), Lifecycle.stable());

        @Override
        public DynamicOps<JsonElement> ops() {
            return INSTANCE;
        }

        @Override
        public DataResult<JsonElement> build(JsonElement prefix) {
            DataResult<JsonElement> result = builder.flatMap(array -> {
                JsonArray arr = prefix instanceof JsonArray a ? a : null;
                if (arr == null && prefix != ops().empty()) {
                    return DataResult.error("Cannot access prefix as array: " + prefix);
                }

                JsonArray ret = new JsonArray();
                if (prefix != ops().empty() && arr != null) {
                    for (int i = 0; i < arr.size(); i++) {
                        ret.add(arr.get(i));
                        if (arr.get(i) instanceof Commented commented) {
                            ret.setComment(i, commented.getComment());
                        }
                    }
                }
                for (int i = 0; i < array.size(); i++) {
                    ret.add(array.get(i));
                    if (array.get(i) instanceof Commented commented) {
                        ret.setComment(i, commented.getComment());
                    }
                }
                return DataResult.success(ret);
            });
            builder = DataResult.success(new JsonArray(), Lifecycle.stable());
            return result;
        }

        @Override
        public ListBuilder<JsonElement> add(JsonElement value) {
            builder = builder.map(array -> {
                array.add(value);
                if (value instanceof Commented commented) {
                    array.setComment(array.indexOf(value), commented.getComment());
                }
                return array;
            });
            return this;
        }

        @Override
        public ListBuilder<JsonElement> add(DataResult<JsonElement> value) {
            builder = builder.apply2stable((array, jsonElement) -> {
                array.add(jsonElement);
                if (jsonElement instanceof Commented commented) {
                    array.setComment(array.indexOf(jsonElement), commented.getComment());
                }
                return array;
                }, value);
            return this;
        }

        @Override
        public ListBuilder<JsonElement> withErrorsFrom(DataResult<?> result) {
            builder = builder.flatMap(array -> result.map(o -> array));
            return this;
        }

        @Override
        public ListBuilder<JsonElement> mapError(UnaryOperator<String> onError) {
            builder = builder.mapError(onError);
            return this;
        }
    }

    static class JanksonMapBuilder implements RecordBuilder<JsonElement> {
        private DataResult<JsonObject> builder = DataResult.success(new JsonObject(), Lifecycle.stable());

        @Override
        public DynamicOps<JsonElement> ops() {
            return INSTANCE;
        }

        @Override
        public RecordBuilder<JsonElement> add(JsonElement key, JsonElement value) {
            builder = ops().getStringValue(key).flatMap(s -> {
                return builder.map(object -> {
                    object.put(s, value);
                    if (value instanceof Commented commented) {
                        object.setComment(s, commented.getComment());
                    }
                    return object;
                });
            });
            return this;
        }

        @Override
        public RecordBuilder<JsonElement> add(JsonElement key, DataResult<JsonElement> value) {
            builder = ops().getStringValue(key).flatMap(s -> {
                return builder.apply2stable((object, v) -> {
                    object.put(s, v);
                    if (v instanceof Commented commented) {
                        object.setComment(s, commented.getComment());
                    }
                    return object;
                }, value);
            });
            return this;
        }

        @Override
        public RecordBuilder<JsonElement> add(DataResult<JsonElement> key, DataResult<JsonElement> value) {
            builder = key.flatMap(element -> {
               add(element, value);
               return builder;
            });
            return this;
        }

        @Override
        public RecordBuilder<JsonElement> withErrorsFrom(DataResult<?> result) {
            builder = builder.flatMap(object -> result.map(o -> object));
            return this;
        }

        @Override
        public RecordBuilder<JsonElement> setLifecycle(Lifecycle lifecycle) {
            builder = builder.setLifecycle(lifecycle);
            return this;
        }

        @Override
        public RecordBuilder<JsonElement> mapError(UnaryOperator<String> onError) {
            builder = builder.mapError(onError);
            return this;
        }

        @Override
        public DataResult<JsonElement> build(JsonElement prefix) {
            if (prefix == null || prefix == JsonNull.INSTANCE) {
                return builder.map(object -> object);
            }
            if (prefix instanceof JsonObject object) {
                JsonObject ret = new JsonObject();
                for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                    ret.put(entry.getKey(), entry.getValue());
                    if (object.getComment(entry.getKey()) != null) {
                        ret.setComment(entry.getKey(), object.getComment(entry.getKey()));
                    }
                }
                builder = builder.map(object1 -> {
                    for (Map.Entry<String, JsonElement> entry : object1.entrySet()) {
                        ret.put(entry.getKey(), entry.getValue());
                        if (object1.getComment(entry.getKey()) != null) {
                            ret.setComment(entry.getKey(), object1.getComment(entry.getKey()));
                        }
                    }
                    return object1;
                });

                return DataResult.success(ret);
            }
            return DataResult.error("Could not merge the given prefix: " + prefix);
        }
    }
}
