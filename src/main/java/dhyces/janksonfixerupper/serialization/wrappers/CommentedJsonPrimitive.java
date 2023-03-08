package dhyces.janksonfixerupper.serialization.wrappers;

import blue.endless.jankson.JsonPrimitive;

import javax.annotation.Nonnull;

public class CommentedJsonPrimitive extends JsonPrimitive implements Commented {
    private String comment;

    public CommentedJsonPrimitive(@Nonnull Object value, String comment) {
        super(value);
        this.comment = comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }
}
