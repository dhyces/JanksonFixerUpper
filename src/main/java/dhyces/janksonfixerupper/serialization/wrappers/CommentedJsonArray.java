package dhyces.janksonfixerupper.serialization.wrappers;

import blue.endless.jankson.JsonArray;
import dhyces.janksonfixerupper.serialization.comments.Comment;

public class CommentedJsonArray extends JsonArray implements Commented {
    private Comment comment;

    public CommentedJsonArray(JsonArray array, Comment comment) {
        for (int i = 0; i < array.size(); i++) {
            add(array.get(i));
            if (array.getComment(i) != null) {
                setComment(i, array.getComment(i));
            }
        }
        this.comment = comment;
    }

    @Override
    public Comment getComment() {
        return comment;
    }

    @Override
    public void setComment(Comment comment) {
        this.comment = comment;
    }
}
