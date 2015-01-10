package me.august.bf4jvm;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    private Reader reader;

    private int peek() throws IOException {
        reader.mark(1);
        int chr = reader.read();
        reader.reset();
        return chr;
    }

    public Parser(Reader reader) {
        this.reader = reader;
    }

    public Instruction parse() throws IOException {
        while (true) {
            switch (reader.read()) {
                case -1:  return null;
                case '+': return Instruction.INCREMENT_DATA;
                case '-': return Instruction.DECREMENT_DATA;
                case '>': return Instruction.INCREMENT_POINTER;
                case '<': return Instruction.DECREMENT_POINTER;
                case '.': return Instruction.OUTPUT;
                case ',': return Instruction.INPUT;
                case '[': {
                    List<Instruction> insns = new ArrayList<>();
                    int chr = peek();
                    while (peek() != ']') {
                        if (chr == -1) throw new RuntimeException("Unexpected EOF in loop");
                        insns.add(parse());
                    }
                    reader.read();

                    return new Instruction.Loop(insns);
                }
                case ']': throw new RuntimeException("Unmatched loop close");
            }
        }
    }

}
