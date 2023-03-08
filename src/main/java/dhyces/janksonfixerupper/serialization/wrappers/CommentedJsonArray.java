package dhyces.janksonfixerupper.serialization.wrappers;

import blue.endless.jankson.JsonArray;

public class CommentedJsonArray extends JsonArray implements Commented {
    private String comment;

    public CommentedJsonArray(JsonArray array, String comment) {
        for (int i = 0; i < array.size(); i++) {
            add(array.get(i));
            if (array.getComment(i) != null) {
                setComment(i, array.getComment(i));
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
