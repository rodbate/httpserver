package com.rodbate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test {

    public static void main(String[] args) throws InterruptedException {

        Logger logger = LoggerFactory.getLogger(Test.class);


        logger.error("======================== ");
        int i = 0;
        while (true){
            logger.info("======================== {}", i++);
            Thread.sleep(1000);
       }

    }
}
