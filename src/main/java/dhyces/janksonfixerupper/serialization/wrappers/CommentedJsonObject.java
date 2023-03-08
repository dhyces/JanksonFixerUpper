package dhyces.janksonfixerupper.serialization.wrappers;

import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;

import java.util.Map;

public class CommentedJsonObject extends JsonObject implements Commented {
    private String comment;

    public CommentedJsonObject(JsonObject object, String comment) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            put(entry.getKey(), entry.getValue());
            if (object.getComment(entry.getKey()) != null) {
                setComment(entry.getKey(), object.getComment(entry.getKey()));
            }
        }
        this.comment = comment;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public void setComment(String comment) {
        this.comment = comment;
    }
}
