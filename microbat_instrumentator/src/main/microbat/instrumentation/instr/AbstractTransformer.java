package microbat.instrumentation.instr;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import microbat.instrumentation.runtime.ExecutionTracer;
import microbat.instrumentation.runtime.IExecutionTracer;
import sav.common.core.utils.FileUtils;

public abstract class AbstractTransformer implements ClassFileTransformer {

	@Override
	public final byte[] transform(ClassLoader loader, String classFName, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		if (ExecutionTracer.isShutdown()) {
			return null;
		}
		IExecutionTracer tracer = ExecutionTracer.getCurrentThreadStore();
		boolean needToReleaseLock = !tracer.lock();
		
		byte[] data = doTransform(loader, classFName, classBeingRedefined, protectionDomain, classfileBuffer);
		log(classfileBuffer, data, classFName, false);
					
		if (needToReleaseLock) {
			tracer.unLock();
		}
		return data;
	}

	protected abstract byte[] doTransform(ClassLoader loader, String classFName, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException;
		
	public static void log(byte[] classfileBuffer, byte[] data, String classFName, boolean dump) {
		if (data == null) {
			return;
		}
		if (!dump) {
			System.out.println("instrumented class: " + classFName);
			return;
		}
		String orgPath = "E:/lyly/Projects/inst_src/org/" + classFName.substring(classFName.lastIndexOf(".") + 1)
				+ ".class";
		System.out.println("dump org class to file: " + orgPath);
		dumpToFile(classfileBuffer, orgPath);
		String filePath = "E:/lyly/Projects/inst_src/test/" + classFName.substring(classFName.lastIndexOf(".") + 1)
				+ ".class";
		System.out.println("dump instrumented class to file: " + filePath);
		dumpToFile(data, filePath);
	}

	private static void dumpToFile(byte[] data, String filePath) {
		FileUtils.getFileCreateIfNotExist(filePath);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(filePath);
			out.write(data);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
