package net.npaka.textrecognitionex;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.google.firebase.ml.vision.text.FirebaseVisionText;

//그리기 뷰
public class DrawView extends View {
    //정수
    private static final int COLOR_BLUE = Color.argb(76, 0, 0, 255);
    private static final int COLOR_WHITE = Color.WHITE;

    //정보
    private Rect imageRect = new Rect();
    private float imageScale = 1;
    public FirebaseVisionText texts = null;
    private float density;
    private Paint paint;


//====================
//라이프 사이클 
//====================
    //컨스트럭터 
    public DrawView(Context context) {
        super(context);
        init();
    }

    //컨스트럭터
    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    //컨스트럭터
    public DrawView(Context context, AttributeSet attrs,
        int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    //초기화
    private void init() {
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        density = metrics.density;
        paint = new Paint();
    }


//====================
//액세스
//====================
    //이미지 크기 지정
    public void setImageSize(int imageWidth, int imageHeight) {
        //이미지 표시영역 계산(AspectFill)
        float scale =
            ((float)getWidth()/(float)imageWidth < (float)getHeight()/(float)imageHeight) ?
            (float)getWidth()/(float)imageWidth :
            (float)getHeight()/(float)imageHeight;
        float dw = imageWidth*scale;
        float dh = imageHeight*scale;
        this.imageRect = new Rect(
            (int)((getWidth()-dw)/2),
            (int)((getHeight()-dh)/2),
            (int)((getWidth()-dw)/2+dw),
            (int)((getHeight()-dh)/2+dh));
        this.imageScale = scale;
    }


//====================
//검출결과 그리기
//====================
    //(3)검출결과 그리기
    @Override
    protected void onDraw(Canvas canvas) {
        if (texts == null) return;

        //텍스트 검출 그리기 
        for (FirebaseVisionText.TextBlock block : texts.getTextBlocks()) {
            for (FirebaseVisionText.Line line : block.getLines()) {
                for (FirebaseVisionText.Element element : line.getElements()) {
                    //영역그리기
                    Rect rect = convertRect(element.getBoundingBox());
                    paint.setColor(COLOR_BLUE);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawRect(rect, paint);

                    //텍스트 그리기
                    drawText(canvas, element.getText(), 12, rect);
                }
            }
        }
    }

    //텍스트 그리기
    private void drawText(Canvas canvas, String text, float fontSize, Rect rect) {
        if (text == null) return;
        paint.setColor(COLOR_WHITE);
        paint.setTextSize(fontSize*density);
        Paint.FontMetrics metrics = paint.getFontMetrics();
        canvas.save();
        canvas.clipRect(rect);
        float sw = paint.measureText(text);
        if (rect.width() > sw) {
            canvas.drawText(text, rect.left+(rect.width()-sw)/2, rect.top-metrics.ascent, paint);
        } else {
            canvas.drawText(text, rect.left, rect.top-metrics.ascent, paint);
        }
        canvas.restore();
    }

    //검출결과 좌표계를 화면 좌표계로 변환
    private Rect convertRect(Rect rect) {
        return new Rect(
            (int)(imageRect.left+rect.left*imageScale),
            (int)(imageRect.top+rect.top*imageScale),
            (int)(imageRect.left+rect.left*imageScale+rect.width()*imageScale),
            (int)(imageRect.top+rect.top*imageScale+rect.height()*imageScale));
    }
}