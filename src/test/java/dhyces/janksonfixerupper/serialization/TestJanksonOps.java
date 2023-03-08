package dhyces.janksonfixerupper.serialization;

import blue.endless.jankson.*;
import blue.endless.jankson.api.SyntaxError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dhyces.janksonfixerupper.serialization.comments.Comment;
import dhyces.janksonfixerupper.serialization.io.JanksonIoHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TestJanksonOps {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().create();
    private static final Jankson JANKSON = Jankson.builder().build();
    private static final JsonGrammar GRAMMER = new JsonGrammar.Builder().bareSpecialNumerics(true).printUnquotedKeys(true).build();
    private static final Logger LOGGER = LoggerFactory.getLogger(TestJanksonOps.class);

    private static final Path TEST_RESOURCES = Paths.get("src/test/resources/");

    @Test
    public void write(@TempDir(cleanup = CleanupMode.NEVER) Path path) {
        Path jankFile = path.resolve("jank_test.json5");
        TestObject obj = new TestObject("hello", 4.5, List.of(4.1f, 2.7f, 5.23423f));
        DataResult<JsonElement> test = TestObject.CODEC.encodeStart(JanksonOps.INSTANCE, obj);
        if (test.error().isPresent()) {
            LOGGER.error(() -> test.error().get().message());
        }
        try {
            Files.write(jankFile, test.result().get().toJson(GRAMMER).getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void writeRegularJank(@TempDir(cleanup = CleanupMode.NEVER) Path path) throws IOException {
        Path jank = path.resolve("test.json5");
        JsonObject json = new JsonObject();
        json.put("someStr", JsonPrimitive.of("hello"));
        json.put("someDouble", JsonPrimitive.of(4.5));
        // Regular jankson has precision error here
        json.put("floatList", new JsonArray(List.of(4.2f, 3.1f), JANKSON.getMarshaller()));
        Files.write(jank, json.toJson(GRAMMER).getBytes());
    }

    @Test
    public void read() {
        Path readTest = TEST_RESOURCES.resolve("jankson_read.json5");
        DataResult<TestObject> test = JanksonIoHelper.parseCodec(readTest, TestObject.CODEC);
        if (test.error().isPresent()) {
            LOGGER.error(() -> test.error().get().message());
            return;
        }
        TestObject o = test.result().get();
    }

    private record TestObject(String someStr, double someDouble, List<Float> floatList) {
        public static final Codec<TestObject> CODEC = CommentedCodec.commented(RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("someStr").forGetter(TestObject::someStr),
                        Codec.DOUBLE.fieldOf("someDouble").forGetter(TestObject::someDouble),
                        CommentedCodec.commentedList(Codec.FLOAT, builder -> builder.whenIndexIs(1, "comment on index 1")).fieldOf("floatList").forGetter(TestObject::floatList)
                ).apply(instance, TestObject::new)
        ), Comment.ifMapElseEmpty((object, key) -> {
            return switch (key) {
                case "someStr" -> "Comment on string!";
                case "someDouble" -> "Comment on double!";
                case "floatList" -> "Comment on list!";
                default -> "";
            };
        }));
    }
}
