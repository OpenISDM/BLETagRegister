package com.ossf.www.bletagregister;

import android.app.Activity;
import android.os.Environment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import static com.ossf.www.bletagregister.HomeActivity.regDevice_list;

/*
    class : FileStream
    description : input and output string from ble.txt
    author : Cynthia
    date : 2018.08.01
 */

public class FileStream extends Activity{
    File extDir; // now path
    File fullFilename;

    public FileStream() throws IOException {
        // get connect with ble.txt
        extDir = Environment.getExternalStorageDirectory();
        String filename = "ble.txt";
        fullFilename = new File(extDir, filename);
        fullFilename.createNewFile();
        fullFilename.setWritable(Boolean.TRUE);
    }

    // write data parameter into ble.txt
    public void write(String data) throws IOException {
        FileWriter fw = new FileWriter(extDir+"/ble.txt",true);
        fw.write(data);
        fw.close();
    }

    // read data and put it into regDevice_list
    public void readRegList() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fullFilename));
        String line;
        while ((line = br.readLine()) != null) {
            //mac=tokens[0]  reg_name=tokens[1]
            String[] tokens = line.split(" ");
            BLEdevice ble=new BLEdevice(tokens[1],tokens[0]);
            regDevice_list.put(tokens[0],ble);
        }
    }
}
