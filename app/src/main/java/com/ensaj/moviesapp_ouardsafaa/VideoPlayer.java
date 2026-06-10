package com.ensaj.moviesapp_ouardsafaa;

import android.content.res.Configuration;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class VideoPlayer extends AppCompatActivity {

    private WebView webView;
    private String videoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videoUrl = getIntent().getStringExtra("videoUrl");
        webView = findViewById(R.id.webView);

        // ✅ Vérification que l'URL n'est pas null
        if (videoUrl == null || videoUrl.isEmpty()) {
            Toast.makeText(this, "Trailer non disponible", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ✅ Configuration améliorée du WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setDomStorageEnabled(true);

        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl(videoUrl);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (webView != null && videoUrl != null) {
            webView.loadUrl(videoUrl);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}