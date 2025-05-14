package com.example;

enum TokenType {
    //characters
    COMMA, DASH, PLUS, MINUS, COLON, DOT, SLASH, PERIOD,

    //constant keywords
    KEYWORD, FIRST_STRIKE, VIGILANCE, MENACE, TRAMPLE, REACH, LIFELINK, FLASH, FLYING, INDESTRUCTIBLE,

    //variable keywords
    WARD, HEXPROOF,

    //actions
    PAY, CREATE, PUT, GAIN, GET, CAST, PREVENT, DRAW, DEAL, EQUIP, ATTACK, BLOCK, ATTACH,

    //attributes
    COST, HAS, 

    //events
    TARGETS, ENTERS,

    //game concepts
    LIFE, CONTROL, COMBAT, DAMAGE, WIN_GAME, LOSE_GAME, CARD, COUNTER, END, TURN,

    //logic
    WHENEVER, WHEN, WITH, THATD_BE, IF, CANT, ON_THEM, WOULD, INSTEAD,

    //groupings
    OR, AND,

    //references
    IT, THAT, YOU, OPPONENT, OTHER, ANOTHER, CARDNAME, SPELLNAME, TARGET, EQUIPPED_CREATURE, TO, EACH, UP_TO, NAMED, BE,

    //timing
    DURING, UNTIL, ONCE_A_TURN, YOURTURNS, EOT, THIS_TURN, YOUR_TURN,

    //checks
    WAS, IS,

    //quantity
    NUM, OR_MORE, OR_LESS, MORE, LESS, FOR_EACH, ALL, ANY_NUM_OF, NUM_OF, EQUAL_TO, NUMBER, ITERATION, QUANTITY,

    //object properties
    SUPERTYPE, TYPE, NON, COLOR,

    LINE_END

}
