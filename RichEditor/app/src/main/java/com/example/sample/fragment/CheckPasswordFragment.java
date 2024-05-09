package com.example.sample.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.sample.R;
import com.example.sample.communication.SendHttp;
import com.example.sample.util.ACache;


public class CheckPasswordFragment extends Fragment {
    private Handler handler;
    private EditText checkPasswordView;
    private Button checkPasswordConfirmView;
    private View view;
    private ACache aCache;
    private SendHttp sendHttp = new SendHttp();
    public CheckPasswordFragment(Handler handler) {
        // Required empty public constructor
        this.handler = handler;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    public void setCheckPasswordViewError(String Text){
        checkPasswordView.setError(Text);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_check_password, container, false);
        aCache = ACache.get(getContext());
        checkPasswordView = view.findViewById(R.id.check_password);
        checkPasswordConfirmView =view.findViewById(R.id.check_password_confirm);
        checkPasswordConfirmView.setOnClickListener(v->{
            sendHttp.checkPassword(aCache.getAsString("user_name"),aCache.getAsString("token"),checkPasswordView.getText().toString(),handler);
        });
        return view;
    }
}