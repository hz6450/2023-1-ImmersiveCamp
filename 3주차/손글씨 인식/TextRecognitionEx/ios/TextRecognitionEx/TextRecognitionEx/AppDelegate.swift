import UIKit
import Firebase

//AppDelegate
@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?

    //앱 기동시 호출
    func application(_ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions:
        [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        //Firebase 설정
        FirebaseApp.configure()
        return true
    }
}
