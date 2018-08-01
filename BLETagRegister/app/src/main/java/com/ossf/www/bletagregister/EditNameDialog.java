package com.ossf.www.bletagregister;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.IOException;
import static com.ossf.www.bletagregister.HomeActivity.initializeList;
import static com.ossf.www.bletagregister.HomeActivity.regDevice_list;

/*
    dialog : EditNameDialog
    description : if you choose the item on the device list of the BlueToothScanActivity , this dialog will show.
    author : Cynthia
    date : 2018.08.01
 */
public class EditNameDialog extends Dialog{
    private TextView tv_mac;
    private Button btn_confirm;
    private EditText et_name;
    String mac;
    BLEdevice device;

    public EditNameDialog(@NonNull Context context , BLEdevice d) {
        super(context,android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        setContentView(R.layout.dialog_edit_name);

        //get the information of the device which is choosed by user
        device=new BLEdevice(d);// device = d
        mac=device.getMac();

        initializeXml();
    }

    private void initializeXml(){
        this.setTitle("為此設備命名");
        et_name=(EditText)findViewById(R.id.et_name);
        tv_mac=(TextView)findViewById(R.id.tv_mac);
        tv_mac.setText("MAC: "+mac);
        btn_confirm=(Button)findViewById(R.id.btn_confirm_name);
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm(mac);
            }
        });
    }

    // the function which called by button onClick
    private void confirm(String mac){

        // make the text which will be wrote into ble.txt
        String name=et_name.getText().toString();
        String data = mac+" "+name+"\n";

        //write into ble.txt
        try {
            FileStream fs=new FileStream();
            fs.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //put into regDevice_list and refresh the list
        device.isReg=true;
        device.register(name);
        regDevice_list.put(device.getMac(),device);
        initializeList(); // refresh list

        // close the dialog
        this.dismiss();
    }
}
