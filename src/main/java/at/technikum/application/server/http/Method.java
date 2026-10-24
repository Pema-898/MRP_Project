package at.technikum.application.server.http;

public enum Method {
    GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD;

    public static Method from(String value) {
        return Method.valueOf(value.toUpperCase());
    }
}
