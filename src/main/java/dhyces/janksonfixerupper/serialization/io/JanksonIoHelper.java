package dhyces.janksonfixerupper.serialization.io;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.api.SyntaxError;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dhyces.janksonfixerupper.serialization.JanksonOps;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public class JanksonIoHelper {
    public static final Supplier<Jankson> DEFAULT_JANKSON = Suppliers.memoize(() -> Jankson.builder().build());
    public static final JsonGrammar STANDARD_JSON5 = new JsonGrammar.Builder().bareSpecialNumerics(true).printUnquotedKeys(true).build();

    public static DataResult<JsonElement> read(Path path) {
        return read(path, DEFAULT_JANKSON.get());
    }

    public static DataResult<JsonElement> read(Path path, Jankson jankson) {
        try {
            JsonElement element = jankson.load(path.toFile());
            return DataResult.success(element);
        } catch (IOException e) {
            return DataResult.error(e.getMessage());
        } catch (SyntaxError e) {
            return DataResult.error(e.getMessage() + ": " + e.getLineMessage());
        }
    }

    public static <T> DataResult<T> parseCodec(Path path, Codec<T> codec) {
        return parseCodec(path, codec, DEFAULT_JANKSON.get());
    }

    public static <T> DataResult<T> parseCodec(Path path, Codec<T> codec, Jankson jankson) {
        DataResult<JsonElement> elementDataResult = read(path, jankson);
        return elementDataResult.flatMap(element -> codec.parse(JanksonOps.INSTANCE, element));
    }

    public static DataResult<JsonElement> write(Path path, DataResult<JsonElement> jsonResult) {
        return write(path, jsonResult, STANDARD_JSON5);
    }

    public static DataResult<JsonElement> write(Path path, DataResult<JsonElement> jsonResult, JsonGrammar grammar) {
        if (jsonResult.error().isPresent()) {
            return jsonResult;
        }
        try {
            Files.write(path, jsonResult.result().get().toJson(grammar).getBytes());
        } catch (IOException e) {
            return jsonResult.mapError(s -> e.getMessage());
        }
        return jsonResult;
    }
}
