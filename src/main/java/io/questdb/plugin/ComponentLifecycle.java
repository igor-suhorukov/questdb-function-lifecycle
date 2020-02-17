package io.questdb.plugin;


import io.questdb.cairo.CairoEngine;

import java.io.Closeable;

public interface ComponentLifecycle extends Closeable {
    void init(CairoEngine cairoEngine);
}