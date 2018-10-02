package io.github.nibiruos.retrosoap;

public enum SoapSpec {
    V_1_2("http://schemas.xmlsoap.org/soap/envelope",
            "http://schemas.xmlsoap.org/wsdl/",
            "http://schemas.xmlsoap.org/wsdl/soap12/"),
    V_1_1("http://schemas.xmlsoap.org/soap/envelope/",
            "http://schemas.xmlsoap.org/wsdl/",
            "http://schemas.xmlsoap.org/wsdl/soap/");

    private final String envelopeNamespace;
    private final String wsdlNamespace;
    private final String wsdlSoapNamespace;

    SoapSpec(String envelopeNamespace,
             String wsdlNamespace,
             String wsdlSoapNamespace) {
        this.envelopeNamespace = envelopeNamespace;
        this.wsdlNamespace = wsdlNamespace;
        this.wsdlSoapNamespace = wsdlSoapNamespace;
    }

    public String getEnvelopeNamespace() {
        return envelopeNamespace;
    }

    public String getWsdlNamespace() {
        return wsdlNamespace;
    }

    public String getWsdlSoapNamespace() {
        return wsdlSoapNamespace;
    }
}
