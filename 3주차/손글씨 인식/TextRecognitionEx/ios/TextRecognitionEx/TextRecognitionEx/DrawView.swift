import UIKit
import FirebaseMLVision

//그리기 뷰
class DrawView: UIView {
    //정수
    let COLOR_BLUE: UIColor = UIColor(red: 0.0, green: 0.0, blue: 255.0, alpha: 0.3)
    let COLOR_WHITE: UIColor = UIColor.white

    //속성
    var imageRect: CGRect = CGRect.zero
    var imageScale: CGFloat = 1
    var texts: VisionText! = nil

    //초기화
    override init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    //초기화
    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)!
    }

    //이미지크기 지정
    func setImageSize(_ imageSize: CGSize) {
        //이미지 표시영역 계산(AspectFit)
        let scale: CGFloat =
            (self.frame.width/imageSize.width < self.frame.height/imageSize.height) ?
            self.frame.width/imageSize.width :
            self.frame.height/imageSize.height
        let dw: CGFloat = imageSize.width*scale
        let dh: CGFloat = imageSize.height*scale
        self.imageRect = CGRect(
            x: (self.frame.width-dw)/2,
            y: (self.frame.height-dh)/2,
            width: dw, height: dh)
        self.imageScale = scale
    }

    //(3)검출결과 그리기
    override func draw(_ rect: CGRect) {
        if texts == nil {return}

        //그래픽 컨텍스트 생성
        let context = UIGraphicsGetCurrentContext()!
        
        //텍스트 검출 그리기
        for block in self.texts.blocks {
            for line in block.lines {
                for element in line.elements {
                    //영역 그리기
                    let rect = convertRect(element.frame)
                    context.setFillColor(COLOR_BLUE.cgColor)
                    context.fill(rect)

                    //텍스트 그리기
                    drawText(context, text: element.text,
                        fontSize: 12, rect: rect)
                }
            }
        }
    }
    
    //텍스트 크리기
    func drawText(_ context: CGContext, text: String!, fontSize: CGFloat, rect: CGRect) {
        if text == nil {return}
        let paragraphStyle = NSMutableParagraphStyle()
        paragraphStyle.alignment = .center
        let attributes = [
            NSAttributedString.Key.paragraphStyle: paragraphStyle,
            NSAttributedString.Key.font: UIFont.systemFont(ofSize: fontSize),
            NSAttributedString.Key.foregroundColor: COLOR_WHITE
        ]
        let attributedString = NSAttributedString(string: text, attributes: attributes)
        attributedString.draw(in: rect)
    }
    
    //검출결과 좌표계를 화면 좌표계로 변환
    func convertRect(_ rect:CGRect) -> CGRect {
        return CGRect(
            x: Int(imageRect.minX+rect.minX*imageScale),
            y: Int(imageRect.minY+rect.minY*imageScale),
            width: Int(rect.width*imageScale),
            height: Int(rect.height*imageScale))
    }
}
