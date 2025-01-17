/*
 * Copyright (c) 1998, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 * This source code is provided to illustrate the usage of a given feature
 * or technique and has been deliberately simplified. Additional steps
 * required for a production-quality application, such as security checks,
 * input validation and proper error handling, might not be present in
 * this sample code.
 */

package microbat.codeanalysis.runtime.jpda.bdi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;

import javax.swing.SwingUtilities;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;

class ChildSession extends Session {

  private Process process;

  private PrintWriter in;
  private BufferedReader out;
  private BufferedReader err;

  private InputListener input;
  private OutputListener output;
  private OutputListener error;

  public ChildSession(
      ExecutionManager runtime,
      String userVMArgs,
      String cmdLine,
      InputListener input,
      OutputListener output,
      OutputListener error,
      OutputListener diagnostics) {
    this(runtime, getVM(diagnostics, userVMArgs, cmdLine), input, output, error, diagnostics);
  }

  public ChildSession(
      ExecutionManager runtime,
      LaunchingConnector connector,
      Map<String, Connector.Argument> arguments,
      InputListener input,
      OutputListener output,
      OutputListener error,
      OutputListener diagnostics) {
    this(
        runtime,
        generalGetVM(diagnostics, connector, arguments),
        input,
        output,
        error,
        diagnostics);
  }

  private ChildSession(
      ExecutionManager runtime,
      VirtualMachine vm,
      InputListener input,
      OutputListener output,
      OutputListener error,
      OutputListener diagnostics) {
    super(vm, runtime, diagnostics);
    this.input = input;
    this.output = output;
    this.error = error;
  }

  @Override
  public boolean attach() {

    if (!connectToVMProcess()) {
      diagnostics.putString("Could not launch VM");
      return false;
    }

    /*
     * Create a Thread that will retrieve and display any output.
     * Needs to be high priority, else debugger may exit before
     * it can be displayed.
     */

    // ### Rename InputWriter and OutputReader classes
    // ### Thread priorities cribbed from ttydebug.  Think about them.

    OutputReader outputReader =
        new OutputReader("output reader", "output", out, output, diagnostics);
    outputReader.setPriority(Thread.MAX_PRIORITY - 1);
    outputReader.start();

    OutputReader errorReader = new OutputReader("error reader", "error", err, error, diagnostics);
    errorReader.setPriority(Thread.MAX_PRIORITY - 1);
    errorReader.start();

    InputWriter inputWriter = new InputWriter("input writer", in, input);
    inputWriter.setPriority(Thread.MAX_PRIORITY - 1);
    inputWriter.start();

    if (!super.attach()) {
      if (process != null) {
        process.destroy();
        process = null;
      }
      return false;
    }

    // ### debug
    // System.out.println("IO after attach: "+ inputWriter + " " + outputReader + " "+ errorReader);

    return true;
  }

  @Override
  public void detach() {

    // ### debug
    // System.out.println("IO before detach: "+ inputWriter + " " + outputReader + " "+
    // errorReader);

    super.detach();

    /*
    inputWriter.quit();
    outputReader.quit();
    errorReader.quit();
    */

    if (process != null) {
      process.destroy();
      process = null;
    }
  }

  /** Launch child java interpreter, return host:port */
  private static void dumpStream(OutputListener diagnostics, InputStream stream)
      throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(stream));
    String line;
    while ((line = in.readLine()) != null) {
      diagnostics.putString(line);
    }
  }

  private static void dumpFailedLaunchInfo(OutputListener diagnostics, Process process) {
    try {
      dumpStream(diagnostics, process.getErrorStream());
      dumpStream(diagnostics, process.getInputStream());
    } catch (IOException e) {
      diagnostics.putString("Unable to display process output: " + e.getMessage());
    }
  }

  private static VirtualMachine getVM(
      OutputListener diagnostics, String userVMArgs, String cmdLine) {
    VirtualMachineManager manager = Bootstrap.virtualMachineManager();
    LaunchingConnector connector = manager.defaultConnector();
    Map<String, Connector.Argument> arguments = connector.defaultArguments();
    arguments.get("options").setValue(userVMArgs);
    arguments.get("main").setValue(cmdLine);
    return generalGetVM(diagnostics, connector, arguments);
  }

  private static VirtualMachine generalGetVM(
      OutputListener diagnostics,
      LaunchingConnector connector,
      Map<String, Connector.Argument> arguments) {
    VirtualMachine vm = null;
    try {
      diagnostics.putString("Starting child.");
      vm = connector.launch(arguments);
    } catch (IOException ioe) {
      diagnostics.putString("Unable to start child: " + ioe.getMessage());
    } catch (IllegalConnectorArgumentsException icae) {
      diagnostics.putString("Unable to start child: " + icae.getMessage());
    } catch (VMStartException vmse) {
      diagnostics.putString("Unable to start child: " + vmse.getMessage() + '\n');
      dumpFailedLaunchInfo(diagnostics, vmse.process());
    }
    return vm;
  }

  private boolean connectToVMProcess() {
    if (vm == null) {
      return false;
    }
    process = vm.process();
    in = new PrintWriter(new OutputStreamWriter(process.getOutputStream()));
    // ### Note small buffer sizes!
    out = new BufferedReader(new InputStreamReader(process.getInputStream()), 1);
    err = new BufferedReader(new InputStreamReader(process.getErrorStream()), 1);
    return true;
  }

  /** Threads to handle application input/output. */
  private static class OutputReader extends Thread {

    private String streamName;
    private BufferedReader stream;
    private OutputListener output;
    private OutputListener diagnostics;
    private boolean running = true;
    private char[] buffer = new char[512];

    OutputReader(
        String threadName,
        String streamName,
        BufferedReader stream,
        OutputListener output,
        OutputListener diagnostics) {
      super(threadName);
      this.streamName = streamName;
      this.stream = stream;
      this.output = output;
      this.diagnostics = diagnostics;
    }

    @Override
    public void run() {
      try {
        int count;
        while (running && (count = stream.read(buffer, 0, 512)) != -1) {
          if (count > 0) {
            // Run in Swing event dispatcher thread.
            final String chars = new String(buffer, 0, count);
            SwingUtilities.invokeLater(
                new Runnable() {
                  @Override
                  public void run() {
                    output.putString(chars);
                  }
                });
          }
          // ### Should we sleep briefly here?
        }
      } catch (IOException e) {
        // Run in Swing event dispatcher thread.
        SwingUtilities.invokeLater(
            new Runnable() {
              @Override
              public void run() {
                diagnostics.putString(
                    "IO error reading " + streamName + " stream of child java interpreter");
              }
            });
      }
    }
  }

  private static class InputWriter extends Thread {

    private PrintWriter stream;
    private InputListener input;
    private boolean running = true;

    InputWriter(String threadName, PrintWriter stream, InputListener input) {
      super(threadName);
      this.stream = stream;
      this.input = input;
    }

    @Override
    public void run() {
      String line;
      while (running) {
        line = input.getLine();
        stream.println(line);
        // Should not be needed for println above!
        stream.flush();
      }
    }
  }
}
