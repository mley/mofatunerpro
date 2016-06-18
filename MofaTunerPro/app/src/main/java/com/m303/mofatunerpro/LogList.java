package com.m303.mofatunerpro;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnLogListFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class LogList extends Fragment {


    private OnLogListFragmentInteractionListener mListener;

    private Storage storage;
    private List<String> logEntries;

    LogEntryAdapter adapter;

    public LogList() {

        storage = Storage.instance();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        logEntries = new ArrayList<>(storage.getDataSetsNames());

        adapter = new LogEntryAdapter(getContext(), logEntries);

        ListView listView = (ListView) getView().findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String fileName = adapter.getItem(position);
                mListener.onLogListFragmentInteraction(fileName);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_log_list, container, false);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLogListFragmentInteractionListener) {
            mListener = (OnLogListFragmentInteractionListener) context;
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

    public void addListItem(final String fileName) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logEntries.add(0, fileName);
                adapter.notifyDataSetChanged();
            }
        });

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnLogListFragmentInteractionListener {
        void onLogListFragmentInteraction(String fileName);
    }


    public class LogEntryAdapter extends ArrayAdapter<String> {
        public LogEntryAdapter(Context context, List<String> entries) {
            super(context, 0, entries);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            String entry = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_entry, parent, false);
            }
            // Lookup view for data population
            TextView title = (TextView) convertView.findViewById(R.id.fileNameText);
                       // Populate the data into the template view using the data object
            title.setText(entry);

            // Return the completed view to render on screen
            return convertView;
        }
    }

}
