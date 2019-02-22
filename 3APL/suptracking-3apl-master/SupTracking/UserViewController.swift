//
//  UserViewController.swift
//  SupTracking
//
//  Created by Sami on 24/12/17.
//  Copyright © 2017 Sami Alaoui (thedroidgeek). All rights reserved.
//

import UIKit

class UserViewController: UIViewController {

    @IBOutlet weak var userInfo: UITextView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        userInfo.text = "ID: \(UserInfo.id)\n" +
                        "Username: \(UserInfo.username)\n" +
                        "Phone: \(UserInfo.phone)\n" +
                        "First name: \(UserInfo.firstName)\n" +
                        "Last name: \(UserInfo.lastName)\n" +
                        "Postal code: \(UserInfo.postalCode)\n" +
                        "Address: \(UserInfo.address)\n" +
                        "Email: \(UserInfo.email)"
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
}
