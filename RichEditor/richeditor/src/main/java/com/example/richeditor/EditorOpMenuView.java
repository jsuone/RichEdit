package com.example.richeditor;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class EditorOpMenuView extends FrameLayout implements View.OnClickListener {


    private ImageButton undoView;
    private ImageButton redoView;
    private ImageButton boldView;
    private ImageButton italicView;
    private ImageButton orderListView;
    private ImageButton materialsImageView;
    private ImageButton microphoneView;
    private View textSizeListView;
    private TextView textSizeView;
    private View view;

    private OnInsertImageListener mOnInsertImageListener;
    private OnSpeechRecogListener mOnSpeechRecogListener;
    private RichEditor richEditor;

    private List<FontSizeBean> fontSizeBeans;

    private final static String TAG = "RichEditor";
    // 图标颜色状态
    private final static ColorStateList sColorStateList;
    static {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_selected}, // 选中
                new int[]{-android.R.attr.state_selected} // 未选中
        };

        int[] colors = new int[]{
                Color.parseColor("#4786ff"),
                Color.parseColor("#68696e")
        };
        sColorStateList = new ColorStateList(states, colors);
    }


    public EditorOpMenuView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditorOpMenuView(@NonNull Context context) {
        this(context,null);
    }
    public EditorOpMenuView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();


    }

    public void setOnInsertImageListener(OnInsertImageListener onInsertImageListener) {
        this.mOnInsertImageListener = onInsertImageListener;
    }
    public void setOnSpeechRecogListener(OnSpeechRecogListener onSpeechRecogListener){
        this.mOnSpeechRecogListener = onSpeechRecogListener;
    }

    void initView(){
        view = LayoutInflater.from(getContext()).inflate(R.layout.module_editor_op_menu_view,null);
        undoView = view.findViewById(R.id.editor_action_undo);
        redoView = view.findViewById(R.id.editor_action_redo);
        boldView = view.findViewById(R.id.editor_action_bold);
        textSizeListView = view.findViewById(R.id.editor_action_font_size);
        textSizeView = view.findViewById(R.id.editor_font_size);
        italicView = view.findViewById(R.id.editor_action_italic);
        orderListView = view.findViewById(R.id.editor_action_ordered_list);
        materialsImageView = view.findViewById(R.id.editor_action_materials);
        microphoneView = view.findViewById(R.id.editor_action_microphone);

        undoView.setOnClickListener(this);
        redoView.setOnClickListener(this);
        boldView.setOnClickListener(this);
        textSizeListView.setOnClickListener(this);
        italicView.setOnClickListener(this);
        orderListView.setOnClickListener(this);
        materialsImageView.setOnClickListener(this);
        microphoneView.setOnClickListener(this);

        ImageUtils.setTintList(undoView.getDrawable(), sColorStateList);
        ImageUtils.setTintList(redoView.getDrawable(), sColorStateList);
        ImageUtils.setTintList(italicView.getDrawable(), sColorStateList);
        ImageUtils.setTintList(boldView.getDrawable(),sColorStateList);
        ImageUtils.setTintList(orderListView.getDrawable(),sColorStateList);
        ImageUtils.setTintList(microphoneView.getDrawable(),sColorStateList);
        addView(view);
    }
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(richEditor!=null){
            if(id==R.id.editor_action_undo){
                richEditor.undo();
            }else if(id == R.id.editor_action_redo){
                richEditor.redo();
            }else if(id == R.id.editor_action_bold){
                richEditor.setBold();
            }else if(id == R.id.editor_action_italic){
                richEditor.setItalic();
            }else if(id == R.id.editor_action_font_size){
                setFontSize();
            }else if(id == R.id.editor_action_ordered_list){
                richEditor.setNumbers();
            }else if(id == R.id.editor_action_materials){
                mOnInsertImageListener.openGallery();
            }else if(id == R.id.editor_action_microphone){
                setMicrophoneSelect(!microphoneView.isSelected());
                mOnSpeechRecogListener.onASRChange();
            }
        }


    }
    public void setEnable(Boolean isEnable){
        //view.setEnabled(isEnable);
        undoView.setEnabled(isEnable);
        redoView.setEnabled(isEnable);
        boldView.setEnabled(isEnable);
        textSizeListView.setEnabled(isEnable);
        textSizeView.setEnabled(isEnable);
        italicView.setEnabled(isEnable);
        orderListView.setEnabled(isEnable);
        materialsImageView.setEnabled(isEnable);
        microphoneView.setEnabled(isEnable);
        if(isEnable){
            settoumingdu(1f);
        }else{
            //禁止使用
            settoumingdu(0.5f);
        }

        /*materialsImageView = view.findViewById(R.id.editor_action_materials);
        microphoneView = view.findViewById(R.id.editor_action_microphone);*/
    }
    void settoumingdu(Float alpha ){
        undoView.setAlpha(alpha);
        redoView.setAlpha(alpha);
        boldView.setAlpha(alpha);
        textSizeListView.setAlpha(alpha);
        italicView.setAlpha(alpha);
        orderListView.setAlpha(alpha);
        materialsImageView.setAlpha(alpha);
        microphoneView.setAlpha(alpha);

    }
