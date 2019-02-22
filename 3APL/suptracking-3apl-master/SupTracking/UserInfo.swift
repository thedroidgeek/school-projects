//
//  UserInfo.swift
//  SupTracking
//
//  Created by Sami on 20/12/17.
//  Copyright © 2017 Sami Alaoui (thedroidgeek). All rights reserved.
//

import UIKit

class UserInfo: NSObject {
    private(set) static var id :Int = 0
    private(set) static var username :String = ""
    private(set) static var password :String = ""
    private(set) static var phone :String = ""
    private(set) static var firstName :String = ""
    private(set) static var lastName :String = ""
    private(set) static var postalCode :String = ""
    private(set) static var address :String = ""
    private(set) static var email :String = ""

    static func setDataFromDict(dict: Dictionary<String, AnyObject>) {
        self.id = dict["id"] as! Int
        self.phone = dict["phone"] as! String
        self.firstName = dict["firstname"] as! String
        self.lastName = dict["lastname"] as! String
        self.postalCode = dict["postalCode"] as! String
        self.address = dict["address"] as! String
        self.email = dict["email"] as! String
    }
    
    static func setCreds(user: String, pass: String) {
        self.username = user
        self.password = pass
    }
}
