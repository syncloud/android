package org.syncloud.android.core.redirect

import org.syncloud.android.core.redirect.model.User

interface IUserService {
    fun getUser(email: String, password: String): User?
    fun createUser(email: String, password: String): User?
}