package com.yqc.basic.stop;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import util.concurrent.Executors;

public class CloseResource {
    public static void main(String[] args) throws IOException {
        ExecutorService exec = Executors.newCachedThreadPool();
        ServerSocket server = new ServerSocket(8080);
    }
}
