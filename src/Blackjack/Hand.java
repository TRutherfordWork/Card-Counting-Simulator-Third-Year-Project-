package Blackjack;

import java.util.ArrayList;
import java.util.TreeMap;

public class Hand {

    ArrayList<Card> cards;

    //constructing TreeMap to process card values from retrieved enum tokens
    private TreeMap<Card.Value, String> valueTreeMap = new TreeMap<>();

    private boolean aceConversion;

    public Hand(){
        cards = new ArrayList<Card>();

        //initialising valueTreeMap
        valueTreeMap.put(Card.Value.TWO, "2");
        valueTreeMap.put(Card.Value.THREE, "3");
        valueTreeMap.put(Card.Value.FOUR, "4");
        valueTreeMap.put(Card.Value.FIVE, "5");
        valueTreeMap.put(Card.Value.SIX, "6");
        valueTreeMap.put(Card.Value.SEVEN, "7");
        valueTreeMap.put(Card.Value.EIGHT, "8");
        valueTreeMap.put(Card.Value.NINE, "9");
        valueTreeMap.put(Card.Value.TEN, "10");
        valueTreeMap.put(Card.Value.JACK, "10");
        valueTreeMap.put(Card.Value.KING, "10");
        valueTreeMap.put(Card.Value.QUEEN, "10");
        valueTreeMap.put(Card.Value.ACE, "11");
    }

    public Card getCard(int handIndex) {
        return this.cards.get(handIndex);
    }

    public void addCard(Card card){
        this.cards.add(card);
    }

    public void removeCard(int handIndex){
        this.cards.remove(handIndex);
    }

    public int getValue(){

        aceConversion = false;

        int valueTotal = 0;
        Card.Value value;
        int intValue;
        ArrayList<Card.Value> cardList = new ArrayList<>();

        //to determine the number of aces for proper ace conversion
        int aceNum = 0;

        for(Card card : this.cards){
            value = card.getValue();
            //appending card list in case of an ace
            cardList.add(0, value);

            if(value == Card.Value.ACE){ aceNum++; }

            //searching value in TreeMap
            intValue = Integer.parseInt(valueTreeMap.get(value));
            valueTotal += intValue;
        }


        if(valueTotal > 21 && cardList.contains(Card.Value.ACE)){
            for(int i = 0; i < aceNum; i++){
                if(valueTotal > 21){
                    valueTotal = valueTotal - 10;
                    aceConversion = true;
                }
            }
        }

        return valueTotal;

    }

    public String toString(){
        return cards.toString();
    }

    public boolean aceConversion() {
        return aceConversion;
    }
}
