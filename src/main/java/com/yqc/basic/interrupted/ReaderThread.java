package com.yqc.basic.interrupted;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * interrupt方法将非标准的取消封装在Thread中
 *
 * @author yangqc
 * 2016年9月1日
 */
public class ReaderThread extends Thread {
    private final Socket socket;
    private final InputStream in;

    public ReaderThread(Socket socket) throws IOException {
        this.socket = socket;
        this.in = socket.getInputStream();
    }

    public void interrupt() {
        try {
            socket.close();
        } catch (IOException e) {
        } finally {
            super.interrupt();
        }
    }

    public void run() {
        try {
            byte[] buf = new byte[1024];
            while (true) {
                int count = in.read(buf);
                if (count < 0) {
                    break;
                } else if (count < 0) {
                    // 操作
                }
            }
        } catch (IOException e) {
            /*
             * 允许线程退出
			 */
        }
    }
}
