package io.github.nibiruos.retrosoap;

import io.github.nibiruos.retrosoap.ParserAdapter.Attribute;
import okhttp3.ResponseBody;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
import retrofit2.Converter;

import javax.annotation.Nonnull;
import javax.inject.Provider;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.StringWriter;

import static java.util.Objects.requireNonNull;

class ResponseConverter<T>
        implements Converter<ResponseBody, T> {
    private final Converter<ResponseBody, T> bodyConverter;
    private final Provider<XmlPullParser> pullParserProvider;
    private final Provider<XmlSerializer> serializerProvider;
    private final SoapSpec soapSpec;

    ResponseConverter(Converter<ResponseBody, T> bodyConverter,
                      Provider<XmlPullParser> pullParserProvider,
                      Provider<XmlSerializer> serializerProvider,
                      SoapSpec soapSpec) {
        this.bodyConverter = bodyConverter;
        this.serializerProvider = serializerProvider;
        this.pullParserProvider = pullParserProvider;
        this.soapSpec = soapSpec;
    }

    @Override
    public T convert(@Nonnull ResponseBody responseBody) throws IOException {
        requireNonNull(responseBody);
        try {
            StringWriter output = new StringWriter();
            XmlSerializer serializer = serializerProvider.get();
            serializer.setOutput(output);

            XmlPullParser pullParser = pullParserProvider.get();
            pullParser.setInput(responseBody.byteStream(), null);
            ParserAdapter parser = new ParserAdapter(pullParser);

            QName body = new QName(soapSpec.getEnvelopeNamespace(),
                    RetroSoapFactory.BODY_TAG);

            parser.next();
            while (!parser.isTagStart(body)) {
                checkDocumentEnd(parser, "Body start");
                parser.next();
            }

            parser.next();
            while (!parser.isTagStart() && !parser.isDocumentEnd()) {
                parser.next();
            }

            throwFault(parser);

            while (!parser.isTagEnd(body)) {
                checkDocumentEnd(parser, "Body end");
                switch (parser.getEventType()) {
                    case XmlPullParser.START_TAG:
                        registerPrefix(parser.getPrefix(),
                                parser.getNamespace(),
                                serializer);
                        for (Attribute attribute : parser.getAttributes()) {
                            registerPrefix(attribute.getPrefix(),
                                    parser.getNamespace(),
                                    serializer);
                        }
                        serializer.startTag(lookupNamespace(parser),
                                parser.getName());
                        for (Attribute attribute : parser.getAttributes()) {
                            serializer.attribute(lookupNamespace(attribute.getPrefix(),
                                    attribute.getNamespace(),
                                    parser),
                                    attribute.getName(),
                                    attribute.getValue());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        serializer.endTag(lookupNamespace(parser),
                                parser.getName());
                        break;
                    default:
                        serializer.text(parser.getText());
                }
                parser.next();
            }
            serializer.endDocument();

            return bodyConverter.convert(ResponseBody.create(responseBody.contentType(),
                    output.toString()));

        } catch (XmlPullParserException ex) {
            throw new IOException(ex);
        }
    }

    private void throwFault(ParserAdapter parser) {
        QName fault = new QName(soapSpec.getEnvelopeNamespace(),
                RetroSoapFactory.FAULT_TAG);

        if (parser.isTagStart(fault)) {
            boolean inCode = false;
            boolean inString = false;

            QName faultCode = new QName("",
                    RetroSoapFactory.FAULT_CODE_TAG);

            QName faultString = new QName("",
                    RetroSoapFactory.FAULT_STRING_TAG);

            StringBuilder code = new StringBuilder();
            StringBuilder string = new StringBuilder();
            parser.next();

            while (!parser.isTagEnd(fault) || parser.isDocumentEnd()) {
                if (parser.isTagEnd(faultCode)) {
                    inCode = false;
                } else if (parser.isTagEnd(faultString)) {
                    inString = false;
                }
                if (inCode) {
                    code.append(parser.getText());
                } else if (inString) {
                    string.append(parser.getText());
                }
                if (parser.isTagStart(faultCode)) {
                    inCode = true;
                } else if (parser.isTagStart(faultString)) {
                    inString = true;
                }
                parser.next();
            }
            throw new SoapFault(code.toString(),
                    string.toString());
        }
    }

    private static void checkDocumentEnd(ParserAdapter parser, String requiredTag) {
        if (parser.isDocumentEnd()) {
            throw new RuntimeException(requiredTag + " tag not found.");
        }
    }

    private static String lookupNamespace(String prefix,
                                          String namespace,
                                          ParserAdapter parser) {
        return namespace != null
                ? namespace
                : parser.getNamespace(prefix);
    }

    private static String lookupNamespace(ParserAdapter parser) {
        return lookupNamespace(parser.getPrefix(),
                parser.getNamespace(),
                parser);
    }

    private static void registerPrefix(String prefix,
                                       String namespace,
                                       XmlSerializer serializer) throws IOException {
        if (namespace != null && prefix != null) {
            serializer.setPrefix(prefix, namespace);
        }
    }

}
