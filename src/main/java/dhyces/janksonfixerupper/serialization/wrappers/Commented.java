package dhyces.janksonfixerupper.serialization.wrappers;

import blue.endless.jankson.JsonElement;
import dhyces.janksonfixerupper.serialization.comments.Comment;

public interface Commented {
    Comment getComment();
    void setComment(Comment comment);

    default String resolveComment(JsonElement container, JsonElement key) {
        return getComment().apply(container, key);
    }
}
