package doophie.forgethearena;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class ReforgeWeaponActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reforge_weapon);


    }

    public void drawWeapon(){
        //draws the selected weapon to the interface

    }

    public void changeWeapon(Boolean isNext){
        //changes the selected weapon

    }

    public void reforgeWeapon(int index){
        //reforges the weapon in owened wepaons at the supplied index to have new values based on its
        //default values
    }


    @Override
    public void onClick(View view){
        switch (view.getId()){
            //case R.id.reforge_button:
              //  break;

            case R.id.next_weapon_switch:
                break;

            case R.id.prev_weapon_switch:
                break;
        }
    }


}
