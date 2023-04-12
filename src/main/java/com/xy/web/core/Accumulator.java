package com.xy.web.core;

import com.xy.web.exceptions.TooLongFrameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * buf Accumulator
 * - 完成接受到的数据的缓冲，从而实现数据的解析的处理
 * - 读取，不停，没有关系，异常停止，忽略就好
 */
public abstract class Accumulator {
    private Logger logger = LoggerFactory.getLogger(Accumulator.class);

    private ByteBuffer buf;

    private byte[] arr = new byte[10 * 1024];

    int headerSize = 0;

    public Accumulator() {
        this(50 * 1024);
    }

    public Accumulator(Integer capacity) {
        this.buf = ByteBuffer.allocateDirect(50 * 1024);
    }

    /**
     * 数据读取解析
     *
     * @param buffer 数据buf
     */
    public abstract void read0(ByteBuffer buffer, ParseStep step);

    /**
     * 需要解决是否需要对客户端断开进行处理
     *
     * @param e
     */
    public abstract void clientDisConnect(Exception e);


    public void doRead(InputStream is) {
        ParseStep step = ParseStep.READ_INITIAL;
        try {

            while (true) {
                // 读取数据一部分，如果没有数据了就读空了，就阻塞了，直到下一个消息的数据过来
                int readBytes = is.read(arr);

                // 读取到-1就结束了本次读取
                if (readBytes == -1) break;

                // 读取一次数据从socket，如果没有数据则会处于等待阻塞状态，如果能够读取满，则返回一次数据
                for (int i = 0; i < readBytes; i++) {
                    buf.put(arr[i]);
                }

                buf.flip();
                ByteBuffer readAbleBuf = buf.slice();

                read0(readAbleBuf, step);

                if (readAbleBuf.remaining() > 0) {
                    for (int i = readAbleBuf.position(); i < readAbleBuf.limit(); i++)
                        // 将没有消费的字节，继续放入buf中
                        buf.put(readAbleBuf.get());
                }
            }
        } catch (Exception e) {
            // 当读取释放则表示结束了，断开了客户端的连接
            clientDisConnect(e);
        }

    }

    protected void skipControlCharacters(ByteBuffer buffer) {
        for (; ; ) {
            char c = (char) buffer.get();
            if (!Character.isISOControl(c) &&
                    !Character.isWhitespace(c)) {
                break;
            }
        }
    }

    protected String[] readInial(ByteBuffer buf, int maxLen) {

        String first = readLine(buf, maxLen);
        if (null != first && first.length() > 0) {
            return first.split("" + (char) HttpCodecUtil.SP);
        }

        return null;
    }

    protected String readHeader(ByteBuffer buffer) {
        StringBuilder sb = new StringBuilder(64);
        loop:
        for (; ; ) {
            char nextByte = (char) buffer.get();
            switch (nextByte) {
                case HttpCodecUtil.CR:
                    nextByte = (char) buffer.get();
                    headerSize++;
                    if (nextByte == HttpCodecUtil.LF) {
                        // 直接就退出循环了，这样就解析完成了一条head行
                        break loop;
                    }
                    break;
                case HttpCodecUtil.LF:
                    break loop;
            }

            sb.append(nextByte);
        }

        return sb.toString();
    }

    protected String readLine(ByteBuffer buffer, int maxLineLength) throws TooLongFrameException {
        StringBuilder sb = new StringBuilder(64);
        int lineLength = 0;
        loop:
        for (; ; ) {
            byte nextByte = buffer.get();
            switch (nextByte) {
                case HttpCodecUtil.CR:
                    nextByte = buffer.get();
                    if (nextByte == HttpCodecUtil.LF) {
                        break loop;
                    }
                case HttpCodecUtil.LF:
                    break loop;
            }
            if (lineLength >= maxLineLength) {
                throw new TooLongFrameException(
                        "An HTTP line is larger than " + maxLineLength +
                                " bytes.");
            }
            lineLength++;
            sb.append((char) nextByte);
        }

        return sb.toString();
    }

    /**
     * 简单的完成字符换的切分
     *
     * @param line
     * @return
     */
    protected String[] splitHeader(String line) {
        String[] split = line.split(":");
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].trim();
        }
        return split;
    }
}