/*从图库中选择的图片资源进行插入*/
    public void insertImage(String path){
        /*将图片资源转成base64字节字符串形式方便数据库统一存储管理和网页显示*/
       // ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        //bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

        //byte[] byteArray = byteArrayOutputStream.toByteArray();
        /*如果改用base64.defeault模式会在每隔70多字符进行换行，造成图片解析失败*/
        //String imgageBase64 = Base64.encodeToString(byteArray, Base64.NO_WRAP);
       // String image = "data:image/png;base64," + imgageBase64;
       // Log.d(TAG,"editmenu 图片选择："+image);
        richEditor.insertImage(path,"图片");
    }

    void setFontSizeSelect(int size){
        textSizeView.setTag(size);
        String[] fontSizeArr = getResources().getStringArray(R.array.editor_font_size_arr);
        textSizeView.setText(fontSizeArr[size - 1]); //size-1 因为fontSizeArr从0开始的
        //对应文字大小集合中设置为选中
        for(FontSizeBean sizeBean : generateFontSizes()){
            if(sizeBean.getSize() == size){
                sizeBean.setSelect(true);
            }else{
                sizeBean.setSelect(false);
            }
        }
    }
    void setFontSize(){
        new FontSizeSelectDialog(getContext())
                .setTitle(getContext().getString(R.string.editor_select_font_size))
                .setFontSizes(generateFontSizes())
                .setOnFontSizeSelectListener(new OnFontSizeSelectListener() {
                    @Override
                    public void onFontSizeSelect(FontSizeBean bean, int pos) {
                        //设置编辑器文字大小
                        if (richEditor != null) {
                            richEditor.setFontSize(bean.getSize());
                        }
                        //改变菜单图标的文字大小
                        setFontSizeSelect(bean.getSize());
                    }
                }).show();
    }
    private List<FontSizeBean> generateFontSizes(){
        if(fontSizeBeans!=null&&!fontSizeBeans.isEmpty()  ){
            return  fontSizeBeans;
        }
        fontSizeBeans = new ArrayList<>();
        String[] fontSizes = getContext().getResources().getStringArray(R.array.editor_font_size_arr);

        for (int i = 0; i < fontSizes.length; i++) {
            FontSizeBean bean = new FontSizeBean(i+1,fontSizes[i],false);
            Log.d(TAG, "generateFontSizes: "+fontSizes[i]);
            fontSizeBeans.add(bean);
        }
        fontSizeBeans.get(2).setSelect(true);
        return fontSizeBeans;
    }
    /**
     * 绑定RichEditor
     *
     * @param mRichEditor RichEditor对象
     */
    public void setRichEditor(final RichEditor mRichEditor) {
        this.richEditor = mRichEditor;
        if (richEditor != null) {
            //enableWebViewCookie(mRichEditor);
            mRichEditor.setOnDecorationChangeListener(new OnPartStateChangeListener() {

                @Override
                public void onPartStateChange( List<EditorOpType> types) {
                    if (types == null) {
                        return;
                    }

                    //处理操作图标变色
                    boolean isBold = false;
                    boolean isItalic = false;
                    boolean isOrderedList = false;
                    for (EditorOpType type : types) {
                        switch (type) {
                            case BOLD:
                                isBold = true;
                                break;
                            case ITALIC:
                                isItalic = true;
                                break;
                            case FONTSIZE:
                                setFontSizeSelect(Integer.parseInt(type.getValue().toString()));
                                break;
                            case ORDEREDLIST:
                                isOrderedList = true;
                                break;
                            default:
                                break;
                        }
                    }

                    setBoldSelect(isBold);
                    setItalicSelect(isItalic);
                    setOrderedList(isOrderedList);
                }
            });
        }
    }

    /**
     * 设置选中粗体
     *
     * @param select 是否选中
     */
    private void setBoldSelect(boolean select) {
        boldView.setSelected(select);
        Log.d("RichEditor", "setBoldSelect: "+boldView.isSelected()+select);

    }

    /**
     * 设置选中斜体
     *
     * @param select 是否选中
     */
    private void setItalicSelect(boolean select) {
        italicView.setSelected(select);
        Log.d("RichEditor","进入斜体选择"+select);
    }
    /**
     * 设置选中有序列表
     *
     * @param select 是否选中
     */
    private void setOrderedList(boolean select) {
        orderListView.setSelected(select);
    }
    private void setMicrophoneSelect(boolean select){microphoneView.setSelected(select);}

}
