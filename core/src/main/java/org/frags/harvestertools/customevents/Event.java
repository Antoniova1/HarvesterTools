package org.frags.harvestertools.customevents;

import org.frags.harvestertools.enums.Tools;

import java.util.HashMap;
import java.util.List;

public class Event {

    private Tools tools;
    private long time;
    private List<String> startMessage;
    private List<String> endMessage;
    private HashMap<Integer, Rewards> rewards;

    private final HashMap<String, Double> playersStats = new HashMap<>();

    public Event(Tools tools, long time, List<String> startMessage, List<String> endMessage, HashMap<Integer, Rewards> rewards) {
        this.tools = tools;
        this.time = time;
        this.startMessage = startMessage;
        this.endMessage = endMessage;
        this.rewards = rewards;
        clearMap();
    }

    public Tools getTools() {
        return tools;
    }

    public long getTime() {
        return time;
    }

    public List<String> getStartMessage() {
        return startMessage;
    }

    public HashMap<Integer, Rewards> getRewards() {
        return rewards;
    }

    public List<String> getEndMessage() {
        return endMessage;
    }

    public void clearMap() {
        playersStats.clear();
    }



}
