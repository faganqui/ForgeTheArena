package doophie.forgethearena;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoadFromDatabase extends AppCompatActivity {

    //Prefs
    private static final String SHARED_PREFS = "FORGE_SAVED_PREFS";

    private static final String DISPLAY_NAME = "name";

    private static final String FIRST_WEAPON = "weapon1";
    private static final String SECOND_WEAPON = "weapon2";
    private static final String THIRD_WEAPON = "weapon3";

    private static final String PLAYER_LEGS = "playerlegs";
    private static final String PLAYER_TORSO = "playertorso";
    private static final String PLAYER_HEAD = "playerhead";

    private static final String OWNED_OUTFITS = "ownedoutfits";

    //todo: may change this to just colours not 3 diff images
    private static final String FIRST_GEM = "gem1";
    private static final String SECOND_GEM = "gem2";
    private static final String THIRD_GEM = "gem3";
    private static final String AMULET_STRING = "amulet";

    private static final String WEAPON_ONE_SPENT_STATS = "weapononespentstats";
    private static final String WEAPON_TWO_SPENT_STATS = "weapontwospentstats";
    private static final String WEAPON_THREE_SPENT_STATS = "weaponthreespentstats";


    private static final String FIRST_WEAPON_STATS = "stats1";
    private static final String SECOND_WEAPON_STATS = "stats2";
    private static final String THIRD_WEAPON_STATS = "stats3";

    private static final String OWNED_WEAPONS = "ownedweapons";
    private static final String AVAILIBLE_CURRENCY = "moneymoneymoney";
    private static final String PLAYER_EXPERIENCE = "playerexp";

    String[] stat_array = new String[20];

    //get database objects
    FirebaseDatabase database;

    //objects for temporary data collection
    String result = "";
    int index = 0;
    Boolean hasNextStat = false;

    //User stats
    String userId;

    //shared preferences
    SharedPreferences sharedPref;

    //Text view for showing load percent
    TextView loadingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_from_database);

        //Loading text
        loadingText = findViewById(R.id.loading_text);

        //get prefs
        sharedPref = this.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        // Get database instance
        database = FirebaseDatabase.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

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
        stat_array[18] = AVAILIBLE_CURRENCY;
        stat_array[19] = OWNED_OUTFITS;
        stat_array[20] = PLAYER_EXPERIENCE;

        collectAllData();

    }

    public void collectAllData(){
        //display % of download completed
        loadingText.setText(String.format("%1$.2f",(Double.valueOf(index)/Double.valueOf(stat_array.length)) *100) + "%");

        //breaks if we've collected all the data
        if(index == stat_array.length){
            finishLoading();
            return;
        }

        //placeStatInPrefs
        if (hasNextStat){
            SharedPreferences.Editor editor = sharedPref.edit();
            //puts the read value in shared pref
            editor.putString(stat_array[index], result);
            editor.apply();
            index++;
            hasNextStat = false;
        }

        //retrieves each stat value from database
        if(index < stat_array.length) {
            getStat(stat_array[index]);
        } else {
            finishLoading();
        }
    }

    public void finishLoading(){
        Intent battle = new Intent(getBaseContext(), BattleActivity.class);
        startActivity(battle);
    }

    @Override
    public void onBackPressed(){
        //do nothing - makes it so nobody can stop the load from server from happening
    }

    public void getStat(String stat){
        //sets result to the current read data
        // sets a specific statistic for a user
        DatabaseReference statData = database.getReference("/users/" + userId + "/" + stat);

        // Attach a listener to read the data at our posts reference
        statData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                result = value;
                //calls to collect data after previous data is read
                hasNextStat = true;
                collectAllData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

    }

}
