package eu.koboo.endpoint.core.transfer;

import eu.koboo.endpoint.core.codec.EndpointPacket;
import eu.koboo.endpoint.core.util.BufUtils;
import io.netty.buffer.ByteBuf;

import java.util.Map;

public class TransferMapPacket implements EndpointPacket {

    private TransferMap transferMap;

    public TransferMap getTransferMap() {
        return transferMap;
    }

    public TransferMapPacket setTransferMap(TransferMap transferMap) {
        this.transferMap = transferMap;
        return this;
    }

    @Override
    public void read(ByteBuf byteBuf) {
        transferMap = new TransferMap();
        int keyLength = BufUtils.readVarInt(byteBuf);
        for(int i = 0; i < keyLength; i++) {
            Primitive primitive = Primitive.valueOf(BufUtils.readString(byteBuf));
            String key = BufUtils.readString(byteBuf);
            Object object = PrimitiveUtils.readByteBuf(byteBuf, primitive);
            transferMap.put(key, object);
        }
    }

    @Override
    public void write(ByteBuf byteBuf) {
        int keyLength = transferMap.keySet().size();
        BufUtils.writeVarInt(keyLength, byteBuf);
        for (Map.Entry<String, Object> entry : transferMap.entrySet()) {
            String key = entry.getKey();
            Object object = entry.getValue();
            Primitive primitive = Primitive.of(object);
            if (primitive == null) {
                throw new IllegalArgumentException("Value " + object.getClass().getName() + " is not a primitive type!");
            }
            BufUtils.writeString(primitive.name(), byteBuf);
            BufUtils.writeString(key, byteBuf);
            PrimitiveUtils.writeByteBuf(byteBuf, object);
        }
    }
}
