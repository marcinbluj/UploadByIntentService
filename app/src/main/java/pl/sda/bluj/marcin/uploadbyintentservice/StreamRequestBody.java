package pl.sda.bluj.marcin.uploadbyintentservice;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class StreamRequestBody extends RequestBody {

    private Uri uri;

    private Context context;

    public StreamRequestBody(Context context, Uri uri) {
        this.context = context;
        this.uri = uri;
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse("application/octet-stream");
    }

    @Override
    public long contentLength() {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        long size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
        cursor.close();

        return size;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        Source source = null;
        try {
            source = Okio.source(context.getContentResolver().openInputStream(uri));
            sink.writeAll(source);
        } finally {
            Util.closeQuietly(source);
        }
    }
}
