/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.micode.notes.ui;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.micode.notes.R;
import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.TextNote;
import net.micode.notes.data.NotesDatabaseHelper;
import net.micode.notes.model.WorkingNote;
import net.micode.notes.model.WorkingNote.NoteSettingChangedListener;
import net.micode.notes.tool.DataUtils;
import net.micode.notes.tool.ResourceParser;
import net.micode.notes.tool.ResourceParser.TextAppearanceResources;
import net.micode.notes.ui.DateTimePickerDialog.OnDateTimeSetListener;
import net.micode.notes.ui.NoteEditText.OnTextViewChangeListener;
import net.micode.notes.widget.NoteWidgetProvider_2x;
import net.micode.notes.widget.NoteWidgetProvider_4x;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


//该类主要是针对标签的编辑
//继承了系统内部许多和监听有关的类

public class NoteEditActivity extends AppCompatActivity implements OnClickListener,
        NoteSettingChangedListener, OnTextViewChangeListener {
    private int mode;
    private android.media.MediaRecorder mMediaRecorder = new android.media.MediaRecorder();

    private class HeadViewHolder {
        public TextView tvModified;

        public ImageView ivAlertIcon;

        public TextView tvAlertDate;

        public ImageView ibSetBgColor;
    }

    //Map实现数据管理
    private static final Map<Integer, Integer> sBgSelectorBtnsMap = new HashMap<Integer, Integer>();
    static {
        sBgSelectorBtnsMap.put(R.id.iv_bg_yellow, ResourceParser.YELLOW);
        sBgSelectorBtnsMap.put(R.id.iv_bg_red, ResourceParser.RED);
        sBgSelectorBtnsMap.put(R.id.iv_bg_blue, ResourceParser.BLUE);
        sBgSelectorBtnsMap.put(R.id.iv_bg_green, ResourceParser.GREEN);
        sBgSelectorBtnsMap.put(R.id.iv_bg_white, ResourceParser.WHITE);
    }

    private static final Map<Integer, Integer> sBgSelectorSelectionMap = new HashMap<Integer, Integer>();
    static {
        sBgSelectorSelectionMap.put(ResourceParser.YELLOW, R.id.iv_bg_yellow_select);
        sBgSelectorSelectionMap.put(ResourceParser.RED, R.id.iv_bg_red_select);
        sBgSelectorSelectionMap.put(ResourceParser.BLUE, R.id.iv_bg_blue_select);
        sBgSelectorSelectionMap.put(ResourceParser.GREEN, R.id.iv_bg_green_select);
        sBgSelectorSelectionMap.put(ResourceParser.WHITE, R.id.iv_bg_white_select);
    }

    private static final Map<Integer, Integer> sFontSizeBtnsMap = new HashMap<Integer, Integer>();
    static {
        sFontSizeBtnsMap.put(R.id.ll_font_large, ResourceParser.TEXT_LARGE);
        sFontSizeBtnsMap.put(R.id.ll_font_small, ResourceParser.TEXT_SMALL);
        sFontSizeBtnsMap.put(R.id.ll_font_normal, ResourceParser.TEXT_MEDIUM);
        sFontSizeBtnsMap.put(R.id.ll_font_super, ResourceParser.TEXT_SUPER);
    }

    private static final Map<Integer, Integer> sFontSelectorSelectionMap = new HashMap<Integer, Integer>();
    static {
        sFontSelectorSelectionMap.put(ResourceParser.TEXT_LARGE, R.id.iv_large_select);
        sFontSelectorSelectionMap.put(ResourceParser.TEXT_SMALL, R.id.iv_small_select);
        sFontSelectorSelectionMap.put(ResourceParser.TEXT_MEDIUM, R.id.iv_medium_select);
        sFontSelectorSelectionMap.put(ResourceParser.TEXT_SUPER, R.id.iv_super_select);
    }

    private static final String TAG = "NoteEditActivity";
    //私有化一个界面操作mHeadViewPanel，对表头的操作
    private HeadViewHolder mNoteHeaderHolder;

    private View mHeadViewPanel;

    private View mNoteBgColorSelector;
    //私有化一个界面操作mFontSizeSelector，对标签字体的操作
    private View mFontSizeSelector;
    //声明编辑控件，对文本操作
    private EditText mNoteEditor;
    //私有化一个界面操作mNoteEditorPanel，文本编辑的控制板
    private View mNoteEditorPanel;
    //对模板WorkingNote的初始化
    private WorkingNote mWorkingNote;
    //私有化SharedPreferences的数据存储方式
    //它的本质是基于XML文件存储key-value键值对数据
    private SharedPreferences mSharedPrefs;
    private int mFontSizeId;

    private static final String PREFERENCE_FONT_SIZE = "pref_font_size";

    private static final int SHORTCUT_ICON_TITLE_MAX_LEN = 10;

    public static final String TAG_CHECKED = String.valueOf('\u221A');
    public static final String TAG_UNCHECKED = String.valueOf('\u25A1');

    private LinearLayout mEditTextList;

    private String mUserQuery;
    private Pattern mPattern;
    private final int PHOTO_REQUEST = 1;//请求码

    private static final int RECORD_REQUEST = 1987;//录音程序请求码
    private NoteEditText editText;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.note_edit);

        if (savedInstanceState == null && !initActivityState(getIntent())) {
            finish();
            return;
        }
        initResources();

        //在Activity后实现的点击图标,但是为了目录导航栏的统一,使用了AppCompatActivity的目录功能
