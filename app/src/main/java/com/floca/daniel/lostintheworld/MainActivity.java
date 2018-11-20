package com.floca.daniel.lostintheworld;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.net.Uri;
import android.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements
                        multiplechoice.OnFragmentInteractionListener{

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public enum Models {
        capitol("capitol.sfb", 0, "United States Capitol"),
        christ_rio("Christ_Rio.sfb", 1, "Christ The Redeemer"),
        colosseum("colosseum.sfb", 2, "Colosseum"),
        dubai("Dubai.sfb", 3, "Burj Khalifa"),
        eiffel_tower("Eiffel_Tower.sfb", 4, "Eiffel Tower"),
        goldgate("GOLDGATE.sfb", 5, "Golden Gate Bridge"),
        libertystatue("LibertyStatue.sfb", 6, "Statue of Liberty"),
        pisa("pisa.sfb", 7, "Leaning Tower of Pisa");

        private String stringVal;
        private int intVal;

        private String answerVal;

        private Models(String toString, int value, String answer){
            stringVal = toString;
            intVal = value;
            answerVal = answer;
        }

        @Override
        public String toString(){
            return stringVal;
        }
    }

    private static final String TAG = MainActivity.class.getName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private int previousModel = 999999;
    private FragmentManager fragmentManager;
    private multiplechoice multiplechoice;

    private ArFragment arFragment;
    private ViewRenderable viewRenderable;
    private ModelRenderable modelRenderable;

    private Random r;
    private int randomized;

    private Anchor anchor;
    private AnchorNode anchorNode;
    private Session session;
    private HitResult hitResult;

    private Button btnGuess;

    private RadioGroup rdgChoices;

    private RadioButton rdb1;
    private RadioButton rdb2;
    private RadioButton rdb3;
    private RadioButton rdb4;

    private int numModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        r  = new Random();

        // make sure OpenGL version is supported
        if (!checkIsSupportedDevice(this)) {
            String errorMessage =  "Sceneform requires OpenGL ES " + MIN_OPENGL_VERSION + " or later";
            Log.e(TAG, errorMessage);
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            finish(); // finish the activity
            return;
        }
        setContentView(R.layout.activity_main);

        fragmentManager = getFragmentManager();
        multiplechoice = new multiplechoice();

        numModels = Models.values().length;

        setupView();
        //collapseChoices();

        setupArScene();
        handleUserTaps();

        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onSceneUpdate);
    }

    private void setupView(){
        rdgChoices = findViewById(R.id.rdgChoices);

        rdb1 = findViewById(R.id.rdb1);
        rdb2 = findViewById(R.id.rdb2);
        rdb3 = findViewById(R.id.rdb3);
        rdb4 = findViewById(R.id.rdb4);

        btnGuess = findViewById(R.id.btnGuess);
    }

    private void handleUserTaps() {
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {

            // modelRenderable must be loaded
            /*if (modelRenderable ==  null) {
                return;
            }*/
            if(this.hitResult == null)
                this.hitResult = hitResult;
        });
    }

    private void onSceneUpdate(FrameTime frameTime){
        arFragment.onUpdate(frameTime);

        //no frame
        if(arFragment.getArSceneView().getArFrame() == null)
            return;

        //camera is not yet tracking
        if(arFragment.getArSceneView().getArFrame().getCamera().getTrackingState() != TrackingState.TRACKING)
            return;

        if(this.anchorNode == null && this.hitResult != null){
            session = arFragment.getArSceneView().getSession();
            anchor = session.createAnchor(Pose.makeTranslation(0, 0.5f, 0).compose(hitResult.getHitPose()));
            anchorNode = new AnchorNode(anchor);
            anchorNode.setRenderable(modelRenderable);
            anchorNode.setParent(arFragment.getArSceneView().getScene());
            addRenderableToScene(anchorNode, modelRenderable);

            build3dModel();
            generateAnswers();
        }
    }

    private void setupArScene() {
        // ARFragment is what is displaying our scene
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        //hide ArCore hand
        arFragment.getPlaneDiscoveryController().hide();
        arFragment.getPlaneDiscoveryController().setInstructionView(null);

        //build3dModel();
    }

    private Node addRenderableToScene(AnchorNode anchorNode, Renderable renderable) {
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());

        // anchor node knows where it fits into our world
        node.setParent(anchorNode);
        node.setRenderable(renderable);
        //comment node.select() to hide model's base indicator (circle)
        //node.select();

        return node;
    }

    private void build3dModel() {

        randomized = r.nextInt(numModels);

        while (previousModel == randomized) {
            randomized = r.nextInt(numModels);
        }

        ModelRenderable.builder()
                .setSource(this, Uri.parse(Models.values()[randomized].toString()))
                .build()
                .thenAccept(renderable -> modelRenderable = renderable)
                .exceptionally(throwable -> {
                    Toast toast = Toast.makeText(MainActivity.this, "Unable to display model",
                            Toast.LENGTH_LONG);

                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return null;
                });
    }

    private boolean checkIsSupportedDevice(final Activity activity) {

        ActivityManager activityManager =
                (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);

        if (activityManager == null) {
            Log.e(TAG, "ActivityManager is null");
            return false;
        }

        String openGlVersion = activityManager.getDeviceConfigurationInfo().getGlEsVersion();

        return openGlVersion != null && Double.parseDouble(openGlVersion) >= MIN_OPENGL_VERSION;
    }

    public void btnGuessClick(View view) {
        if(btnGuess.getText().equals("▲")){
            expandChoices();
        }
        else{
            collapseChoices();
        }
    }

    private void expandChoices(){
        btnGuess.setText("▼");
        rdb1.setVisibility(View.VISIBLE);
        rdb2.setVisibility(View.VISIBLE);
        rdb3.setVisibility(View.VISIBLE);
        rdb4.setVisibility(View.VISIBLE);
    }

    private void collapseChoices(){
        btnGuess.setText("▲");
        rdb1.setVisibility(View.INVISIBLE);
        rdb2.setVisibility(View.INVISIBLE);
        rdb3.setVisibility(View.INVISIBLE);
        rdb4.setVisibility(View.INVISIBLE);
    }

    private void generateAnswers(){

        List<String> answers = new ArrayList<>();

        answers.add(Models.values()[randomized].answerVal);

        int current;
        for(;;){
            current = r.nextInt(numModels);

            if(!answers.contains(Models.values()[current].answerVal))
                answers.add(Models.values()[current].answerVal);
            else if(answers.size() == 4)
                break;
        }

        Collections.shuffle(answers);

        rdb1.setText(answers.get(0));
        rdb2.setText(answers.get(1));
        rdb3.setText(answers.get(2));
        rdb4.setText(answers.get(3));
    }
}
