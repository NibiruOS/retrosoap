package io.github.nibiruos.retrosoap;

import okhttp3.RequestBody;
import okio.Buffer;
import org.junit.Before;
import org.junit.Test;
import org.xmlpull.mxp1_serializer.MXSerializer;
import org.xmlpull.v1.XmlSerializer;
import retrofit2.Converter;

import javax.inject.Provider;
import java.io.IOException;

import static io.github.nibiruos.retrosoap.Utils.XML_MEDIA_TYPE;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

public class RequestConverterTest {
    private Converter<String, RequestBody> requestConverter;
    private Provider<XmlSerializer> serializerProvider;

    @Before
    public void setup() {
        requestConverter = createMock(Converter.class);
        serializerProvider = createMock(Provider.class);
    }

    @Test
    public void testSoap1_2Conversion() throws IOException {
        // Expectations setup
        expect(requestConverter.convert("Patoruzu"))
                .andReturn(RequestBody.create(XML_MEDIA_TYPE,
                        "<name>PATORUZU</name>"));
        expect(serializerProvider.get())
                .andReturn(new MXSerializer());

        replay(requestConverter);
        replay(serializerProvider);

        // Test execution
        RequestBody body = new RequestConverter<>(requestConverter,
                serializerProvider,
                SoapSpec.V_1_2)
                .convert("Patoruzu");

        // Assertions
        assertEquals(XML_MEDIA_TYPE, body.contentType());

        Buffer buffer = new Buffer();
        body.writeTo(buffer);
        assertEquals("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope\">" +
                        "<soapenv:Header />" +
                        "<soapenv:Body>" +
                        "<name>PATORUZU</name>" +
                        "</soapenv:Body>" +
                        "</soapenv:Envelope>",
                buffer.readUtf8());

        // Mock verification
        verify(requestConverter);
        verify(serializerProvider);
    }

    @Test
    public void testSoap1_1Conversion() throws IOException {
        // Expectations setup
        expect(requestConverter.convert("Patoruzu"))
                .andReturn(RequestBody.create(XML_MEDIA_TYPE,
                        "<name>PATORUZU</name>"));
        expect(serializerProvider.get())
                .andReturn(new MXSerializer());

        replay(requestConverter);
        replay(serializerProvider);

        // Test execution
        RequestBody body = new RequestConverter<>(requestConverter,
                serializerProvider,
                SoapSpec.V_1_1)
                .convert("Patoruzu");

        // Assertions
        assertEquals(XML_MEDIA_TYPE,
                body.contentType());

        Buffer buffer = new Buffer();
        body.writeTo(buffer);
        assertEquals("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                        "<soapenv:Header />" +
                        "<soapenv:Body>" +
                        "<name>PATORUZU</name>" +
                        "</soapenv:Body>" +
                        "</soapenv:Envelope>",
                buffer.readUtf8());

        // Mock verification
        verify(requestConverter);
        verify(serializerProvider);
    }
}
