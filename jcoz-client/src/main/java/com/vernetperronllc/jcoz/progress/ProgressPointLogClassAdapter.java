package com.vernetperronllc.jcoz.progress;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

/**
 * A class for transforming a line where a progress point has been listed
 * to increment the progress point counter.
 * @author David
 */
public class ProgressPointLogClassAdapter extends ClassVisitor implements Opcodes {
	final int lineNum;
	
	public ProgressPointLogClassAdapter(final ClassVisitor cv, final int lineNum) {
		super(ASM5, cv);
		this.lineNum = lineNum;
	}

	@Override
	public MethodVisitor visitMethod(
			final int access, final String name, final String desc,
			final String signature, final String[] exceptions) {
		MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
		return mv == null ? null : new ProgressPointMethodAdapter(mv, this.lineNum);
	}
}

class ProgressPointMethodAdapter extends MethodVisitor implements Opcodes {
	final int lineNum;
	
	public ProgressPointMethodAdapter(final MethodVisitor mv, final int lineNum) {
		super(ASM5, mv);
		this.lineNum = lineNum;
	}
	
	@Override
	public void visitLineNumber(int line, Label start) {
		// If we're at the line, transform it to call logProgressPointHit.
		if (line == this.lineNum) {
	        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
	        mv.visitLdcInsn("CLASS INSTRUMENTED -- label is " + start);
	        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
		}
		
		mv.visitLineNumber(line, start);
	}
}