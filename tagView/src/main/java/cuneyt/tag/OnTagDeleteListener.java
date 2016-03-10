package cuneyt.tag;

/**
 * listener for tag delete
 */
public interface OnTagDeleteListener {
    void onTagDeleted(TagView view, Tag tag, int position);
}