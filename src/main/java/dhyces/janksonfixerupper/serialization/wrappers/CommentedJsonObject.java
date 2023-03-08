package dhyces.janksonfixerupper.serialization.wrappers;

import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import dhyces.janksonfixerupper.serialization.comments.Comment;

import java.util.Map;

public class CommentedJsonObject extends JsonObject implements Commented {
    private Comment comment;

    public CommentedJsonObject(JsonObject object, Comment comment) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            put(entry.getKey(), entry.getValue());
            if (object.getComment(entry.getKey()) != null) {
                setComment(entry.getKey(), object.getComment(entry.getKey()));
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
