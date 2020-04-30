package com.github.luoyemyy.aclin.app.common.profile

import com.github.luoyemyy.aclin.profile.Profile
import com.github.luoyemyy.aclin.profile.ProfileAdd
import com.github.luoyemyy.aclin.profile.ProfileProperties

class Properties : ProfileAdd {
    override fun add() {
        Profile.add(ProfileProperties.API_URL, "http://192.168.0.105:10000/files/")
    }
}