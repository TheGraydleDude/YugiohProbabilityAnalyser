package com.example.yugiohprobabilityanalyser;

import javafx.scene.image.Image;
import org.json.JSONObject;

public class Card {
    private int id;
    private String name;
    private String type;
    private String desc;
    private String race;
    private Image cardImg;

    //For monsters only. All numerical values are > 0 so initialising to -1 for non-monsters
    private int atk = -1;
    private int def = -1;
    private int level = -1;
    private String attribute = null;

    //Pendulum monsters only: -1 for non-pendulums
    private int scale = -1;

    /*
      pre:  card id passed
      post: card id in object updated
     */
    public Card(int id){
        this.id = id;
    }

    /*
      pre:  JSONObject containing the card information for the object
      post: All fields in the object contain correct info

      The JSONObject will contain a large amount of data that we do not care about, e.g. the sets the card is in.
      As a result we can basically discard everything after "race" for non monsters.
      But, if "type" is a monster (which can have type "Effect Monster", "Flip Effect Monster") etc. we have to look deeper:
        Every monster type has "def" and "level" apart from link monsters.
        We can replace "level" with the "linkval".
        We cannot replace "def" with anything, so we make sure it is undefined later in the GUI with an if statement.

        Pendulum monsters also have a "scale", which has a separate field in the class.

        Xyz monsters do not have a level, but a rank instead. However, the API shows them as having a level.
        We can deal with this later in the GUI by changing the word "level" to "rank", which is easy.

        Monsters are the only "type" to end in r, link monsters are the only one to start with L and pendulum monsters are the only one to start with P.
        As such, we can use 3 if statements to do all this logic, which I think is more efficient than subclasses, if a little more messy.
     */
    public void updateCard(JSONObject cardInfo) {
        this.id = cardInfo.getInt("id");
        this.name = cardInfo.getString("name");
        this.type = cardInfo.getString("type");
        this.desc = cardInfo.getString("desc");
        this.race = cardInfo.getString("race");
        if(this.type.charAt(this.type.length() - 1) == 'r'){
            this.atk = cardInfo.getInt("atk");
            this.attribute = cardInfo.getString("attribute");
            if(this.type.charAt(0) == 'L'){
                this.level = cardInfo.getInt("linkval");
            } else{
                this.def = cardInfo.getInt("def");
                this.level = cardInfo.getInt("level");
            }
            if(this.type.charAt(0) == 'P'){
                this.scale = cardInfo.getInt("scale");
            }
        }
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(this.id);
    }

    /*
      This is needed as the .get method on HashMap does not check HashCode equivalence.
      Instead, it checks object equivalence; and since 2 cards will never have the same id and not be equal we can use this.
     */
    @Override
    public boolean equals(Object o) {
        if(o instanceof Card){
            return this.id == ((Card) o).id;
        }
        else{
            return false;
        }
    }

    @Override
    public String toString(){
        return name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public String getRace() {
        return race;
    }

    public int getAtk() {
        return atk;
    }

    public int getDef() {
        return def;
    }

    public int getLevel() {
        return level;
    }

    public String getAttribute() {
        return attribute;
    }

    public int getScale() {
        return scale;
    }

    public Image getCardImg() {
        return cardImg;
    }

    public void setCardImg(Image cardImg) {
        this.cardImg = cardImg;
    }
}
