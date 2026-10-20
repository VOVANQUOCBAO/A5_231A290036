package vn.edu.vhu.ltdd.a5intent;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.graphics.*;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.io.*;
import java.util.*;

/** Runs the submitted scenarios on the device; no test hooks in the release app. */
public class LabRunner extends Instrumentation {
    private volatile Activity active;
    private final ArrayList<String> results=new ArrayList<>();
    private String mode="full";
    @Override public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        if(arguments!=null) mode=arguments.getString("mode","full");
        start();
    }
    @Override public void onStart() {
        try {
            ((Application)getTargetContext().getApplicationContext()).registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                public void onActivityResumed(Activity a){active=a;}
                public void onActivityCreated(Activity a,Bundle b){active=a;}
                public void onActivityStarted(Activity a){}
                public void onActivityPaused(Activity a){}
                public void onActivityStopped(Activity a){}
                public void onActivitySaveInstanceState(Activity a,Bundle b){}
                public void onActivityDestroyed(Activity a){}
            });
            Intent intent=new Intent(getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            active=startActivitySync(intent); settle();
            execute();
            save("results.txt",android.text.TextUtils.join("\n",results)+"\nALL PASSED\n");
            Bundle done=new Bundle();done.putString("stream","\n"+android.text.TextUtils.join("\n",results)+"\nALL PASSED\n");finish(Activity.RESULT_OK,done);
        } catch(Throwable e) {
            StringWriter sw=new StringWriter();e.printStackTrace(new PrintWriter(sw));
            try { save("results.txt",android.text.TextUtils.join("\n",results)+"\nFAILED\n"+sw); shot("FAILED"); } catch(Throwable ignored){}
            Bundle b=new Bundle();b.putString("stream","FAILED: "+sw);finish(Activity.RESULT_CANCELED,b);
        }
    }
    private void ui(Runnable action) { runOnMainSync(action); waitForIdleSync(); }
    private void pause(long ms) { SystemClock.sleep(ms); }
    private void settle(){waitForIdleSync();pause(900);}
    private View view(int id){return active.findViewById(id);}
    private void hideKeyboard(){ui(()->((InputMethodManager)active.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(active.getWindow().getDecorView().getWindowToken(),0));}
    private void show(int id) {
        hideKeyboard(); ui(()->{View v=view(id);v.requestRectangleOnScreen(new Rect(0,0,v.getWidth(),v.getHeight()),true);});pause(300);
    }
    private void click(int id){show(id);ui(()->view(id).performClick());settle();}
    private void edit(int id,String value){ui(()->{
        EditText e=(EditText)view(id);int type=e.getInputType();
        e.setKeyListener(android.text.method.TextKeyListener.getInstance());e.setRawInputType(type);
        e.setText(value);
    });}
    private String text(int id){final String[] s={""};ui(()->s[0]=((TextView)view(id)).getText().toString());return s[0];}
    private String error(int id){final String[] s={""};ui(()->{CharSequence e=((TextView)view(id)).getError();s[0]=e==null?"":e.toString();});return s[0];}
    private void check(String label,boolean pass){
        if(!pass)throw new AssertionError(label);
        results.add("PASS | "+label);Bundle b=new Bundle();b.putString("stream","PASS | "+label+"\n");sendStatus(0,b);
    }
    private void shot(String name)throws Exception{
        hideKeyboard();pause(200);Bitmap b=getUiAutomation().takeScreenshot();
        if(b==null)throw new IOException("Screenshot unavailable");
        File dir=new File(getTargetContext().getExternalFilesDir(null),"evidence");dir.mkdirs();
        try(FileOutputStream out=new FileOutputStream(new File(dir,name+".png"))){b.compress(Bitmap.CompressFormat.PNG,100,out);}
    }
    private void save(String name,String data)throws Exception{
        File dir=new File(getTargetContext().getExternalFilesDir(null),"evidence");dir.mkdirs();
        try(FileOutputStream out=new FileOutputStream(new File(dir,name))){out.write(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
    }
    private Activity waitActivity(String name){
        for(int i=0;i<100;i++){if(active!=null&&active.getClass().getSimpleName().equals(name)){settle();return active;}pause(100);}
        throw new AssertionError("Activity not resumed: "+name);
    }
    private void rotate(boolean landscape){
        Activity old=active;
        ui(()->old.setRequestedOrientation(landscape?ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        for(int i=0;i<80;i++){if(active!=old){settle();return;}pause(100);}
        throw new AssertionError("Rotation did not recreate Activity");
    }
    private void back(){hideKeyboard();pause(400);sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);settle();}
    private AccessibilityNodeInfo findNode(String id,String value){
        AccessibilityNodeInfo root=getUiAutomation().getRootInActiveWindow();if(root==null)return null;
        if(id!=null){List<AccessibilityNodeInfo> list=root.findAccessibilityNodeInfosByViewId(id);if(!list.isEmpty())return list.get(0);}
        if(value!=null){List<AccessibilityNodeInfo> list=root.findAccessibilityNodeInfosByText(value);for(AccessibilityNodeInfo node:list)if(value.equals(String.valueOf(node.getText()))||value.equals(String.valueOf(node.getContentDescription())))return node;}
        return null;
    }
    private void systemClick(String id,String text){
        for(int i=0;i<60;i++){AccessibilityNodeInfo node=findNode(id,text);if(node!=null){
            while(node!=null&&!node.isClickable())node=node.getParent();
            if(node!=null&&node.performAction(AccessibilityNodeInfo.ACTION_CLICK)){settle();return;}
        }pause(150);}
        throw new AssertionError("System control not found: "+id+" / "+text);
    }
    private void shell(String command)throws Exception{
        try(ParcelFileDescriptor fd=getUiAutomation().executeShellCommand(command);FileInputStream in=new FileInputStream(fd.getFileDescriptor())){byte[] b=new byte[2048];while(in.read(b)!=-1){}}
        settle();
    }
    private void execute()throws Exception{

        edit(R.id.edtHoTen,"Võ Văn Quốc Bảo");edit(R.id.edtDienThoai,"0901234567");edit(R.id.edtEmail,"bao@example.com");shot("00_form");
        click(R.id.btnChiTiet);waitActivity("DetailActivity");
        check("A5-T01: explicit Intent receives Contact and sender",text(R.id.tvThongTin).contains("0901234567")&&text(R.id.tvNguoiGui).contains("A5_231A290036"));shot("01_chi_tiet");
        check("A5-NC2: Parcelable list received",text(R.id.tvNguoiGui).contains("1 liên hệ"));
        edit(R.id.edtHoTenMoi,"Võ Văn Quốc Bảo (đã sửa)");click(R.id.btnLuu);waitActivity("MainActivity");
        check("A5-T02: RESULT_OK updates name",text(R.id.edtHoTen).endsWith("(đã sửa)")&&text(R.id.tvKetQuaTraVe).contains("đã sửa"));shot("02_ket_qua_tra_ve");
        click(R.id.btnChiTiet);waitActivity("DetailActivity");back();waitActivity("MainActivity");
        check("A5-T03: Back returns RESULT_CANCELED",text(R.id.tvKetQuaTraVe).contains("đã hủy"));shot("03_huy_back");
        click(R.id.btnChiTiet);waitActivity("DetailActivity");edit(R.id.edtHoTenMoi,"Bản nháp chưa lưu");
        rotate(true);check("A5-T04: Intent and unsaved draft survive rotation",text(R.id.tvThongTin).contains("0901234567")&&text(R.id.edtHoTenMoi).equals("Bản nháp chưa lưu"));shot("04_xoay_giu_nhap");rotate(false);
        click(R.id.btnHuy);waitActivity("MainActivity");
        click(R.id.btnChiaSe);shot("05_chooser");back();waitActivity("MainActivity");
        check("A5-T05: share chooser returns with form preserved",text(R.id.edtDienThoai).equals("0901234567"));
        click(R.id.btnGoi);shot("06_goi_dien");back();waitActivity("MainActivity");
        check("A5-extra: ACTION_DIAL returns safely",text(R.id.edtDienThoai).equals("0901234567"));
        click(R.id.btnWeb);pause(1500);shot("07_web");back();waitActivity("MainActivity");
        Intent incoming=new Intent(getTargetContext(),MainActivity.class).setAction(Intent.ACTION_SEND).setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT,"Văn bản chia sẻ từ ứng dụng khác").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        active=startActivitySync(incoming);settle();
        check("A5-NC4: receives external ACTION_SEND text/plain",text(R.id.edtHoTen).equals("Văn bản chia sẻ từ ứng dụng khác"));shot("NC4_nhan_chia_se");
        edit(R.id.edtHoTen,"");click(R.id.btnChiTiet);check("A5-extra: empty name prevents navigation",!error(R.id.edtHoTen).isEmpty());
        Intent missing=new Intent(getTargetContext(),DetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        active=startActivitySync(missing);settle();check("A5-extra: null Contact disables Save safely",!view(R.id.btnLuu).isEnabled()&&text(R.id.tvThongTin).contains("Không nhận"));
        click(R.id.btnHuy);waitActivity("MainActivity");

    }
}
