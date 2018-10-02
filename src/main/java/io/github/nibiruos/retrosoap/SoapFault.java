package io.github.nibiruos.retrosoap;

public class SoapFault extends RuntimeException{
    public SoapFault(String message) {
        super(message);
    }
}
