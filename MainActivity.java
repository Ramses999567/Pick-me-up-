package com.example.pickmeup;
import android.app.Activity; import android.os.Bundle; import android.webkit.WebView; import android.webkit.WebSettings; import android.graphics.Color;
public class MainActivity extends Activity {
 @Override public void onCreate(Bundle b){super.onCreate(b); getWindow().setStatusBarColor(Color.rgb(17,19,24)); WebView w=new WebView(this); WebSettings s=w.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(false); s.setBuiltInZoomControls(false); w.setBackgroundColor(Color.rgb(17,19,24)); w.loadUrl("file:///android_asset/index.html"); setContentView(w);}
}
