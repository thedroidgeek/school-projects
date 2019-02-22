//
//  LoginViewController.swift
//  SupTracking
//
//  Created by Sami on 24/12/17.
//  Copyright © 2017 Sami Alaoui (thedroidgeek). All rights reserved.
//

import UIKit

class LoginViewController: UIViewController {

    @IBOutlet weak var usernameField: UITextField!
    @IBOutlet weak var passwordField: UITextField!
    
    var activityIndicator:UIActivityIndicatorView = UIActivityIndicatorView()
    
    @IBOutlet weak var login: UIButton!
    override func viewDidLoad() {
        super.viewDidLoad()
        activityIndicator.hidesWhenStopped = true
        activityIndicator.activityIndicatorViewStyle = UIActivityIndicatorViewStyle.gray
        view.addSubview(activityIndicator)
    }
    
    func showErrorDialogThreadSafe(message: String) {
        let alert = UIAlertController(title: "Error", message: message, preferredStyle: UIAlertControllerStyle.alert)
        alert.addAction(UIAlertAction(title: "Ok", style: UIAlertActionStyle.default, handler: nil))
        DispatchQueue.main.async(execute: {
            self.present(alert, animated: true, completion: nil)
        })
    }
    
    func setUIBusy(toggle: Bool) {
        if (toggle) {
            activityIndicator.center = login.center
            activityIndicator.startAnimating()
            usernameField.isEnabled = false
            passwordField.isEnabled = false
            login.isEnabled = false
        }
        else {
            activityIndicator.stopAnimating()
            usernameField.isEnabled = true
            passwordField.isEnabled = true
            login.isEnabled = true
        }
    }
    
    @IBAction func loginAction(_ sender: Any) {
        
        let inputUser = usernameField.text
        let inputPass = passwordField.text
        
        if (inputUser == "" || inputPass == "") {
            return
        }
        
        UserInfo.setCreds(user: inputUser!, pass: inputPass!)
        
        setUIBusy(toggle: true)
        
        ServerApi.makePostRequest(action: "login", parameters: nil, completion: { data, response, error in
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
                        self.showErrorDialogThreadSafe(message: "Wrong username and/or password!")
                        return
                    }
                    guard let userDict = dict["user"] as? [String: AnyObject] else {
                        self.showErrorDialogThreadSafe(message: "Unexpected server response! (invalid user)")
                        return
                    }
                    
                    UserInfo.setDataFromDict(dict: userDict)
                    
                    DispatchQueue.main.async(execute: {
                        self.passwordField.text = ""
                        self.performSegue(withIdentifier: "loginSegue", sender: nil)
                    })
                }
                
            } catch let error {
                print(error.localizedDescription)
                self.showErrorDialogThreadSafe(message: "Exception: \(error.localizedDescription)")
            }
            
        })
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }


}

