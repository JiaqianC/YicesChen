package aprove.GraphUserInterface.Factories.Solvers.Engines;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import aprove.*;
import aprove.Framework.Logic.*;
import aprove.Framework.PropositionalLogic.*;
import aprove.Framework.PropositionalLogic.Formulae.*;
import aprove.Framework.PropositionalLogic.SMTLIB.*;
import aprove.Framework.PropositionalLogic.SMTLIB.SMTLIBFunctions.*;
import aprove.Framework.Utility.GenericStructures.*;
import aprove.Framework.Utility.SMTUtility.*;
import aprove.Strategies.Abortions.*;
import aprove.Strategies.Annotations.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ToRunYices {

    public static void main(String[] args) {
        List<String> commands = new ArrayList<>();
        //commands.add("echo hello world");
        //commands.add("dir");
        //commands.add("ping www.baidu.com");
        commands.add("yices -h");
        //commands.add("yices");
        //commands.add("(define b::(bitvector 4))");
        //commands.add("(assert (= b (bv-add 0b0010 0b0011)))");
        commands.add("(check)");


        startProcesses(commands);
    }

    public static void startProcesses(List<String> commands) {
        ExecutorService executor = Executors.newFixedThreadPool(commands.size());
        List<Future<String>> futures = new ArrayList<>();

        for (String command : commands) {
            Callable<String> task = new ProcessTask(command);
            futures.add(executor.submit(task));
        }

        for (Future<String> future : futures) {
            try {
                String result = future.get();
                System.out.println(result);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
    }

    private static class ProcessTask implements Callable<String> {
        private final String command;

        public ProcessTask(String command) {
            this.command = command;
        }

        @Override
        public String call() throws Exception {
            ProcessBuilder builder = new ProcessBuilder(command.split(" "));
            builder.redirectErrorStream(true);

            Process process = builder.start();
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            process.waitFor();

            return output.toString();
        }
    }
}

