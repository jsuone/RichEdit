package com.example.sample.activity;

import static com.baidu.aip.asrwakeup3.core.recog.IStatus.STATUS_FINISHED;
import static com.baidu.aip.asrwakeup3.core.recog.IStatus.STATUS_LONG_SPEECH_FINISHED;
import static com.baidu.aip.asrwakeup3.core.recog.IStatus.STATUS_NONE;
import static com.baidu.aip.asrwakeup3.core.recog.IStatus.STATUS_READY;
import static com.baidu.aip.asrwakeup3.core.recog.IStatus.STATUS_RECOGNITION;
import static com.baidu.aip.asrwakeup3.core.recog.IStatus.STATUS_SPEAKING;
import static com.baidu.aip.asrwakeup3.core.recog.IStatus.STATUS_STOPPED;
import static com.baidu.aip.asrwakeup3.core.recog.IStatus.STATUS_WAITING_READY;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.asrwakeup3.core.mini.AutoCheck;
import com.baidu.aip.asrwakeup3.core.recog.MyRecognizer;
import com.baidu.aip.asrwakeup3.core.recog.listener.IRecogListener;
import com.baidu.aip.asrwakeup3.core.recog.listener.MessageStatusRecogListener;
import com.baidu.aip.asrwakeup3.core.util.AuthUtil;
import com.baidu.speech.asr.SpeechConstant;
import com.example.richeditor.EditorOpMenuView;
import com.example.richeditor.OnContentChangeListener;
import com.example.richeditor.OnInsertImageListener;
import com.example.richeditor.OnJSFocusChangeListener;
import com.example.richeditor.OnSpeechRecogListener;
import com.example.richeditor.RichEditor;
import com.example.sample.Impl.OnNoteCompileSelectedTitleListener;
import com.example.sample.R;
import com.example.sample.model.NoteModel;
import com.example.sample.util.ACache;
import com.example.sample.util.Constant;
import com.example.sample.util.DataManager;
import com.example.sample.util.ImageUtils;
import com.example.sample.widget.SelectedTitleDialog;
import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddNoteActivity extends AppCompatActivity implements View.OnClickListener, OnNoteCompileSelectedTitleListener {

    private EditText note_theme_edit;
    private TextView note_all_title;
    private ImageButton note_add_cancel;
    private ImageButton note_add_complete;
    private RichEditor editor;
    private EditorOpMenuView editorOpMenuView;
    private int REQUEST_CODE_PICK = 1;
    private String selectedTitleGUID;//经过弹窗被选择的主题的数据
    private String firstTitleGUID;//对笔记进行修改前的主题
    private NoteModel noteModel;
    private List<String> FirstPath;//笔记初始化内容里的图片路径
    private List<String> SecondPath;//每次插入图片的路径
    private List<String> EndPath;//笔记保存时内容里的图片路径 是删除一些图片之后的
    private Integer flag_module_int = 1;//0是增加 1 是修改
    private ACache aCache;
    private String uuid;
    /**
     * 识别控制器，使用MyRecognizer控制识别的流程
     */
    protected MyRecognizer myRecognizer;
    protected Handler handler;
    private Gson gson;
    /**
     * 控制UI按钮的状态
     */
    protected int status;
    private final static String TAG = "notedata";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        initData();
        initView();
    }
    void initData(){
        //如果有intent传递数据说明是修改，初始化数据
        gson = new Gson();
        FirstPath = new ArrayList<>();
        SecondPath = new ArrayList<>();
        EndPath = new ArrayList<>();
        Intent intent = getIntent();
        String flag_module = intent.getStringExtra("note_manage");
        aCache = ACache.get(this);
        if(flag_module.equals("add")){
            flag_module_int = 0;
            uuid = UUID.randomUUID().toString();
        }else if(flag_module.equals("modify")){
            flag_module_int = 1;
            noteModel = gson.fromJson(intent.getStringExtra("note_data"),NoteModel.class);
            firstTitleGUID = noteModel.getTitle_guid();
            uuid = noteModel.getGuid();

        }
    }
    void initView(){
        note_theme_edit = findViewById(R.id.note_theme_edit);
        note_all_title = findViewById(R.id.note_all_title);
        editor = findViewById(R.id.note_editor);
        editorOpMenuView = findViewById(R.id.note_editor_op_menu_view);
        note_add_cancel = findViewById(R.id.note_add_cancel);
        note_add_complete = findViewById(R.id.note_add_complete);


        editor.setPlaceholder("请填写文章正文内容（必填）"); //设置占位文字
        editor.setEditorFontSize(16); //设置文字大小
        editor.setPadding(10, 10, 10, 10); //设置编辑器内边距
        editor.setBackgroundColor(getResources().getColor(R.color.white)); //设置编辑器背景色
        editor.setOnTextChangeListener(new OnContentChangeListener() {
            @Override
            public void onContentChange(String text) { //输入文本回调监听

            }
        });
        editor.requestFocus();
        //绑定编辑器
        editorOpMenuView.setRichEditor(editor);
        editorOpMenuView.setOnInsertImageListener(new OnInsertImageListener() {
            @Override
            public void openGallery() {
                Intent getImage = new Intent(Intent.ACTION_PICK);
                getImage.setType("image/*");
                startActivityForResult(getImage,REQUEST_CODE_PICK);
            }
        });
        note_theme_edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    if(note_add_complete.getVisibility()==View.GONE || editorOpMenuView.getVisibility()==View.GONE){
                        note_add_complete.setVisibility(View.VISIBLE);
                        editorOpMenuView.setVisibility(View.VISIBLE);
                    }
                    //将富文本设置不可用
                    editorOpMenuView.setEnable(false);
                }
            }
        });
        editor.setOnJSFocusChangeListener(new OnJSFocusChangeListener() {
            @Override
            public void onFocusChange(Boolean hasFocus) {
                if(hasFocus){
                    if(note_add_complete.getVisibility()==View.GONE || editorOpMenuView.getVisibility()==View.GONE){
                        note_add_complete.setVisibility(View.VISIBLE);
                        editorOpMenuView.setVisibility(View.VISIBLE);
                    }
                    //设置富文本可用
                    editorOpMenuView.setEnable(true);
                }else{
                    //失去焦点
                    //Toast.makeText(AddNoteActivity.this,"失去焦点",Toast.LENGTH_SHORT).show();
                }
            }
        });
        if(flag_module_int==0){//创建
            selectedTitleGUID = DataManager.getInstance().getTitleData(Constant.Title_KEY).getGuid();
            note_all_title.setText(DataManager.getInstance().getTitleData(Constant.Title_KEY).getTitle());
        }else{//修改
            selectedTitleGUID = noteModel.getTitle_guid();
            note_all_title.setText(DataManager.getInstance().getTitlePathByTitleGUID(noteModel.getTitle_guid()));
            note_theme_edit.setText(noteModel.getTheme());
            editor.setText(aCache.getAsString(noteModel.getGuid()));
            //隐藏方便查看，当用户点击标题或者内容想要编辑才显示
            note_add_complete.setVisibility(View.GONE);
            editorOpMenuView.setVisibility(View.GONE);


        }

        //editor.setHtml(aCache.);
        initImgPathList(FirstPath);



        status = STATUS_NONE;
        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                handleMsg(msg);
            }
        };
        // 基于DEMO集成第1.1, 1.2, 1.3 步骤 初始化EventManager类并注册自定义输出事件
        // DEMO集成步骤 1.2 新建一个回调类，识别引擎会回调这个类告知重要状态和识别结果
        IRecogListener listener = new MessageStatusRecogListener(handler);
        // DEMO集成步骤 1.1 1.3 初始化：new一个IRecogListener示例 & new 一个 MyRecognizer 示例,并注册输出事件
        myRecognizer = new MyRecognizer(this, listener);
        editorOpMenuView.setOnSpeechRecogListener(new OnSpeechRecogListener() {
            @Override
            public void onASRChange() {
                switch (status) {
                    case STATUS_NONE: // 初始状态
                        start();
                        status = STATUS_WAITING_READY;

                        break;
                    case STATUS_WAITING_READY: // 调用本类的start方法后，即输入START事件后，等待引擎准备完毕。
                    case STATUS_READY: // 引擎准备完毕。
                    case STATUS_SPEAKING: // 用户开始讲话
                    case STATUS_FINISHED: // 一句话识别语音结束
                    case STATUS_RECOGNITION: // 识别中
                        stop();
                        status = STATUS_STOPPED; // 引擎识别中

                        break;
                    case STATUS_LONG_SPEECH_FINISHED: // 长语音识别结束
                    case STATUS_STOPPED: // 引擎识别中
                        cancel();
                        status = STATUS_NONE; // 识别结束，回到初始状态

                        break;
                    default:
                        break;
                }
            }
        });
        note_all_title.setOnClickListener(this);
        note_add_cancel.setOnClickListener(this);
        note_add_complete.setOnClickListener(this);

    }
    public void initImgPathList(List<String> path){

        Document doc = Jsoup.parse(editor.getContent());
        // 获取所有图片标签
        Elements imgTags = doc.select("img");
        if(imgTags.size()!=0){
            for (Element imgTag : imgTags) {
                path.add(imgTag.attr("src"));
                Log.d(TAG, "initImgPathList: 在添加修改活动中 初始化当前图片集合"+imgTag.attr("src"));
            }
        }
    }
    void handleMsg(Message msg){
        switch (msg.what) { // 处理MessageStatusRecogListener中的状态回调
            case STATUS_FINISHED:
                if (msg.arg2 == 1) {
                    //txtResult.setText(msg.obj.toString());
                    editor.setHtml(msg.obj.toString());
                }
                status = msg.what;

                break;
            case STATUS_NONE:
            case STATUS_READY:
            case STATUS_SPEAKING:
            case STATUS_RECOGNITION:
                status = msg.what;

                break;
            default:
                break;

        }
    }
    /**
     * 开始录音，点击“开始”按钮后调用。
     * 基于DEMO集成2.1, 2.2 设置识别参数并发送开始事件
     */
    protected void start() {
        // DEMO集成步骤2.1 拼接识别参数： 此处params可以打印出来，直接写到你的代码里去，最终的json一致即可。
        final Map<String, Object> params = fetchParams();
        // params 也可以根据文档此处手动修改，参数会以json的格式在界面和logcat日志中打印
        Log.i(TAG, "设置的start输入参数：" + params);
        // 复制此段可以自动检测常规错误
        (new AutoCheck(getApplicationContext(), new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainErrorMessage(); // autoCheck.obtainAllMessage();
                        //txtLog.append(message + "\n");
                        ; // 可以用下面一行替代，在logcat中查看代码
                        Log.w("AutoCheckMessage", message);
                    }
                }
            }
        }, false)).checkAsr(params);

        // 这里打印出params， 填写至您自己的app中，直接调用下面这行代码即可。
        // DEMO集成步骤2.2 开始识别
        myRecognizer.start(params);
    }

    /**
     * 开始录音后，手动点击“停止”按钮。
     * SDK会识别不会再识别停止后的录音。
     * 基于DEMO集成4.1 发送停止事件 停止录音
     */
    protected void stop() {

        myRecognizer.stop();
    }
    protected Map<String, Object> fetchParams() {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT,"0");
        params.put(SpeechConstant.PID,"15372");
        params.put(SpeechConstant.BDS_ASR_ENABLE_LONG_SPEECH,true);
        params.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT,"0");
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME,false);
        params.put(SpeechConstant.APP_ID, AuthUtil.getAppId()); // 添加appId
        params.put(SpeechConstant.APP_KEY, AuthUtil.getAk()); // 添加apiKey
        params.put(SpeechConstant.SECRET, AuthUtil.getSk()); // 添加secretKey
        //  集成时不需要上面的代码，只需要params参数。
        return params;
    }
    /**
     * 开始录音后，手动点击“取消”按钮。
     * SDK会取消本次识别，回到原始状态。
     * 基于DEMO集成4.2 发送取消事件 取消本次识别
     */
    protected void cancel() {

        myRecognizer.cancel();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_PICK&&resultCode == RESULT_OK &&data != null){
            Uri selectedImage = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedImage);
                /*getContentResolver()方法用于获取ContentResolver对象，访问应用程序的内容提供者（包括系统提供的和应用程序自己创建的）*/
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                String path = ImageUtils.saveImageToStorage(this,bitmap,uuid);
                SecondPath.add(path);
                editorOpMenuView.insertImage(path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void onDestroy() {
        // 如果之前调用过myRecognizer.loadOfflineEngine()， release()里会自动调用释放离线资源
        // 基于DEMO5.1 卸载离线资源(离线时使用) release()方法中封装了卸载离线资源的过程
        // 基于DEMO的5.2 退出事件管理器
        myRecognizer.release();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.note_add_cancel:
                //清除已经保存的图片
                if(SecondPath.size()!=0){
                    for (String s : SecondPath) {
                        Log.d(TAG, "添加笔记中: 当前笔记图片路径"+s);
                       ImageUtils.deleteImageFile(s);
                    }
                }
                SecondPath.clear();
                finish();
                break;
            case R.id.note_add_complete:
                if(note_theme_edit.getText().toString().equals("")){
                    Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show();
                    break;
                }
                addCompleteFinish();
                break;
            case R.id.note_all_title:
                //进行主题选择
                SelectedTitleDialog dialog;
                if(noteModel == null&&selectedTitleGUID==null){
                  dialog = new SelectedTitleDialog(this,
                            DataManager.getInstance().getTitleData(Constant.Title_KEY).getGuid());
                }else{
                    //dialog = new SelectedTitleDialog(this,noteModel.getTitle_guid());
                    dialog = new SelectedTitleDialog(this,selectedTitleGUID);
                }
                dialog.getWindow().setGravity(Gravity.CENTER);
                dialog.show();


        }
    }

    @Override
    public void getSelectedData(Map<String, String> data) {

        for (Map.Entry<String, String> entry : data.entrySet()) {
            selectedTitleGUID = entry.getKey(); // 获取键
            String value = entry.getValue(); // 获取值
            note_all_title.setText(value);
        }

    }
    private void addCompleteFinish(){
        initImgPathList(EndPath);
        //将保存的图片路径进行比较 删除不必要的图片
        FirstPath.addAll(SecondPath);
        FirstPath.removeAll(EndPath);
        if(FirstPath.size()!=0){

            for (String s : FirstPath) {
                Log.d(TAG, "addCompleteFinish: 这是笔记编辑或者修改被删除后的图片"+s);
                ImageUtils.deleteImageFile(s);
            }
        }
        Gson gson = new Gson();
        int ssn = (int) aCache.getAsObject("user_ssn");
        aCache.put("user_ssn",ssn+1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 获取当前时间
        Date currentDate = new Date();
        // 格式化当前时间
        String create_time = sdf.format(currentDate);
        //返回数据
        if(flag_module_int==0){
            //增加

            NoteModel noteModel = new NoteModel(uuid,note_theme_edit.getText().toString(),
                    editor.getContent(),selectedTitleGUID,false,ssn,create_time,"",aCache.getAsString("user_name"));
            Intent intent = new Intent();
            intent.putExtra("result",gson.toJson(noteModel));
            setResult(Constant.NOTE_ADD_FINISH,intent);
        }else{
            noteModel.setSsn(ssn);
            noteModel.setUpdateTime(create_time);
            noteModel.setTitle_guid(selectedTitleGUID);
            noteModel.setTheme(note_theme_edit.getText().toString());
            //TODO 将笔记内容存入缓存
            aCache.put(noteModel.getGuid(),editor.getContent());
            noteModel.setNote_context(editor.getContent());//方便数据库操作 可以在数据库更新完清除引用的
            Intent intent = new Intent();
            intent.putExtra("result",gson.toJson(noteModel));
            intent.putExtra("extradata",firstTitleGUID);
            setResult(Constant.NOTE_COMPILE_FINISH,intent);
        }
        finish();
    }


}