/*
 * Copyright (c) 1999, 2008, Oracle and/or its affiliates. All rights reserved.
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

import javax.swing.SwingUtilities;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;

import microbat.codeanalysis.runtime.jpda.event.AbstractEventSet;
import microbat.codeanalysis.runtime.jpda.event.AccessWatchpointEventSet;
import microbat.codeanalysis.runtime.jpda.event.ClassPrepareEventSet;
import microbat.codeanalysis.runtime.jpda.event.ClassUnloadEventSet;
import microbat.codeanalysis.runtime.jpda.event.ExceptionEventSet;
import microbat.codeanalysis.runtime.jpda.event.JDIListener;
import microbat.codeanalysis.runtime.jpda.event.LocationTriggerEventSet;
import microbat.codeanalysis.runtime.jpda.event.ModificationWatchpointEventSet;
import microbat.codeanalysis.runtime.jpda.event.ThreadDeathEventSet;
import microbat.codeanalysis.runtime.jpda.event.ThreadStartEventSet;
import microbat.codeanalysis.runtime.jpda.event.VMDeathEventSet;
import microbat.codeanalysis.runtime.jpda.event.VMDisconnectEventSet;
import microbat.codeanalysis.runtime.jpda.event.VMStartEventSet;

/** */
class JDIEventSource extends Thread {

  private /*final*/ EventQueue queue;
  private /*final*/ Session session;
  private /*final*/ ExecutionManager runtime;
  private final JDIListener firstListener = new FirstListener();

  private boolean wantInterrupt; // ### Hack

  /** Create event source. */
  JDIEventSource(Session session) {
    super("JDI Event Set Dispatcher");
    this.session = session;
    this.runtime = session.runtime;
    this.queue = session.vm.eventQueue();
  }

  @Override
  public void run() {
    try {
      runLoop();
    } catch (Exception exc) {
      // ### Do something different for InterruptedException???
      // just exit
    }
    session.running = false;
  }

  private void runLoop() throws InterruptedException {
    AbstractEventSet es;
    do {
      EventSet jdiEventSet = queue.remove();
      es = AbstractEventSet.toSpecificEventSet(jdiEventSet);
      session.interrupted = es.suspendedAll();
      dispatchEventSet(es);
    } while (!(es instanceof VMDisconnectEventSet));
  }

  // ### Gross foul hackery!
  private void dispatchEventSet(final AbstractEventSet es) {
    SwingUtilities.invokeLater(
        new Runnable() {
          @Override
          public void run() {
            boolean interrupted = es.suspendedAll();
            es.notify(firstListener);
            boolean wantInterrupt = JDIEventSource.this.wantInterrupt;
            for (JDIListener jl : session.runtime.jdiListeners) {
              es.notify(jl);
            }
            if (interrupted && !wantInterrupt) {
              session.interrupted = false;
              // ### Catch here is a hack
              try {
                session.vm.resume();
              } catch (VMDisconnectedException ee) {
              }
            }
            if (es instanceof ThreadDeathEventSet) {
              ThreadReference t = ((ThreadDeathEventSet) es).getThread();
              session.runtime.removeThreadInfo(t);
            }
          }
        });
  }

  private void finalizeEventSet(AbstractEventSet es) {
    if (session.interrupted && !wantInterrupt) {
      session.interrupted = false;
      // ### Catch here is a hack
      try {
        session.vm.resume();
      } catch (VMDisconnectedException ee) {
      }
    }
    if (es instanceof ThreadDeathEventSet) {
      ThreadReference t = ((ThreadDeathEventSet) es).getThread();
      session.runtime.removeThreadInfo(t);
    }
  }

  // ### This is a Hack, deal with it
  private class FirstListener implements JDIListener {

    @Override
    public void accessWatchpoint(AccessWatchpointEventSet e) {
      session.runtime.validateThreadInfo();
      wantInterrupt = true;
    }

    @Override
    public void classPrepare(ClassPrepareEventSet e) {
      wantInterrupt = false;
      runtime.resolve(e.getReferenceType());
    }

    @Override
    public void classUnload(ClassUnloadEventSet e) {
      wantInterrupt = false;
    }

    @Override
    public void exception(ExceptionEventSet e) {
      wantInterrupt = true;
    }

    @Override
    public void locationTrigger(LocationTriggerEventSet e) {
      session.runtime.validateThreadInfo();
      wantInterrupt = true;
    }

    @Override
    public void modificationWatchpoint(ModificationWatchpointEventSet e) {
      session.runtime.validateThreadInfo();
      wantInterrupt = true;
    }

    @Override
    public void threadDeath(ThreadDeathEventSet e) {
      wantInterrupt = false;
    }

    @Override
    public void threadStart(ThreadStartEventSet e) {
      wantInterrupt = false;
    }

    @Override
    public void vmDeath(VMDeathEventSet e) {
      // ### Should have some way to notify user
      // ### that VM died before the session ended.
      wantInterrupt = false;
    }

    @Override
    public void vmDisconnect(VMDisconnectEventSet e) {
      // ### Notify user?
      wantInterrupt = false;
      session.runtime.endSession();
    }

    @Override
    public void vmStart(VMStartEventSet e) {
      // ### Do we need to do anything with it?
      wantInterrupt = false;
    }
  }
}
