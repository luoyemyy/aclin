package com.github.luoyemyy.aclin.bus;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface BusResult {
    void busResult(@NotNull BusMsg msg);
}