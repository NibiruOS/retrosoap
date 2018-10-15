package io.github.nibiruos.retrosoap;

import okhttp3.ResponseBody;
import org.junit.Before;
import org.junit.Test;
import org.xmlpull.mxp1_serializer.MXSerializer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;
import retrofit2.Converter;

import javax.inject.Provider;
import java.io.IOException;

import static io.github.nibiruos.retrosoap.Utils.XML_MEDIA_TYPE;
import static io.github.nibiruos.retrosoap.Utils.buildXmlPullParser;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ResponseConverterTest {
    private Provider<XmlPullParser> pullParserProvider;
    private Converter<ResponseBody, String> responseConverter;
    private Provider<XmlSerializer> serializerProvider;

    @Before
    public void setup() {
        pullParserProvider = createMock(Provider.class);
        responseConverter = createMock(Converter.class);
        serializerProvider = createMock(Provider.class);
    }

    @Test
    public void testConvertSoap11() throws IOException {
        // Expectations setup
        expect(serializerProvider.get())
                .andReturn(new MXSerializer());
        expect(pullParserProvider.get())
                .andReturn(buildXmlPullParser());
        expect(responseConverter.convert(isA(ResponseBody.class))) // Using "isA" because ResponseBody does not implement equals()
                .andReturn("PATORUZU");

        replay(pullParserProvider);
        replay(responseConverter);
        replay(serializerProvider);

        // Test execution
        String body = new ResponseConverter<>(responseConverter,
                pullParserProvider,
                serializerProvider,
                SoapSpec.V_1_1)
                .convert(ResponseBody.create(XML_MEDIA_TYPE,
                        "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                                "<SOAP-ENV:Body>" +
                                "<data>Patoruzu</data>" +
                                "</SOAP-ENV:Body></SOAP-ENV:Envelope>"));

        // Assertions
        assertEquals("PATORUZU", body);

        // Mock verification
        verify(pullParserProvider);
        verify(responseConverter);
        verify(serializerProvider);
    }

    @Test
    public void testConvertSoap12() throws IOException {
        // Expectations setup
        expect(serializerProvider.get())
                .andReturn(new MXSerializer());
        expect(pullParserProvider.get())
                .andReturn(buildXmlPullParser());
        expect(responseConverter.convert(isA(ResponseBody.class))) // Using "isA" because ResponseBody does not implement equals()
                .andReturn("PATORUZU");

        replay(pullParserProvider);
        replay(responseConverter);
        replay(serializerProvider);

        // Test execution
        String body = new ResponseConverter<>(responseConverter,
                pullParserProvider,
                serializerProvider,
                SoapSpec.V_1_2)
                .convert(ResponseBody.create(XML_MEDIA_TYPE,
                        "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope\">" +
                                "<SOAP-ENV:Body>" +
                                "<data>Patoruzu</data>" +
                                "</SOAP-ENV:Body>" +
                                "</SOAP-ENV:Envelope>"));

        // Assertions
        assertEquals("PATORUZU", body);

        // Mock verification
        verify(pullParserProvider);
        verify(responseConverter);
        verify(serializerProvider);
    }

    @Test
    public void testConvertFault() throws IOException {
        // Expectations setup
        expect(serializerProvider.get())
                .andReturn(new MXSerializer());
        expect(pullParserProvider.get())
                .andReturn(buildXmlPullParser());

        replay(pullParserProvider);
        replay(responseConverter);
        replay(serializerProvider);

        // Test execution
        try {
            new ResponseConverter<>(responseConverter,
                    pullParserProvider,
                    serializerProvider,
                    SoapSpec.V_1_1)
                    .convert(ResponseBody.create(XML_MEDIA_TYPE,
                            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                                    "   <soapenv:Body>\n" +
                                    "      <soapenv:Fault>\n" +
                                    "         <faultcode xmlns:ns1=\"http://xml.apache.org/axis/\">ns1:coe.alreadyAuthenticated</faultcode>\n" +
                                    "         <faultstring>El CEE ya posee un TA valido para el acceso al WSN solicitado</faultstring>\n" +
                                    "         <detail>\n" +
                                    "            <ns2:exceptionName xmlns:ns2=\"http://xml.apache.org/axis/\">gov.afip.desein.dvadac.sua.view.wsaa.LoginFault</ns2:exceptionName>\n" +
                                    "            <ns3:hostname xmlns:ns3=\"http://xml.apache.org/axis/\">lujuria.afip.gov.ar</ns3:hostname>\n" +
                                    "         </detail>\n" +
                                    "      </soapenv:Fault>\n" +
                                    "   </soapenv:Body>\n" +
                                    "</soapenv:Envelope>"));
            fail("SoapFault expected");
        } catch (SoapFault expected) {
            // Assertions
            assertEquals("ns1:coe.alreadyAuthenticated - El CEE ya posee un TA valido para el acceso al WSN solicitado",
                    expected.getMessage());
        }

        // Mock verification
        verify(pullParserProvider);
        verify(responseConverter);
        verify(serializerProvider);
    }

}
