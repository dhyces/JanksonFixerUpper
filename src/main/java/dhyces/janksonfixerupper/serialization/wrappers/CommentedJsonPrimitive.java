package dhyces.janksonfixerupper.serialization.wrappers;

import blue.endless.jankson.JsonPrimitive;
import dhyces.janksonfixerupper.serialization.comments.Comment;

import javax.annotation.Nonnull;

public class CommentedJsonPrimitive extends JsonPrimitive implements Commented {
    private Comment comment;

    public CommentedJsonPrimitive(@Nonnull Object value, Comment comment) {
        super(value);
        this.comment = comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public Comment getComment() {
        return comment;
    }
}
