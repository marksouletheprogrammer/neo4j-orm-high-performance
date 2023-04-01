package software.msoule.demo;

public class SerializationError extends RuntimeException {

    private final Object toSerialize;

    public SerializationError(Throwable cause, Object toSerialize) {
        super(cause);
        this.toSerialize = toSerialize;
    }
}
