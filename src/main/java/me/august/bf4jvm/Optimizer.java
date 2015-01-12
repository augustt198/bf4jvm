package me.august.bf4jvm;

import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static me.august.bf4jvm.Instruction.*;

public class Optimizer implements InstructionSource, Opcodes {

    private static Instruction incrementDataBy(byte amount) {
        return (m) -> {
            m.visitVarInsn(ALOAD, ARRAY_VAR); // arr
            m.visitVarInsn(ILOAD, POINTER_VAR); // idx
            m.visitInsn(DUP2); // dup top 2 (for accessing arr ref and idx quickly)

            m.visitInsn(BALOAD);   // get int at idx
            m.visitLdcInsn(amount);
            m.visitInsn(IADD);
            m.visitInsn(BASTORE);
        };
    }

    private static Instruction decrementDataBy(byte amount) {
        return (m) -> {
            m.visitVarInsn(ALOAD, ARRAY_VAR); // arr
            m.visitVarInsn(ILOAD, POINTER_VAR); // idx
            m.visitInsn(DUP2); // dup top 2 (for accessing arr ref and idx quickly)

            m.visitInsn(BALOAD);   // get int at idx
            m.visitLdcInsn(amount);
            m.visitInsn(ISUB);
            m.visitInsn(BASTORE);
        };
    }

    private static Instruction incrementPointerBy(int amount) {
        return (m) -> {
            m.visitVarInsn(ILOAD, POINTER_VAR);

            m.visitLdcInsn(amount);
            m.visitInsn(IADD);

            m.visitVarInsn(ISTORE, POINTER_VAR);
        };
    }

    private static Instruction decrementPointerBy(int amount) {
        return (m) -> {
            m.visitVarInsn(ILOAD, POINTER_VAR);

            m.visitLdcInsn(amount);
            m.visitInsn(ISUB);

            m.visitVarInsn(ISTORE, POINTER_VAR);
        };
    }

    private List<Instruction> unoptimized;
    private List<Instruction> optimized;
    private Parser parser;

    int pos = 0;

    public Optimizer(Parser parser) {
        this.parser = parser;
    }

    private Instruction peek() throws IOException {
        if (pos >= unoptimized.size()) {
            return null;
        } else {
            return unoptimized.get(pos);
        }
    }

    private Instruction next() {
        if (pos >= unoptimized.size()) {
            return null;
        } else {
            return unoptimized.get(pos++);
        }
    }

    private void load() throws IOException {
        unoptimized = new ArrayList<>();
        while (true) {
            Instruction insn = parser.parse();
            unoptimized.add(insn);

            if (insn == null) break;
        }
    }


    private void optimize() throws IOException {
        load();

        optimized = new ArrayList<>();

        while (peek() != null) {
            Instruction insn = next();
            if (insn == INCREMENT_DATA || insn == DECREMENT_DATA)  {
                optimizeData(insn);
            } else if (insn == INCREMENT_POINTER || insn == DECREMENT_POINTER) {
                optimizePointer(insn);
            } else {
                optimized.add(insn);
            }
        }

        optimized.add(null);
    }

    private void optimizeData(Instruction current) throws IOException {
        byte delta = (byte) (current == INCREMENT_DATA ? 1 : -1);

        while (peek() == INCREMENT_DATA || peek() == DECREMENT_DATA) {
            if (next() == INCREMENT_DATA)
                delta++;
            else
                delta--;
        }

        if (delta == 1 || delta == -1) {
            optimized.add(current);
        } else if (delta > 1) {
            optimized.add(incrementDataBy(delta));
        } else if (delta < 0) {
            optimized.add(decrementDataBy(delta));
        }
    }

    private void optimizePointer(Instruction current) throws IOException {
        int delta = current == INCREMENT_POINTER ? 1 : -1;

        while (peek() == INCREMENT_POINTER || peek() == DECREMENT_POINTER) {
            if (next() == INCREMENT_POINTER)
                delta++;
            else
                delta--;
        }

        if (delta == 1 || delta == -1) {
            optimized.add(current);
        } else if (delta > 1) {
            optimized.add(incrementPointerBy(delta));
        } else if (delta < 0) {
            optimized.add(decrementPointerBy(delta));
        }
    }

    @Override
    public Instruction parse() throws IOException {
        if (optimized == null) {
            optimize();
            pos = 0;
        }

        if (pos >= optimized.size()) {
            return null;
        } else {
            return optimized.get(pos++);
        }
    }
}
