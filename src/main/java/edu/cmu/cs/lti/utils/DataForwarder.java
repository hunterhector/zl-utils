package edu.cmu.cs.lti.utils;

import com.google.common.base.Joiner;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class DataForwarder extends Thread {
    private OutputStream out;
    private BufferedReader in;

    public static final String MSG_END = "<MSG_END>";

    public DataForwarder(BufferedReader in, OutputStream out) {
        this.out = out;
        this.in = in;
    }

    @Override
    public void run() {
        try {
//            System.out.println("Forwarding data.");
            List<String> inputLines = IOUtils.readLines(in);
            String input = Joiner.on("\n").join(inputLines);
//            System.out.println("Got input.");
//            System.out.println(input);
            IOUtils.write(input, out);
            // Write message end so the caller can know when to consume.
            IOUtils.write("\n", out);
//            IOUtils.write(MSG_END, out);
//            IOUtils.write("\n", out);
//            System.out.println("Got output.");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}