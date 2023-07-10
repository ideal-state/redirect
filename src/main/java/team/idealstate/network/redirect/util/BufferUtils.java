package team.idealstate.network.redirect.util;

import java.nio.ByteBuffer;

/**
 * <p>BufferUtils</p>
 *
 * <p>Created on 2023/7/9 7:32</p>
 *
 * @author ketikai
 * @since 1.0.0
 */
public abstract class BufferUtils {

    public static ByteBuffer copyToBuffer(ByteBuffer original) {
        final ByteBuffer target = ByteBuffer.allocate(original.capacity());
        original.rewind();
        target.put(original);
        original.rewind();
        target.flip();
        return target;
    }

    public static byte[] copyToBytes(ByteBuffer original) {
        original.flip();
        final int len = original.limit() - original.position();
        final byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++) {
            bytes[i] = original.get();
        }
        return bytes;
    }
}
