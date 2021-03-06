package doophie.forgethearena;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.Arrays;

public class CustomizeCharacterActivity extends AppCompatActivity implements View.OnClickListener{

    //useful list
    private static final String[] statsOrder = {"Durability","Toughness","Power","Speed","Elemental\nForce","Elemental\nResist","Attack Type"};

    //Prefs
    private static final String SHARED_PREFS = "FORGE_SAVED_PREFS";

    private static final String DISPLAY_NAME = "name";

    private static final String FIRST_WEAPON = "weapon1";
    private static final String SECOND_WEAPON = "weapon2";
    private static final String THIRD_WEAPON = "weapon3";

    private static final String PLAYER_LEGS = "playerlegs";
    private static final String PLAYER_TORSO = "playertorso";
    private static final String PLAYER_HEAD = "playerhead";

    private static final String WEAPON_ONE_SPENT_STATS = "weapononespentstats";
    private static final String WEAPON_TWO_SPENT_STATS = "weapontwospentstats";
    private static final String WEAPON_THREE_SPENT_STATS = "weaponthreespentstats";

    private static final String OWNED_OUTFITS = "ownedoutfits";

    //todo: may change this to just colours not 3 diff images
    private static final String FIRST_GEM = "gem1";
    private static final String SECOND_GEM = "gem2";
    private static final String THIRD_GEM = "gem3";
    private static final String AMULET_STRING = "amulet";

    private static final String FIRST_WEAPON_STATS = "stats1";
    private static final String SECOND_WEAPON_STATS = "stats2";
    private static final String THIRD_WEAPON_STATS = "stats3";

    private static final String OWNED_WEAPONS = "ownedweapons";

    private static final String TAG = "CustCharAct";

    //datatbase
    FirebaseDatabase database;

    //unlocked items
    String[] ownedWeapons;
    String[] ownedOutfits;
    String[] gems = {"gemearth","gemfire","gemwater","gemlight","gemdark"};

    //User stats
    String userId;
    String userDisplay;
    String[] playerOneStatString = new String[3];
    String[] playerOneWeaponLocations = new String[3];
    String[] stringPlayerOne = new String[3];
    String[] stringPlayerOneSpent = new String[3];
    int availible_points = 0;
    String[] stringGem = new String[3];
    String stringAmulet;

    //locations of items for ease of saving
    String[] stat_array = new String[18];
    int save_index = 0;

    //interface objects
    EditText displayNameInput;
    Button saveDisplayButton;
    LinearLayout stuffLayout;
    ImageView imageView;

    //set size of drawn character
    private int frameWidth = 600;
    private int frameHeight = 600;

    private Rect frameToDraw = new Rect(
            0, 0,
            frameWidth,
            frameHeight);

