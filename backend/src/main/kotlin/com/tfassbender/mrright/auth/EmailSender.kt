package com.tfassbender.mrright.auth

import io.quarkus.mailer.Mail
import io.quarkus.mailer.Mailer
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty

/** Sends the auth emails. An interface so [AuthService] can be unit-tested without a mailer. */
interface EmailSender {
    fun sendVerification(email: String, displayName: String, rawToken: String)

    fun sendPasswordReset(email: String, displayName: String, rawToken: String)
}

/** Sends plain-text mails via the Quarkus [Mailer]; the raw token travels as `?token=` on the SPA link. */
@ApplicationScoped
class MailerEmailSender(
    private val mailer: Mailer,
    @param:ConfigProperty(name = "mrright.auth.email-verification.link-base-url")
    private val verificationBaseUrl: String,
    @param:ConfigProperty(name = "mrright.auth.password-reset.link-base-url")
    private val resetBaseUrl: String,
) : EmailSender {

    override fun sendVerification(email: String, displayName: String, rawToken: String) {
        val body = """
            Welcome to Mr Right, $displayName!

            Please confirm your email address by opening the link below:
            $verificationBaseUrl?token=$rawToken

            If you did not create this account you can safely ignore this email.
        """.trimIndent()
        mailer.send(Mail.withText(email, "Verify your Mr Right account", body))
    }

    override fun sendPasswordReset(email: String, displayName: String, rawToken: String) {
        val body = """
            Hi $displayName,

            We received a request to reset your Mr Right password.
            Open the link below to choose a new one:
            $resetBaseUrl?token=$rawToken

            If you did not request this, you can safely ignore this email.
        """.trimIndent()
        mailer.send(Mail.withText(email, "Reset your Mr Right password", body))
    }
}
