package dhyces.janksonfixerupper.serialization;

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.ListCodec;
import dhyces.janksonfixerupper.serialization.comments.Comment;
import dhyces.janksonfixerupper.serialization.comments.ListSensitiveComment;
import dhyces.janksonfixerupper.serialization.comments.MapSensitiveComment;
import dhyces.janksonfixerupper.serialization.wrappers.CommentedJsonArray;
import dhyces.janksonfixerupper.serialization.wrappers.CommentedJsonObject;
import dhyces.janksonfixerupper.serialization.wrappers.CommentedJsonPrimitive;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CommentedCodec<A> implements Codec<A> {
    private final Codec<A> wrapped;
    private final Comment comment;

    private CommentedCodec(Codec<A> wrapped, Comment comment) {
        this.wrapped = wrapped;
        this.comment = comment;
    }

    public static <A> CommentedCodec<A> commented(Codec<A> codec, String comment) {
        return commented(codec, Comment.constant(comment));
    }

    public static <A> CommentedCodec<A> commented(Codec<A> codec, Comment comment) {
        return new CommentedCodec<>(codec, comment);
    }

    public static <A> Codec<List<A>> commentedCommentedList(Codec<A> codec, Comment commentOnList, Consumer<ListSensitiveComment.Builder> elementCommentConsumer) {
        return commented(commentedList(codec, elementCommentConsumer), commentOnList);
    }

    public static <A> Codec<List<A>> commentedList(Codec<A> codec, Consumer<ListSensitiveComment.Builder> elementCommentConsumer) {
        ListSensitiveComment.Builder builder = new ListSensitiveComment.Builder();
        elementCommentConsumer.accept(builder);
        return Codec.list(new CommentedCodec<>(codec, builder.build()));
    }

    public static <A, V> CommentedCodec<Map<A, V>> commentedMap(Codec<Map<A, V>> codec, Consumer<MapSensitiveComment.Builder> elementCommentConsumer) {
        MapSensitiveComment.Builder builder = new MapSensitiveComment.Builder();
        elementCommentConsumer.accept(builder);
        return new CommentedCodec<>(codec, builder.build());
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
        private Comment comment;
        public CommentedObject(A obj, Comment comment) {
            this.obj = obj;
            this.comment = comment;
        }
        public static <A> CommentedObject<A> of(A obj) {
            return new CommentedObject<>(obj, (container, value) -> "");
        }
    }
}
