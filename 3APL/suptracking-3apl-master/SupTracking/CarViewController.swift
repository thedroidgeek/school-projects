//
//  CarViewController.swift
//  SupTracking
//
//  Created by Sami on 25/12/17.
//  Copyright © 2017 Sami Alaoui (thedroidgeek). All rights reserved.
//

import UIKit
import MapKit

class CarViewController: UIViewController {

    @IBOutlet weak var mapView: MKMapView!
    
    var activityIndicator:UIActivityIndicatorView = UIActivityIndicatorView()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        activityIndicator.hidesWhenStopped = true
        activityIndicator.activityIndicatorViewStyle = UIActivityIndicatorViewStyle.gray
        activityIndicator.center = view.center
        view.addSubview(activityIndicator)
        
        setUIBusy(toggle: true)
        
        ServerApi.makePostRequest(action: "getCarPosition", parameters: nil, completion: { data, response, error in
            DispatchQueue.main.async(execute: {
                self.setUIBusy(toggle: false)
            })
            guard error == nil else {
                self.showErrorDialogThreadSafe(message: (error?.localizedDescription)!)
                return
            }
            guard let data = data else {
                self.showErrorDialogThreadSafe(message: "Unexpected server response! (empty response)")
                return
            }
            do {
                if let dict = try JSONSerialization.jsonObject(with: data) as? [String: AnyObject] {
                    guard let successValue = dict["success"] as? Bool else {
                        self.showErrorDialogThreadSafe(message: "Unexpected server response! (no success flag)")
                        return
                    }
                    if (!successValue) {
                        self.showErrorDialogThreadSafe(message: "Failed to get car position!")
                        return
                    }
                    guard let posDict = dict["position"] as? [String: AnyObject] else {
                        self.showErrorDialogThreadSafe(message: "Unexpected server response! (invalid position)")
                        return
                    }
                    
                    let annotation = MKPointAnnotation()
                    annotation.coordinate = CLLocationCoordinate2D(latitude: posDict["latitude"] as! Double, longitude: posDict["longitude"] as! Double)
                    
                    let region = MKCoordinateRegion(center: annotation.coordinate, span: MKCoordinateSpan(latitudeDelta: 0.01, longitudeDelta: 0.01))
                    
                    DispatchQueue.main.async(execute: {
                        self.mapView.addAnnotation(annotation)
                        self.mapView.setRegion(region, animated: true)
                    })
                }
            } catch let error {
                self.showErrorDialogThreadSafe(message: "JSON parser: \(error.localizedDescription)")
            }
        })
    }
    
    func showErrorDialogThreadSafe(message: String) {
        let alert = UIAlertController(title: "Error", message: message, preferredStyle: UIAlertControllerStyle.alert)
        alert.addAction(UIAlertAction(title: "Ok", style: UIAlertActionStyle.default, handler: nil))
        DispatchQueue.main.async(execute: {
            _ = self.navigationController?.popToRootViewController(animated: true)
            self.present(alert, animated: true, completion: nil)
        })
    }
    
    func setUIBusy(toggle: Bool) {
        if (toggle) {
            activityIndicator.startAnimating()
        }
        else {
            activityIndicator.stopAnimating()
        }
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
}
