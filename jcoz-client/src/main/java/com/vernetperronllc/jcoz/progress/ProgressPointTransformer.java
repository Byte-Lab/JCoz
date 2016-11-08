package com.vernetperronllc.jcoz.progress;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * A class for transforming a line where a progress point has been listed
 * to increment the progress point counter.
 * @author David
 */
public class ProgressPointTransformer {
    public static void main(String[] args) throws Exception {
		Options ops = new Options();
		
		Option ppClassOption = new Option("c", "class", true, "Class of ProgressPoint");
		ppClassOption.setRequired(true);
		ops.addOption(ppClassOption);
		
		Option ppLineNoOption = new Option("l", "lineno", true, "Line number of progress point");
		ppLineNoOption.setRequired(true);
		ops.addOption(ppLineNoOption);

		CommandLineParser parser = new DefaultParser();
		CommandLine cl = parser.parse(ops, args);
		String klass = cl.getOptionValue('c');
		String lineNoStr = cl.getOptionValue('l');
		int lineNo = -1;
		try{
			lineNo = Integer.parseInt(cl.getOptionValue('l'));
		}catch(NumberFormatException e){
			System.err.println("Invalid Line Number : "+ cl.getOptionValue('l'));
			System.exit(-1);
		}

        FileInputStream fileIS = new FileInputStream(klass);
        ClassReader cr = new ClassReader(fileIS);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv =
            new ProgressPointLogClassAdapter(cw, lineNo);

        cr.accept(cv, 0);

        FileOutputStream fileOS = new FileOutputStream(klass);

        fileOS.write(cw.toByteArray());
        fileOS.close();
    }
}
