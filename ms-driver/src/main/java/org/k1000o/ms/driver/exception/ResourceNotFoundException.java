package org.k1000o.ms.driver.exception;

public class ResourceNotFoundException extends RuntimeException {

    private final String name;

    public ResourceNotFoundException(String name) {
        super( name);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
