package dhyces.janksonfixerupper.serialization;

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dhyces.janksonfixerupper.serialization.wrappers.CommentedJsonArray;
import dhyces.janksonfixerupper.serialization.wrappers.CommentedJsonObject;
import dhyces.janksonfixerupper.serialization.wrappers.CommentedJsonPrimitive;

public class CommentedCodec<A> implements Codec<A> {
    private final Codec<A> wrapped;
    private final String comment;

    private CommentedCodec(Codec<A> wrapped, String comment) {
        this.wrapped = wrapped;
        this.comment = comment;
    }

    public static <A> CommentedCodec<A> commented(Codec<A> codec, String comment) {
        return new CommentedCodec<>(codec, comment);
    }

    @Override
    public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
        return wrapped.decode(ops, input);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
        // Check ops, so we don't wrap stuff that won't be operated on
        if (!(ops instanceof JanksonOps)) {
            return wrapped.encode(input, ops, prefix);
        }

        DataResult<T> result = wrapped.encode(input, ops, prefix);
        return (DataResult<T>)result.map(t -> {
            if (t instanceof JsonPrimitive primitive) {
                return new CommentedJsonPrimitive(primitive.getValue(), comment);
            }
            if (t instanceof JsonArray array) {
                return new CommentedJsonArray(array, comment);
            }
            if (t instanceof JsonObject obj) {
                return new CommentedJsonObject(obj, comment);
            }
            return t;
        });
    }


    public static class CommentedObject<A> {
        private A obj;
        private String comment;
        public CommentedObject(A obj, String comment) {
            this.obj = obj;
            this.comment = comment;
        }
        public static <A> CommentedObject<A> of(A obj) {
            return new CommentedObject<>(obj, "");
        }
    }
}
