package com.yqc.endThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CloseResource {
	public static void main(String[] args) throws IOException {
		ExecutorService exec=Executors.newCachedThreadPool();
		ServerSocket server=new ServerSocket(8080);
	}
}
