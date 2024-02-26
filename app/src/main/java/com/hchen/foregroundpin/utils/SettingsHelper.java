package com.hchen.foregroundpin.utils;

import com.hchen.foregroundpin.callback.IThreadWrite;
import com.hchen.foregroundpin.utils.shell.ShellExec;
import com.hchen.foregroundpin.utils.shell.ShellInit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsHelper {
    public static void write(String command) {
        ShellExec shellExec = ShellInit.getShell();
        shellExec.run(command).sync();
    }

    public static void threadWrite(IThreadWrite threadWrite) {
        ExecutorService executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors());
        executorService.submit(threadWrite::thread);
    }
}
