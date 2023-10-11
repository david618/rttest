package com.esri;

import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;

public class Test {
   
    
    public static void main(String args[]) {
        System.out.println("hello");
        String filename = "/Users/davi5017/kafka_2.13-3.3.1/velokafka.jks";

        Path path = Paths.get(filename); 

        path.getFileName().getParent();

        String justfilename = path.getFileName().toString();
        System.out.println(justfilename);

        String ext = FilenameUtils.getExtension(justfilename);
        System.out.println(ext);

    }
}
