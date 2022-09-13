package Blackjack;

import java.util.ArrayList;
import java.util.Random;

public class Deck {

    private ArrayList<Card> cards;
    //used to update card counting info, dealers flip cards etc
    private Card lastCardOut;

    public Deck(){
        this.cards = new ArrayList<Card>();
    }

    public void constructDeck(){
        for(Card.Suit suits : Card.Suit.values()){
            for(Card.Value values : Card.Value.values()){
                this.cards.add(new Card(suits, values));
            }
        }
    }

    public void shuffle(){
        //generating random number in order to "swap" cards via index call
        Random random = new Random();
        int index;
        int deckSize = this.cards.size();

        //creating new temp array list with size limited to size of deck to shuffle
        ArrayList<Card> tempDeck = new ArrayList<Card>(51);

        //shuffle loop
        for(int i = 0; i < deckSize; i++){
            index = random.nextInt(this.cards.size());
            Card chosenCard = this.cards.get(index);
            //moving card from this.deck to tmp deck via add/remove
            tempDeck.add(chosenCard);
            this.cards.remove(chosenCard);
        }

        this.cards = tempDeck;

    }

    public Card getCard(int deckIndex) {
        return this.cards.get(deckIndex);
    }

    public void addCard(Card card){
        this.cards.add(card);
    }

    public void removeCard(int deckIndex){
        lastCardOut = this.cards.get(deckIndex);
        this.cards.remove(deckIndex);
    }

    public void draw(Hand intoHand){
        intoHand.addCard(getCard(0));

        lastCardOut = getCard(0);

        removeCard(0);
    }

    public ArrayList<Card> getCards(){
        return cards;
    }

    public Card getLastCardOut() {
        return lastCardOut;
    }

    public void setLastCardOut(Card card){
        lastCardOut = card;
    }

    public String toString(){
        String deck = "";
        for(Card cards : this.cards){
            deck += cards.toString() + "\n";
        }
        return deck;
    }

}
