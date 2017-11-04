package doophie.forgethearena;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

import org.w3c.dom.Text;

public class BattleActivity extends AppCompatActivity implements View.OnClickListener {

    //Prefs
    private static final String SHARED_PREFS = "FORGE_SAVED_PREFS";

    //names for prefs
    private static final String STRING_BACKGROUND = "background";

    private static final String FIRST_WEAPON = "weapon1";
    private static final String SECOND_WEAPON = "weapon2";
    private static final String THIRD_WEAPON = "weapon3";

    private static final String WEAPON_ONE_SPENT_STATS = "weapononespentstats";
    private static final String WEAPON_TWO_SPENT_STATS = "weapontwospentstats";
    private static final String WEAPON_THREE_SPENT_STATS = "weaponthreespentstats";

    private static final String FIRST_ENEMY_WEAPON = "enemyweapon1";
    private static final String SECOND_ENEMY_WEAPON = "enemyweapon2";
    private static final String THIRD_ENEMY_WEAPON = "enemyweapon3";

    private static final String PLAYER_LEGS = "playerlegs";
    private static final String PLAYER_TORSO = "playertorso";
    private static final String PLAYER_HEAD = "playerhead";

    private static final String ENEMY_LEGS = "enemylegs";
    private static final String ENEMY_TORSO = "enemytorso";
    private static final String ENEMY_HEAD = "enemyhead";

    //todo: may change this to just colours not 3 diff images
    private static final String FIRST_GEM = "gem1";
    private static final String SECOND_GEM = "gem2";
    private static final String THIRD_GEM = "gem3";
    private static final String AMULET_STRING = "amulet";

    private static final String FIRST_WEAPON_STATS = "stats1";
    private static final String SECOND_WEAPON_STATS = "stats2";
    private static final String THIRD_WEAPON_STATS = "stats3";

    private static final String FIRST_ENEMY_WEAPON_STATS = "enemystats1";
    private static final String SECOND_ENEMY_WEAPON_STATS = "enemystats2";
    private static final String THIRD_ENEMY_WEAPON_STATS = "enemystats3";

    Button goToCustomize;
    Button goToBattle;
    Button goToReforge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        Intent recievedIntent = getIntent();
        String result = recievedIntent.getStringExtra("result");

        TextView header_text = findViewById(R.id.helloworld);
        header_text.setText(result);

        goToBattle = findViewById(R.id.go_to_battle);
        goToCustomize = findViewById(R.id.go_to_customize);
        goToReforge = findViewById(R.id.go_to_reforge);

        goToBattle.setOnClickListener(this);
        goToCustomize.setOnClickListener(this);
        goToReforge.setOnClickListener(this);

    }

    @Override
    public void onClick(View view){
        Intent intent;
        switch(view.getId()){
            case R.id.go_to_battle:
                intent = new Intent(this, SpriteSheetAnimation.class);

                SharedPreferences sharedPref = this.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();

                //make player one stats
                editor.putString(FIRST_WEAPON_STATS, addStatStrings(sharedPref.getString(FIRST_WEAPON_STATS, ""), sharedPref.getString(WEAPON_ONE_SPENT_STATS, "0,0,0,0,0,0,0")));
                editor.putString(SECOND_WEAPON_STATS, addStatStrings(sharedPref.getString(SECOND_WEAPON_STATS, ""), sharedPref.getString(WEAPON_TWO_SPENT_STATS, "0,0,0,0,0,0,0")));
                editor.putString(THIRD_WEAPON_STATS, addStatStrings(sharedPref.getString(THIRD_WEAPON_STATS, ""), sharedPref.getString(WEAPON_THREE_SPENT_STATS, "0,0,0,0,0,0,0")));

                //make new enimy and commit editor
                makeNewEnemy(editor);

                //start the battle
                startActivity(intent);
                break;
            case R.id.go_to_customize:
                intent = new Intent(this, CustomizeCharacterActivity.class);
                startActivity(intent);
                break;
            case R.id.go_to_reforge:
                intent = new Intent(this, ReforgeWeaponActivity.class);
                startActivity(intent);
                break;
            case R.id.go_to_shop:
                intent = new Intent(this, PurchaseItemsActivity.class);
                startActivity(intent);
                break;
            case R.id.go_to_leaderboard:
                //todo
                break;
            case R.id.go_to_pvp:
                //todo
                break;
        }

    }

    public String addStatStrings(String stat_string_one, String stat_string_two){
        // adds all the integers in stat string one and stat string two which are seperated by commas
        // they are each expected to contain 7 ints

        String new_stat_string = "";

        String[] stat_array1 = stat_string_one.split(",");
        String[] stat_array2 = stat_string_two.split(",");

        for (int i = 0 ; i < 7; i ++){
            new_stat_string += String.valueOf(Integer.valueOf(stat_array1[i]) + Integer.valueOf(stat_array2[i])) + ",";
        }

        //also add stats of the weapon types to the end for battle only
        new_stat_string += stat_array1[7] + "," + stat_array1[8];

        return new_stat_string;
    }

    @Override
    public void onBackPressed(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void makeNewEnemy(SharedPreferences.Editor editor){
        editor.putString(STRING_BACKGROUND, "background");

        String[] weapons = {"sword","axe","mace"};

        String w1 = weapons[(randomWithRange(0,2))];
        String w2 = weapons[(randomWithRange(0,2))];
        String w3 = weapons[(randomWithRange(0,2))];

        editor.putString(FIRST_ENEMY_WEAPON, w1);
        editor.putString(SECOND_ENEMY_WEAPON, w2);
        editor.putString(THIRD_ENEMY_WEAPON, w3);

        String[] heads = {"head","head2","head3"};
        editor.putString(ENEMY_HEAD, heads[(randomWithRange(0,2))]);
        editor.putString(ENEMY_LEGS, "legs");
        editor.putString(ENEMY_TORSO, "body2");

        editor.putString(FIRST_ENEMY_WEAPON_STATS, getRandomStatString(w1));
        editor.putString(SECOND_ENEMY_WEAPON_STATS, getRandomStatString(w2));
        editor.putString(THIRD_ENEMY_WEAPON_STATS, getRandomStatString(w3));
        editor.commit();
    }

    public String getRandomStatString(String weapon){

        String element_type = "";
        switch (randomWithRange(0,4)) {
            case 0:
                element_type = "Wa";
                break;
            case 1:
                element_type = "Fi";
                break;
            case 2:
                element_type = "Ea";
                break;
            case 3:
                element_type = "Da";
                break;
            case 4:
                element_type = "Li";
                break;
        }

        String attack_type = "";
        switch (weapon){
            case "mace":
                attack_type = "Cr";
                break;
            case "axe":
                attack_type = "Sl";
                break;
            case "sword":
                attack_type = "Pi";
                break;
        }

        return String.valueOf("0," + //cur health always 0
                (randomWithRange(20,40)) +","+ //durability
                (randomWithRange(20,60)) +","+ //toughness
                (randomWithRange(20,60)) +","+ //power
                (randomWithRange(20,60)) +","+ //speed
                (randomWithRange(20,60)) +","+ //elemental force
                (randomWithRange(20,60)) +","+ //elemental resistance
                attack_type + "-" + element_type +
                "," + element_type);

    }

    int randomWithRange(int min, int max) {
        //returns a random int in the range of min and max

        int range = Math.abs(max - min) + 1;
        return (int)(Math.random() * range) + (min <= max ? min : max);
    }
}

