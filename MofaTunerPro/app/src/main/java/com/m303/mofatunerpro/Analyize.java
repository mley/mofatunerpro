package com.m303.mofatunerpro;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnAnalyzeFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Analyize#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Analyize extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String LOG_ENTRY = "logEntry";

    private String entry;

    private OnAnalyzeFragmentInteractionListener mListener;

    NumberFormat timeFormat = new DecimalFormat("###.0");
    NumberFormat valueFormat = new DecimalFormat("0.000");

    public Analyize() {
        // Required empty public constructor
    }


    public static Analyize newInstance(String param1) {
        Analyize fragment = new Analyize();
        Bundle args = new Bundle();
        args.putString(LOG_ENTRY, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            entry = getArguments().getString(LOG_ENTRY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_analyize, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        showLogEntry(entry);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAnalyzeFragmentInteractionListener) {
            mListener = (OnAnalyzeFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void showLogEntry(String fileName) {
        if(fileName == null) {
            return;
        }

        List<LogValue> dataSet = Storage.instance().getDataSet(fileName);

        if(dataSet == null || dataSet.isEmpty()) {
            return;
        }

        View view = getView();

        if(view == null ) {
            return;
        }

        LineChart chart = (LineChart) view.findViewById(R.id.chart);


        ArrayList<Entry> throttle = new ArrayList<Entry>();
        ArrayList<Entry> lambda = new ArrayList<Entry>();
        ArrayList<Entry> rpm = new ArrayList<Entry>();

        ArrayList<String> xVals = new ArrayList<String>();
        int i=0;
        for(LogValue v : dataSet) {
            Entry e;

            e = new Entry(v.throttle, i);
            throttle.add(e);

            e = new Entry(v.lambda, i);
            lambda.add(e);

            e = new Entry(v.rpm, i);
            rpm.add(e);

            xVals.add(timeFormat.format(v.millis/1000.0));
            i++;
        }

        ValueFormatter vf = new ValueFormatter() {
            @Override
            public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
                return valueFormat.format(v);
            }
        };

        LineDataSet throttleData = new LineDataSet(throttle, "Throttle");
        throttleData.setDrawCircleHole(false);
        throttleData.setDrawCircles(false);
        throttleData.setAxisDependency(YAxis.AxisDependency.LEFT);
        throttleData.setColor(Color.DKGRAY);
        throttleData.setValueFormatter(vf);

        LineDataSet lambdaData = new LineDataSet(lambda, "Lambda");
        lambdaData.setDrawCircleHole(false);
        lambdaData.setDrawCircles(false);
        lambdaData.setAxisDependency(YAxis.AxisDependency.LEFT);
        lambdaData.setColor(Color.GREEN);
        lambdaData.setValueFormatter(vf);

        LineDataSet rpmData = new LineDataSet(rpm, "RPM");
        rpmData.setDrawCircleHole(false);
        rpmData.setDrawCircles(false);
        rpmData.setAxisDependency(YAxis.AxisDependency.RIGHT);
        rpmData.setColor(Color.MAGENTA);


        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(throttleData);
        dataSets.add(lambdaData);
        dataSets.add(rpmData);


        LineData data = new LineData(xVals, dataSets);
        //chart.getAxisRight().setAxisMinValue(0f);
        //chart.getAxisRight().setAxisMinValue(15000f);

        chart.getAxisLeft().setAxisMinValue(0f);
        chart.getAxisLeft().setAxisMaxValue(1.3f);

        chart.setDescription(fileName);
        chart.setData(data);
        chart.invalidate();
    }


    public interface OnAnalyzeFragmentInteractionListener {
        void onAnalyzeFragmentInteraction(Uri uri);
    }
}
