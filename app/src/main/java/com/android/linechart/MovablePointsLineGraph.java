package com.android.linechart;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class MovablePointsLineGraph extends View {

    final int mImageWidth = 60, mImageHeight = 60;
    boolean isPointMoved = false;
    private Context mContext;
    private int mCellWidth = 80;
    private int mCellHeight = 30;
    private int mPrevX, mPrevY;
    private boolean mCanImageMove;
    private ArrayList<Rect> mArrayRect;
    private ArrayList<Region> mArrayRegion;
    private int mSelectedRegionIndex = -1;
    private int mVerticalLines, mHorizontalLines;
    private ArrayList<String> mArrayVertical, mArrayHorizontal;
    private Paint mTextPaint;
    private Paint mPointPaint;
    private Paint mPointConnectPaint;
    private Paint mTouchPointPaint;
    private Paint mDottedDarkLinePaint;
    private int mTouchPointStroke = 12;
    private int mScreenWidth;
    private RelativeLayout mToolTipView;
    private boolean isToolTipVisible = false;

    public MovablePointsLineGraph(Context pContext) {
        super(pContext);
    }

    public MovablePointsLineGraph(Context pContext, AttributeSet pAttrs, ArrayList<String> pVerticalLines, ArrayList<String> pHorizontalLines, ArrayList<Point> pArrayPoints) {
        super(pContext, pAttrs);
        this.mContext = pContext;
        this.mVerticalLines = pVerticalLines.size();
        this.mHorizontalLines = pHorizontalLines.size();
        this.mArrayVertical = pVerticalLines;
        this.mArrayHorizontal = pHorizontalLines;
        initPoints();
        setPointsSizeAndRegion(pArrayPoints);
        initPaint();
    }

    public void setToolTip(RelativeLayout pToolTipView) {
        this.mToolTipView = pToolTipView;
        isToolTipVisible = true;
    }

    private void initPoints() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int mScreenHeight = displayMetrics.heightPixels;
        mScreenWidth = displayMetrics.widthPixels;
        float margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        mCellWidth = (mScreenWidth / mVerticalLines) - (int) margin;
        mCellHeight = (mScreenHeight / mHorizontalLines) - (int) margin;
    }

    private void initPaint() {
        //Line Paint
        Paint mLinePaint = new Paint();
        mLinePaint.setStrokeWidth(3);
        mLinePaint.setColor(getContext().getResources().getColor(R.color.dotted_dark_line_color));

        mDottedDarkLinePaint = new Paint();
        mDottedDarkLinePaint.setStrokeWidth(3);
        mDottedDarkLinePaint.setStyle(Paint.Style.STROKE);
        mDottedDarkLinePaint.setColor(getContext().getResources().getColor(R.color.dotted_dark_line_color));
        mDottedDarkLinePaint.setPathEffect(new DashPathEffect(new float[]{3f, 3f}, 0));

        //Text Paint
        mTextPaint = new Paint();
        mTextPaint.setColor(getContext().getResources().getColor(R.color.text_color));
        float textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics());
        mTextPaint.setTextSize(textSize);
        mTextPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));

        //point paint
        mPointPaint = new Paint();
        mPointPaint.setColor(getContext().getResources().getColor(R.color.point_connecting_line_color));

        //connecting lines paint
        mPointConnectPaint = new Paint();
        mPointConnectPaint.setStrokeWidth(4);
        mPointConnectPaint.setColor(getContext().getResources().getColor(R.color.point_connecting_line_color));

        mTouchPointPaint = new Paint();
        mTouchPointPaint.setColor(Color.TRANSPARENT);
    }


    public void setPointsSizeAndRegion(ArrayList<Point> pArrayPoints) {
        mArrayRect = new ArrayList<>();
        mArrayRegion = new ArrayList<>();
        for (int i = 0; i < pArrayPoints.size(); i++) {
            Rect lRect = getRect(pArrayPoints.get(i));
            mArrayRect.add(lRect);
            Region lRegion = new Region(lRect);
            mArrayRegion.add(lRegion);
        }
    }

    public Rect getRect(Point pPoint) {
        return new Rect((mCellWidth * pPoint.x) - (mImageWidth / 2),
                mCellHeight * Math.abs(pPoint.y - mHorizontalLines - 1),
                (mCellWidth * pPoint.x) + (mImageWidth / 2),
                (mCellHeight * Math.abs(pPoint.y - mHorizontalLines - 1)) + mImageHeight);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int lPositionX = (int) event.getX();
        int lPositionY = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                for (int i = 0; i < mArrayRegion.size(); i++) {
                    if (mArrayRegion.get(i).contains(lPositionX, lPositionY)) {
                        mSelectedRegionIndex = i;
                        if (isToolTipVisible) {
                            if (lPositionX > (mScreenWidth / 2))
                                mToolTipView.setX(lPositionX - mToolTipView.getWidth());
                            else
                                mToolTipView.setX(lPositionX + (mCellWidth / 2f));
                            mToolTipView.setY(lPositionY + mCellHeight);
                            updateTooltipData();
                            mToolTipView.setVisibility(VISIBLE);
                        }
                        mPrevX = lPositionX;
                        mPrevY = lPositionY;
                        mCanImageMove = true;
                        mTouchPointStroke = mCellWidth / 2;
                        isPointMoved = false;
                        invalidate();
                    }
                }
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                if (mCanImageMove && mSelectedRegionIndex != -1) {
                    int deltaX = lPositionX - mPrevX;
                    int deltaY = lPositionY - mPrevY;
                    if ((mArrayRect.get(mSelectedRegionIndex).left + deltaX) > 0 && ((mArrayRect.get(mSelectedRegionIndex).right + deltaX) < 10000)
                            && (mArrayRect.get(mSelectedRegionIndex).top + deltaY) > (mCellHeight / 1.5) && ((mArrayRect.get(mSelectedRegionIndex).bottom + deltaY) < ((mCellHeight * mHorizontalLines + 2) + ((mImageHeight))))) {
                        mArrayRect.get(mSelectedRegionIndex).top = mArrayRect.get(mSelectedRegionIndex).top + deltaY;
                        mArrayRect.get(mSelectedRegionIndex).bottom = mArrayRect.get(mSelectedRegionIndex).top + mImageHeight;
                        mArrayRegion.set(mSelectedRegionIndex, new Region(mArrayRect.get(mSelectedRegionIndex)));
                        if (isToolTipVisible) {
                            mToolTipView.setY(lPositionY + mCellHeight);
                            updateTooltipData();
                        }
                        mPrevX = lPositionX;
                        mPrevY = lPositionY;
                        isPointMoved = true;
                        invalidate();
                    }
                }
            }
            break;
            case MotionEvent.ACTION_UP:
                if (mSelectedRegionIndex != -1) {
                    int modX = mArrayRect.get(mSelectedRegionIndex).top % mCellHeight;
                    if (modX >= (mCellHeight / 2)) {
                        mArrayRect.get(mSelectedRegionIndex).top = mArrayRect.get(mSelectedRegionIndex).top + (mCellHeight - modX);
                        mArrayRect.get(mSelectedRegionIndex).bottom = mArrayRect.get(mSelectedRegionIndex).bottom + (mCellHeight - modX);
                    } else {
                        mArrayRect.get(mSelectedRegionIndex).top = mArrayRect.get(mSelectedRegionIndex).top - (modX);
                        mArrayRect.get(mSelectedRegionIndex).bottom = mArrayRect.get(mSelectedRegionIndex).bottom - (modX);
                    }
                    mArrayRegion.set(mSelectedRegionIndex, new Region(mArrayRect.get(mSelectedRegionIndex)));
                    mCanImageMove = false;
                    mTouchPointStroke = 12;
                    if (isToolTipVisible)
                        mToolTipView.setVisibility(INVISIBLE);
                    mSelectedRegionIndex = -1;
                    invalidate();
                }
                break;
        }
        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {

        //horizontal lines
        for (int i = 1; i < mHorizontalLines + 1; i++) {

            canvas.drawLine(mCellWidth - (mCellWidth / 8f),
                    (mCellHeight * i) + (mImageHeight / 2f),
                    (mCellWidth * (mVerticalLines)) + (mCellWidth / 8f),
                    (mCellHeight * i) + (mImageHeight / 2f), mDottedDarkLinePaint);
        }

        //vertical lines
        for (int i = 1; i < mVerticalLines + 1; i++) {
            canvas.drawLine(mCellWidth * i,
                    mCellHeight + (mImageHeight / 2f),
                    mCellWidth * i,
                    (mCellHeight * (mHorizontalLines)) + (mImageHeight / 2f), mDottedDarkLinePaint);
        }

        //draw points
        for (int i = 0; i < mArrayRect.size(); i++) {
            if (mSelectedRegionIndex == i) {
                mPointPaint.setColor(getContext().getResources().getColor(R.color.point_touch_color));
                canvas.drawCircle(mArrayRect.get(i).centerX(), mArrayRect.get(i).centerY(), mTouchPointStroke, mPointPaint);
            } else {
                mPointPaint.setColor(getContext().getResources().getColor(R.color.point_connecting_line_color));
                canvas.drawCircle(mArrayRect.get(i).centerX(), mArrayRect.get(i).centerY(), 12, mPointPaint);
            }
            canvas.drawRect(mArrayRect.get(i), mTouchPointPaint);
        }

        //draw line between points
        for (int i = 0; i < mArrayRect.size() - 1; i++) {
            canvas.drawLine(mArrayRect.get(i).centerX(), mArrayRect.get(i).centerY(), mArrayRect.get(i + 1).centerX(), mArrayRect.get(i).centerY(), mPointConnectPaint);
            canvas.drawLine(mArrayRect.get(i + 1).centerX(), mArrayRect.get(i).centerY(), mArrayRect.get(i + 1).centerX(), mArrayRect.get(i + 1).centerY(), mPointConnectPaint);
        }

        //draw vertical text
        for (int i = 0; i < mArrayVertical.size(); i++) {
            canvas.drawText(mArrayVertical.get(i), mCellWidth + ((mCellWidth * i) - 10), (mCellHeight * (mHorizontalLines + 2)) + (mImageHeight / 2f), mTextPaint);
        }
        //draw horizontal text
        for (int i = 0; i < mArrayHorizontal.size(); i++) {
            canvas.drawText(mArrayHorizontal.get(i), 0, mCellHeight + ((mCellHeight * i) + 10 + (mImageHeight / 2)), mTextPaint);
        }
    }

    //You can receive the graph points from this method
    public ArrayList<Point> getGraphPoint() {
        ArrayList<Point> list = new ArrayList<>();
        for (int i = 0; i < mArrayRect.size(); i++) {
            Point lPoint = new Point(Math.abs(mArrayRect.get(i).centerX() / mCellWidth), Math.abs(((mArrayRect.get(i).centerY() - (mImageHeight / 2)) / mCellHeight)));
            list.add(lPoint);
        }
        return list;
    }

    private String[] getClickedPoints() {
        String[] str = new String[2];
        str[0] = mArrayVertical.get(Math.abs(mArrayRect.get(mSelectedRegionIndex).centerX() / mCellWidth) - 1) + "";
        str[1] = mArrayHorizontal.get(Math.abs(((mArrayRect.get(mSelectedRegionIndex).centerY() - (mImageHeight / 2)) / mCellHeight) - 1)) + "";
        return str;
    }

    private void updateTooltipData() {
        updateTooltip(getClickedPoints());
    }

    public void updateTooltip(String[] points) {
        ((TextView) mToolTipView.findViewById(R.id.tv_xAxis)).setText(points[1]);
        ((TextView) mToolTipView.findViewById(R.id.tv_yAxis)).setText(points[0]);
    }

}