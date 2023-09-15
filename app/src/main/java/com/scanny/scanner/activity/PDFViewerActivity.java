package com.scanny.scanner.activity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.google.android.gms.ads.AdView;
import com.scanny.scanner.R;
import com.scanny.scanner.utils.AdsUtils;
import com.shockwave.pdfium.PdfDocument;

import java.util.List;

public class PDFViewerActivity extends BaseActivity implements OnPageChangeListener, OnLoadCompleteListener, OnPageErrorListener {
    private static final String TAG = "PDFViewerActivity";
    protected Uri pdf_uri;
    protected String title;
    protected TextView tv_page;
    protected TextView tv_title;
    private int page_no = 0;
    private PDFView pdfView;
    private AdView adView;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_pdfviewer);
        init();
    }

    private void init() {
        adView = findViewById(R.id.adView);
        AdsUtils.showGoogleBannerAd(this, adView);

        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_page = (TextView) findViewById(R.id.tv_page);
        pdfView = (PDFView) findViewById(R.id.pdfView);
        pdfView.setBackgroundColor(getResources().getColor(R.color.bg_color));
        title = getIntent().getStringExtra("title");
        tv_title.setText(title);
        pdf_uri = Uri.parse(getIntent().getStringExtra("pdf_path"));
        loadPDF(pdf_uri);
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void loadPDF(Uri uri) {
        pdfView.fromUri(uri).defaultPage(page_no).onPageChange(this).enableAnnotationRendering(true).onLoad(this).scrollHandle(new DefaultScrollHandle(this)).spacing(12).onPageError(this).load();
    }

    @Override
    public void loadComplete(int i) {
        PdfDocument.Meta documentMeta = pdfView.getDocumentMeta();
        Log.e(TAG, "title = " + documentMeta.getTitle());
        Log.e(TAG, "author = " + documentMeta.getAuthor());
        Log.e(TAG, "subject = " + documentMeta.getSubject());
        Log.e(TAG, "keywords = " + documentMeta.getKeywords());
        Log.e(TAG, "creator = " + documentMeta.getCreator());
        Log.e(TAG, "producer = " + documentMeta.getProducer());
        Log.e(TAG, "creationDate = " + documentMeta.getCreationDate());
        Log.e(TAG, "modDate = " + documentMeta.getModDate());
        printBookmarksTree(pdfView.getTableOfContents(), "-");
    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> list, String str) {
        for (PdfDocument.Bookmark next : list) {
            Log.e(TAG, String.format("%s %s, p %d", new Object[]{str, next.getTitle(), Long.valueOf(next.getPageIdx())}));
            if (next.hasChildren()) {
                List<PdfDocument.Bookmark> children = next.getChildren();
                printBookmarksTree(children, str + "-");
            }
        }
    }

    @Override
    public void onPageChanged(int i, int i2) {
        page_no = i;
        tv_page.setText(String.format("%s / %s", new Object[]{Integer.valueOf(i + 1), Integer.valueOf(i2)}));
    }

    @Override
    public void onPageError(int i, Throwable th) {
        Log.e(TAG, "Cannot load page " + i);
    }
}
