package pl.zimi.repository;

public class TypedValue<T> extends Value {
    public TypedValue(T object, String failureReason) {
        super(object, failureReason);
    }

    public T getValue() {
        return (T)getObject();
    }
}