//        //根据id获取添加图片按钮
//        final ImageButton add_img_btn = (ImageButton) findViewById(R.id.add_img_btn);
//        //为点击图片按钮设置监听器
//        add_img_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d(TAG, "onClick: click add image button");
//                //ACTION_GET_CONTENT: 允许用户选择特殊种类的数据，并返回（特殊种类的数据：照一张相片或录一段音）
//                Intent loadImage = new Intent(Intent.ACTION_GET_CONTENT);
//                //Category属性用于指定当前动作（Action）被执行的环境.
//                //CATEGORY_OPENABLE; 用来指示一个ACTION_GET_CONTENT的intent
//                loadImage.addCategory(Intent.CATEGORY_OPENABLE);
//                loadImage.setType("image/*");
//                startActivityForResult(loadImage, PHOTO_REQUEST);
//            }
//        });

        editText = findViewById(R.id.note_edit_view);
        textView = findViewById(R.id.text_num);
        // 添加文本改变监听器
        editText.addTextChangedListener(new TextWatcher() {
            int currentLength = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                textView.setText("字数：" + currentLength);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String processedText = removeImagesAndLinks(editText.getText().toString());
                currentLength = processedText.length();
            }

            @Override
            public void afterTextChanged(Editable s) {
                textView.setText("字数：" + currentLength);
            }
        });

        //添加一键删除的功能键
        Button clearButton = findViewById(R.id.clearButton); // 假设你的清屏按钮的 id 是 "clearButton"
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(""); // 清空文本内容
            }
        });


    }
    private String removeImagesAndLinks(String text) {
        // 剔除<img>标签
        text = text.replaceAll("<img[^>]*>", "");
        // 剔除链接
        text = text.replaceAll("<a\\b[^>]*>(.*?)</a>", "");
        // 剔除图片路径名称字符
        text = text.replaceAll("\\[local\\].*?\\[/local\\]", "");
        // 剔除换行符和空格
        text = text.replaceAll("\\s", "");
        return text;

//        StringBuffer stringBuffer = new StringBuffer(text);
//        // 剔除<img>标签
//
//        // 剔除链接
//
//        int Flag1 = -1;
//        int Flag2 = -1;
//        do {//不计入表示图片的字符
//            Flag1 = stringBuffer.indexOf("[local]");
//            Flag2 = stringBuffer.indexOf("[/local]");
//            if (Flag1 != -1 && Flag2 != -1) {
//                stringBuffer = stringBuffer.replace(Flag1, Flag2+1, "");
//            }
//        } while (Flag1 != -1 && Flag2 != -1);
//        do {//不计入换行字符
//            Flag1 = stringBuffer.indexOf("\n");
//            if (Flag1 != -1){
//                stringBuffer = stringBuffer.replace(Flag1, Flag1+1, "");
//            }
//        } while (Flag1 != -1);
//        do {//不计入空格字符
//            Flag1 = stringBuffer.indexOf(" ");
//            if (Flag1 != -1) {
//                stringBuffer = stringBuffer.replace(Flag1, Flag1+1, "");
//            }
//        } while (Flag1 != -1);
//        return stringBuffer.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.note_edit, menu);
        return true;
    }


    /**
     * Current activity may be killed when the memory is low. Once it is killed, for another time
     * user load this activity, we should restore the former state
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(Intent.EXTRA_UID)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.putExtra(Intent.EXTRA_UID, savedInstanceState.getLong(Intent.EXTRA_UID));
            if (!initActivityState(intent)) {
                finish();
                return;
            }
            Log.d(TAG, "Restoring from killed activity");
        }//为防止内存不足时程序的终止，在这里有一个保存现场的函数
    }

    private boolean initActivityState(Intent intent) {
        /**
         * If the user specified the {@link Intent#ACTION_VIEW} but not provided with id,
         * then jump to the NotesListActivity
         */
        mWorkingNote = null;
        if (TextUtils.equals(Intent.ACTION_VIEW, intent.getAction())) {
            long noteId = intent.getLongExtra(Intent.EXTRA_UID, 0);
            mUserQuery = "";

            /**
             * Starting from the searched result
             */
            if (intent.hasExtra(SearchManager.EXTRA_DATA_KEY)) {
                noteId = Long.parseLong(intent.getStringExtra(SearchManager.EXTRA_DATA_KEY));
                mUserQuery = intent.getStringExtra(SearchManager.USER_QUERY);
            }

            if (!DataUtils.visibleInNoteDatabase(getContentResolver(), noteId, Notes.TYPE_NOTE)) {
                Intent jump = new Intent(this, NotesListActivity.class);
                startActivity(jump);
                showToast(R.string.error_note_not_exist);
                finish();
                return false;
            } else {
                mWorkingNote = WorkingNote.load(this, noteId);
                if (mWorkingNote == null) {
                    Log.e(TAG, "load note failed with note id" + noteId);
                    finish();
                    return false;
                }
            }
            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                            | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        } else if(TextUtils.equals(Intent.ACTION_INSERT_OR_EDIT, intent.getAction())) {
            // New note
            long folderId = intent.getLongExtra(Notes.INTENT_EXTRA_FOLDER_ID, 0);
            int widgetId = intent.getIntExtra(Notes.INTENT_EXTRA_WIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            int widgetType = intent.getIntExtra(Notes.INTENT_EXTRA_WIDGET_TYPE,
                    Notes.TYPE_WIDGET_INVALIDE);
            int bgResId = intent.getIntExtra(Notes.INTENT_EXTRA_BACKGROUND_ID,
                    ResourceParser.getDefaultBgId(this));

            // Parse call-record note
            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            long callDate = intent.getLongExtra(Notes.INTENT_EXTRA_CALL_DATE, 0);
            if (callDate != 0 && phoneNumber != null) {
                if (TextUtils.isEmpty(phoneNumber)) {
                    Log.w(TAG, "The call record number is null");
                }
                long noteId = 0;
                if ((noteId = DataUtils.getNoteIdByPhoneNumberAndCallDate(getContentResolver(),
                        phoneNumber, callDate)) > 0) {
                    mWorkingNote = WorkingNote.load(this, noteId);
                    if (mWorkingNote == null) {
                        Log.e(TAG, "load call note failed with note id" + noteId);
                        finish();
                        return false;
                    }
                } else {
                    mWorkingNote = WorkingNote.createEmptyNote(this, folderId, widgetId,
                            widgetType, bgResId);
                    mWorkingNote.convertToCallNote(phoneNumber, callDate);
                }
            } else {
                mWorkingNote = WorkingNote.createEmptyNote(this, folderId, widgetId, widgetType,
                        bgResId);
            }

            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                            | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        //重点是这个判断条件,通过输入的字符进行展示
        else if(TextUtils.equals(Intent.ACTION_SEARCH, intent.getAction()))  //点击搜索按钮响应
        {
            String querystring = intent.getStringExtra(SearchManager.QUERY); //获取搜索框内的字符串
            NotesDatabaseHelper dbhelper = new NotesDatabaseHelper(this);
            List<String> list = dbhelper.dosearch(querystring);

            //完善判断逻辑，减少异常处理状态
            if(list.isEmpty()){
                Toast.makeText(NoteEditActivity.this, "搜索结果为空", Toast.LENGTH_SHORT).show();
                return false;
            }
            else {
                // 打印查询结果
                for (String result : list) {
                    System.out.println(result);
                }

                // 创建包含查询结果的Intent
                Intent showResultIntent = new Intent(NoteEditActivity.this, ShowResultActivity.class);
                showResultIntent.putStringArrayListExtra("searchResult", new ArrayList<>(list));

                // 启动ShowResultActivity来展示查询结果
                startActivity(showResultIntent);
                return false;
            }
        }
        else {
            Log.e(TAG, "Intent not specified action, should not support");
            finish();
            return false;
        }
        mWorkingNote.setOnSettingStatusChangedListener(this);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        initNoteScreen();
    }

    //设置外观
    private void initNoteScreen() {
        mNoteEditor.setTextAppearance(this, TextAppearanceResources
                .getTexAppearanceResource(mFontSizeId));
        if (mWorkingNote.getCheckListMode() == TextNote.MODE_CHECK_LIST) {
            switchToListMode(mWorkingNote.getContent());
        } else {
            mNoteEditor.setText(getHighlightQueryResult(mWorkingNote.getContent(), mUserQuery));
            mNoteEditor.setSelection(mNoteEditor.getText().length());
        }
        for (Integer id : sBgSelectorSelectionMap.keySet()) {
            findViewById(sBgSelectorSelectionMap.get(id)).setVisibility(View.GONE);
        }
        mHeadViewPanel.setBackgroundResource(mWorkingNote.getTitleBgResId());
        mNoteEditorPanel.setBackgroundResource(mWorkingNote.getBgColorResId());

        mNoteHeaderHolder.tvModified.setText(DateUtils.formatDateTime(this,
                mWorkingNote.getModifiedDate(), DateUtils.FORMAT_SHOW_DATE
                        | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME
                        | DateUtils.FORMAT_SHOW_YEAR));

        /**
         * TODO: Add the menu for setting alert. Currently disable it because the DateTimePicker
         * is not ready
         */
        showAlertHeader();

        //!注意这里需要将有图片的位置转换图片格式
        convertToImage();
    }


    //设置闹钟提醒
    private void showAlertHeader() {
        if (mWorkingNote.hasClockAlert()) {
            long time = System.currentTimeMillis();
            if (time > mWorkingNote.getAlertDate()) {
                mNoteHeaderHolder.tvAlertDate.setText(R.string.note_alert_expired);
            }  //如果系统时间大于了闹钟设置的时间，那么闹钟失效
            else {
                mNoteHeaderHolder.tvAlertDate.setText(DateUtils.getRelativeTimeSpanString(
                        mWorkingNote.getAlertDate(), time, DateUtils.MINUTE_IN_MILLIS));
            }
            mNoteHeaderHolder.tvAlertDate.setVisibility(View.VISIBLE);
            mNoteHeaderHolder.ivAlertIcon.setVisibility(View.VISIBLE);
        } else {
            mNoteHeaderHolder.tvAlertDate.setVisibility(View.GONE);
            mNoteHeaderHolder.ivAlertIcon.setVisibility(View.GONE);
        };
    }

    private void convertToImage() {
        NoteEditText noteEditText = findViewById(R.id.note_edit_view);
        Editable editable = noteEditText.getEditableText();

        String noteText = editable.toString();
        int length = editable.length();

        int cursorPositionBeforeInsert = noteEditText.getSelectionStart();
        // 在光标位置插入换行符

        noteEditText.setSelection(length);

        boolean inserted = false;
        for (int i = 0; i < length; i++) {
            for (int j = i; j < length; j++) {
                String img_fragment = noteText.substring(i, j + 1);
                if (img_fragment.length() > 15 && img_fragment.endsWith("[/local]") && img_fragment.startsWith("[local]")) {
                    int limit = 7;
                    int len = img_fragment.length() - 15;
                    String path = img_fragment.substring(limit, limit + len);
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    if (bitmap != null) {
                        int desiredHeight = 2000;
                        int originalWidth = bitmap.getWidth();
                        int originalHeight = bitmap.getHeight();
                        int desiredWidth = (originalWidth * desiredHeight) / originalHeight;

                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, desiredWidth, desiredHeight, false);

                        ImageSpan imageSpan = new ImageSpan(this, scaledBitmap);
                        String ss = "[local]" + path + "[/local]";
                        SpannableString spannableString = new SpannableString(ss);
                        spannableString.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // 设置居中对齐
                        spannableString.setSpan(imageSpan, 0, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        // 添加ClickableSpan，使得点击图片末尾的光标可以和图片对齐
                        ClickableSpan clickableSpan = new ClickableSpan() {
                            @Override
                            public void onClick(View view) {
                                int selectionEnd = noteEditText.getSelectionEnd();
                                if (selectionEnd == cursorPositionBeforeInsert) {
                                    noteEditText.setSelection(cursorPositionBeforeInsert + 1);
                                }
                            }
                        };
                        spannableString.setSpan(clickableSpan, ss.length(), ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        editable.replace(i, i + len + 15, spannableString);

                        inserted = true;
                        noteEditText.setSelection(cursorPositionBeforeInsert);

                        ClickableSpan[] spans = editable.getSpans(cursorPositionBeforeInsert, cursorPositionBeforeInsert, ClickableSpan.class);
                        if (spans != null && spans.length > 0) {
                            for (ClickableSpan span : spans) {
                                editable.removeSpan(span);
                            }
                        }
                    }
                }
            }
        }

        if (inserted) {
            editable.append("\n");
            // 在插入图片后为NoteEditText设置触摸事件监听器
            noteEditText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int action = event.getAction();
                    if (action == MotionEvent.ACTION_DOWN) {
                        int offset = noteEditText.getOffsetForPosition(event.getX(), event.getY());
                        ImageSpan[] imageSpans = editable.getSpans(0, editable.length(), ImageSpan.class);
                        for (ImageSpan span : imageSpans) {
                            int start = editable.getSpanStart(span);
                            int end = editable.getSpanEnd(span);
                            if (offset >= start && offset <= end) {
                                // 如果光标位于图片所在行，返回true，表示消费了该事件
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });

        }
    }






    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initActivityState(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在创建一个新的标签时，先在数据库中匹配
        //如果不存在，那么先在数据库中存储
        /**
         * For new note without note id, we should firstly save it to
         * generate a id. If the editing note is not worth saving, there
         * is no id which is equivalent to create new note
         */
        if (!mWorkingNote.existInDatabase()) {
            saveNote();
        }
        //在创建一个新的标签时，先在数据库中匹配
        //如果不存在，那么先在数据库中存储
        outState.putLong(Intent.EXTRA_UID, mWorkingNote.getNoteId());
        Log.d(TAG, "Save working note id: " + mWorkingNote.getNoteId() + " onSaveInstanceState");
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mNoteBgColorSelector.getVisibility() == View.VISIBLE
                && !inRangeOfView(mNoteBgColorSelector, ev)) {
            mNoteBgColorSelector.setVisibility(View.GONE);
            return true;
        }

        if (mFontSizeSelector.getVisibility() == View.VISIBLE
                && !inRangeOfView(mFontSizeSelector, ev)) {
            mFontSizeSelector.setVisibility(View.GONE);
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    //对屏幕触控的坐标范围进行操作
    private boolean inRangeOfView(View view, MotionEvent ev) {
        int []location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        if (ev.getX() < x
                || ev.getX() > (x + view.getWidth())
                || ev.getY() < y
                || ev.getY() > (y + view.getHeight())) {
                    return false;
                }
        return true;
    }

    private void initResources() {
        mHeadViewPanel = findViewById(R.id.note_title);
        mNoteHeaderHolder = new HeadViewHolder();
        mNoteHeaderHolder.tvModified = (TextView) findViewById(R.id.tv_modified_date);
        mNoteHeaderHolder.ivAlertIcon = (ImageView) findViewById(R.id.iv_alert_icon);
        mNoteHeaderHolder.tvAlertDate = (TextView) findViewById(R.id.tv_alert_date);
        mNoteHeaderHolder.ibSetBgColor = (ImageView) findViewById(R.id.btn_set_bg_color);
        mNoteHeaderHolder.ibSetBgColor.setOnClickListener(this);
        mNoteEditor = (EditText) findViewById(R.id.note_edit_view);
        mNoteEditorPanel = findViewById(R.id.sv_note_edit);
        mNoteBgColorSelector = findViewById(R.id.note_bg_color_selector);
        for (int id : sBgSelectorBtnsMap.keySet()) {
            ImageView iv = (ImageView) findViewById(id);
            iv.setOnClickListener(this);
        }

        mFontSizeSelector = findViewById(R.id.font_size_selector);
        for (int id : sFontSizeBtnsMap.keySet()) {
            View view = findViewById(id);
            view.setOnClickListener(this);
        };
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mFontSizeId = mSharedPrefs.getInt(PREFERENCE_FONT_SIZE, ResourceParser.BG_DEFAULT_FONT_SIZE);
        /**
         * HACKME: Fix bug of store the resource id in shared preference.
         * The id may larger than the length of resources, in this case,
         * return the {@link ResourceParser#BG_DEFAULT_FONT_SIZE}
         */
        if(mFontSizeId >= TextAppearanceResources.getResourcesSize()) {
            mFontSizeId = ResourceParser.BG_DEFAULT_FONT_SIZE;
        }
        mEditTextList = (LinearLayout) findViewById(R.id.note_edit_list);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(saveNote()) {
            Log.d(TAG, "Note data was saved with length:" + mWorkingNote.getContent().length());
        }
        clearSettingState();
    }

    //和桌面小工具的同步
    private void updateWidget() {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        if (mWorkingNote.getWidgetType() == Notes.TYPE_WIDGET_2X) {
            intent.setClass(this, NoteWidgetProvider_2x.class);
        } else if (mWorkingNote.getWidgetType() == Notes.TYPE_WIDGET_4X) {
            intent.setClass(this, NoteWidgetProvider_4x.class);
        } else {
            Log.e(TAG, "Unspported widget type");
            return;
        }

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {
            mWorkingNote.getWidgetId()
        });

        sendBroadcast(intent);
        setResult(RESULT_OK, intent);
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_set_bg_color) {
            mNoteBgColorSelector.setVisibility(View.VISIBLE);
            findViewById(sBgSelectorSelectionMap.get(mWorkingNote.getBgColorId())).setVisibility(
                    View.VISIBLE);
        } else if (sBgSelectorBtnsMap.containsKey(id)) {
            findViewById(sBgSelectorSelectionMap.get(mWorkingNote.getBgColorId())).setVisibility(
                    View.GONE);
            mWorkingNote.setBgColorId(sBgSelectorBtnsMap.get(id));
            mNoteBgColorSelector.setVisibility(View.GONE);
        } else if (sFontSizeBtnsMap.containsKey(id)) {
            findViewById(sFontSelectorSelectionMap.get(mFontSizeId)).setVisibility(View.GONE);
            mFontSizeId = sFontSizeBtnsMap.get(id);
            mSharedPrefs.edit().putInt(PREFERENCE_FONT_SIZE, mFontSizeId).commit();
            findViewById(sFontSelectorSelectionMap.get(mFontSizeId)).setVisibility(View.VISIBLE);
            if (mWorkingNote.getCheckListMode() == TextNote.MODE_CHECK_LIST) {
                getWorkingText();
                switchToListMode(mWorkingNote.getContent());
            } else {
                mNoteEditor.setTextAppearance(this,
                        TextAppearanceResources.getTexAppearanceResource(mFontSizeId));
            }
            mFontSizeSelector.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if(clearSettingState()) {
            return;
        }

        saveNote();
        super.onBackPressed();
    }

    private boolean clearSettingState() {
        if (mNoteBgColorSelector.getVisibility() == View.VISIBLE) {
            mNoteBgColorSelector.setVisibility(View.GONE);
            return true;
        } else if (mFontSizeSelector.getVisibility() == View.VISIBLE) {
            mFontSizeSelector.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    public void onBackgroundColorChanged() {
        findViewById(sBgSelectorSelectionMap.get(mWorkingNote.getBgColorId())).setVisibility(
                View.VISIBLE);
        mNoteEditorPanel.setBackgroundResource(mWorkingNote.getBgColorResId());
        mHeadViewPanel.setBackgroundResource(mWorkingNote.getTitleBgResId());
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isFinishing()) {
            return true;
        }
        clearSettingState();
        menu.clear();
        if (mWorkingNote.getFolderId() == Notes.ID_CALL_RECORD_FOLDER) {
            getMenuInflater().inflate(R.menu.call_note_edit, menu);
        } else {
            getMenuInflater().inflate(R.menu.note_edit, menu);
        }
        if (mWorkingNote.getCheckListMode() == TextNote.MODE_CHECK_LIST) {
            menu.findItem(R.id.menu_list_mode).setTitle(R.string.menu_normal_mode);
        } else {
            menu.findItem(R.id.menu_list_mode).setTitle(R.string.menu_list_mode);
        }
        if (mWorkingNote.hasClockAlert()) {
            menu.findItem(R.id.menu_alert).setVisible(false);
        } else {
            menu.findItem(R.id.menu_delete_remind).setVisible(false);
        }
//        //更换背景
//        if(mode==0){
//            menu.findItem(R.id.menu_hutao).setVisible(false);
//        }else if(mode==1){
//            menu.findItem(R.id.menu_keli).setVisible(false);
//        } else if (mode==2) {
//            menu.findItem(R.id.menu_moren).setVisible(false);
//        }

        return true;
    }

    //删除标签，修改字体大小
    //ToDo
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_new_note:
                createNewNote();
                break;
            case R.id.menu_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.alert_title_delete));
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.setMessage(getString(R.string.alert_message_delete_note));
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                deleteCurrentNote();
                                finish();
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.show();
                break;
            case R.id.menu_font_size:
                mFontSizeSelector.setVisibility(View.VISIBLE);
                findViewById(sFontSelectorSelectionMap.get(mFontSizeId)).setVisibility(View.VISIBLE);
                break;
            case R.id.menu_list_mode:
                mWorkingNote.setCheckListMode(mWorkingNote.getCheckListMode() == 0 ?
                        TextNote.MODE_CHECK_LIST : 0);
                break;
            case R.id.menu_share:
                getWorkingText();
                sendTo(this, mWorkingNote.getContent());
                break;
            case R.id.menu_send_to_desktop:
                sendToDesktop();
                break;
            case R.id.menu_alert:
                setReminder();
                break;
            case R.id.menu_delete_remind:
                mWorkingNote.setAlertDate(0, false);
                break;
            case R.id.menu_select_image:
                // 用户点击了选择图片菜单项
                Intent loadImage = new Intent(Intent.ACTION_GET_CONTENT);
                loadImage.addCategory(Intent.CATEGORY_OPENABLE);
                loadImage.setType("image/*");
                startActivityForResult(loadImage, PHOTO_REQUEST);
                break;
            case R.id.menu_hutao:
                mode=0;
                getWindow().setBackgroundDrawableResource(R.drawable.hutao);
                break;

            case R.id.menu_keli:
                mode=1;
                getWindow().setBackgroundDrawableResource(R.drawable.keli);
                break;

            case R.id.menu_moren:
                mode=2;
                getWindow().setBackgroundDrawableResource(R.drawable.mi1);
                break;
            case R.id.menu_insert_audio:
                Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                startActivityForResult(intent, RECORD_REQUEST);
            case R.id.join_password: {
                // 获取SharedPreferences对象
                SharedPreferences sharedPreferences = getSharedPreferences("NoteLock", MODE_PRIVATE);

                // 检查笔记是否未被锁定
                if (sharedPreferences.getBoolean("isLocked", false)) {
                    // 如果笔记未被锁定，弹出提示信息

                } else {
                    // 如果笔记没有被锁定，弹出确认对话框
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setTitle("重要提醒");
                    dialog.setMessage("您确认将此笔记加入笔记锁吗？");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 获取 SharedPreferences 中保存的密码
                            SharedPreferences prefs = getSharedPreferences("MyApp", MODE_PRIVATE);
                            final String savedPassword = prefs.getString("password", "");
                            if (!savedPassword.isEmpty()) {
                                // 如果密码存在，弹出一个对话框让用户输入密码
                                AlertDialog.Builder passwordDialog = new AlertDialog.Builder(NoteEditActivity.this);
                                passwordDialog.setTitle("输入密码");
                                final EditText input = new EditText(NoteEditActivity.this);
                                passwordDialog.setView(input);
                                passwordDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String enteredPassword = input.getText().toString();
                                        try {
                                            // 创建 MessageDigest 实例
                                            MessageDigest digest = MessageDigest.getInstance("SHA-256");
                                            // 生成哈希值
                                            byte[] hash = digest.digest(enteredPassword.getBytes(Charset.forName("UTF-8")));
                                            // 将字节转换为十六进制字符串
                                            StringBuilder hexString = new StringBuilder();
                                            for (byte b : hash) {
                                                String hex = Integer.toHexString(0xff & b);
                                                if (hex.length() == 1) hexString.append('0');
                                                hexString.append(hex);
                                            }
                                            // 获取输入密码的哈希值
                                            String enteredHashedPassword = hexString.toString();
                                            // 比较输入密码的哈希值与保存的哈希密码是否相同
                                            if (enteredHashedPassword.equals(savedPassword)) {
                                                // 如果密码正确，设置笔记被锁定
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putBoolean("isLocked", true);
                                                editor.apply();

                                                // 弹出提示信息
                                                Toast.makeText(NoteEditActivity.this, "笔记锁已添加", Toast.LENGTH_SHORT).show();
                                                // 显示笔记内容
                                                // 这里需要你自己实现显示笔记内容的逻辑
                                            } else {
                                                // 如果密码错误，提示用户
                                                Toast.makeText(NoteEditActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (NoSuchAlgorithmException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                passwordDialog.setNegativeButton("取消", null);
                                passwordDialog.show();
                            } else {
                                // 如果密码不存在，直接显示笔记内容
                                // 这里需要你自己实现显示笔记内容的逻辑
                            }
                        }
                    });
                    dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    });
                    dialog.show();
                }
                break;
            }

            case R.id.out_password:{

                // 获取SharedPreferences对象
                SharedPreferences sharedPreferences = getSharedPreferences("NoteLock", MODE_PRIVATE);

                // 检查笔记是否未被锁定
                if (!sharedPreferences.getBoolean("isLocked", false)) {
                    // 如果笔记未被锁定，弹出提示信息
                    Toast.makeText(NoteEditActivity.this, "该笔记未被锁定", Toast.LENGTH_SHORT).show();
                } else {
                    // 如果笔记被锁定，弹出确认对话框
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setTitle("重要提醒");
                    dialog.setMessage("您确认将此笔记删除笔记锁吗？");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 删除笔记锁
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("isLocked", false);
                            editor.apply();

                            // 弹出提示信息
                            Toast.makeText(NoteEditActivity.this, "笔记锁已删除", Toast.LENGTH_SHORT).show();
                            // 显示笔记内容
                            // 这里需要你自己实现显示笔记内容的逻辑
                        }
                    });
                    dialog.setNegativeButton("取消", null);
                    dialog.show();
                }
                break;
            }

            // 导出为文件 png、word、txt
            case R.id.menu_export_text:
                saveAsTxtFile();
                break;
            case R.id.menu_export_png:
                saveAsImageFile();
                break;
            case R.id.menu_export_doc:
                saveAsWordFile();
                break;

            default:
                break;

        }
        return true;
    }

    //TODO
    //添加提醒
    private void setReminder() {
        DateTimePickerDialog d = new DateTimePickerDialog(this, System.currentTimeMillis());
        d.setOnDateTimeSetListener(new OnDateTimeSetListener() {
            public void OnDateTimeSet(AlertDialog dialog, long date) {
                mWorkingNote.setAlertDate(date	, true);
            }
        });
        d.show();
    }

    /**
     * Share note to apps that support {@link Intent#ACTION_SEND} action
     * and {@text/plain} type
     */
    //TODO
    //分享
    private void sendTo(Context context, String info) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, info);
        intent.setType("text/plain");
        context.startActivity(intent);
    }

    // 新建标签
    //TODO
    private void createNewNote() {
        // Firstly, save current editing notes
        saveNote();

        // For safety, start a new NoteEditActivity
        finish();
        Intent intent = new Intent(this, NoteEditActivity.class);
        intent.setAction(Intent.ACTION_INSERT_OR_EDIT);
        intent.putExtra(Notes.INTENT_EXTRA_FOLDER_ID, mWorkingNote.getFolderId());
        startActivity(intent);
    }

    private void deleteCurrentNote() {
        if (mWorkingNote.existInDatabase()) {
            //假如当前运行的便签内存有数据
            HashSet<Long> ids = new HashSet<Long>();
            long id = mWorkingNote.getNoteId();
            if (id != Notes.ID_ROOT_FOLDER) {
                ids.add(id);
            } else {
                Log.d(TAG, "Wrong note id, should not happen");
            }
            if (!isSyncMode()) {
                if (!DataUtils.batchDeleteNotes(getContentResolver(), ids)) {
                    Log.e(TAG, "Delete Note error");
                }
            } else {
                if (!DataUtils.batchMoveToFolder(getContentResolver(), ids, Notes.ID_TRASH_FOLER)) {
                    Log.e(TAG, "Move notes to trash folder error, should not happens");
                }
            }
        }
        mWorkingNote.markDeleted(true);
    }

    private boolean isSyncMode() {
        return NotesPreferenceActivity.getSyncAccountName(this).trim().length() > 0;
    }

    public void onClockAlertChanged(long date, boolean set) {
        /**
         * User could set clock to an unsaved note, so before setting the
         * alert clock, we should save the note first
         */
        if (!mWorkingNote.existInDatabase()) {
            saveNote();
        }
        if (mWorkingNote.getNoteId() > 0) {
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.setData(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, mWorkingNote.getNoteId()));
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
            AlarmManager alarmManager = ((AlarmManager) getSystemService(ALARM_SERVICE));
            showAlertHeader();
            if(!set) {
                alarmManager.cancel(pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, date, pendingIntent);
            }
        } else {
            /**
             * There is the condition that user has input nothing (the note is
             * not worthy saving), we have no note id, remind the user that he
             * should input something
             */
            Log.e(TAG, "Clock alert setting error");
            showToast(R.string.error_note_empty_for_clock);
        }
    }

    public void onWidgetChanged() {
        updateWidget();
    }

    /*
     * 函数功能： 删除编辑文本框所触发的事件
     * 函数实现：如下注释
     */
    public void onEditTextDelete(int index, String text) {
        int childCount = mEditTextList.getChildCount();//没有编辑框的话直接返回
        if (childCount == 1) {
            return;
        }

        for (int i = index + 1; i < childCount; i++) {
            ((NoteEditText) mEditTextList.getChildAt(i).findViewById(R.id.et_edit_text))
                    .setIndex(i - 1);  //通过id把编辑框存在便签编辑框中
        }

        mEditTextList.removeViewAt(index); //删除特定位置的视图
        NoteEditText edit = null;
        if(index == 0) {
            edit = (NoteEditText) mEditTextList.getChildAt(0).findViewById(
                    R.id.et_edit_text);
        } else {
            edit = (NoteEditText) mEditTextList.getChildAt(index - 1).findViewById(
                    R.id.et_edit_text);
        }//通过id把编辑框存在空的NoteEditText中
        int length = edit.length();
        edit.append(text);
        edit.requestFocus();
        edit.setSelection(length);
    }

    /*
     * 函数功能：进入编辑文本框所触发的事件
     * 函数实现：如下注释
     */
    public void onEditTextEnter(int index, String text) {
        /**
         * Should not happen, check for debug
         */
        if(index > mEditTextList.getChildCount()) {
            Log.e(TAG, "Index out of mEditTextList boundrary, should not happen");
        }

        View view = getListItem(text, index);
        mEditTextList.addView(view, index);
        NoteEditText edit = (NoteEditText) view.findViewById(R.id.et_edit_text);
        edit.requestFocus();
        edit.setSelection(0);
        for (int i = index + 1; i < mEditTextList.getChildCount(); i++) {
            ((NoteEditText) mEditTextList.getChildAt(i).findViewById(R.id.et_edit_text))
                    .setIndex(i);
        }
    }
    /*
     * 函数功能：切换至列表模式
     * 函数实现：如下注释
     */
    private void switchToListMode(String text) {
        mEditTextList.removeAllViews();
        String[] items = text.split("\n");
        int index = 0;
        for (String item : items) {
            if(!TextUtils.isEmpty(item)) {
                mEditTextList.addView(getListItem(item, index));
                index++;
            }
        }
        mEditTextList.addView(getListItem("", index));
        mEditTextList.getChildAt(index).findViewById(R.id.et_edit_text).requestFocus();

        mNoteEditor.setVisibility(View.GONE);
        mEditTextList.setVisibility(View.VISIBLE);
    }

    /*
     * 函数功能：获取高亮效果的反馈情况
     * 函数实现：如下注释
     */
    private Spannable getHighlightQueryResult(String fullText, String userQuery) {
        SpannableString spannable = new SpannableString(fullText == null ? "" : fullText);
        if (!TextUtils.isEmpty(userQuery)) {
            mPattern = Pattern.compile(userQuery);
            Matcher m = mPattern.matcher(fullText);
            int start = 0;
            while (m.find(start)) {
                spannable.setSpan(
                        new BackgroundColorSpan(this.getResources().getColor(
                                R.color.user_query_highlight)), m.start(), m.end(),
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                start = m.end();
            }
        }
        return spannable;
    }

    //获取列表项
    private View getListItem(String item, int index) {
        View view = LayoutInflater.from(this).inflate(R.layout.note_edit_list_item, null);
        final NoteEditText edit = (NoteEditText) view.findViewById(R.id.et_edit_text);
        edit.setTextAppearance(this, TextAppearanceResources.getTexAppearanceResource(mFontSizeId));
        CheckBox cb = ((CheckBox) view.findViewById(R.id.cb_edit_item));
        cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    edit.setPaintFlags(edit.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    edit.setPaintFlags(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
                }
            }
        });

        if (item.startsWith(TAG_CHECKED)) {
            cb.setChecked(true);
            edit.setPaintFlags(edit.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            item = item.substring(TAG_CHECKED.length(), item.length()).trim();
        } else if (item.startsWith(TAG_UNCHECKED)) {
            cb.setChecked(false);
            edit.setPaintFlags(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
            item = item.substring(TAG_UNCHECKED.length(), item.length()).trim();
        }

        edit.setOnTextViewChangeListener(this);
        edit.setIndex(index);
        edit.setText(getHighlightQueryResult(item, mUserQuery));
        return view;
    }

    // 函数功能：便签内容发生改变所触发的事件
    public void onTextChange(int index, boolean hasText) {
        if (index >= mEditTextList.getChildCount()) {
            Log.e(TAG, "Wrong index, should not happen");
            return;
        }
        if(hasText) {
            mEditTextList.getChildAt(index).findViewById(R.id.cb_edit_item).setVisibility(View.VISIBLE);
        } else {
            mEditTextList.getChildAt(index).findViewById(R.id.cb_edit_item).setVisibility(View.GONE);
        }
        //如果内容不为空则将其子编辑框可见性置为可见，否则不可见
    }

    public void onCheckListModeChanged(int oldMode, int newMode) {
        if (newMode == TextNote.MODE_CHECK_LIST) {
            switchToListMode(mNoteEditor.getText().toString());
        } else {
            if (!getWorkingText()) {
                mWorkingNote.setWorkingText(mWorkingNote.getContent().replace(TAG_UNCHECKED + " ",
                        ""));
            }
            mNoteEditor.setText(getHighlightQueryResult(mWorkingNote.getContent(), mUserQuery));
            mEditTextList.setVisibility(View.GONE);
            mNoteEditor.setVisibility(View.VISIBLE);
            convertToImage(); //退出清单模式，应该将有图片的地方显示出来
        }
    }

    //TODO
    //分享，设置勾选选项表并返回是否勾选的标记
    private boolean getWorkingText() {
        boolean hasChecked = false;
        if (mWorkingNote.getCheckListMode() == TextNote.MODE_CHECK_LIST) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mEditTextList.getChildCount(); i++) {
                View view = mEditTextList.getChildAt(i);
                NoteEditText edit = (NoteEditText) view.findViewById(R.id.et_edit_text);
                if (!TextUtils.isEmpty(edit.getText())) {
                    if (((CheckBox) view.findViewById(R.id.cb_edit_item)).isChecked()) {
                        sb.append(TAG_CHECKED).append(" ").append(edit.getText()).append("\n");
                        hasChecked = true;
                    } else {
                        sb.append(TAG_UNCHECKED).append(" ").append(edit.getText()).append("\n");
                    }
                }
            }
            mWorkingNote.setWorkingText(sb.toString());
        } else {
            mWorkingNote.setWorkingText(mNoteEditor.getText().toString());
        }
        return hasChecked;
    }

    //保存注释
    private boolean saveNote() {
        getWorkingText();
        boolean saved = mWorkingNote.saveNote();
        if (saved) {
            /**
             * There are two modes from List view to edit view, open one note,
             * create/edit a node. Opening node requires to the original
             * position in the list when back from edit view, while creating a
             * new node requires to the top of the list. This code
             * {@link #RESULT_OK} is used to identify the create/edit state
             */
            //如英文注释所说链接RESULT_OK是为了识别保存的2种情况：
            // 一是创建后保存，二是修改后保存
            setResult(RESULT_OK);
        }
        return saved;
    }

    //TODO
    //发送到桌面
    private void sendToDesktop() {
        /**
         * Before send message to home, we should make sure that current
         * editing note is exists in databases. So, for new note, firstly
         * save it
         */
        if (!mWorkingNote.existInDatabase()) {
            saveNote();
        }//若不存在数据也就是新的标签就保存起来先

        if (mWorkingNote.getNoteId() > 0) {
            Intent sender = new Intent();
            //建立发送到桌面的连接器
            Intent shortcutIntent = new Intent(this, NoteEditActivity.class);
            //链接是一个视图
            shortcutIntent.setAction(Intent.ACTION_VIEW);
            shortcutIntent.putExtra(Intent.EXTRA_UID, mWorkingNote.getNoteId());
            sender.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            sender.putExtra(Intent.EXTRA_SHORTCUT_NAME,
                    makeShortcutIconTitle(mWorkingNote.getContent()));
            sender.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(this, R.drawable.icon_app));
            ////将便签的相关信息都添加到要发送的文件里
            sender.putExtra("duplicate", true);
            //设置sneder的行为是发送
            sender.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            //显示到桌面
            showToast(R.string.info_note_enter_desktop);
            sendBroadcast(sender);
        } else {
            /**
             * There is the condition that user has input nothing (the note is
             * not worthy saving), we have no note id, remind the user that he
             * should input something
             */
            Log.e(TAG, "Send to desktop error");
            showToast(R.string.error_note_empty_for_send_to_desktop);
        }
    }

    /*
     * 函数功能：编辑小图标的标题
     * 函数实现：如下注释
     * 直接设置为content中的内容并返回，有勾选和未勾选2种
     */
    private String makeShortcutIconTitle(String content) {
        content = content.replace(TAG_CHECKED, "");
        content = content.replace(TAG_UNCHECKED, "");
        return content.length() > SHORTCUT_ICON_TITLE_MAX_LEN ? content.substring(0,
                SHORTCUT_ICON_TITLE_MAX_LEN) : content;
    }

    private void showToast(int resId) {
        showToast(resId, Toast.LENGTH_SHORT);
    }

    private void showToast(int resId, int duration) {
        Toast.makeText(this, resId, duration).show();
    }


    //重写onActivityResult()来处理返回的数据
    //直接采用简单的方法类返回数据
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        ContentResolver resolver = getContentResolver();
        switch (requestCode) {
            case PHOTO_REQUEST:
                Uri originalUri = intent.getData(); //1.获得图片的真实路径
                Bitmap bitmap = null;
                try {
                    bitmap = BitmapFactory.decodeStream(resolver.openInputStream(originalUri));//2.解码图片
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "onActivityResult: get file_exception");
                    e.printStackTrace();
                }

                if(bitmap != null){
                    //3.根据Bitmap对象创建ImageSpan对象
                    Log.d(TAG, "onActivityResult: bitmap is not null");
                    ImageSpan imageSpan = new ImageSpan(NoteEditActivity.this, bitmap);
                    String path = getPath(this,originalUri);
                    //4.使用[local][/local]将path括起来，用于之后方便识别图片路径在note中的位置
                    String img_fragment= "[local]" + path + "[/local]";
                    //创建一个SpannableString对象，以便插入用ImageSpan对象封装的图像
                    SpannableString spannableString = new SpannableString(img_fragment);
                    spannableString.setSpan(imageSpan, 0, img_fragment.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    //5.将选择的图片追加到EditText中光标所在位置
                    NoteEditText e = (NoteEditText) findViewById(R.id.note_edit_view);
                    int index = e.getSelectionStart(); //获取光标所在位置
                    Log.d(TAG, "Index是: " + index);
                    Editable edit_text = e.getEditableText();
                    edit_text.insert(index, spannableString); //将图片插入到光标所在位置

//
//                    // 添加以下代码来在图片插入后自动换行
//                    edit_text.insert(index + spannableString.length(), "\n");// 在图片后添加一个换行符
//
//                    // 更新光标
//                    e.setSelection(index + spannableString.length() + 1); // 将光标移动到换行符后面

                    mWorkingNote.mContent = e.getText().toString();
                    mWorkingNote.mContent = mWorkingNote.mContent.replaceAll("(?m)^[ \t]*\r?\n", ""); // 删除空白行


//                    //6.把改动提交到数据库中,两个数据库表都要改的
//                    ContentResolver contentResolver = getContentResolver();
//                    ContentValues contentValues = new ContentValues();
//                    final long id = mWorkingNote.getNoteId();
//                    contentValues.put("snippet",mWorkingNote.mContent);
//                    contentResolver.update(Uri.parse("content://micode_notes/note"), contentValues,"_id=?",new String[]{""+id});
//                    ContentValues contentValues1 = new ContentValues();
//                    contentValues1.put("content",mWorkingNote.mContent);
//                    contentResolver.update(Uri.parse("content://micode_notes/data"), contentValues1,"mime_type=? and note_id=?", new String[]{"vnd.android.cursor.item/text_note",""+id});

                }else{
                    Toast.makeText(NoteEditActivity.this, "获取图片失败", Toast.LENGTH_SHORT).show();
                }
                break;
            case RECORD_REQUEST:
                Uri audioUri = intent.getData();
                Button playButton = findViewById(R.id.button_play_audio);
                playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 在按钮点击事件中处理播放音频的逻辑
                        playAudio(audioUri);
                    }
                });
                break;
            default:
                break;
        }
    }
    private void playAudio(Uri audioUri) {
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(getApplicationContext(), audioUri);
            mediaPlayer.prepare();
            mediaPlayer.start();

            // 当音频播放完成后，可以在需要的地方添加监听器
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // 音频播放完成后的操作
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            // 处理播放音频时的异常情况
        }
    }
    //获取文件的real path
    public String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
