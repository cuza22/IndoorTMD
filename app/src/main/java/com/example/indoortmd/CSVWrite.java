package com.example.indoortmd;

import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class CSVWrite {
    public CSVWrite() { }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void writeCsv(List<String[]> data, String filename) {
        //파일 저장 경로 설정
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TMDData";
        //디렉토리 없으면 생성
        File dir = new File(dirPath);
        if(!dir.exists()){dir.mkdir();}


        try {
            CSVWriter cw = new CSVWriter(new FileWriter(dir + "/" + filename), ',', '"');
            Iterator<String[]> it = data.iterator();
            try {
                while (it.hasNext()) {
                    String[] s = (String[]) it.next();
                    cw.writeNext(s);
                }
            } finally {
                cw.close();
            }
        } catch (IOException e) {
            Log.e("LOG", "Can't Save");
            e.printStackTrace();
        }
    }

}
