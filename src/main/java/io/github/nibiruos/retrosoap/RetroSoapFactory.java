package io.github.nibiruos.retrosoap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

import static java.util.Objects.requireNonNull;

public class RetroSoapFactory extends Converter.Factory {
    static final String BODY_TAG = "Body";
    static final String FAULT_TAG = "Fault";
    static final String FAULT_CODE_TAG = "faultcode";
    static final String FAULT_STRING_TAG = "faultstring";
    static final String ENVELOPE_TAG = "Envelope";
    static final String HEADER_TAG = "Header";
    static final String SOAP_ENVELOPE_PREFIX = "soapenv";

    private final Provider<XmlSerializer> serializerProvider;
    private final Provider<XmlPullParser> pullParserProvider;
    private final SoapSpec soapSpec;

    public static RetroSoapFactory create(@Nonnull Converter.Factory bodyFactory,
                                          @Nonnull Provider<XmlSerializer> serializerProvider,
                                          @Nonnull Provider<XmlPullParser> pullParserProvider,
                                          @Nonnull SoapSpec soapSpec) {
        requireNonNull(bodyFactory);
        requireNonNull(serializerProvider);
        requireNonNull(pullParserProvider);
        requireNonNull(soapSpec);
        return new RetroSoapFactory(bodyFactory,
                serializerProvider,
                pullParserProvider,
                soapSpec);
    }

    private final Converter.Factory bodyFactory;

    private RetroSoapFactory(Converter.Factory bodyFactory,
                             Provider<XmlSerializer> serializerProvider,
                             Provider<XmlPullParser> pullParserProvider,
                             SoapSpec soapSpec) {
        this.bodyFactory = bodyFactory;
        this.serializerProvider = serializerProvider;
        this.pullParserProvider = pullParserProvider;
        this.soapSpec = soapSpec;
    }

    @Override
    @Nullable
    public Converter<?, RequestBody> requestBodyConverter(Type type,
                                                          Annotation[] parameterAnnotations,
                                                          Annotation[] methodAnnotations,
                                                          Retrofit retrofit) {
        Converter<?, RequestBody> converter = bodyFactory.requestBodyConverter(type,
                parameterAnnotations,
                methodAnnotations,
                retrofit);
        return converter != null
                ? new RequestConverter<>((Converter<Object, RequestBody>) converter,
                serializerProvider,
                soapSpec)
                : null;
    }

    @Override
    @Nullable
    public Converter<ResponseBody, ?> responseBodyConverter(Type type,
                                                            Annotation[] annotations,
                                                            Retrofit retrofit) {
        Converter<ResponseBody, ?> converter = bodyFactory.responseBodyConverter(type,
                annotations,
                retrofit);
        return converter != null
                ? new ResponseConverter<>((Converter<ResponseBody, Object>) converter,
                pullParserProvider,
                serializerProvider,
                soapSpec)
                : null;
    }
}
