package io.github.nibiruos.retrosoap;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;

import javax.annotation.Nonnull;
import javax.inject.Provider;

import okhttp3.RequestBody;
import okio.Buffer;
import retrofit2.Converter;

import static io.github.nibiruos.retrosoap.RetroSoapFactory.*;
import static java.util.Objects.requireNonNull;

class RequestConverter<T>
        implements Converter<T, RequestBody> {
    private final Converter<T, okhttp3.RequestBody> bodyConverter;
    private final Provider<XmlSerializer> serializerProvider;
    private final SoapSpec soapSpec;

    RequestConverter(Converter<T, okhttp3.RequestBody> bodyConverter,
                     Provider<XmlSerializer> serializerProvider,
                     SoapSpec soapSpec) {
        this.bodyConverter = bodyConverter;
        this.serializerProvider = serializerProvider;
        this.soapSpec = soapSpec;
    }

    @Override
    public RequestBody convert(@Nonnull T data) throws IOException {
        requireNonNull(data);
        RequestBody requestBody = bodyConverter.convert(data);

        StringWriter output = new StringWriter();
        XmlSerializer serializer = serializerProvider.get();
        serializer.setOutput(output);
        serializer.setPrefix(SOAP_ENVELOPE_PREFIX,
                soapSpec.getEnvelopeNamespace());

        serializer.startTag(soapSpec.getEnvelopeNamespace(),
                ENVELOPE_TAG);
        serializer.startTag(soapSpec.getEnvelopeNamespace(),
                HEADER_TAG);
        serializer.endTag(soapSpec.getEnvelopeNamespace(),
                HEADER_TAG);
        serializer.startTag(soapSpec.getEnvelopeNamespace(),
                BODY_TAG);

        serializer.flush();

        Buffer bodyBuffer = new Buffer();
        requestBody.writeTo(bodyBuffer);
        output.write(bodyBuffer.readUtf8());

        serializer.endTag(soapSpec.getEnvelopeNamespace(),
                BODY_TAG);
        serializer.endTag(soapSpec.getEnvelopeNamespace(),
                ENVELOPE_TAG);
        serializer.endDocument();

        return RequestBody.create(requestBody.contentType(),
                output.toString());
    }
}
