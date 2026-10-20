package vn.edu.vhu.ltdd.a5intent;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.IntentCompat;
import java.util.ArrayList;

public class MainActivity extends LabActivity {
    public static final String EXTRA_CONTACT="extra_contact";
    public static final String EXTRA_NGUOI_GUI="extra_nguoi_gui";
    public static final String EXTRA_CONTACTS="extra_contacts";
    private EditText edtHoTen,edtDienThoai,edtEmail;
    private TextView tvKetQuaTraVe;
    private final ActivityResultLauncher<Intent> chiTietLauncher=registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),result->{
            Log.d(TAG,"resultCode="+result.getResultCode());
            if(result.getResultCode()==RESULT_OK && result.getData()!=null) {
                Contact contact=IntentCompat.getParcelableExtra(result.getData(),EXTRA_CONTACT,Contact.class);
                if(contact!=null) {
                    edtHoTen.setText(contact.getHoTen()); edtDienThoai.setText(contact.getDienThoai());
                    edtEmail.setText(contact.getEmail());
                    tvKetQuaTraVe.setText(getString(R.string.returned,contact.getHoTen()));
                    Log.d(TAG,"Nhận kết quả trả về: "+contact.getHoTen());
                } else tvKetQuaTraVe.setText(R.string.no_data);
            } else tvKetQuaTraVe.setText(R.string.returned_cancel);
        });
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state); showLayout(R.layout.activity_main);
        edtHoTen=findViewById(R.id.edtHoTen); edtDienThoai=findViewById(R.id.edtDienThoai);
        edtEmail=findViewById(R.id.edtEmail); tvKetQuaTraVe=findViewById(R.id.tvKetQuaTraVe);
        findViewById(R.id.btnChiTiet).setOnClickListener(v->moChiTiet());
        findViewById(R.id.btnGoi).setOnClickListener(v->goiDien());
        findViewById(R.id.btnWeb).setOnClickListener(v->moAnToan(new Intent(Intent.ACTION_VIEW,Uri.parse(getString(R.string.school_url)))));
        findViewById(R.id.btnChiaSe).setOnClickListener(v->chiaSe());
        if(state==null) nhanChiaSe(getIntent());
        else tvKetQuaTraVe.setText(state.getString("returned",getString(R.string.returned_none)));
    }
    private void moChiTiet() {
        String name=edtHoTen.getText().toString().trim();
        edtHoTen.setError(null);
        if(name.isEmpty()) { edtHoTen.setError(getString(R.string.err_empty)); edtHoTen.requestFocus(); return; }
        Contact contact=new Contact(name,edtDienThoai.getText().toString().trim(),edtEmail.getText().toString().trim());
        ArrayList<Contact> list=new ArrayList<>(); list.add(contact);
        Intent intent=new Intent(this,DetailActivity.class);
        Bundle extras=new Bundle(); extras.putParcelable(EXTRA_CONTACT,contact); extras.putString(EXTRA_NGUOI_GUI,TAG);
        intent.putExtras(extras);
        // NC2: truyền và đọc lại một danh sách Parcelable thật.
        intent.putParcelableArrayListExtra(EXTRA_CONTACTS,list);
        chiTietLauncher.launch(intent);
    }
    private void goiDien() {
        String phone=edtDienThoai.getText().toString().trim(); edtDienThoai.setError(null);
        if(phone.isEmpty()) { edtDienThoai.setError(getString(R.string.err_empty)); edtDienThoai.requestFocus(); return; }
        moAnToan(new Intent(Intent.ACTION_DIAL,Uri.fromParts("tel",phone,null)));
    }
    private void chiaSe() {
        Intent intent=new Intent(Intent.ACTION_SEND); intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT,getString(R.string.share_subject));
        intent.putExtra(Intent.EXTRA_TEXT,getString(R.string.share_text,edtHoTen.getText().toString(),edtDienThoai.getText().toString()));
        moAnToan(Intent.createChooser(intent,getString(R.string.share_title)));
    }
    private void moAnToan(Intent intent) {
        try { startActivity(intent); }
        catch(ActivityNotFoundException e) { Toast.makeText(this,R.string.err_no_app,Toast.LENGTH_SHORT).show(); Log.w(TAG,"Không có ứng dụng xử lý Intent",e); }
    }
    private void nhanChiaSe(Intent intent) {
        if(Intent.ACTION_SEND.equals(intent.getAction()) && "text/plain".equals(intent.getType())) {
            String text=intent.getStringExtra(Intent.EXTRA_TEXT);
            if(text!=null) { edtHoTen.setText(text); tvKetQuaTraVe.setText(R.string.received_share); Log.d(TAG,"Đã nhận ACTION_SEND text/plain"); }
        }
    }
    @Override protected void onNewIntent(Intent intent) { super.onNewIntent(intent); setIntent(intent); nhanChiaSe(intent); }
    @Override protected void onSaveInstanceState(Bundle state) {
        state.putString("returned",tvKetQuaTraVe.getText().toString()); super.onSaveInstanceState(state);
    }
}
