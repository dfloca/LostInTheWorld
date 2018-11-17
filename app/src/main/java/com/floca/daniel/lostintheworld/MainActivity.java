package com.floca.daniel.lostintheworld;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public enum Models {
        capitol("capitol.sfb", 0),
        christ_rio("Christ_Rio.sfb", 1),
        colosseum("colosseum.sfb", 2),
        dubai("Dubai.sfb", 3),
        eiffel_tower("Eiffel_Tower.sfb", 4),
        goldgate("GOLDGATE.sfb", 5),
        libertystatue("LibertyStatue.sfb", 6),
        pisa("pisa", 7);

        private String stringVal;
        private int intVal;

        private Models(String toString, int value){
            stringVal = toString;
            intVal = value;
        }

        @Override
        public String toString(){
            return stringVal;
        }
    }

    private static final String TAG = MainActivity.class.getName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private ArFragment arFragment;
    private ViewRenderable viewRenderable;
    private ModelRenderable modelRenderable;

    private Random r;
    private int randomized;
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

        setupArScene();
    }

    private void setupArScene() {
        // ARFragment is what is displaying our scene
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        arFragment.getPlaneDiscoveryController().hide();
        arFragment.getPlaneDiscoveryController().setInstructionView(null);
        // load the renderables
       // buildAndroidWidgetModel();
        build3dModel();

        // handle taps
        handleUserTaps();
    }

    private void handleUserTaps() {
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {

            // viewRenderable must be loaded
            if (modelRenderable ==  null) {
                return;
            }

            // create the an anchor on the scene
            AnchorNode anchorNode = createAnchorNode(hitResult);
            // add the view to the scene
            addRenderableToScene(anchorNode, modelRenderable);

        });
    }

    private AnchorNode createAnchorNode(HitResult hitResult) {

        // create an anchor based off the the HitResult (what was tapped)
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);

        // attach this anchor to the scene
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        return anchorNode;
    }

    private Node addRenderableToScene(AnchorNode anchorNode, Renderable renderable) {
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());

        // anchor node knows where it fits into our world
        node.setParent(anchorNode);
        node.setRenderable(renderable);
        node.select();

        return node;
    }

   /* private void buildAndroidWidgetModel() {

        ViewRenderable.builder()
                .setView(this, R.layout.hello_world_view)
                .build()
                .thenAccept(renderable -> {
                    viewRenderable = renderable;

                    if (viewRenderable != null) {
                        // get the view from the renderable
                        View androidView = viewRenderable.getView();

                        Button btnToast = androidView.findViewById(R.id.button_toast);
                        btnToast.setOnClickListener(view -> {
                            Toast.makeText(MainActivity.this, "Hello World",
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(MainActivity.this, "Unable to display Hello World",
                            Toast.LENGTH_LONG).show();

                    return null;
                });
    }*/

    private void build3dModel() {

        int randomized = r.nextInt(Models.values().length);

        ModelRenderable.builder()
                // helloWorld.sfb was added to the assets folder by the Sceneform plugin when
                // we imported the asset
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
}
