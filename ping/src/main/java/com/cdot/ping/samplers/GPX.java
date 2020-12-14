package com.cdot.ping.samplers;

import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.util.Log;

import com.cdot.ping.R;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static org.droidparts.Injector.getApplicationContext;

public class GPX {
    private static final String TAG = GPX.class.getSimpleName();

    // GPX
    public static final SimpleDateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    // Namespaces
    public static final String NS_GPX = "http://www.topografix.com/GPX/1/1";
    public static String NS_PING = "http://cdot.github.io/Ping/GPX";
    //xmlns:xsi=http://www.w3.org/2001/XMLSchema-instance
    //xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd

    static Document openDocument(ContentResolver cr, Uri uri) {
        Document gpxDocument = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setValidating(false);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Element gpxTrk;
            try {
                AssetFileDescriptor afd = cr.openAssetFileDescriptor(uri, "r");
                InputSource source = new InputSource(new FileInputStream(afd.getFileDescriptor()));
                Log.d(TAG, "Parsing existing content");
                gpxDocument = db.parse(source);
                afd.close();
                Log.d(TAG, "...existing content retained");
            } catch (Exception ouch) {
                Log.e(TAG, ouch.toString());
                Log.d(TAG, "Creating new document");
                gpxDocument = db.newDocument();
                gpxDocument.setDocumentURI(uri.toString());
                gpxDocument.setXmlVersion("1.1");
                gpxDocument.setXmlStandalone(true);
                Element gpxGpx = gpxDocument.createElementNS(GPX.NS_GPX, "gpx");
                gpxGpx.setAttribute("version", "1.1");
                gpxGpx.setAttribute("creator", getApplicationContext().getResources().getString(R.string.app_name));
                gpxDocument.appendChild(gpxGpx);
                gpxTrk = gpxDocument.createElementNS(GPX.NS_GPX, "trk");
                gpxGpx.appendChild(gpxTrk);
            }
        } catch (ParserConfigurationException ignore) {
        }
        return gpxDocument;
    }
}
