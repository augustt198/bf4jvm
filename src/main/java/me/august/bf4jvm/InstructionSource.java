package me.august.bf4jvm;

import java.io.IOException;

interface InstructionSource {

    Instruction parse() throws IOException;

}
