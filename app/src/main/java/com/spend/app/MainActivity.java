package com.spend.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    WebView w;
    SharedPreferences sp;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.parseColor("#f5f5f7"));
        sp = getSharedPreferences("spend", MODE_PRIVATE);
        String u = sp.getString("url", "");
        if (u.isEmpty()) showSetup("");
        else openWeb(u);
    }

    void showSetup(String msg) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setBackgroundColor(Color.parseColor("#f5f5f7"));
        int p = (int) (24 * getResources().getDisplayMetrics().density);
        l.setPadding(p, p * 2, p, p);
        TextView t = new TextView(this);
        t.setText("Connect your Spend app");
        t.setTextSize(26);
        t.setTextColor(Color.BLACK);
        TextView d = new TextView(this);
        d.setText((msg.isEmpty() ? "" : msg + "\n\n") + "Paste your Apps Script web app URL (the one ending in /exec). You only do this once.");
        d.setPadding(0, p / 2, 0, p / 2);
        final EditText e = new EditText(this);
        e.setHint("https://script.google.com/macros/s/.../exec");
        e.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        e.setText(sp.getString("url", ""));
        Button s = new Button(this);
        s.setText("Save and open");
        s.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String x = e.getText().toString().trim();
                if (!x.startsWith("https://script.google.com/") && !x.startsWith("https://script.googleusercontent.com/")) {
                    Toast.makeText(MainActivity.this, "Enter a valid Apps Script web app URL", Toast.LENGTH_LONG).show();
                    return;
                }
                sp.edit().putString("url", x).apply();
                openWeb(x);
            }
        });
        l.addView(t); l.addView(d); l.addView(e); l.addView(s);
        setContentView(l);
    }

    void openWeb(String url) {
        w = new WebView(this);
        setContentView(w);
        WebSettings s = w.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        w.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                String h = r.getUrl().getHost();
                if (h != null && (h.endsWith("google.com") || h.endsWith("googleusercontent.com"))) return false;
                startActivity(new Intent(Intent.ACTION_VIEW, r.getUrl()));
                return true;
            }
            @Override public void onReceivedError(WebView v, WebResourceRequest r, WebResourceError err) {
                if (r.isForMainFrame()) showSetup("Could not load the app. Check your internet and the URL.");
            }
        });
        w.loadUrl(url);
    }

    // Hold the back button to change the URL
    @Override public boolean onKeyDown(int k, KeyEvent e) {
        if (k == KeyEvent.KEYCODE_BACK) { e.startTracking(); return true; }
        return super.onKeyDown(k, e);
    }
    @Override public boolean onKeyLongPress(int k, KeyEvent e) {
        if (k == KeyEvent.KEYCODE_BACK) { showSetup("Change URL"); w = null; return true; }
        return super.onKeyLongPress(k, e);
    }
    @Override public boolean onKeyUp(int k, KeyEvent e) {
        if (k == KeyEvent.KEYCODE_BACK && !e.isCanceled()) {
            if (w != null && w.canGoBack()) w.goBack(); else finish();
            return true;
        }
        return super.onKeyUp(k, e);
    }
}
