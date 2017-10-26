package com.yqc.locks.filelock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.util.concurrent.TimeUnit;

/**
 * 文件加锁
 *
 * @author Administrator
 */
public class FileLocking {
    public static void main(String[] args) throws IOException, InterruptedException {
        FileOutputStream fos = new FileOutputStream(new File("file.txt"));
        FileLock fl = fos.getChannel().tryLock();
        if (fl != null) {
            System.out.println("Locked File");
            TimeUnit.MICROSECONDS.sleep(10000);
            fl.release();
            FileOutputStream fs = new FileOutputStream("file.txt");
            fs.write(12);
            System.out.println("Released Lock");
        }
        fos.close();
    }
}
