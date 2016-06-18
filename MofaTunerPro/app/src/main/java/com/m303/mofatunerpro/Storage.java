package com.m303.mofatunerpro;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by mley on 15.06.16.
 */
public class Storage {

    public static final DateFormat DF = new SimpleDateFormat("yyyy.MM.dd_HH:mm:ss");

    private File dataDir;

    private List<String> dataSetsNames;

    private static Storage instance = new Storage();

    public static Storage instance() {
        return instance;
    }

    private Storage() {
        dataDir = new File("/storage/emulated/legacy/mofatunerpro");
        dataDir.mkdirs();
        if (!dataDir.isDirectory() || !dataDir.canWrite()) {
            throw new RuntimeException("cannot write to data dir: "+dataDir.getAbsolutePath());
        }

        dataSetsNames = new ArrayList<>();
        for (File f : dataDir.listFiles()) {
            dataSetsNames.add(f.getName());
        }

        Collections.sort(dataSetsNames);
        Collections.reverse(dataSetsNames);
    }

    public List<String> getDataSetsNames() {
        return dataSetsNames;
    }

    public List<LogValue> getDataSet(String name) {
        File f = new File(dataDir, name);

        List<LogValue> result = new ArrayList<>();

        try {
            BufferedReader r = new BufferedReader(new FileReader(f));
            String l;
            while((l = r.readLine()) != null) {
                result.add(new LogValue(l));
            }
            r.close();
        } catch (FileNotFoundException e) {
            Log.e("Storage", "error reading data set", e);
        } catch (IOException e) {
            Log.e("Storage", "error reading data set", e);
        }

        return result;
    }

    public void saveDataSet(String name, List<LogValue> data) {
        File f = new File(dataDir, name);
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(f));
            for(LogValue v: data) {
                String s = v.toString();
                w.write(s, 0, s.length());
                w.newLine();
            }
            w.close();
            dataSetsNames.add(0, name);
        } catch (IOException e) {
            Log.e("Storage", "error writing data set", e);
        }
    }
}
