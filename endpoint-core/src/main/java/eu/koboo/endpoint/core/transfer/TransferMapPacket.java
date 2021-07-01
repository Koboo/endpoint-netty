package eu.koboo.endpoint.core.transfer;

import eu.koboo.endpoint.core.codec.EndpointPacket;
import eu.koboo.endpoint.core.util.BufUtils;
import io.netty.buffer.ByteBuf;

public class TransferMapPacket implements EndpointPacket {

    private TransferMap transferMap;

    public TransferMap getTransferMap() {
        return transferMap;
    }

    public void setTransferMap(TransferMap transferMap) {
        this.transferMap = transferMap;
    }

    @Override
    public void read(ByteBuf byteBuf) {
        byte[] encoded = BufUtils.readArray(byteBuf);
        setTransferMap(TransferMap.decode(encoded));
    }

    @Override
    public void write(ByteBuf byteBuf) {
        byte[] encoded = TransferMap.encode(getTransferMap());
        BufUtils.writeArray(encoded, byteBuf);
    }
}
