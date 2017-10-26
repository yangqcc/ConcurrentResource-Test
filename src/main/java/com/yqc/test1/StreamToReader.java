package com.yqc.test1;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

/**
 * InputStream转化为InputReader
 * @author yangqc
 * 2016年7月13日
 */
public class StreamToReader {
	public static void main(String[] args) throws IOException {
		InputStream inputStream = new FileInputStream("C:\\Users\\Administrator\\Desktop\\hello.txt");
		Reader reader = new InputStreamReader(inputStream);
		//-1是一个int类型，不是byte或者char类型，这是不一样的。
		int data = reader.read();
		while (data != -1) {
			// 这里不会造成数据丢失，因为返回的int类型变量data只有低16位有数据，高16位没有数据
			char theChar = (char) data;
			data = reader.read();
			System.out.print(theChar);
		}
		reader.close();
		OutputStream os=new OutputStream() {
			@Override
			public void write(int b) throws IOException {
			}
		};
	}
}
