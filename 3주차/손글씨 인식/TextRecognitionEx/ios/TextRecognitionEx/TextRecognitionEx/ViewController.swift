import UIKit
import FirebaseMLVision

//텍스트 인식
class ViewController: UIViewController,
    UINavigationControllerDelegate,
    UIImagePickerControllerDelegate {
    //UI
    @IBOutlet weak var imageView: UIImageView!
    @IBOutlet weak var drawView: DrawView!
    @IBOutlet weak var segmentControl: UISegmentedControl!

    //정보
    var isCloud: Bool = false
    //var textDetector: VisionTextRecognizer!
    //var cloudTextDetector: VisionCloudTextDetector!


//====================
//라이프사이클
//====================
    //뷰 표시시 호출
    override func viewDidAppear(_ animated: Bool) {
        //(1)온디바이스 API 텍스트 인식 검출기 생성
        //textDetector = Vision.vision().textDetector()

        //(4)클라우드 API 텍스트 인식 검출기 생성
        //cloudTextDetector = Vision.vision().cloudTextDetector()

        //액션시트 표시
        if self.imageView.image == nil {
            showActionSheet()
        }
    }
    
    
//====================
//이벤트
//====================
    //화면 터치시 호출
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        showActionSheet()
    }


//====================
//액션시트
//====================
    //액션시트 표시
    func showActionSheet() {
        let actionSheet = UIAlertController(title: nil, message: nil,
            preferredStyle: .actionSheet)
        actionSheet.addAction(UIAlertAction(title: "카메라", style: .default) {
            action in
            self.openPicker(sourceType: .camera)
        })
        actionSheet.addAction(UIAlertAction(title: "사진 라이브러리", style: .default) {
            action in
            self.openPicker(sourceType: .photoLibrary)
        })
        actionSheet.addAction(UIAlertAction(title: "취소", style: .cancel))
        self.present(actionSheet, animated: true, completion: nil)
    }
    
    
//====================
//경고
//====================
    //경고표시
    func showAlert(_ text: String!) {
        let alert = UIAlertController(title: text, message: nil,
            preferredStyle: UIAlertController.Style.alert)
        alert.addAction(UIAlertAction(title: "OK",
            style: UIAlertAction.Style.default, handler: nil))
        self.present(alert, animated: true, completion: nil)
    }
   
   
//====================
//이미지 피커
//====================
    //이미지 피커 결기
    func openPicker(sourceType: UIImagePickerController.SourceType) {
        let picker = UIImagePickerController()
        picker.sourceType = sourceType
        picker.delegate = self
        self.present(picker, animated: true, completion: nil)
    }

    //이미지 피커로 이미지 획득시 호출
    func imagePickerController(_ picker: UIImagePickerController,
        didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
        //이미지 획득
        var image = info[UIImagePickerController.InfoKey.originalImage] as! UIImage
        
        //이미지 방향 보정
        let size = image.size
        UIGraphicsBeginImageContext(size)
        image.draw(in: CGRect(x: 0, y: 0, width: size.width, height: size.height))
        image = UIGraphicsGetImageFromCurrentImageContext()!
        UIGraphicsEndImageContext()

        //이미지 지정
        self.imageView.image = image
        
        //닫기
        picker.presentingViewController!.dismiss(animated:true, completion:nil)

        //예측
        if self.segmentControl.selectedSegmentIndex == 0 {
            detectText(image)
        } else {
            detectCloudText(image)
        }
    }
    
    //이미지 피커 취소시 호출
    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        //닫기
        picker.presentingViewController!.dismiss(animated:true, completion:nil)
    }
    
    
//====================
//텍스트 인식
//====================
    //온디바이스 API 텍스트 인식
    func detectText(_ image: UIImage) {
        //이미지 크기 지정
        self.drawView.setImageSize(image.size)
        
        DispatchQueue.global(qos: .default).async {
            //VisionImage 생성
            let visionImage = VisionImage(image: image)
            let imageMetadata = VisionImageMetadata()
            imageMetadata.orientation = self.image2orientation(image)
            visionImage.metadata = imageMetadata

            //(1)텍스트 인식 검출기 생성
            let textRecognizer = Vision.vision().onDeviceTextRecognizer()

            //(2)텍스트 인식 검출기 실행
            textRecognizer.process(visionImage) {
                texts, error in
                //에러처리
                if error != nil {
                    self.showAlert(error!.localizedDescription)
                    return
                }

                DispatchQueue.main.async {
                    //검출결과 취득
                    self.drawView.texts = texts

                    //UI 업데이트
                    self.drawView.setNeedsDisplay()
                }
            }
        }
    }

    //클라우드 API 텍스트 인식
    func detectCloudText(_ image: UIImage) {
        //이미지 크기 지정
        self.drawView.setImageSize(image.size)
        
        DispatchQueue.global(qos: .default).async {
            //VisionImage 생성
            let visionImage = VisionImage(image: image)
            let imageMetadata = VisionImageMetadata()
            imageMetadata.orientation = self.image2orientation(image)
            visionImage.metadata = imageMetadata
            
            //(4)텍스트 인식 검출기 생성
            let textRecognizer = Vision.vision().cloudTextRecognizer()
            
            //(5)텍스트 인식 실행
            textRecognizer.process(visionImage) {
                texts, error in
                //에러 처리
                if error != nil {
                    self.showAlert(error!.localizedDescription)
                    return
                }

                DispatchQueue.main.async {
                    //검출결과 획득
                    self.drawView.texts = texts
                    
                    //UI 업데이트
                    self.drawView.setNeedsDisplay()
                }
            }
        }
    }

    //UIImage→VisionDetectorImageOrientation
    func image2orientation(_ image: UIImage) -> VisionDetectorImageOrientation {
        switch image.imageOrientation {
        case .up:
            return .topLeft
        case .down:
            return .bottomRight
        case .left:
            return .leftBottom
        case .right:
            return .rightTop
        case .upMirrored:
            return .topRight
        case .downMirrored:
            return .bottomLeft
        case .leftMirrored:
            return .leftTop
        case .rightMirrored:
            return .rightBottom
        }
    }
}
