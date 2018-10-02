package io.github.nibiruos.retrosoap;

import retrofit2.Retrofit;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static java.util.Objects.requireNonNull;

public class ServiceFactory {
    private final WsdlParser wsdlParser;
    private final Provider<Retrofit.Builder> retrofitBuilderProvider;

    @Inject
    public ServiceFactory(@Nonnull WsdlParser wsdlParser,
                          @Nonnull Provider<Retrofit.Builder> retrofitBuilderProvider) {
        this.wsdlParser = requireNonNull(wsdlParser);
        this.retrofitBuilderProvider = requireNonNull(retrofitBuilderProvider);
    }

    public <T> T createService(Class<T> serviceClass,
                               SoapSpec soapSpec,
                               String wsdlUrl,
                               String serviceName,
                               String portName) {
        String serviceUrl;
        try (InputStream wsdl = new BufferedInputStream(new URL(wsdlUrl)
                .openStream())) {
            serviceUrl = wsdlParser.findServicePortUrl(wsdl,
                    soapSpec,
                    serviceName,
                    portName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (serviceUrl == null) {
            throw new IllegalStateException(String
                    .format("URL for service %s and port %s not found",
                            serviceName,
                            portName));
        }

        if (!serviceUrl.endsWith("/")) {
            serviceUrl = serviceUrl + "/";
        }

        return retrofitBuilderProvider.get()
                .baseUrl(serviceUrl)
                .build()
                .create(serviceClass);

    }
}
