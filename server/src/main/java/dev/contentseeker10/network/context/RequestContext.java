package dev.contentseeker10.network.context;

public class RequestContext<T> {

    private final T body;
    private final ConnectionContext connection;

    public RequestContext(T body, ConnectionContext connection) {
        this.body = body;
        this.connection = connection;
    }

    public T getBody() {
        return body;
    }

    public ConnectionContext getConnection() {
        return connection;
    }
}
