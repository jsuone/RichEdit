package com.example.sample.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.baidu.aip.asrwakeup3.core.mini.AutoCheck;
import com.baidu.aip.asrwakeup3.core.recog.IStatus;
import com.baidu.aip.asrwakeup3.core.recog.MyRecognizer;
import com.baidu.aip.asrwakeup3.core.recog.listener.IRecogListener;
import com.baidu.aip.asrwakeup3.core.recog.listener.MessageStatusRecogListener;
import com.baidu.aip.asrwakeup3.core.util.AuthUtil;
import com.baidu.speech.asr.SpeechConstant;
import com.example.richeditor.OnContentChangeListener;
import com.example.richeditor.OnInsertImageListener;
import com.example.richeditor.OnSpeechRecogListener;
import com.example.sample.R;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements IStatus {
    private com.example.richeditor.RichEditor mEditor;
    private com.example.richeditor.EditorOpMenuView mEditorOpMenuView;
    private int REQUEST_CODE_PICK = 1;
    /**
     * 识别控制器，使用MyRecognizer控制识别的流程
     */
    protected MyRecognizer myRecognizer;
    protected Handler handler;
    /**
     * 控制UI按钮的状态
     */
    protected int status;
    private final static String TAG = "RichEditor";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        applyPermission();
        Log.d(TAG, "onCreate: 开始执行");
        mEditor = findViewById(R.id.editor);
        mEditorOpMenuView = findViewById(R.id.editor_op_menu_view);
        Log.d(TAG, ""+mEditor.isEnabled());
        mEditor.setPlaceholder("请填写文章正文内容（必填）"); //设置占位文字
        mEditor.setEditorFontSize(16); //设置文字大小
        mEditor.setPadding(10, 10, 10, 10); //设置编辑器内边距
        mEditor.setBackgroundColor(getResources().getColor(R.color.white)); //设置编辑器背景色
        mEditor.setOnTextChangeListener(new OnContentChangeListener() {
            @Override
            public void onContentChange(String text) { //输入文本回调监听

            }
        });
        mEditor.requestFocus();
        //绑定编辑器
        mEditorOpMenuView.setRichEditor(mEditor);
        mEditorOpMenuView.setOnInsertImageListener(new OnInsertImageListener() {
            @Override
            public void openGallery() {
                Intent getImage = new Intent(Intent.ACTION_PICK);
                getImage.setType("image/*");
                startActivityForResult(getImage,REQUEST_CODE_PICK);
            }
        });
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
        mEditorOpMenuView.setOnSpeechRecogListener(new OnSpeechRecogListener() {
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
    }
void handleMsg(Message msg){
    switch (msg.what) { // 处理MessageStatusRecogListener中的状态回调
        case STATUS_FINISHED:
            if (msg.arg2 == 1) {
                //txtResult.setText(msg.obj.toString());
                mEditor.setHtml(msg.obj.toString());
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
               // mEditorOpMenuView.insertImage(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    void applyPermission(){//动态权限申请
        String[] permissions = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
        };
        List<String> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, permission)) {
                permissionList.add(permission);
                // 进入到这里代表没有权限.

            }
        }
        if (!permissionList.isEmpty()){
            String[] applypermissions = permissionList.toArray(new String[permissionList.
                    size()]);

            ActivityCompat.requestPermissions(MainActivity.this, applypermissions, 1);
        }

    }

/*权限申请的回调*/
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }

                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
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
}