package com.codemany.bookly;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class SearchBookActivity extends Activity {
    private static final String TAG = "SearchBookActivity";

    private static final String DOUBAN_BOOK_API = "http://api.douban.com/book/subject/isbn/";

    private Button returnButton;
    private Button addFavoriteButton;
    private WebView resultView;
    private Book book;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadPage();
    }

    // Uses AsyncTask subclass to download the XML feed from douban.com.
    private void loadPage() {
        new DownloadXmlTask().execute(getIntent().getExtras().getString("ISBN"));
    }

    private class DownloadXmlTask extends AsyncTask<String, Void, Book> {
        @Override
        protected Book doInBackground(String... isbns) {
            try {
                return getResultByIsbn(isbns[0]);
            } catch (Exception e) {
                Log.e(TAG, "Cause>>", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Book result) {
            setContentView(R.layout.search_result);
            book = result;

            returnButton = (Button)findViewById(R.id.btn_return);
            returnButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    SearchBookActivity.this.finish();
                }
            });

            addFavoriteButton = (Button)findViewById(R.id.btn_add_favorite);

            if (BookDao.getInstance().get(book.getIsbn()) == null) {
                setAddFavorite();
            } else {
                setHasAddFavorite();
            }
            resultView = (WebView)findViewById(R.id.result_view);
            resultView.getSettings().setSupportZoom(false);
            resultView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            resultView.getSettings().setJavaScriptEnabled(true);

            resultView.loadUrl("file:///android_asset/book_details.html");

            resultView.addJavascriptInterface(book, "book");

            resultView.setWebChromeClient(new WebChromeClient() {
                public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                    Log.d(TAG, message + " -- From line "
                            + lineNumber + " of " + sourceID);
                }
            });
        }
    }

    private void setAddFavorite() {
        addFavoriteButton.setText("收藏");
        addFavoriteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BookDao.getInstance().add(book);
                setHasAddFavorite();
            }
        });
    }

    private void setHasAddFavorite() {
        addFavoriteButton.setText("取消收藏");
        addFavoriteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BookDao.getInstance().delete(book.getIsbn());
                setAddFavorite();
            }
        });
    }

    private Book getResultByIsbn(String isbn) throws IOException, IllegalStateException,
            XmlPullParserException {

        HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
                System.getProperty("http.agent"));
        Log.d(TAG, "Agent: " + System.getProperty("http.agent"));

        HttpGet request = new HttpGet(DOUBAN_BOOK_API + isbn);
        Log.d(TAG, "Book API: " + request.getURI().toString());

        HttpResponse response = client.execute(request);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new IllegalStateException("Status Code " + response.getStatusLine().getStatusCode());
        }
        return readEntry(response.getEntity().getContent());
    }

    private Book readEntry(InputStream inputStream) throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(inputStream, "utf-8");

        Book book = new Book();
        book.setIsbn(getIntent().getExtras().getString("ISBN"));

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
            case XmlPullParser.START_TAG:
                if ("link".equals(parser.getName())
                        && "image".equals(parser.getAttributeValue(null, "rel"))) {
                    book.setImageUrl(parser.getAttributeValue(null, "href"));
                    Log.d(TAG, "image>>" + book.getImageUrl());
                    eventType = parser.next();
                } else if ("summary".equals(parser.getName())) {
                    book.setSummary(parser.nextText());
                    Log.d(TAG, "summary>>" + book.getSummary());
                } else if ("attribute".equals(parser.getName())) {
                    String name = parser.getAttributeValue(null, "name");
                    if ("title".equals(name)) {
                        book.setTitle(parser.nextText());
                        Log.d(TAG, "title>>" + book.getTitle());
                    } else if ("author".equals(name)) {
                        book.setAuthor(parser.nextText());
                        Log.d(TAG, "author>>" + book.getAuthor());
                    } else if ("publisher".equals(name)) {
                        book.setPublisher(parser.nextText());
                        Log.d(TAG, "publisher>>" + book.getPublisher());
                    }
                }
                break;
            case XmlPullParser.END_TAG:
                break;
            }
            eventType = parser.next();
        }

        return book;
    }
}
