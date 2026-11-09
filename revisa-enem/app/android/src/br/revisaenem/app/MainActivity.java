package br.revisaenem.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.Locale;

public class MainActivity extends Activity {
    private WebView web;
    private TextToSpeech tts;
    private volatile boolean ttsReady = false;

    // Pacotes do SmartTube (versões atuais e antigas) e do NewPipe, em ordem de preferência
    private static final String[] SMARTTUBE = {
        "org.smarttube.stable", "org.smarttube.beta", "app.smarttube.fdroid",
        "com.teamsmart.videomanager.tv", "com.liskovsoft.smartyoutubetv2.tv"
    };
    private static final String NEWPIPE = "org.schabi.newpipe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        web.setBackgroundColor(Color.TRANSPARENT);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        web.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, String url) {
                return handleUrl(url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                return handleUrl(r.getUrl().toString());
            }
        });
        web.addJavascriptInterface(new TtsBridge(), "AndroidTTS");
        web.addJavascriptInterface(new AppBridge(), "AndroidApp");
        setContentView(web);
        initTts();
        web.loadUrl("file:///android_asset/index.html");
    }

    private void initTts() {
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.SUCCESS || tts == null) return;
                int r = tts.setLanguage(new Locale("pt", "BR"));
                if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
                    r = tts.setLanguage(new Locale("pt"));
                }
                ttsReady = !(r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED);
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override public void onStart(String id) { }
                    @Override public void onDone(String id) { notifyJs(id, true); }
                    @SuppressWarnings("deprecation")
                    @Override public void onError(String id) { notifyJs(id, false); }
                    @Override public void onError(String id, int code) { notifyJs(id, false); }
                });
            }
        });
    }

    private void notifyJs(final String id, final boolean ok) {
        if (id == null || !id.matches("u\\d+")) return;
        web.post(new Runnable() {
            @Override
            public void run() {
                web.evaluateJavascript("window.__ttsDone&&window.__ttsDone('" + id + "'," + ok + ")", null);
            }
        });
    }

    private boolean handleUrl(String url) {
        if (url == null || url.startsWith("file:")) return false;
        Uri uri = Uri.parse(url);
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        boolean youtube = host.equals("youtube.com") || host.endsWith(".youtube.com") || host.equals("youtu.be");
        if (youtube) {
            for (String p : SMARTTUBE) if (launch(uri, p)) return true;
            if (launch(uri, NEWPIPE)) return true;
        }
        launch(uri, null);
        return true;
    }

    private boolean launch(Uri uri, String pkg) {
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (pkg != null) i.setPackage(pkg);
        try {
            startActivity(i);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        } catch (SecurityException e) {
            return false;
        }
    }

    private boolean installed(String pkg) {
        try {
            getPackageManager().getPackageInfo(pkg, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public class TtsBridge {
        @JavascriptInterface
        public boolean speak(String text, float rate, String id) {
            if (!ttsReady || tts == null || text == null) return false;
            try {
                tts.setSpeechRate(Math.max(0.5f, Math.min(2f, rate)));
                return tts.speak(text, TextToSpeech.QUEUE_ADD, new Bundle(), id) == TextToSpeech.SUCCESS;
            } catch (Exception e) {
                return false;
            }
        }

        @JavascriptInterface
        public void stop() {
            try { if (tts != null) tts.stop(); } catch (Exception e) { }
        }

        @JavascriptInterface
        public boolean ready() { return ttsReady; }
    }

    public class AppBridge {
        @JavascriptInterface
        public String videoApp() {
            for (String p : SMARTTUBE) if (installed(p)) return "SmartTube";
            if (installed(NEWPIPE)) return "NewPipe";
            return "";
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try { if (tts != null) tts.stop(); } catch (Exception e) { }
        web.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        web.onResume();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) { tts.shutdown(); tts = null; }
        web.destroy();
        super.onDestroy();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }
}
