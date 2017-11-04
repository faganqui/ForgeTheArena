package doophie.forgethearena;

import android.content.Context;
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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class PurchaseItemsActivity extends AppCompatActivity implements View.OnClickListener {


    //shared pref things
    private static final String SHARED_PREFS = "FORGE_SAVED_PREFS";

    private static final String CURRENCY = "moneymoneymoney";
    private static final String OWNED_WEAPONS = "ownedweapons";
    private static final String OWNED_AMULETS = "ownedamulets";
    private static final String PLAYER_EXPERIENCE = "playerexp";


    //variables collected from prefs
    String[] owned_weapons;
    String[] owned_outfits;
    String[] owned_amulets;
    int money;
    int exp;

    //needed variables
    String[] all_weapons;
    String[] all_amulets;
    String[] all_outfits;
    String[] cash_purchases;

    //set size of drawn character
    private int frameWidth = 100;
    private int frameHeight = 100;

    private Rect frameToDraw = new Rect(
            0, 0,
            frameWidth,
            frameHeight);

    RectF whereToDraw = new RectF(
            0, 0,
            frameWidth,
            frameHeight);

    theGreatDictionaryOfWeaponsandOtherStuff dict = new theGreatDictionaryOfWeaponsandOtherStuff();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_items);

        getSharedPrefs();

        all_amulets = dict.getAllSomething("amulet");
        all_weapons = dict.getAllSomething("weapon");
        all_outfits = concat(concat(dict.getAllSomething("head"), dict.getAllSomething("body")), dict.getAllSomething("leg"));
    }

    public String[] concat(String[] a, String[] b) {
        int aLen = a.length;
        int bLen = b.length;
        String[] c= new String[aLen+bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.buy_weapons:
                setTable("weapons");
                break;
            case R.id.buy_amulets:
                setTable("amulets");
                break;
            case R.id.buy_outfits:
                setTable("outfits");
                break;
            case R.id.cash_shop:
                setTable("cash");
                break;
        }
    }

    private void setTable(final String shop_type){
        TableLayout table = findViewById(R.id.shop_table);
        table.removeAllViews();

        String[] list_to_sell = {};
        switch (shop_type){
            case "weapons":
                list_to_sell = all_weapons;
                break;
            case "amulets":
                list_to_sell = all_amulets;
                break;
            case "outfits":
                list_to_sell = all_outfits;
                break;
            case "cash":
                list_to_sell = cash_purchases;
                break;
        }

        for (String item_x : list_to_sell) {
            final String item = item_x.split(":")[0];

            final int cost = dict.getItemCost(item);

            TableRow row = new TableRow(this);

            ImageView image = new ImageView(this);
            image.setImageBitmap(drawItem(item));

            TextView item_name = new TextView(this);
            item_name.setText(item);

            Button purchase_button = new Button(this);
            purchase_button.setText("Buy\n$" + cost);
            purchase_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    makePurchase(item,shop_type,cost);
                }
            });

            row.addView(purchase_button);
            row.addView(image);
            row.addView(item_name);
            table.addView(row);
        }
    }

    public Bitmap drawItem(String item){
        //load and scale the player
        int resID = getResources().getIdentifier(item,
                "drawable", getPackageName());
        Bitmap item_bit = BitmapFactory.decodeResource(this.getResources(), resID);

        item_bit = Bitmap.createScaledBitmap(item_bit,
                frameWidth * 5,
                frameHeight,
                false);

        Bitmap drawnBitmap = Bitmap.createBitmap(frameWidth, frameHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(drawnBitmap);

        canvas.drawBitmap(item_bit, frameToDraw, whereToDraw, null);

        return(drawnBitmap);
    }

    public boolean makePurchase(String item, String item_type, int cost){
        return false;
    }

    public void getSharedPrefs(){
        //gets the current shared prefs
        Context context = this.getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        owned_weapons = sharedPref.getString(OWNED_WEAPONS, "").split("]");
        owned_amulets = sharedPref.getString(OWNED_AMULETS, "amulet").split(":");
        money = Integer.valueOf(sharedPref.getString(CURRENCY, "0"));
        exp = Integer.valueOf(sharedPref.getString(PLAYER_EXPERIENCE, "0"));
    }


}
