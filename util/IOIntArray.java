package util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.AbstractList;

//You don't have to look at this class!
public class IOIntArray extends AbstractList<Integer> {
    private ByteBuffer byteBuffer;
    private IntBuffer intBuffer;

    public IOIntArray(int size) {
        byteBuffer = ByteBuffer.allocate(size * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        intBuffer = byteBuffer.asIntBuffer();
    }

    public int size() {
        return intBuffer.limit();
    }

    public Integer get(int i) {
        return intBuffer.get(i);
    }

    public Integer set(int i, Integer x) {
        int old = intBuffer.get(i);
        intBuffer.put(i, x);
        return old;
    }

    public void readFromDisk(String filename) throws IOException {
        try (FileChannel channel = FileChannel.open(Path.of(filename), StandardOpenOption.READ)) {
            channel.read(byteBuffer);
        }
    }

    public void writeToDisk(String filename) throws IOException {
        Files.write(Path.of(filename), byteBuffer.array());
        try (FileChannel channel = FileChannel.open(Path.of(filename),
            StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            channel.write(byteBuffer);
        }
    }
}
