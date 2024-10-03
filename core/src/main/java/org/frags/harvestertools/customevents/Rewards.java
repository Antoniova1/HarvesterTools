package org.frags.harvestertools.customevents;

import java.util.List;

public class Rewards {

    private final int top;
    private final List<String> rewards;

    public Rewards(int top, List<String> rewards) {
        this.top = top;
        this.rewards = rewards;
    }

    public int getTop() {
        return top;
    }

    public List<String> getRewards() {
        return rewards;
    }
}
