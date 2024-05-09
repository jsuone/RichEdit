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


public class SetPasswordFragment extends Fragment {
    private EditText newPassword;
    private Button newPasswordConfirm;
    private View view;
    private Handler handler;
    private SendHttp sendHttp = new SendHttp();
    private ACache aCache;
    public SetPasswordFragment(Handler handler) {
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
        view =  inflater.inflate(R.layout.fragment_set_password, container, false);
        newPassword = view.findViewById(R.id.set_new_password);
        newPasswordConfirm = view.findViewById(R.id.set_new_password_confirm);
        aCache = ACache.get(getContext());
        newPasswordConfirm.setOnClickListener(v->{
            if(newPassword.getText()!=null&&!newPassword.getText().toString().equals("")){
                sendHttp.changePassword(aCache.getAsString("user_name"),aCache.getAsString("token"),newPassword.getText().toString(),handler);
            }else{
                Toast.makeText(getContext(),"新密码不能为空",Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }
}