package doophie.forgethearena;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.renderscript.Double2;
import android.support.annotation.IntegerRes;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SpriteSheetAnimation extends Activity {

    // Our object that will hold the view and
    // the sprite sheet animation logic
    GameView gameView;

    //context of the activity
    public static Context contextOfApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get context
        contextOfApplication = getApplicationContext();

        // Initialize gameView and set it as the view
        gameView = new GameView(this);
        setContentView(gameView);

    }

    public Context getContextOfApplication(){
        return contextOfApplication;
    }


    class GameView extends SurfaceView implements Runnable {


        /*TODO:
            Make positions for each player
            Keep track of player bitmaps - rings/amulet/weapons/head/body/costume
            Make animations for each player [attack, idle, ?] [Possibly multiple attack]
            Write gem and amulet buttons
            add all stat structs
            Make damage calculations
            Make healthbar frames?
         */

        // This is our thread
        Thread gameThread = null;

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


        // This is new. We need a SurfaceHolder
        // When we use Paint and Canvas in a thread
        // We will see it in action in the draw method soon.
        SurfaceHolder ourHolder;

        // A boolean which we will set and unset
        // when the game is running- or not.
        volatile boolean playing;

        // A Canvas and a Paint object
        Canvas canvas;
        Paint paint;

        // This variable tracks the game frame rate
        long fps;

        // This is used to help calculate the fps
        private long timeThisFrame;

        // Bitmaps for other objects
        //this is for the background of the weapons where the break bar is
        Bitmap screenBackground;
        String stringBackground;
        Bitmap bitmapGemAttack;
        Bitmap[] bitmapBreakBarBack = new Bitmap[3];
        Bitmap[] bitmapGem = new Bitmap[3];
        Bitmap bitmapAmulet;
        String[] stringGem = new String[3];
        String stringAmulet;

        //player and enemy stat-strings
        String[] playerOneStatString = new String[3];
        String[] playerTwoStatString = new String[3];

        //gem colours
        Color[] playerGems;
        Color[] enemyGems;

        // Bitmaps for enemy - we load this first, because chivalry
        String[] stringPlayerTwo = new String[3];
        Bitmap[] bitmapPlayerTwo= new Bitmap[3];
        Bitmap bitmapEnemyAttack;

        //enemy weapons arrays
        Bitmap[] playerTwoWeapons = new Bitmap[3];
        String[] playerTwoWeaponLocations = new String[3];
        private int selectedEnemyWeapon = 0;
        int curWep = 0; // keeps track of enemy weapon changes in update()

        //enemy actions
        boolean isEnemyMoving = true;
        boolean isEnemyAttacking = false;
        boolean isEnemyGemming = false;

        //set these when we collect height & width of screen
        float enemyXPosition;
        float enemyYPosition;

        //enemy attack animation tracker
        int enemyAttackLength = 1;
        int enemyAttackCount = 0;

        // Bitmaps for player
        String[] stringPlayerOne = new String[3];
        Bitmap[] bitmapPlayerOne = new Bitmap[3];
        Bitmap bitmapPlayerAttack; //animation of body for attacking/ possibly need to add weapons or remove this if it takes too much fps

        //We use arrays to store bitmaps of weapons for player/enemy/weapons (for both player and enemy)
        Bitmap[] playerOneWeapons = new Bitmap[3];
        String[] playerOneWeaponLocations = new String[3];
        private int selectedWeapon = 0;

        //todo: isMoving - possible replace this or remove if always true
        boolean isMoving = false;
        boolean isAttacking = false;
        boolean isGemming = false;

        //checks if both players have made a moves
        boolean isPlayerTurn = false;
        boolean isEnemyTurn = false;

        //attack length tracks how many frames the attack animation should play (framecount * attacklength)
        int attackLength = 1;
        int attackCount = 0;

        // He can walk at 150 pixels per second
        float walkSpeedPerSecond = 250;

        // Position is set when we get screen height/width
        float playerXPosition;
        float playerYPosition;

        // New for the sprite sheet animation

        // Variables to do with scale of interface objects
        Display display = getWindowManager().getDefaultDisplay();
        private int paddingBottom = 100;
        private int screenHeight;
        private int screenWidth;

        //right now frame width is set to 1/2 of the screen width
        // difference in dimentions makes it so buttons take up 1/4 of screen
        private int frameWidth = 600;
        private int frameHeight = 600;
        private int differenceInDimensions = 2;

        // How many frames are there on the sprite sheet?
        private int frameCount = 5;
        private int maxBreakPercentage = 10;
        private int maxEnemyBreakPercentage = 10;

        // current frame of player 1
        private int currentFrame = 0;
        private int[] currentBreakPercentage = new int[3];
        private int[] currentEnemyBreakPercentage = new int[3];

        // What time was it when we last changed frames
        private long lastFrameChangeTime = 0;

        // How long should each frame last
        private int frameLengthInMilliseconds = 100;

        //keeps track of who attacks first
        boolean isPlayerFirst;

        // A rectangle to define an area of the
        // sprite sheet that represents 1 frame
        private Rect frameToDraw = new Rect(
                0,
                0,
                frameWidth,
                frameHeight);

        //a rectangle to define area for what break percentage to display
        private Rect breakFrameToDraw = new Rect(
                0,
                0,
                frameWidth/ differenceInDimensions,
                frameHeight/ differenceInDimensions);


        // A rect that defines an area of the screen
        // on which to draw
        RectF whereToDraw = new RectF(
                playerXPosition,
                playerYPosition,
                playerXPosition + frameWidth,
                playerYPosition + frameHeight);

        // When the we initialize (call new()) on gameView
        // This special constructor method runs
        public GameView(Context context) {
            // The next line of code asks the
            // SurfaceView class to set up our object.
            // How kind.
            super(context);

            //get size of screen to set up interface
            Point size = new Point();
            display.getSize(size);
            screenHeight = size.y;
            screenWidth = size.x;

            //sets location of players
            playerYPosition = (float)((screenHeight * 1)/2);
            playerXPosition = (float)(0);

            enemyYPosition = (float)((screenHeight * 1)/4);
            enemyXPosition = (float)((screenWidth * 1)/2);

            //set size of players based on screen
            frameHeight = screenHeight/4;
            frameWidth = screenWidth/2;

            // Initialize ourHolder and paint objects
            ourHolder = getHolder();
            paint = new Paint();

            // Load some .png files this may need to be moved
            bitmapPlayerAttack = BitmapFactory.decodeResource(this.getResources(), R.drawable.attackanimation);
            Bitmap bitmapWeaponBackground = BitmapFactory.decodeResource(this.getResources(), R.drawable.weaponbars);

            //sets default values based on shared prefs (set on inventory screen)
            getSharedPrefs();

            int resID = getResources().getIdentifier(stringAmulet,
                    "drawable", getPackageName());
            bitmapAmulet = BitmapFactory.decodeResource(this.getResources(), resID);

            //load gem attack drawable
            bitmapGemAttack = BitmapFactory.decodeResource(this.getResources(), R.drawable.gemattack);

            //load background
            resID = getResources().getIdentifier(stringBackground,
                    "drawable", getPackageName());
            screenBackground = BitmapFactory.decodeResource(this.getResources(), resID);


            //initialize arrays
            for(int i = 0; i < currentBreakPercentage.length; i++){
                //fill empty array
                currentBreakPercentage[i] = 0;
                currentEnemyBreakPercentage[i] = 0;

                //set gem symbol & size
                resID = getResources().getIdentifier(stringGem[i],
                        "drawable", getPackageName());
                bitmapGem[i] = BitmapFactory.decodeResource(this.getResources(), resID);

                bitmapGem[i] = Bitmap.createScaledBitmap(bitmapGem[i],
                        frameWidth / differenceInDimensions,
                        frameHeight / differenceInDimensions,
                        false);

                //setup enemy sprite, temp using player sprite cuz i only have 1 anyway
                //todo fix that
                //setup player sprites
                //get player from strings (should load these from Shared Prefs later)
                resID = getResources().getIdentifier(stringPlayerTwo[i],
                        "drawable", getPackageName());
                Bitmap bmImg = BitmapFactory.decodeResource(this.getResources(), resID);

                //fill player  two array
                bitmapPlayerTwo[i] = Bitmap.createScaledBitmap(reflectImage(bmImg),
                        frameWidth * frameCount,
                        frameHeight,
                        false);


                //setup player sprites
                //get player from strings (should load these from Shared Prefs later)
                resID = getResources().getIdentifier(stringPlayerOne[i],
                        "drawable", getPackageName());
                bmImg = BitmapFactory.decodeResource(this.getResources(), resID);

                //fill player one array
                bitmapPlayerOne[i] = Bitmap.createScaledBitmap(bmImg,
                        frameWidth * frameCount,
                        frameHeight,
                        false);


                //todo: add weapon logo inside circle
                //resize weapon swap buttons
                bitmapBreakBarBack[i] = bitmapWeaponBackground;

                bitmapBreakBarBack[i] = Bitmap.createScaledBitmap(bitmapBreakBarBack[i],
                        (frameWidth/ differenceInDimensions) * maxBreakPercentage,
                        (frameHeight/ differenceInDimensions),
                        false);

                //get weapon from strings for player one (should load these from Shared Prefs later)
                resID = getResources().getIdentifier(playerOneWeaponLocations[i],
                        "drawable", getPackageName());
                bmImg = BitmapFactory.decodeResource(this.getResources(), resID);

                //fill weapon swap array
                playerOneWeapons[i] = Bitmap.createScaledBitmap(bmImg,
                        frameWidth * frameCount,
                        frameHeight,
                        false);

                //get weapon from strings for player two (should load these from Shared Prefs later)
                resID = getResources().getIdentifier(playerTwoWeaponLocations[i],
                        "drawable", getPackageName());
                bmImg = BitmapFactory.decodeResource(this.getResources(), resID);

                //fill weapon swap array
                playerTwoWeapons[i] = Bitmap.createScaledBitmap(reflectImage(bmImg),
                        frameWidth * frameCount,
                        frameHeight,
                        false);

            }

            // Scale the bitmap to the correct size
            // We need to do this because Android automatically
            // scales bitmaps based on screen density
            bitmapPlayerAttack = Bitmap.createScaledBitmap(bitmapPlayerAttack,
                    frameWidth * frameCount,
                    frameHeight,
                    false);

            bitmapGemAttack = Bitmap.createScaledBitmap(bitmapGemAttack,
                    frameWidth * frameCount,
                    frameHeight,
                    false);

            bitmapAmulet = Bitmap.createScaledBitmap(bitmapAmulet,
                    frameWidth / differenceInDimensions,
                    frameHeight / differenceInDimensions,
                    false);

            screenBackground = Bitmap.createScaledBitmap(screenBackground,
                    screenWidth,
                    screenHeight,
                    false);

            // Set our boolean to true - game on!
            //playing = true;

        }

        @Override
        public void run() {
            while (playing) {
                // Capture the current time in milliseconds in startFrameTime
                long startFrameTime = System.currentTimeMillis();

                // Update the frame
                update();

                // Draw the frame
                draw();

                // Calculate the fps this frame
                // We can then use the result to
                // time animations and more.
                timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if (timeThisFrame >= 1) {
                    fps = 1000 / timeThisFrame;
                }
            }
        }

        public void endGame(boolean hasWon){
            //ends the game based on if we won or not
            if (hasWon == false){
                //lost :(
                Intent intent = new Intent(getContext(), BattleActivity.class);
                intent.putExtra("result", "loss");
                startActivity(intent);

            }else{
                //won wooo
                //todo: write this
                Intent intent = new Intent(getContext(), BattleActivity.class);
                intent.putExtra("result", "won");
                startActivity(intent);
            }
        }

        // Everything that needs to be updated goes in here
        // In later projects we will have dozens (arrays) of objects.
        // We will also do other things like collision detection.
        public void update() {

            //both players have made there decision so we execute it
            if(isPlayerTurn && isEnemyTurn && !isMoving){
                isMoving = true;
                selectedEnemyWeapon = curWep;
                damageCalculation();
            }

            //if its the enemies turn - perform enemy action
            if(!isEnemyTurn){
                curWep = selectedEnemyWeapon;
                String enemyAction = getEnemyAction();
                if(enemyAction == "attack"){
                    isEnemyAttacking = true;
                }else if(enemyAction == "swapweapon"){
                    if (currentEnemyBreakPercentage[0] >= 10 && currentEnemyBreakPercentage[1] >= 10 && currentEnemyBreakPercentage[2] >= 10 ){
                        endGame(true);
                        return;
                    }
                    //todo:make so chosen weapon doesnt show until turn occurs
                     // do the todo by replacing selected weapon with this and then changing selected weapon just before dmg calc
                    int prevWeap = curWep;
                    curWep = (curWep + 1 )% 3;
                    while(currentEnemyBreakPercentage[curWep] >= 10){
                        curWep  = (curWep + 1 )% 3;
                    }
                    if(prevWeap == curWep){
                        //this is so the enemy doesn't switch to the weapon he already has out as his whole turn
                        isEnemyAttacking = true;
                    }
                }
                isEnemyTurn = true;
            }

            //if the gem is active play animation for frameCount
            if (isGemming) {
                if(attackCount == 0) {
                    currentFrame = attackCount;
                }
                if (attackCount == frameCount){
                    attackCount = 0;
                    isGemming = false;
                }
                attackCount ++;
            }
            if (isEnemyGemming) {
                if(enemyAttackCount == 0) {
                    currentFrame = enemyAttackCount;
                }
                if(enemyAttackCount == frameCount){
                    enemyAttackCount = 0;
                    isEnemyGemming = false;
                }
                enemyAttackCount++;
            }
            if(isMoving) {
                if (isPlayerFirst) {
                    if (isAttacking) {
                        if (attackCount == attackLength * frameCount) {
                            //Stop attacking after $attackLength animations
                            attackCount = 0;
                            isAttacking = false;

                            //reset the player position after
                            playerYPosition = (float) ((screenHeight * 1) / 2);
                            playerXPosition = (float) (0);
                        } else {
                            attackCount++;
                        }
                        playerXPosition += (walkSpeedPerSecond / fps);
                        playerYPosition -= (walkSpeedPerSecond / fps);
                    } else if (isEnemyAttacking) {
                        if (enemyAttackCount == enemyAttackLength * frameCount) {
                            //Stop attacking after $attackLength animations
                            enemyAttackCount = 0;
                            isEnemyAttacking = false;

                            //reset the player position after
                            enemyYPosition = (float) (screenHeight / 4);
                            enemyXPosition = (float) (screenWidth / 2);
                        } else {
                            enemyAttackCount++;
                        }


                        enemyXPosition -= (walkSpeedPerSecond / fps);
                        enemyYPosition += (walkSpeedPerSecond / fps);
                    } else {
                        isMoving = false;
                        isPlayerTurn = false;
                        isEnemyTurn = false;
                    }
                }else{

                    if (isEnemyAttacking) {
                        if (enemyAttackCount == enemyAttackLength * frameCount) {
                            //Stop attacking after $attackLength animations
                            enemyAttackCount = 0;
                            isEnemyAttacking = false;

                            //reset the player position after
                            enemyYPosition = (float) (screenHeight / 4);
                            enemyXPosition = (float) (screenWidth / 2);
                        } else {
                            enemyAttackCount++;
                        }


                        enemyXPosition -= (walkSpeedPerSecond / fps);
                        enemyYPosition += (walkSpeedPerSecond / fps);
                    } else if (isAttacking) {
                        if (attackCount == attackLength * frameCount) {
                            //Stop attacking after $attackLength animations
                            attackCount = 0;
                            isAttacking = false;

                            //reset the player position after
                            playerYPosition = (float) ((screenHeight * 1) / 2);
                            playerXPosition = (float) (0);
                        } else {
                            attackCount++;
                        }
                        playerXPosition += (walkSpeedPerSecond / fps);
                        playerYPosition -= (walkSpeedPerSecond / fps);
                    } else  {
                        isMoving = false;
                        isPlayerTurn = false;
                        isEnemyTurn = false;
                    }
                }
            }

            //check if we have lost the game
            int islost = 0;
            for ( int tempBreak : currentBreakPercentage){
                if(tempBreak >= maxBreakPercentage){
                    islost++;
                }
            }
            if (islost==3){
                endGame(false);
            }
        }

        public void getCurrentFrame(){

            long time  = System.currentTimeMillis();
            if(true) {// animate idle animation //todo maybe remove this outer if
                if ( time > lastFrameChangeTime + frameLengthInMilliseconds) {
                    lastFrameChangeTime = time;
                    currentFrame++;
                    if (currentFrame >= frameCount) {
                        currentFrame = 0;
                    }
                }
            }
            //update the left and right values of the source of
            //the next frame on the spritesheet
            frameToDraw.left = currentFrame * frameWidth;
            frameToDraw.right = frameToDraw.left + frameWidth;

        }

        //tells what type of background to draw
        public void getCurrentBreak(int selected_weapon){
            breakFrameToDraw.left = (currentBreakPercentage[selected_weapon]) * (frameWidth/ differenceInDimensions);
            breakFrameToDraw.right = breakFrameToDraw.left + (frameWidth/ differenceInDimensions);
        }

        // Draw the newly updated scene
        public void draw() {

            // Make sure our drawing surface is valid or we crash
            if (ourHolder.getSurface().isValid()) {
                // Lock the canvas ready to draw
                canvas = ourHolder.lockCanvas();

                // Draw the background color
                //canvas.drawColor(Color.argb(255,  26, 128, 182));
                whereToDraw.set(0,0,screenWidth,screenHeight);
                canvas.drawBitmap(screenBackground,
                        new Rect(0,0,screenWidth,screenHeight),
                        whereToDraw,paint);

                // Choose the brush color for drawing
                paint.setColor(Color.argb(255,  249, 129, 0));

                // Make the text a bit bigger
                paint.setTextSize(45);

                // Display the current fps on the screen
                //canvas.drawText("FPS:" + fps, 20, 40, paint);

                getCurrentFrame();


                //draw our interface
                int i = 0;
                do {
                    //set where to draw P2
                    whereToDraw.set((int)enemyXPosition,
                            (int)enemyYPosition,
                            (int)enemyXPosition+ frameWidth,
                            (int)enemyYPosition + frameHeight);

                    //what to draw for enemy attacking animation
                    canvas.drawBitmap(bitmapPlayerTwo[i],
                            frameToDraw,
                            whereToDraw, paint);


                    //set where to draw P1
                    whereToDraw.set((int)playerXPosition,
                            (int)playerYPosition,
                            (int)playerXPosition + frameWidth,
                            (int)playerYPosition + frameHeight);

                    //what to draw for idle animation
                    canvas.drawBitmap(bitmapPlayerOne[i],
                            frameToDraw,
                            whereToDraw, paint);

                    //draw gem attacks
                    if(isGemming){
                        paint.setColorFilter(new PorterDuffColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN));

                        whereToDraw.set((int) playerXPosition + (attackCount * 50),
                                (int)playerYPosition - (attackCount * 50),
                                (int)playerXPosition + frameWidth + (attackCount * 50),
                                (int)playerYPosition + frameHeight - (attackCount * 50));

                        canvas.drawBitmap(bitmapGemAttack,
                                frameToDraw,
                                whereToDraw, paint);
                    }

                    if(isEnemyGemming){
                        paint.setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_IN));

                        whereToDraw.set((int)enemyXPosition - (attackCount * 50),
                                (int)enemyYPosition + (attackCount * 50),
                                (int)enemyXPosition+ frameWidth - (attackCount * 50),
                                (int)enemyYPosition + frameHeight + (attackCount * 50));

                        canvas.drawBitmap(bitmapGemAttack,
                                frameToDraw,
                                whereToDraw, paint);
                    }

                    paint.setColorFilter(null);

                    if(currentEnemyBreakPercentage[i] >= maxEnemyBreakPercentage){
                        paint.setColor(Color.argb(255,0,0,0));
                        canvas.drawText("X",
                                (100) * i,
                                40,
                                paint);
                    }

                    //only draw it if weapon still has break left
                    if(currentBreakPercentage[i] < maxBreakPercentage) {
                        getCurrentBreak(i);
                        //set where to draw swap weapon buttons
                        whereToDraw.set(screenWidth - ((int) (i + 1) * (frameWidth / differenceInDimensions)),
                                (int) screenHeight - (frameHeight / differenceInDimensions) - paddingBottom,
                                (screenWidth - (int) (i + 1) * (frameWidth / differenceInDimensions) + (frameWidth / differenceInDimensions)),
                                (int) screenHeight - (frameHeight / differenceInDimensions) - paddingBottom + (frameHeight / differenceInDimensions));

                        //draw the swap weapon buttons
                        canvas.drawBitmap(bitmapBreakBarBack[i],
                                breakFrameToDraw,
                                whereToDraw, paint);

                        //draw icon of weapon on button
                        canvas.drawBitmap(playerOneWeapons[i].createScaledBitmap(playerOneWeapons[i],
                                (frameWidth) * frameCount,
                                (frameHeight),
                                false),
                                frameToDraw,
                                whereToDraw, paint);

                        //draw text for weapon break %

                        // Choose the brush color for drawing
                        paint.setColor(Color.argb(255, 0, 0, 0));
                        // Display the current break percentage of each item
                        canvas.drawText(playerOneStatString[i].split(",")[0],
                                (screenWidth - (int) (i + 1) * (frameWidth / differenceInDimensions)), // +45 is to set text slightly over image
                                (int) screenHeight - (frameHeight / differenceInDimensions) - paddingBottom + 45,
                                paint);

                    }
                    i++;
                }while(i<3);

                //draw break of Player 2
                paint.setColor(Color.argb(255,0,0,0));
                canvas.drawRect(frameWidth,0,screenWidth,frameHeight/4,paint);
                paint.setColor(Color.argb(255,0,255,0));
                canvas.drawRect(frameWidth + (frameWidth * (Float.valueOf(playerTwoStatString[selectedEnemyWeapon].split(",")[0])/Float.valueOf(playerTwoStatString[selectedEnemyWeapon].split(",")[1])))
                        ,0,screenWidth,frameHeight/4,paint);
                paint.setColor(Color.argb(255,255,255,255));
                canvas.drawText(playerTwoStatString[selectedEnemyWeapon].split(",")[0],
                        frameWidth + 20,
                        40,
                        paint);

                //draw gem
                whereToDraw.set((int)screenWidth - (frameWidth/differenceInDimensions),
                        (int)screenHeight - ((frameHeight/differenceInDimensions)*2) - paddingBottom,
                        (int)screenWidth,
                        (int)screenHeight - (frameHeight/differenceInDimensions) - paddingBottom);

                canvas.drawBitmap(bitmapGem[selectedWeapon],
                        new Rect(0,0,(frameWidth/differenceInDimensions), (frameHeight/differenceInDimensions)),
                        whereToDraw,
                        paint);

                //draw amulet
                whereToDraw.set((int)screenWidth - ((frameWidth/differenceInDimensions)*2),
                        (int)screenHeight - ((frameHeight/differenceInDimensions)*2) - paddingBottom,
                        (int)screenWidth - (frameWidth/differenceInDimensions),
                        (int)screenHeight - (frameHeight/differenceInDimensions) - paddingBottom);

                canvas.drawBitmap(bitmapAmulet,new Rect(0,0,(frameWidth/differenceInDimensions),(frameHeight/differenceInDimensions)),whereToDraw,paint);


                //only display weapon if it has break left
                if ((currentBreakPercentage[selectedWeapon] < maxBreakPercentage)) {

                    //set where to draw weapon
                    whereToDraw.set((int)playerXPosition,
                            (int)playerYPosition,
                            (int)playerXPosition + frameWidth,
                            (int)playerYPosition + frameHeight);

                    canvas.drawBitmap(playerOneWeapons[selectedWeapon],
                            frameToDraw,
                            whereToDraw, paint);
                }

                //only display enemy weapon if it has break left //todo: add enemy break ----- THIS
                if ((currentEnemyBreakPercentage[selectedEnemyWeapon] < maxEnemyBreakPercentage)) {

                    //set where to draw weapon
                    whereToDraw.set((int)enemyXPosition,
                            (int)enemyYPosition,
                            (int)enemyXPosition + frameWidth,
                            (int)enemyYPosition + frameHeight);

                    canvas.drawBitmap(playerTwoWeapons[selectedEnemyWeapon],
                            frameToDraw,
                            whereToDraw, paint);
                }

                // Draw everything to the screen
                ourHolder.unlockCanvasAndPost(canvas);
            }

        }

        //Flips an image
        // all default bitmaps face to the right - this will return the same bitmap facing left
        public Bitmap reflectImage(Bitmap originalImage){
            Bitmap reflectedImage;
            Matrix matrix = new Matrix();
            matrix.preScale(-1, 1);
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            reflectedImage = Bitmap.createBitmap(originalImage, 0, 0, width, height, matrix, false);
            return reflectedImage;
        }

        public void getSharedPrefs() {

            Context context = getContextOfApplication();
            SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

            stringBackground = sharedPref.getString(STRING_BACKGROUND, "background");

            playerOneWeaponLocations[0] = sharedPref.getString(FIRST_WEAPON, "sword");
            playerOneWeaponLocations[1] = sharedPref.getString(SECOND_WEAPON, "sword");
            playerOneWeaponLocations[2] = sharedPref.getString(THIRD_WEAPON, "sword");

            stringPlayerOne[0] = sharedPref.getString(PLAYER_LEGS, "legs");
            stringPlayerOne[1] = sharedPref.getString(PLAYER_TORSO, "body");
            stringPlayerOne[2] = sharedPref.getString(PLAYER_HEAD, "head");

            playerTwoWeaponLocations[0] = sharedPref.getString(FIRST_ENEMY_WEAPON, "sword");
            playerTwoWeaponLocations[1] = sharedPref.getString(SECOND_ENEMY_WEAPON, "sword");
            playerTwoWeaponLocations[2] = sharedPref.getString(THIRD_ENEMY_WEAPON, "sword");

            stringPlayerTwo[0] = sharedPref.getString(ENEMY_LEGS, "legs");
            stringPlayerTwo[1] = sharedPref.getString(ENEMY_TORSO, "body");
            stringPlayerTwo[2] = sharedPref.getString(ENEMY_HEAD, "head");

            //get bitmaps for amulets and gem from shared prefs
            stringGem[0] = sharedPref.getString(FIRST_GEM, "sword");
            stringGem[1] = sharedPref.getString(SECOND_GEM, "sword");
            stringGem[2] = sharedPref.getString(THIRD_GEM, "sword");
            stringAmulet = sharedPref.getString(AMULET_STRING, "sword");

            //get stats for each player 1 weapon
            playerOneStatString[0] = sharedPref.getString(FIRST_WEAPON_STATS, "sword");
            playerOneStatString[1] = sharedPref.getString(SECOND_WEAPON_STATS, "sword");
            playerOneStatString[2] = sharedPref.getString(THIRD_WEAPON_STATS, "sword");

            //get stats for each player two weapon
            playerTwoStatString[0] = sharedPref.getString(FIRST_ENEMY_WEAPON_STATS, "sword");
            playerTwoStatString[1] = sharedPref.getString(SECOND_ENEMY_WEAPON_STATS, "sword");
            playerTwoStatString[2] = sharedPref.getString(THIRD_ENEMY_WEAPON_STATS, "sword");
        }

        // If SimpleGameEngine Activity is paused/stopped
        // shutdown our thread.
        public void pause() {
            playing = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Log.e("Error:", "joining thread");
            }

        }

        // If SimpleGameEngine Activity is started then
        // start our thread.
        public void resume() {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        // The SurfaceView class implements onTouchListener
        // So we can override this method and detect screen touches.
        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {

            int x = (int)motionEvent.getX();
            int y = (int)motionEvent.getY();

            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

                // Player has touched the screen
                case MotionEvent.ACTION_DOWN:

                    // Set isMoving so player is moved in the update method
                    //isMoving = true;

                    break;

                // Player has removed finger from screen
                case MotionEvent.ACTION_UP:

                    if(!isPlayerTurn && !isMoving) { // Only allow new motions if it's our turn

                        // Set isMoving so player does not move
                        //isMoving = false;
                        currentFrame = 0;
                        if (y > (screenHeight - (frameHeight / differenceInDimensions) - paddingBottom)) {
                            //left-most weapon swap
                            if ((x > (screenWidth - ((frameWidth/ differenceInDimensions) * 3)))
                                    && (x < (screenWidth - ((frameWidth / differenceInDimensions) * 2)))) {
                                if (currentBreakPercentage[2] < maxBreakPercentage) {
                                    if(selectedWeapon!= 2){
                                        isPlayerTurn = true;
                                    }
                                    selectedWeapon = 2;
                                }
                                //middle weapon-swap
                            } else if ((x > (screenWidth - ((frameWidth / differenceInDimensions) * 2)))
                                    && (x < (screenWidth - ((frameWidth / differenceInDimensions) * 1)))) {
                                if (currentBreakPercentage[1] < maxBreakPercentage) {
                                    if(selectedWeapon!= 1){
                                        isPlayerTurn = true;
                                    }
                                    selectedWeapon = 1;
                                }
                                //rightmost weapon-swap
                            } else if (x > (screenWidth - ((frameWidth / differenceInDimensions) * 1))) {
                                if (currentBreakPercentage[0] < maxBreakPercentage)
                                    if(selectedWeapon!= 0){
                                        isPlayerTurn = true;
                                    }
                                    selectedWeapon = 0;
                            }
                        } else if(y > (screenHeight - ((frameHeight / differenceInDimensions)*2) - paddingBottom)){
                            //we only allow actions if weapon is not broken -> otherwise must change weapon
                            if (currentBreakPercentage[selectedWeapon] < 10) {
                                //gems or amulet area
                                if ((x > (screenWidth - ((frameWidth / differenceInDimensions) * 2)))
                                        && (x < (screenWidth - ((frameWidth / differenceInDimensions) * 1)))) {
                                    //amulet
                                    //todo: perform amulet action
                                    isPlayerTurn = true;

                                } else if (x > (screenWidth - ((frameWidth / differenceInDimensions) * 1))) {

                                    //gem
                                    isGemming = true;
                                    isPlayerTurn = true;
                                }
                            }
                        } else {
                            //if not in the normal interface
                            if (currentBreakPercentage[selectedWeapon] < 10) {
                                isAttacking = true;
                                isPlayerTurn = true;
                            }
                        }

                        break;
                    }
            }
            return true;
        }

        public void damageCalculation(){
            //uses player and enemy strings based on current player and enemy weapons
            //// TODO: 2017-10-01
            /*  0 - curdurability
                1 - durability
                2 - toughness
                3 - power
                4 - speed
                5 - elemental force
                6 - elemental resist
                7 - type
                8 - element
             */

            //get stats
            String[] playerStats = playerOneStatString[selectedWeapon].split(",");
            String[] enemyStats = playerTwoStatString[selectedEnemyWeapon].split(",");

            String[] first;
            String[] second;
            Boolean firstStyle;
            Boolean secondStyle;
            Boolean firstGem;
            Boolean secondGem;

            //see who goes first
            if (Integer.valueOf(playerStats[4]) > Integer.valueOf(enemyStats[4])){
                isPlayerFirst = true;
                first = playerStats;
                second = enemyStats;
                firstStyle = isAttacking;
                secondStyle = isEnemyAttacking;
                firstGem = isGemming;
                secondGem = isEnemyGemming;
            } else if (Integer.valueOf(playerStats[4]) < Integer.valueOf(enemyStats[4])){
                isPlayerFirst = false;
                second = playerStats;
                first = enemyStats;
                secondStyle = isAttacking;
                firstStyle = isEnemyAttacking;
                firstGem = isEnemyGemming;
                secondGem = isGemming;
            } else {
                if(Math.random() < 0.5) {
                    isPlayerFirst = false;
                    second = playerStats;
                    first = enemyStats;
                    secondStyle = isAttacking;
                    firstStyle = isEnemyAttacking;
                    firstGem = isEnemyGemming;
                    secondGem = isGemming;
                } else {
                    isPlayerFirst = true;
                    first = playerStats;
                    second = enemyStats;
                    firstStyle = isAttacking;
                    secondStyle = isEnemyAttacking;
                    firstGem = isGemming;
                    secondGem = isEnemyGemming;
                }
            }

            Double base_damage;
            //if whoever is first is attacking
            if(firstStyle) {
                base_damage = (Double.valueOf(first[3]) * 2 / Double.valueOf(second[2]));      // attack * 2 / defender toughness
                base_damage = base_damage * getTypeMultiplier(first[7], second[7]);            // apply type multipliers
                base_damage = Integer.valueOf(second[0]) + base_damage;                        // get new break
                second[0] = String.valueOf(base_damage.intValue());                            // set value in array
            }else if(firstGem){
                base_damage = (Double.valueOf(first[6]) * 2 / Double.valueOf(second[5]));      // elemental force * 2 / elemental resist
                base_damage = base_damage * getElementMultiplier(first[8], second[8]);            // apply type multipliers
                base_damage = Integer.valueOf(second[0]) + base_damage;                        // get new break
                second[0] = String.valueOf(base_damage.intValue());                            // set value in array
            }

            if(Integer.valueOf(second[0]) < Integer.valueOf(second[1])){
                //if whoever is second is attacking
                if(secondStyle) {
                    base_damage = (Double.valueOf(second[3]) * 2 / Double.valueOf(first[2]));        // attack * 2 / defender toughness
                    base_damage = base_damage * getTypeMultiplier(second[7], first[7]);            // apply type multipliers
                    base_damage = Integer.valueOf(first[0]) + base_damage;                         // get new break
                    first[0] = String.valueOf(base_damage.intValue());                             // set value in array
                }else if(secondGem){
                    base_damage = (Double.valueOf(second[6]) * 2 / Double.valueOf(first[5]));      // elemental force * 2 / elemental resist
                    base_damage = base_damage * getElementMultiplier(second[8], first[8]);            // apply type multipliers
                    base_damage = Integer.valueOf(first[0]) + base_damage;                        // get new break
                    first[0] = String.valueOf(base_damage.intValue());                            // set value in array
                }
            }else{
                //stop animation for player with broken weapon
                if(isPlayerFirst){
                    isEnemyAttacking = false;
                }else{
                    isAttacking = false;
                }
            }

            playerOneStatString[selectedWeapon] = "";
            playerTwoStatString[selectedEnemyWeapon] = "";

            //put new current health in stat string
            if(isPlayerFirst){
                for(String stat : first){
                    playerOneStatString[selectedWeapon] += stat + ",";
                }
                for(String stat : second){
                    playerTwoStatString[selectedEnemyWeapon] += stat + ",";
                }
            }else{
                for(String stat : first){
                    playerTwoStatString[selectedEnemyWeapon] += stat + ",";

                }
                for(String stat : second){
                    playerOneStatString[selectedWeapon] += stat + ",";
                }
            }


            //set break bar percents
            Double tempBreak = ((Double.valueOf(playerOneStatString[selectedWeapon].split(",")[0]) / Double.valueOf(playerOneStatString[selectedWeapon].split(",")[1])) * 10);
            currentBreakPercentage[selectedWeapon] = tempBreak.intValue();

            tempBreak = ((Double.valueOf(playerTwoStatString[selectedEnemyWeapon].split(",")[0]) / Double.valueOf(playerTwoStatString[selectedEnemyWeapon].split(",")[1])) * 10);
            currentEnemyBreakPercentage[selectedEnemyWeapon] = tempBreak.intValue();

        }

        public double getTypeMultiplier(String attacker, String defender){
            Double multiplier = 1.0;
            String attackType = attacker.split("-")[0];
            String defenderType = defender.split("-")[0];
            if(attackType.contains("Cr")){
                if(defenderType.contains("Sl")){
                    multiplier += .5;
                }else if (defenderType.contains("Pi")){
                    multiplier -= .5;
                }
            }else if(attackType.contains("Sl")){
                if(defenderType.contains("Pi")){
                    multiplier += .5;
                }else if (defenderType.contains("Cr")){
                    multiplier -= .5;
                }
            }else if(attackType.contains("Pi")){
                if(defenderType.contains("Cr")){
                    multiplier += .5;
                }else if (defenderType.contains("Sl")){
                    multiplier -= .5;
                }
            }
            multiplier = multiplier * getElementMultiplier(attacker.split("-")[1], defender.split("-")[1]);
            return multiplier;
        }

        public double getElementMultiplier(String attacker, String defender){
            Double multiplier = 1.0;
            String attackType = attacker;
            String defenderType = defender;
            if(attackType.contains("Fi")){
                if(defenderType.contains("Ea")){
                    multiplier += .5;
                }else if (defenderType.contains("Wa")){
                    multiplier -= .5;
                }
            }else if(attackType.contains("Wa")){
                if(defenderType.contains("Fi")){
                    multiplier += .5;
                }else if (defenderType.contains("Ea")){
                    multiplier -= .5;
                }
            }else if(attackType.contains("Ea")){
                if(defenderType.contains("Wa")){
                    multiplier += .5;
                }else if (defenderType.contains("Fi")){
                    multiplier -= .5;
                }
            }else if(attackType.contains("Da")){
                if((defenderType.contains("Wa")) || (defenderType.contains("Fi")) || (defenderType.contains("Ea"))){
                    multiplier += .25;
                }else if (defenderType.contains("Li")){
                    multiplier -= .75;
                }
            }else if(attackType.contains("Li")){
                if((defenderType.contains("Wa")) || (defenderType.contains("Fi")) || (defenderType.contains("Ea"))){
                    multiplier -= .25;
                }else if (defenderType.contains("Da")){
                    multiplier += .75;
                }
            }
            return multiplier;
        }

        public String getEnemyAction(){
            String action;
            if(Math.random() < 0.75) {
                if (Integer.valueOf(playerTwoStatString[selectedEnemyWeapon].split(",")[0]) < Integer.valueOf(playerTwoStatString[selectedEnemyWeapon].split(",")[1])) {
                    action = "attack";
                } else {
                    action = "swapweapon";
                }
            }else{
                action = "swapweapon";
            }
            return action;

        }

    }
    // This is the end of our GameView inner class

    // This method executes when the player starts the game
    @Override
    protected void onResume() {
        super.onResume();

        // Tell the gameView resume method to execute
        gameView.resume();
    }

    // This method executes when the player quits the game
    @Override
    protected void onPause() {
        super.onPause();

        // Tell the gameView pause method to execute
        gameView.pause();
    }

}