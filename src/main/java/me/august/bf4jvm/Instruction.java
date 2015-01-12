package me.august.bf4jvm;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

public interface Instruction extends Opcodes {

    static final String SYSTEM_INTERNAL_NAME        = "java/lang/System";

    static final String PRINTSTREAM_TYPE            = "Ljava/io/PrintStream;";
    static final String PRINTSTREAM_INTERNAL_NAME   = "java/io/PrintStream";
    static final String PRINT_CHR_DESC              = "(C)V";

    static final String INPUTSTREAM_TYPE            = "Ljava/io/InputStream;";
    static final String INPUTSTREAM_INTERNAL_NAME   = "java/io/InputStream";
    static final String READ_INT_DESC               = "()I";

    public static final int ARRAY_VAR   = 1;
    public static final int POINTER_VAR = 2;

    public static final Instruction INCREMENT_DATA = (m) -> {
        m.visitVarInsn(ALOAD, ARRAY_VAR); // arr
        m.visitVarInsn(ILOAD, POINTER_VAR); // idx
        m.visitInsn(DUP2); // dup top 2 (for accessing arr ref and idx quickly)

        m.visitInsn(BALOAD);   // get int at idx
        m.visitInsn(ICONST_1); // add 1
        m.visitInsn(IADD);
        m.visitInsn(BASTORE);
    };

    public static final Instruction DECREMENT_DATA = (m) -> {
        m.visitVarInsn(ALOAD, ARRAY_VAR); // access arr
        m.visitVarInsn(ILOAD, POINTER_VAR); // access idx
        m.visitInsn(DUP2); // dup top 2 (for accessing arr ref and idx quickly)

        m.visitInsn(BALOAD);   // get int at idx
        m.visitInsn(ICONST_M1); // add -1
        m.visitInsn(IADD);
        m.visitInsn(BASTORE);
    };

    public static final Instruction INCREMENT_POINTER = (m) -> {
        m.visitIincInsn(POINTER_VAR, 1);
    };

    public static final Instruction DECREMENT_POINTER = (m) -> {
        m.visitIincInsn(POINTER_VAR, -1);
    };

    public static final Instruction OUTPUT = (m) -> {
        m.visitFieldInsn(
            GETSTATIC,
            SYSTEM_INTERNAL_NAME,
            "out",
            PRINTSTREAM_TYPE
        );

        m.visitVarInsn(ALOAD, 1); // arr
        m.visitVarInsn(ILOAD, 2); // idx

        m.visitInsn(BALOAD); // get int at idx
        m.visitInsn(I2C);    // char cast

        m.visitMethodInsn(
            INVOKEVIRTUAL,
            PRINTSTREAM_INTERNAL_NAME,
            "print",
            PRINT_CHR_DESC,
            false // not interface
        );
    };

    public static final Instruction INPUT = (m) -> {
        m.visitVarInsn(ALOAD, 1); // arr
        m.visitVarInsn(ILOAD, 2); // idx

        m.visitFieldInsn(
            GETSTATIC,
            SYSTEM_INTERNAL_NAME,
            "in",
            INPUTSTREAM_TYPE
        );

        m.visitMethodInsn(
            INVOKEVIRTUAL,
            INPUTSTREAM_INTERNAL_NAME,
            "read",
            READ_INT_DESC,
            false // not interface
        );

        m.visitInsn(BASTORE);
    };

    void gen(MethodVisitor method);

    static class Loop implements Instruction {
        private List<Instruction> insns;

        public Loop(List<Instruction> insns) {
            this.insns = insns;
        }

        @Override
        public void gen(MethodVisitor method) {
            Label top = new Label();
            Label end = new Label();

            method.visitLabel(top);

            method.visitVarInsn(ALOAD, ARRAY_VAR);
            method.visitVarInsn(ILOAD, POINTER_VAR);
            method.visitInsn(BALOAD);

            method.visitJumpInsn(IFEQ, end);

            for (Instruction i : insns)
                i.gen(method);

            method.visitJumpInsn(GOTO, top);
            method.visitLabel(end);
        }
    }

}
