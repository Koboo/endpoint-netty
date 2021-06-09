package eu.koboo.endpoint.core.codec.json;

import eu.koboo.endpoint.core.Endpoint;
import eu.koboo.endpoint.core.codec.AbstractEndpointCodec;
import eu.koboo.endpoint.core.util.BufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class JsonCodec extends AbstractEndpointCodec<JsonPacket> {

    public JsonCodec(Endpoint endpoint) {
        super(endpoint);
    }

    @Override
    public byte[] encodePacket(Channel channel, JsonPacket packet) throws Exception {

        ByteBuf payload = channel.alloc().buffer();

        int oid = endpoint.builder().getIdByClass(packet.getClass());

        if(oid != -1) {

            JSONObject object = new JSONObject();
            packet.write(object);

            String content = object.toString();

            BufUtils.writeVarInt(oid, payload);
            BufUtils.writeString(content, payload);

            return BufUtils.toArray(payload);
        }
        return null;
    }

    @Override
    public JsonPacket decodePacket(Channel channel, ByteBuf in) throws Exception {

        int oid = BufUtils.readVarInt(in);
        String content = BufUtils.readString(in);

        JSONParser parser = new JSONParser();

        JSONObject object = (JSONObject) parser.parse(content);

        Class<?> clazz = endpoint.builder().getClassById(oid);
        if(clazz != null) {

            JsonPacket jsonPacket = (JsonPacket) clazz.newInstance();
            jsonPacket.read(object);

            return jsonPacket;
        }
        return null;
    }

}

