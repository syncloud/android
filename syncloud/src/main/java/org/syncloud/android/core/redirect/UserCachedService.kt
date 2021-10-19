package org.syncloud.android.core.redirect

import org.syncloud.android.core.common.SyncloudException
import org.syncloud.android.core.common.SyncloudResultException
import org.syncloud.android.core.redirect.model.User

class UserCachedService(private val service: IUserService, private val storage: UserStorage) :
    IUserService {
    override fun getUser(email: String, password: String): User? {
        return try {
            val user = service.getUser(email, password)
            storage.save(user)
            user
        } catch (e: SyncloudException) {
            if (e !is SyncloudResultException) {
                val user = storage.load()
                if (user != null && user.email == email) return user
            }
            throw e
        }
    }

    override fun createUser(email: String, password: String): User? {
        val user = service.createUser(email, password)
        storage.save(user)
        return user
    }
}