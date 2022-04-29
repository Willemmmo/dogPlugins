package net.runelite.client.plugins.oneclickblackjack;

import lombok.Getter;

public enum BlackJackNpc {
    MENAPHITE("Menaphite thug", 3550),
    BANDIT1("Bandit lvl-41", 737),
    BANDIT2("Bandit lvl-56", 735);


    @Getter
    private final String banditName;

    @Getter
    private final int id;

    public String toString() {
        return this.banditName;
    }

    BlackJackNpc(String banditName, int id) {
        this.banditName = banditName;
        this.id = id;
    }
}