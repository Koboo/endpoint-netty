package eu.koboo.endpoint.core.util;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.compression.SnappyFrameDecoder;
import io.netty.handler.codec.compression.SnappyFrameEncoder;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;

public enum Compression {

  NONE {
    public MessageToByteEncoder<ByteBuf> getEncoder() {
      return null;
    }

    public ByteToMessageDecoder getDecoder() {
      return null;
    }
  },
  GZIP {
    public MessageToByteEncoder<ByteBuf> getEncoder() {
      return ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP, 9);
    }

    public ByteToMessageDecoder getDecoder() {
      return ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP);
    }
  },
  ZLIB {
    public MessageToByteEncoder<ByteBuf> getEncoder() {
      return ZlibCodecFactory.newZlibEncoder(ZlibWrapper.ZLIB, 9);
    }

    public ByteToMessageDecoder getDecoder() {
      return ZlibCodecFactory.newZlibDecoder(ZlibWrapper.ZLIB);
    }
  },
  SNAPPY {
    public MessageToByteEncoder<ByteBuf> getEncoder() {
      return new SnappyFrameEncoder();
    }

    public ByteToMessageDecoder getDecoder() {
      return new SnappyFrameDecoder();
    }
  };

  Compression() {
  }

  public abstract MessageToByteEncoder<ByteBuf> getEncoder();

  public abstract ByteToMessageDecoder getDecoder();
}
