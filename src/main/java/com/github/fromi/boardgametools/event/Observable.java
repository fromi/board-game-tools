package com.github.fromi.boardgametools.event;

@FunctionalInterface
public interface Observable {
    void addObserver(Object observer);
}
