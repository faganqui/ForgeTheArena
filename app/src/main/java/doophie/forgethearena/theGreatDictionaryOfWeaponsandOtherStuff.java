package doophie.forgethearena;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Quinn on 2017-10-22.
 */

public class theGreatDictionaryOfWeaponsandOtherStuff {
    // this class just holds all the weapons in the game and their default stat values

    private String[] theAlmightyDictionaryWhichIsActuallyAStringArrayButOhWellIUseItLikeADictionairyItsself =
            {"axe:0,45,30,50,15,15,40",
            "sword:0,25,20,35,40,60,25",
            "mace:0,35,20,45,25,40,30"};

    private String[] allOurAmulets =
            {"amulet"};

    private String[] allOurHeads =
            {"head",
            "head2",
            "head3"};

    private String[] allOurBodies =
            {"body",
            "body2"};

    private String[] allOurLegs =
            {"legs",
            "legs2"};

    private Map<String, Integer> costMap = new HashMap<String, Integer>();

    public theGreatDictionaryOfWeaponsandOtherStuff(){
        //required method?
        make_cost();
    }

    public void make_cost(){
        costMap.put("axe", 150);
        costMap.put("sword", 150);
        costMap.put("mace", 150);
        costMap.put("amulet", 150);
        costMap.put("head", 150);
        costMap.put("head2", 150);
        costMap.put("head3", 150);
        costMap.put("legs", 150);
        costMap.put("legs2", 150);
        costMap.put("body", 150);
        costMap.put("body2", 150);
    }

    public String getWeaponBaseStats(String weapon_to_be_found){
        for (String current_weapon : theAlmightyDictionaryWhichIsActuallyAStringArrayButOhWellIUseItLikeADictionairyItsself){
            if(current_weapon.contains(weapon_to_be_found)){
                //returns the base stats
                return current_weapon.split(":")[1];
            }
        }
        return "error";
    }

    public String[] getAllSomething(String something){
        String[] return_list = {};
        switch (something){
            case "amulet":
                return_list = allOurAmulets;
                break;
            case "head":
                return_list = allOurHeads;
                break;
            case "body":
                return_list = allOurBodies;
                break;
            case "leg":
                return_list = allOurLegs;
                break;
            case "weapon":
                return_list = theAlmightyDictionaryWhichIsActuallyAStringArrayButOhWellIUseItLikeADictionairyItsself;
                break;
        }
        return return_list;
    }

    public Integer getItemCost(String item){
        return costMap.get(item);
    }
}
