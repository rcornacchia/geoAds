package com.example.mike.geoadsmobileapp;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by pglosembe on 4/10/16.
 */
public class NotificationID {
    private final static AtomicInteger c = new AtomicInteger(0);
    public static int getID() {
        return c.incrementAndGet();
    }
}
