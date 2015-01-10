package me.august.bf4jvm;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws Exception {
        int stackSize  = Integer.parseInt(System.getProperty("stacksize", "10000"));

        for (String arg : args) {
            String src = new String(Files.readAllBytes(Paths.get(arg)), StandardCharsets.UTF_8);

            Parser parser  = new Parser(new StringReader(src));

            int idx = arg.replace("\\", "/").lastIndexOf('/');
            String name = idx >= 0 ? arg.substring(idx + 1) : arg;
            name = name.substring(0, name.lastIndexOf('.'));

            String newName = arg.substring(0, arg.lastIndexOf('.')) + ".class";

            Generator gen  = new Generator(parser, stackSize, name);

            Files.write(Paths.get(newName), gen.generate());
        }
    }

}
