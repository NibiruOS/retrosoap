package io.github.nibiruos.retrosoap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.xml.namespace.QName;

import static java.util.Objects.requireNonNull;

public class WsdlParser {
    private static final String NAME_ATTRIBUTE = "name";
    private static final String LOCATION_ATTRIBUTE = "location";
    private final Provider<XmlPullParser> pullParserProvider;

    @Inject
    public WsdlParser(@Nonnull Provider<XmlPullParser> pullParserProvider) {
        this.pullParserProvider = requireNonNull(pullParserProvider);
    }

    @Nullable
    public String findServicePortUrl(@Nonnull InputStream wsdl,
                                     @Nonnull SoapSpec soapSpec,
                                     @Nullable String serviceName,
                                     @Nullable String portName) {
        requireNonNull(soapSpec);
        QName serviceTag = new QName(soapSpec.getWsdlNamespace(),
                "service");
        QName portTag = new QName(soapSpec.getWsdlNamespace(),
                "port");
        QName addressTag = new QName(soapSpec.getWsdlSoapNamespace(),
                "address");
        try {
            XmlPullParser pullParser = pullParserProvider.get();
            pullParser.setInput(wsdl, null);
            ParserAdapter parser = new ParserAdapter(pullParser);

            boolean serviceOk = false;
            boolean portOk = false;
            boolean addressOk = false;
            parser.next();
            while (!parser.isDocumentEnd()) {
                if (isTagStart(parser, serviceTag, serviceName)) {
                    serviceOk = true;
                } else if (parser.isTagEnd(serviceTag)) {
                    serviceOk = false;
                }
                if (serviceOk) {
                    if (isTagStart(parser, portTag, portName)) {
                        portOk = true;
                    } else if (parser.isTagEnd(portTag)) {
                        portOk = false;
                    }
                    if (portOk) {
                        if (parser.isTagStart(addressTag)) {
                            addressOk = true;
                        } else if (parser.isTagEnd(addressTag)) {
                            addressOk = false;
                        }
                    }
                }
                if (serviceOk && portOk && addressOk) {
                    return parser.getAttribute(LOCATION_ATTRIBUTE);
                }
                parser.next();
            }

            return null;
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isDocumentEnd(XmlPullParser parser)
            throws XmlPullParserException {
        return parser.getEventType() == XmlPullParser.END_DOCUMENT;
    }

    private static boolean isTagStart(ParserAdapter parser,
                                      QName tag,
                                      String name)
            throws XmlPullParserException {
        return parser.isTagStart(tag)
                && (name == null
                || name.equals(parser.getAttribute(NAME_ATTRIBUTE)));
    }
}
