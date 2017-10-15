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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        Intent recievedIntent = getIntent();
        String result = recievedIntent.getStringExtra("result");

        if (result != null) {
            TextView helloworld = (TextView) findViewById(R.id.helloworld);
            helloworld.setText(result);
        }

        goToBattle = findViewById(R.id.go_to_battle);
        goToCustomize = findViewById(R.id.go_to_customize);

        goToBattle.setOnClickListener(this);
        goToCustomize.setOnClickListener(this);

    }

    @Override
    public void onClick(View view){
        Intent intent;
        switch(view.getId()){
            case R.id.go_to_battle:
                makeNewEnemy();
                intent = new Intent(this, SpriteSheetAnimation.class);
                startActivity(intent);
                break;
            case R.id.go_to_customize:
                intent = new Intent(this, CustomizeCharacterActivity.class);
                startActivity(intent);
                break;
        }

    }
    @Override
    public void onBackPressed(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void makeNewEnemy(){
        SharedPreferences sharedPref = this.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(STRING_BACKGROUND, "background");

        String[] weapons = {"sword","axe","mace"};
        editor.putString(FIRST_ENEMY_WEAPON, weapons[(randomWithRange(0,2))]);
        editor.putString(SECOND_ENEMY_WEAPON, weapons[(randomWithRange(0,2))]);
        editor.putString(THIRD_ENEMY_WEAPON, weapons[(randomWithRange(0,2))]);

        String[] heads = {"head","head2","head3"};
        editor.putString(ENEMY_HEAD, heads[(randomWithRange(0,2))]);
        editor.putString(ENEMY_LEGS, "legs");
        editor.putString(ENEMY_TORSO, "body2");

        editor.putString(FIRST_ENEMY_WEAPON_STATS, getRandomStatString());
        editor.putString(SECOND_ENEMY_WEAPON_STATS, getRandomStatString());
        editor.putString(THIRD_ENEMY_WEAPON_STATS, getRandomStatString());
        editor.commit();
    }

    public String getRandomStatString(){

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
        switch (randomWithRange(0,2)){
            case 0:
                attack_type = "Cr";
                break;
            case 1:
                attack_type = "Sl";
                break;
            case 2:
                attack_type = "Pi";
                break;
        }

        return String.valueOf("0," + //cur health always 0
                (randomWithRange(10,20)) +","+ //durability
                (randomWithRange(10,40)) +","+ //toughness
                (randomWithRange(10,40)) +","+ //power
                (randomWithRange(10,40)) +","+ //speed
                (randomWithRange(10,40)) +","+ //elemental force
                (randomWithRange(10,40)) +","+ //elemental resistance
                attack_type + "-" + element_type +
                "," + element_type);

    }

    int randomWithRange(int min, int max)
    {
        int range = Math.abs(max - min) + 1;
        return (int)(Math.random() * range) + (min <= max ? min : max);
    }
}

