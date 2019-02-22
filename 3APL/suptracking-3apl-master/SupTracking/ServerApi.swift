//
//  ServerApi.swift
//  SupTracking
//
//  Created by Sami on 25/12/17.
//  Copyright © 2017 Sami Alaoui (thedroidgeek). All rights reserved.
//

import UIKit
import SystemConfiguration

class ServerApi: NSObject
{
    // link might not always be up, so feel free to change while testing:
    static let requestUrl = NSURL(string: "https://androidgeek.ga/files/supinfo-apl-proj-api.php")
    
    static func makePostRequest(action: String, parameters: (key: String, value: String)? ..., completion: @escaping (Data?, URLResponse?, Error?) -> Swift.Void)
    {
        if (!isInternetAvailable()) {
            completion(nil, nil, connectivityError.customError)
            return
        }
        
        let escapedUser = UserInfo.username.addingPercentEncoding(withAllowedCharacters: .urlHostAllowed)
        let escapedPass = UserInfo.password.addingPercentEncoding(withAllowedCharacters: .urlHostAllowed)
        
        let session = URLSession.shared
        let request = NSMutableURLRequest(url: requestUrl! as URL)
        request.httpMethod = "POST"
        var postString = "action=\(action)&login=\(escapedUser!)&password=\(escapedPass!)"
        
        for param in parameters {
            if (param?.key != nil && param?.value != nil) {
                postString += "&\((param?.key)!)=\((param?.value)!)"
            }
        }
        
        request.httpBody = postString.data(using: .utf8)
        
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        request.addValue("application/x-www-form-urlencoded", forHTTPHeaderField: "Content-Type")
        
        let task = session.dataTask(with: request as URLRequest, completionHandler: completion)
        
        task.resume()
    }
    
    static func isInternetAvailable() -> Bool
    {
        var zeroAddress = sockaddr_in()
        zeroAddress.sin_len = UInt8(MemoryLayout.size(ofValue: zeroAddress))
        zeroAddress.sin_family = sa_family_t(AF_INET)
        
        let defaultRouteReachability = withUnsafePointer(to: &zeroAddress) {
            $0.withMemoryRebound(to: sockaddr.self, capacity: 1) {zeroSockAddress in
                SCNetworkReachabilityCreateWithAddress(nil, zeroSockAddress)
            }
        }
        
        var flags = SCNetworkReachabilityFlags()
        if !SCNetworkReachabilityGetFlags(defaultRouteReachability!, &flags) {
            return false
        }
        let isReachable = flags.contains(.reachable)
        let needsConnection = flags.contains(.connectionRequired)
        return (isReachable && !needsConnection)
    }
}

enum connectivityError: Error {
    case customError
}

extension connectivityError: LocalizedError {
    public var errorDescription: String? {
        switch self {
        case .customError:
            return NSLocalizedString("Unable to communicate with the server. Please check your internet connectivity.", comment: "Network Error")
        }
    }
}
