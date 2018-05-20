package com.kibisoftware.chatbotexample.chatui;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.kibisoftware.chatbotexample.MainActivity;
import com.kibisoftware.chatbotexample.server.MessageServer;

public class ConversationDivider extends RecyclerView.ItemDecoration {

    private Drawable mDivider;
    private MainActivity activity;

    public ConversationDivider(Drawable divider, MainActivity activity) {
        mDivider = divider;
        this.activity = activity;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        int position = parent.getChildAdapterPosition(view);
        // skip the very first item
        if (parent.getChildAdapterPosition(view) == 0) {
            return;
        }

        // check list items if the item's step number is 2 (start of a conversation)
        // and the previous item's step number was 5 (end of a conversation), put in a divider
        // we have protected against checking before the first value
        if (activity.getItemStep(parent.getChildAdapterPosition(view)) == MessageServer.Step.STEPTWO.getValue() &&
                activity.getItemStep(parent.getChildAdapterPosition(view) - 1) == MessageServer.Step.STEPFIVE.getValue()) {
            outRect.top = mDivider.getIntrinsicHeight();
        } else {
            outRect.setEmpty();
        }
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        int dividerLeft = parent.getPaddingLeft();
        int dividerRight = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int dividerTop = child.getBottom() + params.bottomMargin;
            int dividerBottom = dividerTop + mDivider.getIntrinsicHeight();

            mDivider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom);

            mDivider.draw(canvas);
        }
    }

}