//            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else
            if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // Media
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    //获取数据列_获取此 Uri 的数据列的值。这对MediaStore Uris 和其他基于文件的 ContentProvider。
    public String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
    //是否为外部存储文件
    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    //是否为下载文件
    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
    //是否为媒体文件
    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private void saveAsTxtFile() {
        NoteEditText noteEditText = findViewById(R.id.note_edit_view);
        Editable editable = noteEditText.getEditableText();
        String noteText = editable.toString();
        try {
            // 创建文件名
            String fileName = noteText.substring(0,3)+".txt";

            // 保存到文件管理器的download目录中
            File publicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            if (publicDirectory != null) {
                // 创建保存文件的完整路径
                File file = new File(publicDirectory, fileName);

                // 创建文件输出流
                FileOutputStream outputStream = new FileOutputStream(file);

                // 写入文本内容
                outputStream.write(noteText.getBytes());

                // 关闭文件输出流
                outputStream.close();

                // 获取保存文件的完整路径
                String filePath = file.getAbsolutePath();
                Toast.makeText(this, "成功保存到：" + filePath, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveAsImageFile() {
        NoteEditText noteEditText = findViewById(R.id.note_edit_view);
        Editable editable = noteEditText.getEditableText();
        String noteText = editable.toString();
        try {
            // 创建文件名
            String fileName = noteText.substring(0,3)+".png";

            // 保存到文件管理器的download目录中
            File publicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            if (publicDirectory != null) {
                // 创建保存文件的完整路径
                File file = new File(publicDirectory, fileName);

                // 创建Bitmap对象
                Bitmap bitmap = Bitmap.createBitmap(noteEditText.getWidth(), noteEditText.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                noteEditText.draw(canvas);

                // 创建文件输出流
                FileOutputStream outputStream = new FileOutputStream(file);

                // 将Bitmap保存为PNG格式的图片
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

                // 关闭文件输出流0
                outputStream.close();

                // 获取保存文件的完整路径
                String filePath = file.getAbsolutePath();
                Toast.makeText(this, "成功保存到：" + filePath, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveAsWordFile() {
        NoteEditText noteEditText = findViewById(R.id.note_edit_view);
        Editable editable = noteEditText.getEditableText();
        String noteText = editable.toString();
        try {
            // 创建文件名
            String fileName = noteText.substring(0,3)+".doc";

            // 保存到文件管理器的download目录中
            File publicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            if (publicDirectory != null) {
                // 创建保存文件的完整路径
                File file = new File(publicDirectory, fileName);

                // 创建文件输出流
                FileOutputStream outputStream = new FileOutputStream(file);

                // 写入文本内容
                outputStream.write(noteText.getBytes());

                // 关闭文件输出流
                outputStream.close();

                // 获取保存文件的完整路径
                String filePath = file.getAbsolutePath();
                Toast.makeText(this, "成功保存到：" + filePath, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }


}
