package doophie.forgethearena;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.KeyStore;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private static String TAG = "loginactivity";

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

    private static final String FIRST_WEAPON_STATS = "stats1";
    private static final String SECOND_WEAPON_STATS = "stats2";
    private static final String THIRD_WEAPON_STATS = "stats3";

    private static final String OWNED_WEAPONS = "ownedweapons";
    private static final String AVAILIBLE_CURRENCY = "moneymoneymoney";


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private FirebaseDatabase database;

    Button SignInButton;
    Button SignUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SignInButton = findViewById(R.id.SignInButton);
        SignUpButton = findViewById(R.id.SignUpButton);

        SignInButton.setOnClickListener(this);
        SignUpButton.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() != null) {
            //Starts loading the activity immidiatley if we are already signed in
            Intent load = new Intent(getBaseContext(), LoadFromDatabase.class);
            startActivity(load);
        }

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };


    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onClick(View view){

        EditText emailText = findViewById(R.id.UserEditText);
        String email = String.valueOf(emailText.getText());

        EditText passwordText = findViewById(R.id.PasswordEditText);
        String password = String.valueOf(passwordText.getText());

        if(email.equals("")|| password.equals("")){
            Toast.makeText(LoginActivity.this, R.string.auth_failed,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        switch (view.getId()){
            case R.id.SignInButton:
                //Signs in an existing user
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "signInWithEmail:failed", task.getException());
                                    Toast.makeText(LoginActivity.this, R.string.auth_failed,
                                            Toast.LENGTH_SHORT).show();
                                }else{

                                    Intent load = new Intent(getBaseContext(), LoadFromDatabase.class);
                                    startActivity(load);

                                }

                                // ...
                            }
                        });

                break;
            case R.id.SignUpButton:
                //Signs up a new user
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, R.string.auth_failed,
                                            Toast.LENGTH_SHORT).show();
                                }else{
                                    //verify email
                                    FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();

                                    //instantiate database
                                    database = FirebaseDatabase.getInstance();

                                    //set-up character
                                    buildNewCharacter();

                                    Intent load = new Intent(getBaseContext(), LoadFromDatabase.class);
                                    startActivity(load);
                                }
                            }
                        });
                break;
        }
    }

    public void buildNewCharacter(){
        //all the default items we give to new characters
        theGreatDictionaryOfWeaponsandOtherStuff dict = new theGreatDictionaryOfWeaponsandOtherStuff();

        //unlocked items
        String ownedWeapons = "axe\\[0,10,15,15,20,15,15,Pi-Ea,Ea\\]sword\\[0,10,15,15,20,15,15,Pi-Ea,Ea\\]mace\\[0,10,15,15,20,15,15,Pi-Ea,Ea";
        String ownedOutfits = "legs,legs2\\]body,body2\\]head,head2,head3";

        //User stats
        String[] playerOneStatString = {"0,10,15,15,20,15,15,Pi-Ea,Ea","0,10,15,15,20,15,15,Pi-Ea,Ea","0,10,15,15,20,15,15,Pi-Ea,Ea"};
        String[] playerOneWeaponLocations = {"sword","axe","mace"};
        String[] stringPlayerOne = {"legs","body","head"};
        String[] stringGem = {"gem1","gem2","gem3"};
        String stringAmulet = "amulet";

        String uId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //save all changes to database
        setStat(DISPLAY_NAME, "newUser", uId);
        setStat(PLAYER_HEAD, stringPlayerOne[2], uId);
        setStat(PLAYER_TORSO, stringPlayerOne[1], uId);
        setStat(PLAYER_LEGS, stringPlayerOne[0], uId);
        setStat(FIRST_WEAPON, playerOneWeaponLocations[0], uId);
        setStat(SECOND_WEAPON, playerOneWeaponLocations[1], uId);
        setStat(THIRD_WEAPON, playerOneWeaponLocations[2], uId);
        setStat(FIRST_GEM, stringGem[0], uId);
        setStat(SECOND_GEM, stringGem[1], uId);
        setStat(THIRD_GEM, stringGem[2], uId);
        setStat(AMULET_STRING, stringAmulet, uId);
        setStat(FIRST_WEAPON_STATS, playerOneStatString[0], uId);
        setStat(SECOND_WEAPON_STATS, playerOneStatString[1], uId);
        setStat(THIRD_WEAPON_STATS, playerOneStatString[2], uId);
        setStat(OWNED_WEAPONS, ownedWeapons, uId);
        setStat(OWNED_OUTFITS, ownedOutfits, uId);
        setStat(AVAILIBLE_CURRENCY, "0", uId);

    }

    public void setStat(String stat, String value, String uID){
        // sets a specific statistic for a user
        DatabaseReference statData = database.getReference("/users/" + uID + "/" + stat);
        statData.setValue(value);
    }


}
