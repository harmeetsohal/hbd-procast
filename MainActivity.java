package com.hbdprocast.billing;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends Activity {

    private WebView webView;
    private static final int PERMISSION_REQUEST = 100;
    private static final int FILE_CHOOSER_REQUEST = 101;
    private ValueCallback<Uri[]> filePathCallback;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full-screen immersive mode
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#040f26"));
            getWindow().setNavigationBarColor(Color.parseColor("#040f26"));
        }

        // Request legacy storage permissions (Android 6–9 only)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }, PERMISSION_REQUEST);
            }
        }

        // Create and configure WebView
        webView = new WebView(this);
        webView.setBackgroundColor(Color.parseColor("#040f26"));
        setContentView(webView);

        // Hide system UI for full immersive experience
        webView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        @SuppressLint("SetJavaScriptEnabled")
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        // Required for file:// to load local assets
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMediaPlaybackRequiresUserGesture(false);
        // Text size
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setMinimumFontSize(10);

        // Attach JavaScript bridge
        webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.evaluateJavascript(
                    "window.isAndroidApp = true;" +
                    "window.androidVersion = " + Build.VERSION.SDK_INT + ";" +
                    "window._hbdGoBack = function(){ AndroidBridge.goBack(); };" +
                    "console.log('HBD PROCAST Android " + Build.VERSION.SDK_INT + " Bridge Ready');",
                    null
                );
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView wv, ValueCallback<Uri[]> cb,
                    FileChooserParams params) {
                filePathCallback = cb;
                try {
                    startActivityForResult(params.createIntent(), FILE_CHOOSER_REQUEST);
                } catch (Exception e) {
                    filePathCallback = null;
                    return false;
                }
                return true;
            }
        });

        // Disable remote debugging in production
        WebView.setWebContentsDebuggingEnabled(false);

        // Load app from bundled assets
        webView.loadUrl("file:///android_asset/www/index.html");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST && filePathCallback != null) {
            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK && data != null) {
                results = new Uri[]{data.getData()};
            }
            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    // ── Android JavaScript Bridge ─────────────────────────────────
    public class AndroidBridge {

        @JavascriptInterface
        public boolean isAndroid() { return true; }

        @JavascriptInterface
        public int getSdkVersion() { return Build.VERSION.SDK_INT; }

        @JavascriptInterface
        public void goBack() {
            runOnUiThread(() -> {
                if (webView != null && webView.canGoBack()) webView.goBack();
                else finish();
            });
        }

        @JavascriptInterface
        public void showToast(String message) {
            runOnUiThread(() ->
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show()
            );
        }

        /** Save base64-encoded PDF to device storage */
        @JavascriptInterface
        public String savePdf(String base64Data, String fileName) {
            try {
                // Android 10+: use app-scoped external files (no permission needed)
                // Android ≤9: use public Downloads (requires WRITE_EXTERNAL_STORAGE)
                File dir;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    dir = new File(
                        getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                        "HBD Invoices"
                    );
                } else {
                    dir = new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        "HBD Invoices"
                    );
                }
                if (!dir.exists() && !dir.mkdirs()) {
                    return "error:Could not create directory";
                }

                // Sanitize filename
                String clean = fileName.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
                if (!clean.toLowerCase().endsWith(".pdf")) clean += ".pdf";
                File pdfFile = new File(dir, clean);

                byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
                try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                    fos.write(bytes);
                }

                return "saved:" + pdfFile.getAbsolutePath();
            } catch (Exception e) {
                return "error:" + e.getMessage();
            }
        }

        /** Share a PDF file via Android share sheet */
        @JavascriptInterface
        public void sharePdf(String filePath, String subject) {
            runOnUiThread(() -> {
                try {
                    File file = new File(filePath);
                    if (!file.exists()) {
                        Toast.makeText(MainActivity.this, "File not found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Uri uri = FileProvider.getUriForFile(
                        MainActivity.this,
                        getPackageName() + ".fileprovider",
                        file
                    );
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("application/pdf");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(intent, "Share Invoice via"));
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this,
                        "Share failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        /** Share to WhatsApp specifically */
        @JavascriptInterface
        public void shareWhatsApp(String filePath, String message) {
            runOnUiThread(() -> {
                try {
                    File file = new File(filePath);
                    if (!file.exists()) { sharePdf(filePath, message); return; }
                    Uri uri = FileProvider.getUriForFile(
                        MainActivity.this,
                        getPackageName() + ".fileprovider",
                        file
                    );
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("application/pdf");
                    intent.setPackage("com.whatsapp");
                    intent.putExtra(Intent.EXTRA_TEXT, message);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                } catch (Exception e) {
                    // WhatsApp not installed — fall back to generic share
                    sharePdf(filePath, message);
                }
            });
        }

        /** Trigger Android system print dialog */
        @JavascriptInterface
        public void printInvoice(String html) {
            runOnUiThread(() -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    PrintManager pm = (PrintManager) getSystemService(PRINT_SERVICE);
                    if (pm == null) return;
                    WebView pw = new WebView(MainActivity.this);
                    pw.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            PrintDocumentAdapter adapter =
                                view.createPrintDocumentAdapter("HBD Invoice");
                            PrintAttributes attrs = new PrintAttributes.Builder()
                                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                .setResolution(new PrintAttributes.Resolution(
                                    "hbd_pdf", "HBD PDF", 600, 600))
                                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                                .build();
                            pm.print("HBD_Invoice", adapter, attrs);
                        }
                    });
                    pw.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
                }
            });
        }

        /** Get the storage path where PDFs are saved */
        @JavascriptInterface
        public String getStoragePath() {
            File dir;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                dir = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "HBD Invoices");
            } else {
                dir = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "HBD Invoices"
                );
            }
            return dir.getAbsolutePath();
        }
    }
}
