package io.github.nibiruos.retrosoap;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class SoapFault extends RuntimeException {
    private final String code;
    private final String string;

    public SoapFault(@Nonnull String code,
                     @Nonnull String string) {
        super(code + " - " + string);
        this.code = requireNonNull(code);
        this.string = requireNonNull(string);
    }

    public String getCode() {
        return code;
    }

    public String getString() {
        return string;
    }
}
