package com.tfassbender.mrright.auth

import com.tfassbender.mrright.storage.DataDirectory
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces

/** Turns the plain [UserRepository] into a bean; the class itself stays unit-testable against a temp dir. */
@ApplicationScoped
class AuthProducers {

    @Produces
    @ApplicationScoped
    fun userRepository(dataDirectory: DataDirectory): UserRepository = UserRepository(dataDirectory.path)
}
