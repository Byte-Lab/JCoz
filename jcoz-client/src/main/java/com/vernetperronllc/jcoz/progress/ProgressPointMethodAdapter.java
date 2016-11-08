package com.vernetperronllc.jcoz.progress;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class ProgressPointMethodAdapter extends MethodVisitor implements Opcodes {
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
		
		super.visitLineNumber(line, start);
	}
}
