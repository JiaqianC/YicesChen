package aprove;

import gnu.getopt.*;

import java.io.*;
import java.util.logging.*;

import aprove.Framework.Utility.Profiling.*;
import aprove.Logging.*;
import aprove.exit.*;

public class Main {

    public static boolean firstObligation = true;

    public static UI UI_MODE;
    
    public static void main(final String argv[]) {
        File folder = new File("examples/ptrs");
        File[] listOfFiles = folder.listFiles();       
        
        long startTime = System.nanoTime();

        for (int i = 0; i < listOfFiles.length; i++) {
        long onestartTime = System.nanoTime();
          if (listOfFiles[i].isFile()) {
              String args[] = new String[7];
              args[0] = "-m";
              args[1] = "wst";
              args[2] = "examples/ptrs/" + listOfFiles[i].getName();
              args[3] = "-F";
              args[4] = "-vSEVERE";
              args[5] = "-t";
              args[6] = "3";
              /**
              args[5] = "-p";
              args[6] = "plain";
*/        
              System.out.println("");
              System.out.println(listOfFiles[i].getName() + ":");
              System.out.println("");
              try {
                  doMain(args);
              } catch (KillAproveException e) {
                  e.runSystemExit();
              }
              long oneendTime  = System.nanoTime();
              long stopTime = oneendTime - onestartTime;
              System.out.println("this start time:"+onestartTime+"ms");
              System.out.println("this end time:"+oneendTime+"ms");
              System.out.println("this run time:"+stopTime+"ms");
          }

        }
        long endTime   = System.nanoTime();
        long totalTime = endTime - startTime;
        System.out.println("-----------------------");
        System.out.println("start time:"+startTime+"ms");
        System.out.println("end time:"+endTime+"ms");
        System.out.println("total run time:"+totalTime+"ms");  
    }

    private static void doMain(final String[] argv) throws KillAproveException {
        Logger.getLogger("").setLevel(Level.OFF);
        final String[] origArgv = new String[argv.length];
        System.arraycopy(argv, 0, origArgv, 0, argv.length);
        final Getopt g = new Getopt("testprog", argv, "u:");
        g.setOpterr(false);
        UI mode = UI.CLI;

        if (Globals.PROFILING || Globals.TRAINING) {
            AproveOutput.setMultiOutputFromParam("STDERR");
        }

        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'u':
                    final String ui = g.getOptarg();
                    try {
                        mode = UI.valueOf(ui.toUpperCase());
                    } catch (final IllegalArgumentException e) {
                        System.err.println("Unknown user interface: " + ui + " - using default!");
                    }
                    break;
            }
        }
        Main.UI_MODE = mode;
        switch (mode) {
            case CLI:
                aprove.CommandLineInterface.Main.doMain(origArgv);
                break;
            case SRV:
                aprove.CommandLineInterface.Server.doMain();
                break;
            case BAT:
                aprove.CommandLineInterface.Batch.doMain(origArgv);
                break;
            case BMK:
                aprove.Benchmarking.Benchmark.doMain(origArgv);
                break;
            case DIO:
                aprove.DiophantineSolver.DiophantineMain.doMain(origArgv);
                break;
            default:
                throw new IllegalStateException("This Main class cannot handle UI mode " + Main.UI_MODE + "!");
        }

        if (Globals.PROFILING) {
            try {
                Profiling.getWriter().close();
            } catch (final IOException e) {
                System.err.println("Could not close \"profiling\" Writer");
            }
        }
    }

    public static enum UI {
        BAT, BMK, CLI, DIO, GUI, SRV
    }

}
