package net.micode.notes.data;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class DataFetch extends AppCompatActivity {
    /***
     * 判断文件是否存在
     * @param strFile
     * @return
     */
    public boolean fileIsExists(Context context, String strFile) {
        boolean fileExistFlag;
        try{
            context.openFileInput(strFile);
            fileExistFlag = true;
        } catch (FileNotFoundException e){
            fileExistFlag = false;
        }
        return fileExistFlag;
    }

    /**
     * 文件内容写函数
     * @param context
     * @param file_name
     * @param key
     */
    public void writeFile(Context context, String file_name, String key){
        try {
            FileOutputStream fos = context.openFileOutput(file_name, Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            osw.write(key);
            osw.flush();
            fos.flush();  //输出缓冲区中所有的内容
            osw.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件内容读取函数
     * @param context
     * @param filename
     */
    public String readFile(Context context, String filename) {
        try {
            FileInputStream fis = context.openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            char[] input = new char[fis.available()];  //available()用于获取filename内容的长度,但是对中文有问题，建议使用BufferReader
            isr.read(input);  //读取并存储到input中
            isr.close();
            fis.close();//读取完成后关闭
            String str = new String(input);
            return str;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }

}
