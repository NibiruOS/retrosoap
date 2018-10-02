package io.github.nibiruos.retrosoap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import okhttp3.MediaType;

public class Utils {
    static MediaType XML_MEDIA_TYPE = MediaType.parse("application/xml; charset=utf-8");

    private  Utils() {
    }

    public static XmlPullParser buildXmlPullParser() {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            return factory.newPullParser();
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        }
    }
}
