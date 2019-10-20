package it.angelic.soulissclient.helpers;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class ContextMenuRecyclerView extends RecyclerView {

  private RecyclerContextMenuInfo mContextMenuInfo;

    public ContextMenuRecyclerView(Context context) {
        super(context);
    }

    public ContextMenuRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ContextMenuRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
  protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
    return mContextMenuInfo;
  }

  @Override
  public boolean showContextMenuForChild(View originalView) {
    final int longPressPosition = getChildPosition(originalView);
    if (longPressPosition >= 0) {
        final long longPressId = getAdapter().getItemId(longPressPosition);
        mContextMenuInfo = new RecyclerContextMenuInfo(longPressPosition, longPressId);
        return super.showContextMenuForChild(originalView);
    }
    return false;
  }

  public static class RecyclerContextMenuInfo implements ContextMenu.ContextMenuInfo {

    public RecyclerContextMenuInfo(int position, long id) {
        this.position = position;
        this.id = id;
    }

    final public int position;
    final public long id;
  }
}