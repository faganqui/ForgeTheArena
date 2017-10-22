package doophie.forgethearena;

/**
 * Created by Quinn on 2017-10-22.
 */

public class theGreatDictionaryOfWeapons {
    // this class just holds all the weapons in the game and their default stat values

    String[] theAlmightyDictionaryWhichIsActuallyAStringArrayButOhWellIUseItLikeADictionairyItsself =
            {"axe:0,45,30,50,15,15,40",
            "sword:0,25,20,35,40,60,25",
            "mace:0,35,20,45,25,40,30"};

    public theGreatDictionaryOfWeapons(){
        //required method?
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


}
