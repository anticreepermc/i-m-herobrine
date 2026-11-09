package com.imherobrine.common;

public enum HerobrineStructureType {
    SAND_PYRAMID_SMALL("Sand pyramid · small", HerobrineAction.SELECT_SAND_PYRAMID_SMALL),
    SAND_PYRAMID_MEDIUM("Sand pyramid · medium", HerobrineAction.SELECT_SAND_PYRAMID_MEDIUM),
    SAND_PYRAMID_LARGE("Sand pyramid · large", HerobrineAction.SELECT_SAND_PYRAMID_LARGE),
    COBBLE_PYRAMID_SMALL("Cobblestone pyramid · small", HerobrineAction.SELECT_COBBLE_PYRAMID_SMALL),
    COBBLE_PYRAMID_MEDIUM("Cobblestone pyramid · medium", HerobrineAction.SELECT_COBBLE_PYRAMID_MEDIUM),
    COBBLE_PYRAMID_LARGE("Cobblestone pyramid · large", HerobrineAction.SELECT_COBBLE_PYRAMID_LARGE),
    WORD_HEROBRINE("HEROBRINE", HerobrineAction.SELECT_WORD_HEROBRINE),
    WORD_STOP("STOP", HerobrineAction.SELECT_WORD_STOP),
    LETTER_H("H", HerobrineAction.SELECT_LETTER_H),
    LETTER_E("E", HerobrineAction.SELECT_LETTER_E),
    LETTER_R("R", HerobrineAction.SELECT_LETTER_R),
    LETTER_O("O", HerobrineAction.SELECT_LETTER_O),
    LETTER_B("B", HerobrineAction.SELECT_LETTER_B),
    LETTER_I("I", HerobrineAction.SELECT_LETTER_I),
    LETTER_N("N", HerobrineAction.SELECT_LETTER_N),
    MINE_TUNNEL("2×2 redstone mine tunnel", HerobrineAction.SELECT_MINE_TUNNEL),
    CREEPY_SIGNS("Creepy signs", HerobrineAction.SELECT_CREEPY_SIGNS),
    HEROBRINE_TOTEM("Herobrine totem", HerobrineAction.SELECT_HEROBRINE_TOTEM);

    private final String title;
    private final HerobrineAction selectionAction;

    HerobrineStructureType(String title, HerobrineAction selectionAction) {
        this.title = title;
        this.selectionAction = selectionAction;
    }

    public String title() {
        return title;
    }

    public HerobrineAction selectionAction() {
        return selectionAction;
    }
}
