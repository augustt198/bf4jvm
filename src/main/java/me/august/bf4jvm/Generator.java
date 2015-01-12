package me.august.bf4jvm;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;

public class Generator implements Opcodes {

    private static final String OBJECT_INTERNAL_NAME = "java/lang/Object";
    private static final String[] IMPLEMENT_RUNNABLE = {"java/lang/Runnable"};
    private static final String[] IOEXCEPTION        = {"java/io/IOException"};

    private InstructionSource source;
    private int stackSize;
    private String className;

    public Generator(InstructionSource source, int stackSize, String className) {
        this.source = source;
        this.stackSize = stackSize;
        this.className = className;
    }

    public byte[] generate() {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        writer.visit(
            49,                     // version
            ACC_PUBLIC + ACC_SUPER, // access
            className,
            null,                   // no signature
            OBJECT_INTERNAL_NAME,   // Object superclass,
            IMPLEMENT_RUNNABLE      // implement java.lang.Runnable
        );

        MethodVisitor constructor = writer.visitMethod(
            ACC_PUBLIC, "<init>", "()V", null, null
        );

        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitMethodInsn(
            INVOKESPECIAL,
            OBJECT_INTERNAL_NAME,
            "<init>",
            "()V",
            false
        );
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();

        MethodVisitor main = writer.visitMethod(
            ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null
        );

        main.visitTypeInsn(NEW, className);
        main.visitInsn(DUP);
        main.visitMethodInsn(
            INVOKESPECIAL, className, "<init>", "()V", false
        );
        main.visitMethodInsn(
            INVOKESPECIAL, className, "run", "()V", false
        );
        main.visitInsn(RETURN);
        main.visitMaxs(2, 2);
        main.visitEnd();

        MethodVisitor method = writer.visitMethod(
            ACC_PUBLIC,             // public modifier
            "run",
            "()V",                  // empty params, void method
            null,
            IOEXCEPTION
        );

        // create int array with size `stackSize`,
        // store in first local variable
        method.visitLdcInsn(stackSize);
        method.visitIntInsn(NEWARRAY, T_BYTE);
        method.visitVarInsn(ASTORE, 1);

        // load 0 into second local variable
        method.visitInsn(ICONST_0);
        method.visitVarInsn(ISTORE, 2);

        while (true) {
            Instruction insn = null;
            try {
                insn = source.parse();
            } catch (IOException ignored) {}

            if (insn == null) break;
            insn.gen(method);
        }

        method.visitInsn(RETURN);

        method.visitEnd();
        method.visitMaxs(2, 2);
        writer.visitEnd();

        return writer.toByteArray();
    }
}
