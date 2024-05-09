package com.example.richeditor;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RichEditor extends WebView {
    private final static String TAG = "RichEditor";

    private static final String SETUP_HTML = "file:///android_asset/editor.html";
    private static final String CALLBACK_SCHEME = "re-callback://";
    private static final String STATE_SCHEME = "re-state://";
    private static final String FOCUS_SCHEME = "re-focus://";
    private boolean isReady = false;
    private String mContent = "";//html内容
    private OnContentChangeListener contentChangeListener = null;
    private OnPartStateChangeListener partStateChangeListener = null;
    private OnJSFocusChangeListener jsFocusChangeListener = null;
    public RichEditor(@NonNull Context context) {
        this(context,null);
    }

    public RichEditor(@NonNull Context context, @Nullable AttributeSet attrs) {

        this(context, attrs, android.R.attr.webViewStyle);
        Log.d(TAG, "RichEditor: 执行webview");
    }

    public RichEditor(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        handleLeaks();

        //不显示滚动条
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);

        //允许执行js
        getSettings().setJavaScriptEnabled(true);
        setWebChromeClient(new WebChromeClient());
        setWebViewClient(createWebViewClient());

        loadUrl(SETUP_HTML);

        applyAttributes(context, attrs);
        //设置内容框元素的高度
        this.post(new Runnable() {
            public void run() {
                int height = getHeight();
                setEditorHeight(height);

            }
        });
    }
    WebViewClient createWebViewClient(){
        return new EditorWebViewClient();
    }
    private void handleLeaks() {
        // 4.4以下 清除接口引起的远程代码执行漏洞
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            removeJavascriptInterface("searchBoxJavaBridge_");
            removeJavascriptInterface("accessibility");
            removeJavascriptInterface("accessibilityTraversal");
        }

        WebSettings webSettings = getSettings();
        if (webSettings != null) {
            // 需要使用 file 协议
            webSettings.setAllowFileAccess(true);
            // 不允许通过 file url 加载的js代码读取其他的本地文件
            //webSettings.setAllowFileAccessFromFileURLs(false);
            // 不允许通过 file url 加载的js访问其他的源(包括http、https等源)
            //webSettings.setAllowUniversalAccessFromFileURLs(false);

            webSettings.setAllowUniversalAccessFromFileURLs(true);
            //webSettings.setAllowFileAccess(true);
            webSettings.setAllowFileAccessFromFileURLs(true);
        }
    }
    /**
     * 设置内容监听器
     *
     * @param listener .
     */
    public void setOnTextChangeListener(OnContentChangeListener listener) {
        contentChangeListener = listener;
    }

    /**
     * 设置编辑器状态改变监听
     *
     * @param listener .
     */
    public void setOnDecorationChangeListener(OnPartStateChangeListener listener) {
        partStateChangeListener = listener;
    }
    public void setOnJSFocusChangeListener(OnJSFocusChangeListener listener){
        jsFocusChangeListener = listener;
    }
    /**
     * 执行操作
     *
     * @param trigger
     */
    protected void exec(final String trigger) {
        if (isReady) {
            load(trigger);
        } else {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    exec(trigger);
                }
            }, 100);
        }
    }
    /**
     * 加载js操作
     *
     * @param trigger
     */
    private void load(String trigger) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(trigger, null);
        } else {
            loadUrl(trigger);
        }
    }
    /**
     * 设置编辑区宽度
     *
     * @param width 单位px
     */
    public void setEditorWidth(int width) {
        exec("javascript:RE.setWidth('" + width + "px');");
    }

    /**
     * 设置编辑区高度
     *
     * @param height 单位px
     */
    public void setEditorHeight(int height) {
        exec("javascript:RE.setHeight('" + height + "px');");
    }

    /**
     * 处理WebView自身的gravity属性
     *
     * @param context
     * @param attrs
     */
    private void applyAttributes(Context context, AttributeSet attrs) {
        final int[] attrsArray = new int[]{
                android.R.attr.gravity
        };
        TypedArray ta = context.obtainStyledAttributes(attrs, attrsArray);

        int gravity = ta.getInt(0, NO_ID);
        switch (gravity) {
            case Gravity.LEFT:
                exec("javascript:RE.setTextAlign(\"left\")");
                break;
            case Gravity.RIGHT:
                exec("javascript:RE.setTextAlign(\"right\")");
                break;
            case Gravity.TOP:
                exec("javascript:RE.setVerticalAlign(\"top\")");
                break;
            case Gravity.BOTTOM:
                exec("javascript:RE.setVerticalAlign(\"bottom\")");
                break;
            case Gravity.CENTER_VERTICAL:
                exec("javascript:RE.setVerticalAlign(\"middle\")");
                break;
            case Gravity.CENTER_HORIZONTAL:
                exec("javascript:RE.setTextAlign(\"center\")");
                break;
            case Gravity.CENTER:
                exec("javascript:RE.setVerticalAlign(\"middle\")");
                exec("javascript:RE.setTextAlign(\"center\")");
                break;
        }

        ta.recycle();
    }

    /**
     * 撤销
     */
    public void undo() {
        exec("javascript:RE.undo();");
    }

    /**
     * 反撤销
     */
    public void redo() {
        exec("javascript:RE.redo();");
    }

    /**
     * 粗体
     */
    public void setBold() {
        exec("javascript:RE.setBold();");
    }
    public void setItalic() {
        exec("javascript:RE.setItalic();");
    }
    /**
     * 有序列表
     */
    public void setNumbers() {
        exec("javascript:RE.setNumbers();");
    }
    /**
     * 基础字体大小
     *
     * @param px 单位px
     */
    public void setEditorFontSize(int px) {
        exec("javascript:RE.setBaseFontSize('" + px + "px');");
    }
    /**
     * 文字大小
     *
     * @param fontSize 文字大小 1-7
     */
    public void setFontSize(@IntRange(from = 1, to = 7) int fontSize) {
        exec("javascript:RE.setFontSize('" + fontSize + "');");
    }
    /**
     * 插入图片
     *
     * @param url 图片url
     * @param alt 图片描述
     */
    public void insertImage(String url, String alt) {
/*base64编码过长，js代码无法执行显示*/
        //exec("javascript:RE.prepareInsert();");

        exec("javascript:RE.insertImageWH('" + url + "', '" + alt +"','"+"300px"+"','"+"3.00px"+"');");
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        exec("javascript:RE.setPadding('" + left + "px', '" + top + "px', '" + right + "px', '" + bottom
                + "px');");
    }

    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        // still not support RTL.
        setPadding(start, top, end, bottom);
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
    }

    @Override
    public void setBackgroundResource(int resid) {
        Bitmap bitmap = ImageUtils.decodeResource(getContext(), resid);
        String base64 = ImageUtils.toBase64(bitmap);
        bitmap.recycle();

        exec("javascript:RE.setBackgroundImage('url(data:image/png;base64," + base64 + ")');");
    }

    @Override
    public void setBackground(Drawable background) {
        Bitmap bitmap = ImageUtils.toBitmap(background);
        String base64 = ImageUtils.toBase64(bitmap);
        bitmap.recycle();

        exec("javascript:RE.setBackgroundImage('url(data:image/png;base64," + base64 + ")');");
    }
    /**
     * 直接插入html内容
     *
     * @param contents 内容
     */
    public void setHtml(String contents) {
        if (TextUtils.isEmpty(contents)) {
            contents = "";
        }
        contents = contents.replaceAll("\n", "<br>"); //编辑器不支持换行符\n

        try {
            exec("javascript:RE.setHtml('" + URLEncoder.encode(contents, "UTF-8") + "');");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 直接插入html内容
     *
     * @param contents 内容
     */
    public void setText(String contents) {
        if (TextUtils.isEmpty(contents)) {
            contents = "";
        }
        contents = contents.replaceAll("\n", "<br>"); //编辑器不支持换行符\n
        mContent = contents;
        try {
            exec("javascript:RE.setText('" + URLEncoder.encode(contents, "UTF-8") + "');");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    /**
     * 设置编辑区背景图
     *
     * @param url 图片url
     */
    public void setBackground(String url) {
        exec("javascript:RE.setBackgroundImage('url(" + url + ")');");
    }
    //内容的回调
     void callBack(String  text){
        mContent = text.replace(CALLBACK_SCHEME,"");
        if(contentChangeListener != null){
            contentChangeListener.onContentChange(mContent);
        }
     }
     //状态的回调
    void checkState(String url){
        String state = url.replace(STATE_SCHEME,"").toUpperCase(Locale.ENGLISH);
        List<EditorOpType> typeList = new ArrayList<>();
        String[] states = state.split(",");
        EditorOpType[] editorOpTypes = EditorOpType.values();
        for (String s : states) {
            for (EditorOpType editorOpType : editorOpTypes) {
                if(editorOpType.name().equals(s)){
                    typeList.add(editorOpType);
                }
            }
        }
        if(partStateChangeListener!=null){
            partStateChangeListener.onPartStateChange(typeList);
        }
    }
    //焦点监听的回调
    void onFocusChange(String url){
        String hasFocus = url.replace(FOCUS_SCHEME,"").toUpperCase(Locale.ENGLISH);
        if(jsFocusChangeListener!=null){
            Boolean isFocus = false;
            if(hasFocus.equals("TRUE")){
                isFocus = true;
            }else{
                isFocus = false;
            }
            jsFocusChangeListener.onFocusChange(isFocus);
        }
    }
    /**
     * 设置是否可编辑
     *
     * @param isEdit 是否可编辑
     */
    public void setEdit(boolean isEdit) {
        exec("javascript:RE.setEdit('" + isEdit + "');");
    }
    /**
     * 设置编辑区的 placeholder
     *
     * @param placeholder placeholder内容
     */
    public void setPlaceholder(String placeholder) {
        exec("javascript:RE.setPlaceholder('" + placeholder + "');");
    }
    public String getContent(){
        return mContent;
    }

    class EditorWebViewClient extends WebViewClient{
        @Override
        public void onPageFinished(WebView view, String url) {
           isReady = url.equalsIgnoreCase(SETUP_HTML);//忽略大小写进行比较
            Log.d(TAG, "onPageFinished: webviewinit");

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String decode = "";
            try {
               decode = URLDecoder.decode(request.getUrl().toString(),"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            }
            if(decode.startsWith(CALLBACK_SCHEME)){
                callBack(decode);
                Log.d(TAG, "shouldOverrideUrlLoading: "+decode);
                exec("javascript:RE.refreshEditingItems();");
                return true;
            }else if(decode.startsWith(STATE_SCHEME)){
                checkState(decode);
                return true;
            }else if(decode.startsWith(FOCUS_SCHEME)){
                onFocusChange(decode);
                return true;
            }
            return super.shouldOverrideUrlLoading(view, request);
        }

    }
}
