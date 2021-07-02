package eu.koboo.endpoint.core.primitive;

import eu.koboo.endpoint.core.codec.EndpointPacket;
import eu.koboo.endpoint.core.util.BufUtils;
import io.netty.buffer.ByteBuf;

import java.util.Map;

public class PrimitivePacket implements EndpointPacket {

    private PrimitiveMap primitiveMap;

    public PrimitiveMap getPrimitiveMap() {
        return primitiveMap;
    }

    public PrimitivePacket setPrimitiveMap(PrimitiveMap primitiveMap) {
        this.primitiveMap = primitiveMap;
        return this;
    }

    @Override
    public void read(ByteBuf byteBuf) {
        primitiveMap = new PrimitiveMap();
        int keyLength = BufUtils.readVarInt(byteBuf);
        for(int i = 0; i < keyLength; i++) {
            Primitive primitive = Primitive.valueOf(BufUtils.readString(byteBuf));
            String key = BufUtils.readString(byteBuf);
            Object object = PrimitiveUtils.Buf.readByteBuf(byteBuf, primitive);
            primitiveMap.put(key, object);
        }
    }

    @Override
    public void write(ByteBuf byteBuf) {
        int keyLength = primitiveMap.keySet().size();
        BufUtils.writeVarInt(keyLength, byteBuf);
        for (Map.Entry<String, Object> entry : primitiveMap.entrySet()) {
            String key = entry.getKey();
            Object object = entry.getValue();
            Primitive primitive = Primitive.of(object);
            if (primitive == null) {
                throw new IllegalArgumentException("Value " + object.getClass().getName() + " is not a primitive type!");
            }
            BufUtils.writeString(primitive.name(), byteBuf);
            BufUtils.writeString(key, byteBuf);
            PrimitiveUtils.Buf.writeByteBuf(byteBuf, object);
        }
    }
}
