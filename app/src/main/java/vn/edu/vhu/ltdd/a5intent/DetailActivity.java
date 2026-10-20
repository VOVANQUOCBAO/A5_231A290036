package vn.edu.vhu.ltdd.a5intent;
 
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
 
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
 
public class DetailActivity extends AppCompatActivity {
 
    private static final String TAG = "A5_231A290036";
 
    private Contact contact;
    private EditText edtHoTenMoi;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
 
        TextView tvThongTin = findViewById(R.id.tvThongTin);
        TextView tvNguoiGui = findViewById(R.id.tvNguoiGui);
        edtHoTenMoi = findViewById(R.id.edtHoTenMoi);
        Button btnLuu = findViewById(R.id.btnLuu);
        Button btnHuy = findViewById(R.id.btnHuy);
 
        // L y d  li u do màn hình tr c g i sang
        contact = IntentCompat.getParcelableExtra(getIntent(),
                MainActivity.EXTRA_CONTACT, Contact.class);
        String nguoiGui = getIntent().getStringExtra(MainActivity.EXTRA_NGUOI_GUI);
 
        if (contact == null) {                       // luôn phòng tr ng h p không nh n đ c d  li u
            tvThongTin.setText(R.string.no_data);
            Log.w(TAG, "Không nh n đ c Contact t  Intent");
            return;
        }
 
        tvThongTin.setText(getString(R.string.detail_format,
                contact.getHoTen(), contact.getDienThoai(), contact.getEmail()));
        tvNguoiGui.setText(getString(R.string.sent_by, nguoiGui));
        edtHoTenMoi.setText(contact.getHoTen());
 
        btnLuu.setOnClickListener(v -> luuVaQuayLai());
        btnHuy.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);              // báo cho màn hình tr c bi t là đã h y
            finish();
        });
    }
 
    /** Tr  d  li u đã s a v  màn hình g i. */
    private void luuVaQuayLai() {
        String hoTenMoi = edtHoTenMoi.getText().toString().trim();
        if (hoTenMoi.isEmpty()) {
            edtHoTenMoi.setError(getString(R.string.err_empty));
            return;
        }
        contact.setHoTen(hoTenMoi);
 
        Intent ketQua = new Intent();
        ketQua.putExtra(MainActivity.EXTRA_CONTACT, contact);
        setResult(RESULT_OK, ketQua);
        Log.d(TAG, "Tr  k t qu  v : " + hoTenMoi);
        finish();                                    // đóng màn hình này, quay v  màn hình tr c
    }
}