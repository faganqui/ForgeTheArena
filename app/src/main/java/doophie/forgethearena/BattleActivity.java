package doophie.forgethearena;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

import org.w3c.dom.Text;

public class BattleActivity extends AppCompatActivity {

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

    EditText temp_player_stats_1;
    EditText temp_player_stats_2;
    EditText temp_player_stats_3;

    EditText temp_enemy_stats_1;
    EditText temp_enemy_stats_2;
    EditText temp_enemy_stats_3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        Intent recievedIntent = getIntent();
        String result = recievedIntent.getStringExtra("result");

        if(result != null){
            TextView helloworld = (TextView) findViewById(R.id.helloworld);
            helloworld.setText(result);
        }

        temp_player_stats_1 = new EditText(this);
        temp_player_stats_2 = new EditText(this);
        temp_player_stats_3 = new EditText(this);

        temp_enemy_stats_1 = new EditText(this);
        temp_enemy_stats_2 = new EditText(this);
        temp_enemy_stats_3 = new EditText(this);

        temp_player_stats_1.setText("0,10,15,15,20,15,15,Pi-Ea,Ea");
        temp_player_stats_2.setText("0,10,15,15,20,15,15,Pi-Ea,Ea");
        temp_player_stats_3.setText("0,10,15,15,20,15,15,Pi-Ea,Ea");

        temp_enemy_stats_1.setText("0,10,15,15,20,15,15,Pi-Ea,Ea");
        temp_enemy_stats_2.setText("0,10,15,15,20,15,15,Pi-Ea,Ea");
        temp_enemy_stats_3.setText("0,10,15,15,20,15,15,Pi-Ea,Ea");

        LinearLayout thisView = (LinearLayout) findViewById(R.id.battle_view);
        thisView.addView(temp_player_stats_1);
        thisView.addView(temp_player_stats_2);
        thisView.addView(temp_player_stats_3);

        thisView.addView(temp_enemy_stats_1);
        thisView.addView(temp_enemy_stats_2);
        thisView.addView(temp_enemy_stats_3);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        float ypos = motionEvent.getX();

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

            // Player has touched the screen
            case MotionEvent.ACTION_DOWN:

                break;

            // Player has removed finger from screen
            case MotionEvent.ACTION_UP:
                if(ypos > 750) {
                    setSavedPrefs();

                    Intent intent = new Intent(this, CustomizeCharacterActivity.class);
                    startActivity(intent);
                    break;
                }else{

                    setSavedPrefs();

                    Intent intent = new Intent(this, SpriteSheetAnimation.class);
                    startActivity(intent);
                }
        }
        return true;
    }

    @Override
    public void onBackPressed(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void setSavedPrefs(){
        SharedPreferences sharedPref = this.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        //todo: set these in different places
        editor.putString(STRING_BACKGROUND, "background");

        editor.putString(FIRST_ENEMY_WEAPON, "sword");
        editor.putString(SECOND_ENEMY_WEAPON, "axe");
        editor.putString(THIRD_ENEMY_WEAPON, "mace");

        editor.putString(ENEMY_HEAD, "bob");
        editor.putString(ENEMY_LEGS, "bob");
        editor.putString(ENEMY_TORSO, "bob");

        editor.putString(FIRST_ENEMY_WEAPON_STATS, String.valueOf(temp_enemy_stats_1.getText()));
        editor.putString(SECOND_ENEMY_WEAPON_STATS, String.valueOf(temp_enemy_stats_2.getText()));
        editor.putString(THIRD_ENEMY_WEAPON_STATS, String.valueOf(temp_enemy_stats_3.getText()));

        editor.commit();
    }
}
