package net.micode.notes.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.micode.notes.R;

import java.util.List;

public class ShowResultActivity extends AppCompatActivity {
    /**
     * 进行绑定视图，还未实现具体便签得跳转。后续加上
     */
    private String TAG="ShowResultActivity";

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_list);
        Log.e(TAG,"search");
        ListView listView = findViewById(R.id.listview); // 找到 ListView

        Intent intent = getIntent();
        if (intent != null) {
            List<String> searchResult = intent.getStringArrayListExtra("searchResult");
            if (searchResult != null && !searchResult.isEmpty()) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, searchResult);
                listView.setAdapter(adapter);  // 使用 ArrayAdapter 设置查询结果到 ListView
            }
        }
    }
}