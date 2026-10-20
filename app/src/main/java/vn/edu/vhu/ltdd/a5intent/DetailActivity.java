package vn.edu.vhu.ltdd.a5intent;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import androidx.core.content.IntentCompat;
import java.util.ArrayList;

public class DetailActivity extends LabActivity {
    private Contact contact;
    private EditText edtHoTenMoi;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state); showLayout(R.layout.activity_detail);
        edtHoTenMoi=findViewById(R.id.edtHoTenMoi);
        findViewById(R.id.btnHuy).setOnClickListener(v->{setResult(RESULT_CANCELED);finish();});
        findViewById(R.id.btnLuu).setOnClickListener(v->luu());
        contact=IntentCompat.getParcelableExtra(getIntent(),MainActivity.EXTRA_CONTACT,Contact.class);
        TextView info=findViewById(R.id.tvThongTin);
        if(contact==null) {
            info.setText(R.string.no_data); findViewById(R.id.btnLuu).setEnabled(false); Log.w(TAG,"Không nhận được Contact"); return;
        }
        info.setText(getString(R.string.detail_format,contact.getHoTen(),contact.getDienThoai(),contact.getEmail()));
        String sender=getIntent().getStringExtra(MainActivity.EXTRA_NGUOI_GUI);
        ArrayList<Contact> list=IntentCompat.getParcelableArrayListExtra(getIntent(),MainActivity.EXTRA_CONTACTS,Contact.class);
        ((TextView)findViewById(R.id.tvNguoiGui)).setText(getString(R.string.sent_by,sender==null?"—":sender)+"\n"+getString(R.string.list_count,list==null?0:list.size()));
        if(state==null) edtHoTenMoi.setText(contact.getHoTen());
        else edtHoTenMoi.setText(state.getString("draft",contact.getHoTen()));
    }
    private void luu() {
        if(contact==null) return;
        String name=edtHoTenMoi.getText().toString().trim(); edtHoTenMoi.setError(null);
        if(name.isEmpty()) { edtHoTenMoi.setError(getString(R.string.err_empty)); edtHoTenMoi.requestFocus(); return; }
        contact.setHoTen(name);
        Intent result=new Intent(); result.putExtra(MainActivity.EXTRA_CONTACT,contact);
        setResult(RESULT_OK,result); Log.d(TAG,"Trả RESULT_OK: "+name); finish();
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        state.putString("draft",edtHoTenMoi.getText().toString()); super.onSaveInstanceState(state);
    }
}
