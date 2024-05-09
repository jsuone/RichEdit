package com.example.sample.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sample.R;
import com.example.sample.communication.SendHttp;
import com.example.sample.util.ACache;


public class CheckSecurityFragment extends Fragment {

    private Button confirmView;
    private TextView securityQuestionView;
    private EditText securityAnswerView;
    private View view;
    private Handler handler;
    private SendHttp sendHttp = new SendHttp();
    private ACache aCache;
    private String question;
    public CheckSecurityFragment(Handler handler) {
        // Required empty public constructor
        this.handler = handler;

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    public void setSecurityQuestionView(String text){
            Log.d("notedata", "密保问题: "+text);
            question = text;
           // securityQuestionView.setText(text);

    }
    public void setSecurityAnswerViewError(String text){
        securityAnswerView.setError(text);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_check_security, container, false);
        aCache = ACache.get(getContext());
        securityQuestionView = view.findViewById(R.id.check_security_question);
        securityQuestionView.setText(question);
        securityAnswerView = view.findViewById(R.id.check_security_answer);
        confirmView  =view.findViewById(R.id.check_security_confirm);
        confirmView.setOnClickListener(v->{
            //确认答案是否正确
            if(securityAnswerView.getText()==null||securityAnswerView.getText().toString().equals("")){
                Toast.makeText(getContext(),"答案不能为空",Toast.LENGTH_SHORT).show();
                return;
            }
            sendHttp.checkSecurityAnswer(aCache.getAsString("user_name"),aCache.getAsString("token"),securityAnswerView.getText().toString(),handler);
        });
        return view;
    }
}