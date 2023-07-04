package net.npaka.textrecognitionex;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

//텍스트 인식
public class ViewController extends FrameLayout implements View.OnClickListener {
    //UI
    private ImageView imageView;
    private Button btnSegmentControl0;
    private Button btnSegmentControl1;
    private int segmentControl = 0;
    private DrawView drawView;

    //정보
    private boolean predictFlag = false;


//====================
//Life Cycle
//====================
    //컨스트럭터
    public ViewController(Activity activity) {
        super(activity);

        //레이아웃
        this.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT));
        LayoutInflater inflater = (LayoutInflater)activity.
            getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.main, null);
        addView(view);

        //UI
        this.imageView = this.findViewById(R.id.image_view);
        this.btnSegmentControl0 = this.findViewById(R.id.btn_segment_control0);
        this.btnSegmentControl0.setOnClickListener(this);
        this.btnSegmentControl1 = this.findViewById(R.id.btn_segment_control1);
        this.btnSegmentControl1.setOnClickListener(this);
        this.drawView = this.findViewById(R.id.draw_view);

        //제스처 추가 
        view.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    showActionSheet();
                }
                return false;
            }
        });

        //액션시트 표시
        showActionSheet();
    }


//====================
//이벤트
//====================
    @Override
    public void onClick(View view) {
        if (view == this.btnSegmentControl0) {
            segmentControl = 0;
            this.btnSegmentControl0.setTextColor(Color.WHITE);
            this.btnSegmentControl0.setBackgroundColor(Color.BLUE);
            this.btnSegmentControl0.setEnabled(false);
            this.btnSegmentControl1.setTextColor(Color.BLUE);
            this.btnSegmentControl1.setBackgroundColor(Color.LTGRAY);
            this.btnSegmentControl1.setEnabled(true);
        } else if (view == this.btnSegmentControl1) {
            segmentControl = 1;
            this.btnSegmentControl0.setTextColor(Color.BLUE);
            this.btnSegmentControl0.setBackgroundColor(Color.LTGRAY);
            this.btnSegmentControl0.setEnabled(true);
            this.btnSegmentControl1.setTextColor(Color.WHITE);
            this.btnSegmentControl1.setBackgroundColor(Color.BLUE);
            this.btnSegmentControl1.setEnabled(false);
        }
    }


//====================
//Action Sheet
//====================
    //액션시트 표시 
    private void showActionSheet() {
        String[] items = {"카메라", "갤러리사진"};
        new AlertDialog.Builder(this.getContext())
            .setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((AppDelegate)getContext()).openPicker(which, new AppDelegate.ICompletion(){
                        public void onCompletion(Bitmap image) {
                            if (image == null) return;
                            imageView.setImageBitmap(image);

                            //예측
                            if (segmentControl == 0) {
                                detectTexts(image);
                            } else if (segmentControl == 1) {
                                detectCloudTexts(image);
                            }
                        }
                    });
                }
            })
            .setNegativeButton("닫기", null)
            .show();
    }


//====================
//alert
//====================
    //경고 표시
    private void showAlert(String text) {
        new AlertDialog.Builder(this.getContext())
            .setMessage(text)
            .setPositiveButton("OK", null)
            .show();
    }


//====================
//텍스트 인식
//====================
    //온디바이스 API 텍스트 인식
    private void detectTexts(Bitmap image) {
        //이미지 크기 지정
        drawView.setImageSize(image.getWidth(), image.getHeight());

        //예측중, 무처리
        if (predictFlag) return;
        predictFlag = true;

        //FirebaseVisionImage 생성
        FirebaseVisionImage visionImage = FirebaseVisionImage.fromBitmap(image);

        //(1)텍스트 인식 검출기 생성
        FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance()
            .getOnDeviceTextRecognizer();

        //(2)텍스트 인식 실행
        textRecognizer.processImage(visionImage)
            .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                //성공시 호출
                @Override
                public void onSuccess(final FirebaseVisionText texts) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            //검출결과 취득
                            drawView.texts = texts;

                            //UI 업데이트
                            drawView.postInvalidate();
                            predictFlag = false;
                        }
                    });
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                //에러시 호출
                @Override
                public void onFailure(@NonNull Exception e) {
                    showAlert(e.getMessage());
                    predictFlag = false;
                }
            });
    }

    //클라우드 API 텍스트 인식
    private void detectCloudTexts(Bitmap image) {
        //이미지 크기 지정
        drawView.setImageSize(image.getWidth(), image.getHeight());

        //예측중, 무처리
        if (predictFlag) return;
        predictFlag = true;

        //FirebaseVisionImage 생성
        FirebaseVisionImage visionImage = FirebaseVisionImage.fromBitmap(image);

        //(4)텍스트 인식 검출기 생성
        FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance()
            .getCloudTextRecognizer();

        //(5)텍스트 인식 실행
        textRecognizer.processImage(visionImage)
            .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                //성공시 호출
                @Override
                public void onSuccess(final FirebaseVisionText texts) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            //검출결과 획득
                            drawView.texts = texts;

                            //UI 업데이트
                            drawView.postInvalidate();
                            predictFlag = false;
                        }
                    });
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                //에러시 호출
                @Override
                public void onFailure(@NonNull Exception e) {
                    showAlert(e.getMessage());
                    predictFlag = false;
                }
            });
    }
}