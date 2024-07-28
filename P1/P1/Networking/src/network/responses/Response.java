package network.responses;

import java.io.Serializable;

public class Response implements Serializable {
    private ResponseType type;
    private Object data;

    public ResponseType getType() {
        return type;
    }

    public Response setType(ResponseType type) {
        this.type = type;
        return this;
    }

    public Object getData() {
        return data;
    }

    public Response setData(Object data) {
        this.data = data;
        return this;
    }
}
