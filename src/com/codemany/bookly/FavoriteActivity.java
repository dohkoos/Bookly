package com.codemany.bookly;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;

public class FavoriteActivity extends Activity {
    private static final String TAG = "FavoriteActivity";

    private Button returnButton;
    private Button cleanButton;
    private Button deleteButton;
    private WebView favoriteView;

    private Handler handler = new Handler();

    private Set<String> deleteSet = new HashSet<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite_list);

        returnButton = (Button)findViewById(R.id.btn_return);
        returnButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        cleanButton = (Button)findViewById(R.id.btn_clean);
        cleanButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new Builder(FavoriteActivity.this);
                builder.setTitle("警告");
                builder.setMessage("是否真的要删除所有收藏夹条目？");
                builder.setPositiveButton("确认",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                BookDao.getInstance().deleteAll();
                                deleteSet.clear();
                                favoriteView.loadUrl("javascript:listFavorite();");
                                dialog.dismiss();
                            }
                        });
                builder.setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.create().show();
            }
        });

        deleteButton = (Button)findViewById(R.id.btn_delete);
        deleteButton.setEnabled(false);
        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                for (String isbn : deleteSet) {
                    BookDao.getInstance().delete(isbn);
                }
                deleteButton.setEnabled(false);
                favoriteView.loadUrl("javascript:listFavorite();");
            }
        });

        favoriteView = (WebView)findViewById(R.id.favorite_view);
        favoriteView.getSettings().setSupportZoom(false);
        favoriteView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        favoriteView.getSettings().setJavaScriptEnabled(true);

        favoriteView.loadUrl("file:///android_asset/favorite_list.html");

        favoriteView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public String getBookList() {
                return BookDao.getInstance().list().toString();
            }

            @JavascriptInterface
            public void getBookDetail(final String isbn) {
                Intent intent = new Intent(FavoriteActivity.this,
                        SearchBookActivity.class);
                intent.putExtra("ISBN", isbn);
                startActivity(intent);
            }

            @JavascriptInterface
            public void addDeleteItem(String isbn) {
                deleteSet.add(isbn);
                updateDeleteButtonState();
            }

            @JavascriptInterface
            public void removeDeleteItem(String isbn) {
                deleteSet.remove(isbn);
                updateDeleteButtonState();
            }

            private void updateDeleteButtonState() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        deleteButton.setEnabled(!deleteSet.isEmpty());
                    }
                });
            }
        }, "favoriteControl");

        favoriteView.setWebChromeClient(new WebChromeClient() {
            public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                Log.d(TAG, message + " -- From line "
                        + lineNumber + " of " + sourceID);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        favoriteView.loadUrl("javascript:listFavorite();");
    }
}
