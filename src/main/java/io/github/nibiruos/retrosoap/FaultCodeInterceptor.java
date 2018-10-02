package io.github.nibiruos.retrosoap;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class FaultCodeInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);

        // If response is not sucessful, create a new one with
        // same data and HTTP ok code so fault can be parsed.
        return response.isSuccessful()
                ? response
                : new Response.Builder()
                .request(request)
                .protocol(response.protocol())
                .message(response.message())
                .code(200)
                .headers(response.headers())
                .body(response.body())
                .build();
    }
}
