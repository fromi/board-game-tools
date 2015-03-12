package com.github.fromi.boardgametools;

import java.io.Serializable;
import java.util.Map;

import com.github.fromi.boardgametools.event.Observable;

public interface BoardGame<PLAYER, ID extends Serializable> extends Observable {
    Map<ID, PLAYER> getPlayers();
}
