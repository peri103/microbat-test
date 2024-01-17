package microbat.views;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class DialogUtil {

  public static void popErrorDialog(final String errorMsg, final String title) {
    Display.getDefault()
        .asyncExec(
            () -> {
              Shell shell = Display.getCurrent().getActiveShell();
              MessageDialog.openError(shell, title, errorMsg);
            });
  }

  public static void popInformationDialog(final String message, final String title) {
    Display.getDefault()
        .asyncExec(
            () -> {
              Shell shell = Display.getCurrent().getActiveShell();
              MessageDialog.openInformation(shell, title, message);
            });
  }

  public static boolean popConfirmDialog(final String message, final String title) {
    final AtomicBoolean confirmed = new AtomicBoolean(false);
    Display.getDefault()
        .syncExec(
            () -> {
              Shell shell = Display.getCurrent().getActiveShell();
              confirmed.set(MessageDialog.openConfirm(shell, title, message));
            });
    return confirmed.get();
  }
}
