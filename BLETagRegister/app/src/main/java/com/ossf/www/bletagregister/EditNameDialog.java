package com.ossf.www.bletagregister;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import static com.ossf.www.bletagregister.BlueToothScanActivity.fos;

public class EditNameDialog extends Dialog{
    private TextView tv_mac;
    private Button btn_confirm;
    private EditText et_name;
    public EditNameDialog(@NonNull Context context,final String mac) {
        super(context,android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        setContentView(R.layout.dialog_edit_name);
        this.setTitle("為此設備命名");
        et_name=(EditText)findViewById(R.id.et_name);
        tv_mac=(TextView)findViewById(R.id.tv_mac);
        tv_mac.setText("mac: "+mac);
        btn_confirm=(Button)findViewById(R.id.btn_confirm_name);
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm(mac);
            }
        });
    }
    public void confirm(String mac){
        String name=et_name.getText().toString();
        String data = mac+" "+name+"\n";
        Log.v("apple","data="+data);
        try {
            fos.write(data.getBytes());
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.dismiss();
    }
}
