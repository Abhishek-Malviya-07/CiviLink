package com.example.civilink.data

class User {
    var uid : String?=null
    var name : String?=null
    var profileImage : String?=null
    var emailId : String?=null
    constructor(){
    }
    constructor(uid: String?, name: String?, profileImage: String?, emailId : String?)
    {
        this.emailId=emailId
        this.name=name
        this.uid=uid
        this.profileImage=profileImage
    }
}