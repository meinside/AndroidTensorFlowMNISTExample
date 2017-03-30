package kr.pe.meinside.tensorflowtest;

import android.graphics.PointF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import kr.pe.meinside.tensorflowtest.view.DrawModel;
import kr.pe.meinside.tensorflowtest.view.DrawView;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener{

    static {
        System.loadLibrary("tensorflow_inference"); // XXX - NOT 'libtensorflow_inference'
    }
    private static final String TAG = "MainActivity";

    private static final int PIXEL_WIDTH = 28;

    private float mLastX;

    private float mLastY;

    private DrawModel mModel;
    private DrawView mDrawView;

    private View detectButton;

    private PointF mTmpPoint = new PointF();

    private static final int INPUT_SIZE = 28;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "output";

    private static final String MODEL_FILE = "file:///android_asset/mnist_model_graph.pb";

    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();


    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mModel = new DrawModel(PIXEL_WIDTH, PIXEL_WIDTH);

        mDrawView = (DrawView) findViewById(R.id.view_draw);
        mDrawView.setModel(mModel);
        mDrawView.setOnTouchListener(this);

        detectButton = findViewById(R.id.buttonDetect);
        detectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDetectClicked();
            }
        });

        View clearButton = findViewById(R.id.buttonClear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClearClicked();
            }
        });

//        mResultToast = Toast.makeText(this, "", Toast.LENGTH_LONG);

        initTensorFlowAndLoadModel();
    }

    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = DigitClassifier.create(
                            getAssets(),
                            MODEL_FILE,
                            INPUT_SIZE,
                            INPUT_NAME,
                            OUTPUT_NAME);
                    makeButtonVisible();
                    Log.d(TAG, "Load Success");
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }

    private void makeButtonVisible() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                detectButton.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onResume() {
        mDrawView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mDrawView.onPause();
        super.onPause();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;

        if (action == MotionEvent.ACTION_DOWN) {
            processTouchDown(event);
            return true;

        } else if (action == MotionEvent.ACTION_MOVE) {
            processTouchMove(event);
            return true;

        } else if (action == MotionEvent.ACTION_UP) {
            processTouchUp();
            return true;
        }
        return false;
    }

    private void processTouchDown(MotionEvent event) {
        mLastX = event.getX();
        mLastY = event.getY();
        mDrawView.calcPos(mLastX, mLastY, mTmpPoint);
        float lastConvX = mTmpPoint.x;
        float lastConvY = mTmpPoint.y;
        mModel.startLine(lastConvX, lastConvY);
    }

    private void processTouchMove(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        mDrawView.calcPos(x, y, mTmpPoint);
        float newConvX = mTmpPoint.x;
        float newConvY = mTmpPoint.y;
        mModel.addLineElem(newConvX, newConvY);

        mLastX = x;
        mLastY = y;
        mDrawView.invalidate();
    }

    private void processTouchUp() {
        mModel.endLine();
    }

    private void onDetectClicked() {
        float pixels[] = mDrawView.getPixelData();

        final List<Classifier.Recognition> results = classifier.recognizeImage(pixels);

        if (results.size() > 0) {
            StringBuffer str = new StringBuffer("Result:\n\n");

            for(Classifier.Recognition result : results) {
                str.append(String.format("%s: %.2f%%", result.getTitle(), result.getConfidence() * 100.0f));
            }

            Toast.makeText(this, str, Toast.LENGTH_LONG).show();
        }
    }

    private void onClearClicked() {
        mModel.clear();
        mDrawView.reset();
        mDrawView.invalidate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                classifier.close();
            }
        });
    }
}
