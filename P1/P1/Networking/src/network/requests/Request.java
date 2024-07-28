package network.requests;

import java.io.Serializable;

public class Request implements Serializable {
    private RequestType type;
    private Object data;

    public RequestType getType() {
        return type;
    }

    public Request setType(RequestType type) {
        this.type = type;
        return this;
    }

    public Object getData() {
        return data;
    }

    public Request setData(Object data) {
        this.data = data;
        return this;
    }
}
