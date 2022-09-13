package Blackjack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Card {

    public enum Value {
        TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE
    }

    public enum Suit {
        CLUBS, DIAMONDS, HEARTS, SPADES
    }

    private Suit suit;
    private Value value;

    public Card(Suit suit, Value value){
        this.suit = suit;
        this.value = value;
    }

    public Value getValue(){
        return this.value;
    }

    public Suit getSuit(){
        return this.suit;
    }

    public String toString(){
        return this.value.toString() + " OF " + this.suit.toString();
    }

}
