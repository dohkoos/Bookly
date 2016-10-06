package com.codemany.bookly;

import android.webkit.JavascriptInterface;

public class Book {
    private String title;
    private String author;
    private String isbn;
    private String summary;
    private String imageUrl;
    private String publisher;

    @JavascriptInterface
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @JavascriptInterface
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        if (this.author != null) {
            this.author += ", " + author;
        } else {
            this.author = author;
        }
    }

    @JavascriptInterface
    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    @JavascriptInterface
    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @JavascriptInterface
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @JavascriptInterface
    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
}
