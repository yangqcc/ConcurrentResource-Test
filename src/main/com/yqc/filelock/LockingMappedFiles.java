package com.yqc.filelock;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class LockingMappedFiles {
	static final int LENGTH = 0x8FFFFF;

	public static void main(String[] args) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(new File("text.dat"), "rw");
		FileChannel fc = raf.getChannel();
		MappedByteBuffer out = fc.map(FileChannel.MapMode.READ_WRITE, 0, LENGTH);
		for (int i = 0; i < LENGTH; i++) {
			out.put((byte) 'x');
		}
		new LockAndModify(fc, out, 0, 0 + LENGTH / 2);
		new LockAndModify(fc, out, LENGTH / 2, LENGTH / 2 + LENGTH / 4);
	}

	private static class LockAndModify extends Thread {
		private ByteBuffer buff;
		private int start, end;
		FileChannel fc;

		LockAndModify(FileChannel fc, ByteBuffer mbb, int start, int end) {
			this.start = start;
			this.end = end;
			this.fc = fc;
			mbb.limit(end);
			mbb.position(start);
			buff = mbb.slice();
			start();
		}

		public void run() {
			try {
				FileLock fl = fc.lock(start, end, false);
				System.out.println("Locked:" + start + " to " + end);
				while (buff.position() < buff.limit() - 1) {
					buff.put((byte) (buff.get() + 1));
				}
				fl.release();
				System.out.println("Released:" + start + " to " + end);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			}
		}
	}
}
