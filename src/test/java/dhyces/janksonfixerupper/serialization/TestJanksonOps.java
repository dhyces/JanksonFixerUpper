package dhyces.janksonfixerupper.serialization;

import blue.endless.jankson.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(TestJanksonOps.class);

    private static final Path TEST_RESOURCES = Paths.get("src/test/resources/");
    private static final Path TEMP = Paths.get("src/test/temp/");

    @Test
    public void write() {
        Path jankFile = TEMP.resolve("jank_test1.json5");
        TestObject obj = new TestObject("hello", 4.5, List.of(4.1f, 2.7f, 5.23423f));
        DataResult<JsonElement> test = TestObject.CODEC.encodeStart(JanksonOps.INSTANCE, obj);
        test = JanksonIoHelper.write(jankFile, test);
        if (test.error().isPresent()) {
            String message = test.error().get().message();
            LOGGER.error(() -> message);
        }
    }

    @Test
    public void write2() {
        Path jankFile = TEMP.resolve("jank_test2.json5");
        TestObject2 obj = new TestObject2(true, true, false, true, true, false, false, false, true, false, true, false, false, true, false, false);
        DataResult<JsonElement> test = TestObject2.CODEC.encodeStart(JanksonOps.INSTANCE, obj);
        test = JanksonIoHelper.write(jankFile, test);
        if (test.error().isPresent()) {
            String message = test.error().get().message();
            LOGGER.error(() -> message);
        }
    }

    @Test
    public void write3() {
        Path jankFile = TEMP.resolve("jank_test3.json5");
        EveryPrimitive obj = new EveryPrimitive("HelloWorld", false, (byte)5, (short)2, 7, 124L, 3.1415f, 123.4215);
        DataResult<JsonElement> test = EveryPrimitive.CODEC.encodeStart(JanksonOps.INSTANCE, obj);
        test = JanksonIoHelper.write(jankFile, test);
        if (test.error().isPresent()) {
            String message = test.error().get().message();
            LOGGER.error(() -> message);
        }
    }

    @Test
    public void write4() {
        Path jankFile = TEMP.resolve("jank_test4.json5");
        EveryPrimitiveFail obj = new EveryPrimitiveFail("HelloWorld", false, (byte)5, (short)2, 7, 124L, 3.1415f, 123.4215);
        DataResult<JsonElement> test = EveryPrimitiveFail.CODEC.encodeStart(JanksonOps.INSTANCE, obj);
        test = JanksonIoHelper.write(jankFile, test);
        if (test.error().isPresent()) {
            String message = test.error().get().message();
            LOGGER.error(() -> message);
        }
    }

    @Test
    public void writeRegularJank() throws IOException {
        Path jank = TEMP.resolve("test.json5");
        JsonObject json = new JsonObject();
        json.put("someStr", JsonPrimitive.of("hello"));
        json.put("someDouble", JsonPrimitive.of(4.5));
        // Regular jankson has precision error here
        json.put("floatList", new JsonArray(List.of(4.2f, 3.1f), JANKSON.getMarshaller()));
        Files.write(jank, json.toJson(JanksonIoHelper.STANDARD_JSON5).getBytes());
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

    private record EveryPrimitive(String someStr, boolean someBool, byte someByte, short someShort, int someInt,
                                  long someLong, float someFloat, double someDouble) {
        public static final Codec<EveryPrimitive> CODEC = CommentedCodec.commented(RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("someStr").forGetter(EveryPrimitive::someStr),
                        Codec.BOOL.fieldOf("someBool").forGetter(EveryPrimitive::someBool),
                        Codec.BYTE.fieldOf("someByte").forGetter(EveryPrimitive::someByte),
                        Codec.SHORT.fieldOf("someShort").forGetter(EveryPrimitive::someShort),
                        Codec.INT.fieldOf("someInt").forGetter(EveryPrimitive::someInt),
                        Codec.LONG.fieldOf("someLong").forGetter(EveryPrimitive::someLong),
                        Codec.FLOAT.fieldOf("someFloat").forGetter(EveryPrimitive::someFloat),
                        Codec.DOUBLE.fieldOf("someDouble").forGetter(EveryPrimitive::someDouble)
                ).apply(instance, EveryPrimitive::new)
        ), Comment.ifMapElseEmpty((object, key) -> {
            return switch (key) {
                case "someStr" -> "Comment on string!";
                case "someDouble" -> "Comment on double!";
                case "floatList" -> "Comment on list!";
                default -> "";
            };
        }));
    }

    private record EveryPrimitiveFail(String someStr, boolean someBool, byte someByte, short someShort, int someInt,
                                  long someLong, float someFloat, double someDouble) {
        public static final Codec<EveryPrimitiveFail> CODEC = CommentedCodec.commented(RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("someStr").forGetter(EveryPrimitiveFail::someStr),
                        Codec.BOOL.fieldOf("someBool").forGetter(EveryPrimitiveFail::someBool),
                        Codec.BYTE.fieldOf("someByte").forGetter(EveryPrimitiveFail::someByte),
                        Codec.SHORT.fieldOf("someShort").forGetter(EveryPrimitiveFail::someShort),
                        Codec.INT.fieldOf("someInt").forGetter(EveryPrimitiveFail::someInt),
                        Codec.LONG.fieldOf("someLong").forGetter(EveryPrimitiveFail::someLong),
                        Codec.FLOAT.fieldOf("someFloat").forGetter(EveryPrimitiveFail::someFloat),
                        Codec.DOUBLE.fieldOf("someFloat").forGetter(EveryPrimitiveFail::someDouble) // Two 'someFloat's
                ).apply(instance, EveryPrimitiveFail::new)
        ), Comment.ifMapElseEmpty((object, key) -> {
            return switch (key) {
                case "someStr" -> "Comment on string!";
                case "someDouble" -> "Comment on double!"; // Test what happens when key doesn't exist
                case "someFloat" -> "Comment on list!"; // Test what happens when there are two keys
                default -> "";
            };
        }));
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

    private record TestObject2(boolean a, boolean b, boolean c, boolean d, boolean e, boolean f, boolean g, boolean h,
                               boolean i, boolean j, boolean k, boolean l, boolean m, boolean n, boolean o, boolean p) {
        public static final Codec<TestObject2> CODEC = CommentedCodec.commented(RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.BOOL.fieldOf("test1").forGetter(TestObject2::a),
                        Codec.BOOL.fieldOf("test2").forGetter(TestObject2::b),
                        Codec.BOOL.fieldOf("test3").forGetter(TestObject2::c),
                        Codec.BOOL.fieldOf("test4").forGetter(TestObject2::d),
                        Codec.BOOL.fieldOf("test5").forGetter(TestObject2::e),
                        Codec.BOOL.fieldOf("test6").forGetter(TestObject2::f),
                        Codec.BOOL.fieldOf("test7").forGetter(TestObject2::g),
                        Codec.BOOL.fieldOf("test8").forGetter(TestObject2::h),
                        Codec.BOOL.fieldOf("test9").forGetter(TestObject2::i),
                        Codec.BOOL.fieldOf("test10").forGetter(TestObject2::j),
                        Codec.BOOL.fieldOf("test11").forGetter(TestObject2::k),
                        Codec.BOOL.fieldOf("test12").forGetter(TestObject2::l),
                        Codec.BOOL.fieldOf("test13").forGetter(TestObject2::m),
                        Codec.BOOL.fieldOf("test14").forGetter(TestObject2::n),
                        Codec.BOOL.fieldOf("test15").forGetter(TestObject2::o),
                        Codec.BOOL.fieldOf("test16").forGetter(TestObject2::p)
                ).apply(instance, TestObject2::new)
        ), Comment.ifMapElseEmpty((object, key) -> {
            return switch (key) {
                case "test1" -> "Comment on string!";
                case "test7" -> "Comment on double!";
                case "test14" -> "Comment on list!";
                default -> "";
            };
        }));
    }
}
