package com.github.luoyemyy.aclin.app.profile

import com.github.luoyemyy.aclin.profile.Profile
import com.github.luoyemyy.aclin.profile.ProfileAdd
import com.github.luoyemyy.aclin.profile.ProfileProperties

class Properties : ProfileAdd {
    override fun add() {
        Profile.add(ProfileProperties.API_URL, "http://127.0.0.1:8080/")
    }
}