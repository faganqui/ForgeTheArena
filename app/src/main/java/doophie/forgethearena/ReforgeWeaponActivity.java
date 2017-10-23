package doophie.forgethearena;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

public class ReforgeWeaponActivity extends AppCompatActivity implements View.OnClickListener {

    //shared pref things
    private static final String SHARED_PREFS = "FORGE_SAVED_PREFS";

    private static final String FIRST_WEAPON_STATS = "stats1";
    private static final String SECOND_WEAPON_STATS = "stats2";
    private static final String THIRD_WEAPON_STATS = "stats3";

    private static final String FIRST_WEAPON = "weapon1";
    private static final String SECOND_WEAPON = "weapon2";
    private static final String THIRD_WEAPON = "weapon3";

    private static final String OWNED_WEAPONS = "ownedweapons";

    //static variables
    static int randomRangeForForging = 15;

    //variables
    int selected_weapon = 0;

    String[] playerOneStatString = new String[3];
    String[] playerOneWeaponString = new String[3];
    String[] ownedWeapons;

    theGreatDictionaryOfWeapons dict = new theGreatDictionaryOfWeapons();

    FirebaseDatabase database;
    String userId;

    ImageView weaponImage;
    TextView weaponText;
    TextView resultText;

    Button nextWeaponButton;
    Button prevWeaponButton;
    Button reforgeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reforge_weapon);

        // Get database instance
        database = FirebaseDatabase.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Load variables from preferences
        getSharedPrefs();

        weaponImage = findViewById(R.id.weapon_view);
        weaponText = findViewById(R.id.weapon_text);
        resultText = findViewById(R.id.resultText);

        nextWeaponButton = findViewById(R.id.next_weapon_switch);
        prevWeaponButton = findViewById(R.id.prev_weapon_switch);
        reforgeButton = findViewById(R.id.reforge_button);

        nextWeaponButton.setOnClickListener(this);
        prevWeaponButton.setOnClickListener(this);
        reforgeButton.setOnClickListener(this);

        drawWeapon();
    }

    private Rect frameToDraw = new Rect(
            0, 0,
            600,
            600);

    RectF whereToDraw = new RectF(
            0, 0,
            600,
            600);

    public void drawWeapon(){
        //draws the selected weapon to the interface
        weaponText.setText(ownedWeapons[selected_weapon].split("\\[")[0]);
        weaponImage.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        weaponImage.setImageBitmap(drawWeapon(selected_weapon));
    }

    public Bitmap drawWeapon(int index){
        //load and scale the weapon to the first frame
        int resID = getResources().getIdentifier(ownedWeapons[index].split("\\[")[0],
                "drawable", getPackageName());
        Bitmap weapon = BitmapFactory.decodeResource(this.getResources(), resID);

        weapon = Bitmap.createScaledBitmap(weapon,
                600 * 5,
                600,
                false);

        Bitmap drawnBitmap = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(drawnBitmap);

        canvas.drawBitmap(weapon, frameToDraw, whereToDraw, null);

        return(drawnBitmap);
    }

    public void changeWeapon(Boolean isNext){
        //changes the selected weapon
        if(isNext){
            selected_weapon = (selected_weapon + 1) % ownedWeapons.length;
        }else{
            selected_weapon -= 1;
            if(selected_weapon < 0){
                selected_weapon = ownedWeapons.length - 1;
            }
        }
        drawWeapon();
    }

    int randomWithRange(int min, int max) {
        //returns a random int in the range of min and max

        int range = Math.abs(max - min) + 1;
        return (int)(Math.random() * range) + (min <= max ? min : max);
    }

    public void reforgeWeapon(int index){
        //reforges the weapon in owened wepaons at the supplied index to have new values based on its
        //default values

        String weapon = ownedWeapons[index];
        String weapon_name = weapon.split("\\[")[0];
        String base_weapon_stats;
        String new_weapon_stats = "";
        int bonus = 0;

        base_weapon_stats = dict.getWeaponBaseStats(weapon_name);

        int temp_stat;
        int cur_stat;
        for (String stat : base_weapon_stats.split(",")){
            cur_stat = Integer.valueOf(stat);

            if(cur_stat != 0) {
                temp_stat = cur_stat - randomRangeForForging +
                        randomWithRange(0, randomRangeForForging * 2);
            }else{
                //first stat is always 0, just cuz
                temp_stat = 0;
            }

            bonus += (temp_stat - cur_stat);

            new_weapon_stats += temp_stat + ",";
        }

        ownedWeapons[index] = weapon_name + "[" + new_weapon_stats + ownedWeapons[index].split(",")[7] + "," + ownedWeapons[index].split(",")[8];

        for (int i = 0; i < 3; i++){
            if(playerOneWeaponString[i].contains(weapon_name)){
                //updates held weapon
                playerOneStatString[i] = new_weapon_stats +
                        playerOneStatString[i].split(",")[7] + "," +
                        playerOneStatString[i].split(",")[8];
            }
        }

        displayResults(bonus);
        drawWeapon();
        saveWeapon();
    }

    public void displayResults(int bonus){
        String resultString;

        if(bonus > 0) {
            resultString = "Your weapon has " + bonus + " more stats than its base stats";
        }else{
            resultString = ("Your weapon has " + bonus + " less stats than its base stats");
        }

        resultString += "\n";

        for(String stat : ownedWeapons[selected_weapon].split("\\[")[1].split(",")){
            if(isDigit(stat)) {
                resultString += "new stat = " + stat + "\n";
            }
        }

        resultText.setText(resultString);
    }

    public void getSharedPrefs(){
        //gets the current shared prefs
        Context context = this.getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        //get stats for each player 1 weapon
        playerOneStatString[0] = sharedPref.getString(FIRST_WEAPON_STATS, "sword");
        playerOneStatString[1] = sharedPref.getString(SECOND_WEAPON_STATS, "sword");
        playerOneStatString[2] = sharedPref.getString(THIRD_WEAPON_STATS, "sword");

        //get stats for each player 1 weapon
        playerOneWeaponString[0] = sharedPref.getString(FIRST_WEAPON, "sword");
        playerOneWeaponString[1] = sharedPref.getString(SECOND_WEAPON, "sword");
        playerOneWeaponString[2] = sharedPref.getString(THIRD_WEAPON, "sword");

        ownedWeapons = sharedPref.getString(OWNED_WEAPONS, "").split("]");
    }

    public Boolean isDigit(String digit){
        //returns true if number is a digit
        try{
            int x = Integer.valueOf(digit) + 3;
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public void saveWeapon(){
        //saves new weapon stats to database

        //put owned weapons back into string form
        String weapons = "";
        for (String weapon: ownedWeapons){
            weapons += weapon + "]";
        }
        weapons = weapons.substring(0,weapons.length()-1);

        setStat(OWNED_WEAPONS, weapons);
        setStat(FIRST_WEAPON_STATS, playerOneStatString[0]);
        setStat(SECOND_WEAPON_STATS, playerOneStatString[1]);
        setStat(THIRD_WEAPON_STATS, playerOneStatString[2]);
    }

    public void setStat(String stat, String value){
        //places the value in the database under the users "stat" stat

        DatabaseReference statData = database.getReference("/users/" + userId + "/" + stat);
        statData.setValue(value);
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, LoadFromDatabase.class);
        startActivity(intent);
    }

    public void confirmReforge(String results){
        AlertDialog.Builder adb = new AlertDialog.Builder(this);

        adb.setTitle("Reforge Results");
        adb.setMessage(results);
        adb.setIcon(android.R.drawable.ic_dialog_alert);


        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                Toast.makeText(getApplicationContext(), "Reforge Complete!", Toast.LENGTH_SHORT).show();
                saveWeapon();
            } });


        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                Toast.makeText(getApplicationContext(), "Reforge Canceled!", Toast.LENGTH_SHORT).show();
                finish();
            } });
        adb.show();
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.reforge_button:
                reforgeWeapon(selected_weapon);
                break;

            case R.id.next_weapon_switch:
                changeWeapon(true);
                break;

            case R.id.prev_weapon_switch:
                changeWeapon(false);
                break;
        }
    }
}
