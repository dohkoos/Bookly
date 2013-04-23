package com.codemany.bookly;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
    private static final int BARCODE_SCAN_REQUEST_CODE = 0;
    private Button searchButton;
    private Button favoriteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        searchButton = (Button)findViewById(R.id.btn_search);
        searchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        "com.google.zxing.client.android.SCAN");
                startActivityForResult(intent, BARCODE_SCAN_REQUEST_CODE);
            }
        });

        favoriteButton = (Button)findViewById(R.id.btn_favorite);
        favoriteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, FavoriteActivity.class);
                startActivity(intent);
            }
        });

        BookDao.initBookDao(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != BARCODE_SCAN_REQUEST_CODE || data == null) {
            return;
        }
        Intent intent = new Intent(this, SearchBookActivity.class);
        intent.putExtra("ISBN", data.getExtras().getString("SCAN_RESULT"));
        startActivity(intent);
    }
}