    RectF whereToDraw = new RectF(
            0, 0,
            frameWidth,
            frameHeight);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_character);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userDisplay = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

        getSharedPrefs();

        //set up stat array
        stat_array[0] = DISPLAY_NAME;
        stat_array[1] = FIRST_WEAPON;
        stat_array[2] = SECOND_WEAPON;
        stat_array[3] = THIRD_WEAPON;
        stat_array[4] = PLAYER_LEGS;
        stat_array[5] = PLAYER_TORSO;
        stat_array[6] = PLAYER_HEAD;
        stat_array[7] = FIRST_GEM;
        stat_array[8] = SECOND_GEM;
        stat_array[9] = THIRD_GEM;
        stat_array[10] = AMULET_STRING;
        stat_array[11] = FIRST_WEAPON_STATS;
        stat_array[12] = SECOND_WEAPON_STATS;
        stat_array[13] = THIRD_WEAPON_STATS;
        stat_array[14] = WEAPON_ONE_SPENT_STATS;
        stat_array[15] = WEAPON_TWO_SPENT_STATS;
        stat_array[16] = WEAPON_THREE_SPENT_STATS;
        stat_array[17] = OWNED_WEAPONS;

        //load interface objects
        displayNameInput = findViewById(R.id.DisplayEditText);
        displayNameInput.setText(userDisplay);
        displayNameInput.setOnClickListener(this);

        saveDisplayButton = findViewById(R.id.saveDisplay);
        saveDisplayButton.setText(getEmojiByUnicode(0x1F4BE));
        saveDisplayButton.setOnClickListener(this);

        //stuff layout is used to display the layouts for changing the interface
        stuffLayout = findViewById(R.id.customize_things_zone);

        // Get database instance
        database = FirebaseDatabase.getInstance();

        //set the default interface
        setOutfitInterface();

    }

    @Override
    public void onClick(View view){

        switch (view.getId()){
            case (R.id.saveDisplay):
                savetoDatabase();
                break;
            
            // buttons for selecting inteface
            case (R.id.outfit_change_button):
                setOutfitInterface();
                break;
            case (R.id.weapons_change_button):
                setWeaponInterface();
                break;
            case (R.id.stats_change_button):
                setStatInterface();
                break;
            
            // change outfit interface
            //// TODO: 2017-10-13
            case (R.id.back_button_head):
                changeOutfitButton(2, false);
                break;
            case (R.id.next_button_head):
                changeOutfitButton(2, true);
                break;
            case (R.id.back_button_torso):
                changeOutfitButton(1, false);
                break;
            case (R.id.next_button_torso):
                changeOutfitButton(1, true);
                break;
            case (R.id.back_button_legs):
                changeOutfitButton(0, false);
                break;
            case (R.id.next_button_legs):
                changeOutfitButton(0, true);
                break;

            //change weapon buttons
            case (R.id.next_weapon_switch):
                changeWeaponButton(0, true);
                break;
            case (R.id.next_weapon_switch_1):
                changeWeaponButton(1, true);
                break;
            case (R.id.next_weapon_switch_2):
                changeWeaponButton(2, true);
                break;
            case (R.id.prev_weapon_switch):
                changeWeaponButton(0, false);
                break;
            case (R.id.prev_weapon_switch_1):
                changeWeaponButton(1, false);
                break;
            case (R.id.prev_weapon_switch_2):
                changeWeaponButton(2, false);
                break;

            //change gem buttons
            case (R.id.next_gem_switch):
                changegemButton(0, true);
                break;
            case (R.id.next_gem_switch_1):
                changegemButton(1, true);
                break;
            case (R.id.next_gem_switch_2):
                changegemButton(2, true);
                break;
            case (R.id.prev_gem_switch):
                changegemButton(0, false);
                break;
            case (R.id.prev_gem_switch_1):
                changegemButton(1, false);
                break;
            case (R.id.prev_gem_switch_2):
                changegemButton(2, false);
                break;

            case (R.id.reset_button):
                statResetButtons();
                break;

            default:
                //change stat buttons
                if (view.getId() >= 100 && view.getId() <= 200){
                    //first weapon plus
                    //setWeaponStat(0,view.getId()-100,increaseWeaponStat(0, view.getId()-100);true);
                    increaseWeaponStat(0, view.getId()-100);
                } else if (view.getId() >= 200 && view.getId() <= 300){
                    //second weapon plus
                    //setWeaponStat(1,view.getId()-200,true);
                    increaseWeaponStat(1, view.getId()-200);
                } else if (view.getId() >= 300 && view.getId() <= 400){
                    //third weapon plus
                    //setWeaponStat(2,view.getId()-300,true);
                    increaseWeaponStat(2, view.getId()-300);
                } else if (view.getId() >= 1000 && view.getId() <= 1100){
                    //first weapon minus
                    setWeaponStat(0,view.getId()-1000,false);
                } else if (view.getId() >= 2000 && view.getId() <= 2100){
                    //second weapon minus
                    setWeaponStat(1,view.getId()-2000,false);
                } else if (view.getId() >= 3000 && view.getId() <= 3100){
                    //third weapon minus
                    setWeaponStat(2,view.getId()-3000,false);
                }

        }

    }

    public void savetoDatabase(){
        //save all changes to database
        String weapons = "";
        for (String weapon: ownedWeapons){
            weapons += weapon + "]";
        }
        weapons = weapons.substring(0,weapons.length()-1);

        String[] values = {String.valueOf(displayNameInput.getText()),
                playerOneWeaponLocations[0],playerOneWeaponLocations[1],playerOneWeaponLocations[2],
                stringPlayerOne[0], stringPlayerOne[1], stringPlayerOne[2],
                stringGem[0],stringGem[1],stringGem[2],
                stringAmulet,
                playerOneStatString[0],playerOneStatString[1],playerOneStatString[2],
                stringPlayerOneSpent[0],stringPlayerOneSpent[1],stringPlayerOneSpent[2],
                weapons
        };

        //after we save we leave this page and load all the new settings from data
        if(save_index == values.length) {
            save_index = 0;
            Intent intent = new Intent(this, LoadFromDatabase.class);
            startActivity(intent);
        }else{
            setStat(stat_array[save_index], values[save_index]);
        }

        /*
        setStat(DISPLAY_NAME, String.valueOf(displayNameInput.getText()));
        setStat(PLAYER_HEAD, stringPlayerOne[2]);
        setStat(PLAYER_TORSO, stringPlayerOne[1]);
        setStat(PLAYER_LEGS, stringPlayerOne[0]);
        setStat(FIRST_WEAPON, playerOneWeaponLocations[0]);
        setStat(SECOND_WEAPON, playerOneWeaponLocations[1]);
        setStat(THIRD_WEAPON, playerOneWeaponLocations[2]);
        setStat(FIRST_GEM, stringGem[0]);
        setStat(SECOND_GEM, stringGem[1]);
        setStat(THIRD_GEM, stringGem[2]);
        setStat(AMULET_STRING, stringAmulet);
        setStat(FIRST_WEAPON_STATS, playerOneStatString[0]);
        setStat(SECOND_WEAPON_STATS, playerOneStatString[1]);
        setStat(THIRD_WEAPON_STATS, playerOneStatString[2]);
        */

    }

    public void getSharedPrefs() {

        Context context = this.getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        userDisplay = sharedPref.getString(DISPLAY_NAME, "UserName");

        playerOneWeaponLocations[0] = sharedPref.getString(FIRST_WEAPON, "sword");
        playerOneWeaponLocations[1] = sharedPref.getString(SECOND_WEAPON, "sword");
        playerOneWeaponLocations[2] = sharedPref.getString(THIRD_WEAPON, "sword");

        stringPlayerOne[0] = sharedPref.getString(PLAYER_LEGS, "sword");
        stringPlayerOne[1] = sharedPref.getString(PLAYER_TORSO, "sword");
        stringPlayerOne[2] = sharedPref.getString(PLAYER_HEAD, "sword");

        stringPlayerOneSpent[0] = sharedPref.getString(WEAPON_ONE_SPENT_STATS, "0,0,0,0,0,0,0");
        stringPlayerOneSpent[1] = sharedPref.getString(WEAPON_TWO_SPENT_STATS, "0,0,0,0,0,0,0");
        stringPlayerOneSpent[2] = sharedPref.getString(WEAPON_THREE_SPENT_STATS, "0,0,0,0,0,0,0");

        //get the owned weapons & outfits for each player
        ownedOutfits = sharedPref.getString(OWNED_OUTFITS, "").split("]");
        ownedWeapons = sharedPref.getString(OWNED_WEAPONS, "").split("]");

        //get bitmaps for amulets and gem from shared prefs
        stringGem[0] = sharedPref.getString(FIRST_GEM, "sword");
        stringGem[1] = sharedPref.getString(SECOND_GEM, "sword");
        stringGem[2] = sharedPref.getString(THIRD_GEM, "sword");
        stringAmulet = sharedPref.getString(AMULET_STRING, "sword");

        //get stats for each player 1 weapon
        playerOneStatString[0] = sharedPref.getString(FIRST_WEAPON_STATS, "sword");
        playerOneStatString[1] = sharedPref.getString(SECOND_WEAPON_STATS, "sword");
        playerOneStatString[2] = sharedPref.getString(THIRD_WEAPON_STATS, "sword");

        //see how many spent points have been spent
        availible_points = 75;
        for (int i = 0 ; i < 3; i++){
            for (int j = 0; j < stringPlayerOneSpent[i].split(",").length; j ++){
                availible_points -= Integer.valueOf(stringPlayerOneSpent[i].split(",")[j]);
            }
        }
    }

    /*helper function to set statistics*/
    public String addStatStrings(String stat_string_one, String stat_string_two){
        // adds all the integers in stat string one and stat string two which are seperated by commas
        // they are each expected to contain 7 ints

        String new_stat_string = "";

        String[] stat_array1 = stat_string_one.split(",");
        String[] stat_array2 = stat_string_two.split(",");

        for (int i = 0 ; i < 7; i ++){
            new_stat_string += String.valueOf(Integer.valueOf(stat_array1[i]) + Integer.valueOf(stat_array2[i])) + ",";
        }

        return new_stat_string;
    }

    public void setStat(String stat, String value){
        // sets a specific statistic for a user in the database
        DatabaseReference statData = database.getReference("/users/" + userId + "/" + stat);
        statData.setValue(value);

        statData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                save_index++;
                savetoDatabase();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void changeWeaponButton(int weapon_index, Boolean isNext){
        //changes local weapon pieces and displays them
        int current_index;

        //refund points spent on selected weapon
        int spent_points = 0;
        for (String stat : stringPlayerOneSpent[weapon_index].split(",")){
            spent_points += Integer.valueOf(stat);
        }
        stringPlayerOneSpent[weapon_index] = "0,0,0,0,0,0,0";
        availible_points += spent_points;

        current_index = Arrays.asList(ownedWeapons).indexOf(playerOneWeaponLocations[weapon_index] + "[" + playerOneStatString[weapon_index]);
        if(isNext) {
            current_index = (current_index + 1) % ownedWeapons.length;
        }else{
            if(current_index == 0){
                current_index = ownedWeapons.length-1;
            }else{
                current_index--;
            }
        }

        playerOneWeaponLocations[weapon_index] = ownedWeapons[current_index].split("\\[")[0];
        playerOneStatString[weapon_index] = ownedWeapons[current_index].split("\\[")[1];

        if (ownedWeapons[current_index].split("\\[")[1].contains("Wa")){
            stringGem[weapon_index] = gems[2];
        } else if (ownedWeapons[current_index].split("\\[")[1].contains("Li")){
            stringGem[weapon_index] = gems[3];
        } else if (ownedWeapons[current_index].split("\\[")[1].contains("Da")){
            stringGem[weapon_index] = gems[4];
        } else if (ownedWeapons[current_index].split("\\[")[1].contains("Ea")){
            stringGem[weapon_index] = gems[0];
        } else if (ownedWeapons[current_index].split("\\[")[1].contains("Fi")){
            stringGem[weapon_index] = gems[1];
        }

        //draw gem of newly selected weapon
        ImageView gemImage = findViewById(R.id.gem_view);
        switch (weapon_index){
            case 1:
                gemImage = findViewById(R.id.gem_view_1);
                break;
            case 2:
                gemImage = findViewById(R.id.gem_view_2);
                break;
        }
        gemImage.setImageBitmap(drawGem(weapon_index));

        //update all held weapons
        for(int weapon_held_index = 0; weapon_held_index < 3; weapon_held_index++){
            if(playerOneWeaponLocations[weapon_held_index].equals(playerOneWeaponLocations[weapon_index])) {
                playerOneStatString[weapon_held_index] = ownedWeapons[current_index].split("\\[")[1];
            }
        }

        ImageView weaponView = findViewById(R.id.weapon_view + weapon_index);
        weaponView.setImageBitmap(drawWeapon(weapon_index));
    }

    public void changegemButton(int weapon_index, Boolean isNext){
        //changes local weapon gem pieces and displays them
        //todo: fix all this siht
        int current_index;

        // gets the index of the weapon in "owned weapons" as well as "held weapon"
        // todo: chane playerOneWeaponLocations to hold  the index of it in owned weapons instead
        current_index = Arrays.asList(ownedWeapons).indexOf(playerOneWeaponLocations[weapon_index] + "[" + playerOneStatString[weapon_index]);

        int type;

        //figure out what the old gem type was and change it
        //todo: maybe make this better also use isNext
        if(ownedWeapons[current_index].contains("Ea")) {
            ownedWeapons[current_index] = ownedWeapons[current_index].replace("Ea", "Fi");
            type = 1;
        }else if(ownedWeapons[current_index].contains("Fi")) {
            ownedWeapons[current_index] = ownedWeapons[current_index].replace("Fi", "Wa");
            type = 2;
        }else if(ownedWeapons[current_index].contains("Wa")) {
            ownedWeapons[current_index] = ownedWeapons[current_index].replace("Wa", "Li");
            type = 3;
        }else if(ownedWeapons[current_index].contains("Li")){
            ownedWeapons[current_index] = ownedWeapons[current_index].replace("Li", "Da");
            type = 4;
        } else {
            ownedWeapons[current_index] = ownedWeapons[current_index].replace("Da", "Ea");
            type = 0;
        }

        //update all held weapon gems
        ImageView gemView = new ImageView(this);
        for(int weapon_held_index = 0; weapon_held_index < 3; weapon_held_index++){
            // since we can hold multiple copies of the same weapon we have to update them all
            if(playerOneWeaponLocations[weapon_held_index].equals(playerOneWeaponLocations[weapon_index])) {
                //update stat arrays of held weapons containing the changed gem
                playerOneStatString[weapon_held_index] = ownedWeapons[current_index].split("\\[")[1];

                //update string of gem picture
                stringGem[weapon_held_index] = gems[type];

                //draw the new gem(s)
                switch (weapon_held_index){
                    case 0:
                        gemView = findViewById(R.id.gem_view);
                        break;
                    case 1:
                        gemView = findViewById(R.id.gem_view_1);
                        break;
                    case 2:
                        gemView = findViewById(R.id.gem_view_2);
                        break;
                }
                gemView.setImageBitmap(drawGem(weapon_held_index));
            }
        }
    }

    public void changeOutfitButton(int piece, boolean isNext){
        //changes local outfit pieces and displays them
        String[] temp_list;
        int current_index;

        temp_list = ownedOutfits[piece].split(",");
        current_index = Arrays.asList(temp_list).indexOf(stringPlayerOne[piece]);
        if(isNext) {
            current_index = (current_index + 1) % temp_list.length;
        }else{
            if(current_index == 0){
                current_index = temp_list.length-1;
            }else{
                current_index--;
            }
        }
        stringPlayerOne[piece] = temp_list[current_index];
        drawCharacter();
    }

    public void increaseWeaponStat(int weapon, int stat){
        if (availible_points > 0) {
            String[] cur_stats = stringPlayerOneSpent[weapon].split(",");
            int cur_stat = Integer.valueOf(cur_stats[stat]);

            cur_stat++;
            availible_points--;

            cur_stats[stat] = String.valueOf(cur_stat);

            String stats_string = "";
            for(String temp_stat : cur_stats){
                stats_string += (temp_stat + ",");
            }

            stringPlayerOneSpent[weapon] = stats_string;
        }
        setStatInterface();
    }

    public void setWeaponStat(int weapon, int stat, Boolean isPlus){
        //set the stat of a specific weapon locally - will be saved to database on activity exit
        String[] cur_stats = playerOneStatString[weapon].split(",");
        int cur_stat = Integer.valueOf(cur_stats[stat]);
        if(isPlus){
            cur_stat++;
        }else{
            cur_stat--;
        }
        cur_stats[stat] = String.valueOf(cur_stat);

        String stats_string = "";
        for(String temp_stat : cur_stats){
            stats_string += (temp_stat + ",");
        }

        //update the weapon in owned weapons
        int index_of_weapon = Arrays.asList(ownedWeapons).indexOf(playerOneWeaponLocations[weapon] + "[" + playerOneStatString[weapon]);
        ownedWeapons[index_of_weapon] = ownedWeapons[index_of_weapon].split("\\[")[0] + "[" + stats_string;

        //update all held weapons
        for(int weapon_held_index = 0; weapon_held_index < 3; weapon_held_index++){
            if(playerOneWeaponLocations[weapon_held_index].equals(playerOneWeaponLocations[weapon])) {
                playerOneStatString[weapon_held_index] = ownedWeapons[index_of_weapon].split("\\[")[1];
            }
        }

        setStatInterface();
    }

    public void statResetButtons(){
        stringPlayerOneSpent[0] = "0,0,0,0,0,0,0";
        stringPlayerOneSpent[1] = "0,0,0,0,0,0,0";
        stringPlayerOneSpent[2] = "0,0,0,0,0,0,0";
        availible_points = 75;
        setStatInterface();
    }
    /*end of methods to set statistics

    /* helper methods for drawing bitmaps or other character */
    public String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }

    public Bitmap getCombinedBitmap(Bitmap b, Bitmap b2, Bitmap b3) {
        //returns a bitmap of b, b2, b3 overlayed ontop of each other
        Bitmap drawnBitmap = null;

        try {
            drawnBitmap = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(drawnBitmap);

            canvas.drawBitmap(b, frameToDraw, whereToDraw, null);
            canvas.drawBitmap(b2, frameToDraw, whereToDraw, null);
            canvas.drawBitmap(b3, frameToDraw, whereToDraw, null);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return drawnBitmap;
    }

    public void drawCharacter(){
        //load and scale the player
        int resID = getResources().getIdentifier(stringPlayerOne[0],
                "drawable", getPackageName());
        Bitmap legs = BitmapFactory.decodeResource(this.getResources(), resID);

        legs = Bitmap.createScaledBitmap(legs,
                frameWidth * 5,
                frameHeight,
                false);

        resID = getResources().getIdentifier(stringPlayerOne[1],
                "drawable", getPackageName());
        Bitmap body = BitmapFactory.decodeResource(this.getResources(), resID);

         body = Bitmap.createScaledBitmap(body,
                frameWidth * 5,
                frameHeight,
                false);

        resID = getResources().getIdentifier(stringPlayerOne[2],
                "drawable", getPackageName());
        Bitmap head = BitmapFactory.decodeResource(this.getResources(), resID);

        head = Bitmap.createScaledBitmap(head,
                frameWidth * 5,
                frameHeight,
                false);

        imageView.setImageBitmap(getCombinedBitmap(legs, body, head));
    }

    public Bitmap drawWeapon(int index){
        //load and scale the player
        int resID = getResources().getIdentifier(playerOneWeaponLocations[index],
                "drawable", getPackageName());
        Bitmap weapon = BitmapFactory.decodeResource(this.getResources(), resID);

        weapon = Bitmap.createScaledBitmap(weapon,
                frameWidth * 5,
                frameHeight,
                false);

        Bitmap drawnBitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(drawnBitmap);

        canvas.drawBitmap(weapon, frameToDraw, whereToDraw, null);

        return(drawnBitmap);
    }

    public Bitmap drawGem(int index){
        //load and scale the gem
        int resID = getResources().getIdentifier(stringGem[index],
                "drawable", getPackageName());
        Bitmap weapon = BitmapFactory.decodeResource(this.getResources(), resID);


        weapon = Bitmap.createScaledBitmap(weapon,
                200,
                200,
                false);

        return(weapon);
    }
    /*end of drawing helper messages*/

    public void onBackPressed(){
        Intent intent = new Intent(this, LoadFromDatabase.class);
        startActivity(intent);
    }

    /*draw interfaces*/
    public void setWeaponInterface(){
        //allows user to change weapon
        stuffLayout.removeAllViews();

        TableLayout weaponSwitchTable= new TableLayout(this);

        for (int weapon_index = 0; weapon_index < playerOneStatString.length; weapon_index++){
            TableRow row = new TableRow(this);

            Button prev_button = new Button(this);
            Button next_button = new Button(this);

            prev_button.setText("<");
            next_button.setText(">");

            ImageView weaponView = new ImageView(this);
            weaponView.setImageBitmap(drawWeapon(weapon_index));

            switch (weapon_index){
                case 0:
                    prev_button.setId(R.id.prev_weapon_switch);
                    next_button.setId(R.id.next_weapon_switch);
                    weaponView.setId(R.id.weapon_view);
                    break;
                case 1:
                    prev_button.setId(R.id.prev_weapon_switch_1);
                    next_button.setId(R.id.next_weapon_switch_1);
                    weaponView.setId(R.id.weapon_view_1);
                    break;
                case 2:
                    prev_button.setId(R.id.prev_weapon_switch_2);
                    next_button.setId(R.id.next_weapon_switch_2);
                    weaponView.setId(R.id.weapon_view_2);
                    break;
            }

            prev_button.setOnClickListener(this);
            next_button.setOnClickListener(this);

            LinearLayout gem_layout = new LinearLayout(this);
            gem_layout.setOrientation(LinearLayout.VERTICAL);
            gem_layout.setGravity(Gravity.CENTER);

            Button gemNextButton = new Button(this);
            Button gemPrevButton = new Button(this);

            gemNextButton.setText("/\\");
            gemPrevButton.setText("\\/");

            ImageView gemView = new ImageView(this);
            gemView.setImageBitmap(drawGem(weapon_index));

            switch (weapon_index){
                case 0:
                    gemPrevButton.setId(R.id.prev_gem_switch);
                    gemNextButton.setId(R.id.next_gem_switch);
                    gemView.setId(R.id.gem_view);
                    break;
                case 1:
                    gemPrevButton.setId(R.id.prev_gem_switch_1);
                    gemNextButton.setId(R.id.next_gem_switch_1);
                    gemView.setId(R.id.gem_view_1);
                    break;
                case 2:
                    gemPrevButton.setId(R.id.prev_gem_switch_2);
                    gemNextButton.setId(R.id.next_gem_switch_2);
                    gemView.setId(R.id.gem_view_2);
                    break;
            }

            gemNextButton.setOnClickListener(this);
            gemPrevButton.setOnClickListener(this);

            gem_layout.addView(gemNextButton);
            gem_layout.addView(gemView);
            gem_layout.addView(gemPrevButton);

            row.addView(prev_button);
            row.addView(weaponView);
            row.addView(next_button);
            row.addView(gem_layout);

            weaponSwitchTable.addView(row);
        }

        stuffLayout.addView(weaponSwitchTable);
    }

    public void setStatInterface(){
        //allows user to change stats
        stuffLayout.removeAllViews();

        stuffLayout.setOrientation(LinearLayout.VERTICAL);
        TableLayout.LayoutParams params = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        stuffLayout.setLayoutParams(params);

        LinearLayout hLayout = new LinearLayout(this);
        hLayout.setOrientation(LinearLayout.HORIZONTAL);
        params = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        hLayout.setLayoutParams(params);

        Button resetButton = new Button(this);
        resetButton.setId(R.id.reset_button);
        resetButton.setText("Reset");
        resetButton.setOnClickListener(this);

        TextView availPoints = new TextView(this);
        availPoints.setText("Availible Points: " + availible_points);

        stuffLayout.addView(availPoints);
        stuffLayout.addView(resetButton);

        String[] new_stat_strings = new String[3];

        for(int i = 0; i  < 3; i ++){
            //for each column
            TableLayout tempColumnLayout = new TableLayout(this);
            params = new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
            tempColumnLayout.setLayoutParams(params);

            //get new stat value by adding the spent points and the base weapon stats
            new_stat_strings[i] = addStatStrings(playerOneStatString[i], stringPlayerOneSpent[i]);

            //put name of weapon at top
            TableRow row_name = new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);

            TextView name = new TextView(this);
            name.setText(playerOneWeaponLocations[i]);

            row_name.addView(name);
            tempColumnLayout.addView(row_name);

            for(int j = 1; j < statsOrder.length; j++){
                //for each row
                TableRow row = new TableRow(this);
                row.setLayoutParams(lp);
                row.setGravity(Gravity.CENTER_VERTICAL);
                //linear layout for the + button and number
                LinearLayout temp_buttons = new LinearLayout(this);
                temp_buttons.setOrientation(LinearLayout.VERTICAL);
                temp_buttons.setGravity(Gravity.CENTER);

                TextView tempStatTextView = new TextView(this);
                TextView tempStatDataTextView = new TextView(this);
                tempStatDataTextView.setGravity(Gravity.CENTER);
                tempStatDataTextView.setTextSize(10);
                tempStatTextView.setTextSize(10);

                Button plus = new Button(this);
                Button minus = new Button(this);
                plus.setText("+");
                minus.setText("-");

                tempStatTextView.setText(statsOrder[j-1] + ": ");
                tempStatDataTextView.setText(new_stat_strings[i].split(",")[j]);

                plus.setId(100*(i+1) + j);
                minus.setId(1000*(i+1) + j);

                plus.setOnClickListener(this);
                minus.setOnClickListener(this);

                row.addView(tempStatTextView);
                row.addView(tempStatDataTextView);
                temp_buttons.addView(plus);
                //temp_buttons.addView(minus);
                row.addView(temp_buttons);
                tempColumnLayout.addView(row);
            }

            hLayout.addView(tempColumnLayout);
        }
        stuffLayout.addView(hLayout);

    }

    public void setOutfitInterface(){
        //allows user to change outfit
        stuffLayout.removeAllViews();

        //make back layout horizonal
        stuffLayout.setOrientation(LinearLayout.HORIZONTAL);

        //make vertical layout for going back
        LinearLayout change_back_col = new LinearLayout(this);
        change_back_col.setOrientation(LinearLayout.VERTICAL);

        //make vertical layout for going next
        LinearLayout change_next_col = new LinearLayout(this);
        change_next_col.setOrientation(LinearLayout.VERTICAL);

        imageView = new ImageView(this);
        drawCharacter();

        Button temp_button;
        for (int i = 0; i < 6; i++){
            temp_button = new Button(this);
            if(i < 3) {
                //make a back button for head/torso/legs
                temp_button.setText("<");
                switch (i){
                    case 0:
                        temp_button.setId(R.id.back_button_head);
                        break;
                    case 1:
                        temp_button.setId(R.id.back_button_torso);
                        break;
                    case 2:
                        temp_button.setId(R.id.back_button_legs);
                        break;
                }
                temp_button.setOnClickListener(this);
                change_back_col.addView(temp_button);
            }else{
                //make a next button for head/torso/legs
                temp_button.setText(">");
                switch (i){
                    case 3:
                        temp_button.setId(R.id.next_button_head);
                        break;
                    case 4:
                        temp_button.setId(R.id.next_button_torso);
                        break;
                    case 5:
                        temp_button.setId(R.id.next_button_legs);
                        break;
                }
                temp_button.setOnClickListener(this);
                change_next_col.addView(temp_button);
            }
        }

        stuffLayout.addView(change_back_col);
        stuffLayout.addView(imageView);
        stuffLayout.addView(change_next_col);
    }
    /*end interfaces*/

}
