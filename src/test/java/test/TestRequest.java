package test;

import eu.koboo.endpoint.core.codec.NativePacket;
import eu.koboo.endpoint.core.util.BufUtils;
import io.netty.buffer.ByteBuf;

public class TestRequest implements NativePacket {

    String testString;
    long testLong;
    byte[] testBytes;

    public String getTestString() {
        return testString;
    }

    public TestRequest setTestString(String testString) {
        this.testString = testString;
        return this;
    }

    public long getTestLong() {
        return testLong;
    }

    public TestRequest setTestLong(long testLong) {
        this.testLong = testLong;
        return this;
    }

    public byte[] getTestBytes() {
        return testBytes;
    }

    public TestRequest setTestBytes(byte[] testBytes) {
        this.testBytes = testBytes;
        return this;
    }

    @Override
    public void read(ByteBuf byteBuf) {
        this.testString = BufUtils.readString(byteBuf);
        this.testLong = BufUtils.readVarLong(byteBuf);
        this.testBytes = BufUtils.readArray(byteBuf);
    }

    @Override
    public void write(ByteBuf byteBuf) {
        BufUtils.writeString(testString, byteBuf);
        BufUtils.writeVarLong(testLong, byteBuf);
        BufUtils.writeArray(testBytes, byteBuf);
    }
}