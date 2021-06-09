package eu.koboo.endpoint.core.codec.json;

import org.json.simple.JSONObject;

public interface JsonPacket {

    void read(JSONObject json);

    void write(JSONObject json);

}

