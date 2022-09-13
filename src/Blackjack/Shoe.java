package Blackjack;

import java.util.ArrayList;
import java.util.Random;

public class Shoe {

    ArrayList<Deck> shoe = new ArrayList<Deck>();
    int shoeSize;


    public Shoe(int shoeSize){
        this.shoeSize = shoeSize;
    }

    public void constructShoe(){
        Deck deck;

        //building x decks and adding it to the shoe, where x = shoeSize
        for(int i = 0; i < shoeSize; i++){
            deck = new Deck();
            deck.constructDeck();
            deck.shuffle();
            shoe.add(deck);
        }

        this.shuffle();

    }

    public ArrayList<Deck> shuffle(){
        //generating random number in order to "swap" cards via index call
        Random random = new Random();
        int index;
        int shoeSize = this.shoe.size();

        //creating new temp array list with size limited to size of deck to shuffle
        ArrayList<Card> tempShoe = new ArrayList<Card>();

        //grabbing the cards and placing in tempShoe
        for(Deck deck : shoe){
            for(Card card : deck.getCards()){
                tempShoe.add(card);
            }
        }

        ArrayList<Card> shuffleShoe = new ArrayList<Card>();

        //shuffle loop
        for(int i = 0; i < tempShoe.size(); i++){
            int chosenIndex = random.nextInt(tempShoe.size());
            shuffleShoe.add(tempShoe.get(chosenIndex));
        }

        //putting shoe back into groups of 52
        int decksNum = shuffleShoe.size()/52;

        ArrayList<Deck> finalShoe = new ArrayList<Deck>();

        for(int i = 1; i < decksNum+1; i++){

            Deck tempDeck = new Deck();

            for(int j = 0; j < 52; j++){

                   tempDeck.addCard(shuffleShoe.get((i*j)));

            }

            finalShoe.add(tempDeck);

        }

        return finalShoe;

    }




    public void addDeck(){
        Deck deck = new Deck();
        deck.constructDeck();
        deck.shuffle();
        shoe.add(deck);
        shoeSize++;
    }

    public void removeDeck(int deckIndex){
        shoe.remove(shoe.get(deckIndex));
        shoeSize--;
    }

    public ArrayList<Deck> getDecks(){
        return shoe;
    }

    public int getShoeSize(){
        return shoeSize;
    }

    public void setShoeSize(int shoeSize){
        this.shoeSize = shoeSize;
    }

    public int getCardsNum(){
        int cardNum = 0;

        for(Deck deck : shoe){
            for(Card card : deck.getCards()){
                cardNum++;
            }
        }

        return cardNum;
    }

    public String toString(){
        String shoeLace = "";

        for(Deck deck : shoe){
            shoeLace += deck.toString() + "\n";
        }

        return shoeLace;
    }


    public boolean isEmpty() {
        return shoe.isEmpty();
    }
}
