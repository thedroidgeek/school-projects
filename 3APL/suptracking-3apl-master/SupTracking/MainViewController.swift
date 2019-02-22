//
//  MainViewController.swift
//  SupTracking
//
//  Created by Sami on 24/12/17.
//  Copyright © 2017 Sami Alaoui (thedroidgeek). All rights reserved.
//

import UIKit
import CoreLocation

class MainViewController: UIViewController {

    @IBOutlet weak var welcomeMessage: UILabel!
    @IBOutlet weak var logout: UIButton!
    
    fileprivate var lastKnownLocation: (long: Double, lat: Double)? = nil
    
    weak var timer: Timer?
    
    private lazy var locationManager: CLLocationManager = {
        let manager = CLLocationManager()
        manager.desiredAccuracy = kCLLocationAccuracyBest
        manager.delegate = self
        manager.requestAlwaysAuthorization()
        return manager
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        welcomeMessage.text = "Welcome, \(UserInfo.firstName)!"
        locationManager.startUpdatingLocation()
        let status = CLLocationManager.authorizationStatus()
        if status == .denied {
            // *sarcasm alert*
            let alert = UIAlertController(title: "Warning", message: "You have explicitely disabled location access for SupTracking, and as a result, we can no longer track your position! (good call)\nIf you, by any chance however, change your mind, and decide to revert this, you can always do so by going to Settings, and choosing 'Always' for this app's location access.", preferredStyle: UIAlertControllerStyle.alert)
            alert.addAction(UIAlertAction(title: "Ok", style: UIAlertActionStyle.default, handler: nil))
            self.present(alert, animated: true, completion: nil)
        }
        timer?.invalidate()
        timer = Timer.scheduledTimer(withTimeInterval: 60.0, repeats: true) { [weak self] _ in
            if (self?.lastKnownLocation != nil) {
                ServerApi.makePostRequest(
                    action:
                        "updatePosition",
                    parameters:
                        ("longitude", String(describing: self?.lastKnownLocation!.long)),
                        ("latitude", String(describing: self?.lastKnownLocation!.lat)),
                    completion:
                        { data, response, error in
                            guard error == nil else {
                                return
                            }
                            guard let data = data else {
                                return
                            }
                            do {
                                if let dict = try JSONSerialization.jsonObject(with: data) as? [String: AnyObject] {
                                    guard let successValue = dict["success"] as? Bool else {
                                        return
                                    }
                                    if (!successValue) {
                                        print("updatePosition: Server said no")
                                        return
                                    }
                                }
                            } catch let error {
                                print("updatePosition: \(error.localizedDescription)")
                            }
                        }
                )
            }
            
        }
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    

    @IBAction func logoutAction(_ sender: Any) {
        let alert = UIAlertController(title: "Logout", message: "Are you sure?", preferredStyle: UIAlertControllerStyle.alert)
        alert.addAction(UIAlertAction(title: "Yes", style: UIAlertActionStyle.default, handler: { (action: UIAlertAction!) in
            self.locationManager.stopUpdatingLocation()
            self.timer?.invalidate()
            self.presentingViewController?.dismiss(animated: true, completion: nil)
        }))
        alert.addAction(UIAlertAction(title: "No", style: UIAlertActionStyle.default, handler: nil))
        self.present(alert, animated: true, completion: nil)
    }
}

extension MainViewController: CLLocationManagerDelegate {
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let mostRecentLocation = locations.last else {
            return
        }
        
        let isFirstLocationUpdate = lastKnownLocation == nil
        
        lastKnownLocation = (mostRecentLocation.coordinate.longitude, mostRecentLocation.coordinate.latitude)
        
        if (isFirstLocationUpdate) {
            timer?.fire()
        }
    }
    
}
