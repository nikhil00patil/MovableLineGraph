package com.android.linechart;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private MovablePointsLineGraph mGraphView;
    private ArrayList<String> mVerticalList;
    private ArrayList<String> mHorizontalList;
    private RelativeLayout mToolTipView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FrameLayout lContainer = findViewById(R.id.graph_container);
        mToolTipView = findViewById(R.id.rl_tooltip);
        initializeGraph();
        lContainer.addView(mGraphView);
    }

    //Initialize the points with your data (now using temp data)
    private void initializeGraph() {
        mHorizontalList = new ArrayList<>();
        mVerticalList = new ArrayList<>();
        for (int i = 1; i < 21; i++) {
            mHorizontalList.add(String.valueOf(i));
            if (i < 9)
                mVerticalList.add(String.valueOf(i));
        }

        ArrayList<PointsModel> pointsToDisplay = new ArrayList<>();
        PointsModel lModel = new PointsModel("1", "16");
        pointsToDisplay.add(lModel);
        lModel = new PointsModel("2", "10");
        pointsToDisplay.add(lModel);
        lModel = new PointsModel("3", "13");
        pointsToDisplay.add(lModel);
        lModel = new PointsModel("4", "15");
        pointsToDisplay.add(lModel);
        lModel = new PointsModel("5", "11");
        pointsToDisplay.add(lModel);
        lModel = new PointsModel("6", "3");
        pointsToDisplay.add(lModel);
        lModel = new PointsModel("7", "2");
        pointsToDisplay.add(lModel);
        lModel = new PointsModel("8", "6");
        pointsToDisplay.add(lModel);

        ArrayList<Point> list = new ArrayList<>();
        Collections.reverse(mHorizontalList);
        for (int i = 0; i < pointsToDisplay.size(); i++) {
            Point point = new Point((mVerticalList.indexOf(pointsToDisplay.get(i).getHours()) + 1), (mHorizontalList.indexOf(pointsToDisplay.get(i).getTemperature()) + 1));
            list.add(point);
        }
        Collections.reverse(mHorizontalList);
        mGraphView = new MovablePointsLineGraph(this, null, mVerticalList, mHorizontalList, list);
        mGraphView.setToolTip(mToolTipView);//Remove this if don't want tooltip
        mGraphView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }
}
