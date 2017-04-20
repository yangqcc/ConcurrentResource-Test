package com.yqc.chapter7;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ReaderThread extends Thread {
	private final Socket socket;
	private final InputStream in;

	public ReaderThread(Socket socket) throws IOException {
		this.socket = socket;
		this.in = socket.getInputStream();
	}

	@Override
	public void interrupt() {  //这里既能处理标准中断，也能处理关闭底层的套接字
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			super.interrupt();
		}
	}

	public void run() {
		try {
			byte[] buf = new byte[2014];
			while (true) {
				int count = in.read(buf);
				if (count < 0) {
					break;
				} else if (count > 0) {
//					processBuffer(buf, count);
				}
			}
		} catch (IOException e) {
			// 允许线程退出
		}
	}
}
