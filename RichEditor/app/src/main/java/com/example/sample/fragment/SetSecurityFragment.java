package com.example.sample.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sample.R;
import com.example.sample.communication.SendHttp;
import com.example.sample.util.ACache;

public class SetSecurityFragment extends Fragment {

    private EditText newSecurityQuestion;
    private EditText newSecurityAnswer;
    private Button confirm;
    private Handler handler;
    private SendHttp sendHttp = new SendHttp();
    private ACache aCache;
    private View view;
    public SetSecurityFragment(Handler handler) {
        // Required empty public constructor
        this.handler = handler;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_set_security, container, false);
        aCache = ACache.get(getContext());
        newSecurityAnswer = view.findViewById(R.id.set_new_security_answer);
        newSecurityQuestion = view.findViewById(R.id.set_new_security_question);
        confirm = view.findViewById(R.id.set_new_security_confirm);
        confirm.setOnClickListener(v->{
            if(newSecurityQuestion.getText()==null||newSecurityAnswer.getText()==null){
                Toast.makeText(getContext(),"问题或答案不能为空",Toast.LENGTH_SHORT).show();
                return;
            }
            sendHttp.setNewSecurityQuestion(aCache.getAsString("user_name"),aCache.getAsString("token"),newSecurityQuestion.getText().toString(),
                    newSecurityAnswer.getText().toString(),handler);
        });
        return view;
    }
}