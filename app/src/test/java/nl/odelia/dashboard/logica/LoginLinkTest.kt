package nl.odelia.dashboard.logica

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginLinkTest {
    @Test
    fun goedeLinkGeeftVerversSleutel() {
        val u = LoginLink.lees("access_token=eyJhbGc.xx.yy&expires_at=1795340000&expires_in=3600&refresh_token=abc123XYZ&token_type=bearer&type=magiclink")
        assertEquals(LoginLink.Uitkomst.Ok("abc123XYZ"), u)
    }

    @Test
    fun verlopenLinkGeeftUitleg() {
        val u = LoginLink.lees("#error=access_denied&error_code=otp_expired&error_description=Email+link+is+invalid+or+has+expired")
        assertTrue(u is LoginLink.Uitkomst.Fout && "verlopen" in u.bericht)
    }

    @Test
    fun legeOfRareLinkIsFout() {
        assertTrue(LoginLink.lees(null) is LoginLink.Uitkomst.Fout)
        assertTrue(LoginLink.lees("refresh_token=") is LoginLink.Uitkomst.Fout)
        assertTrue(LoginLink.lees("refresh_token=a b") is LoginLink.Uitkomst.Fout)
        assertTrue(LoginLink.lees("refresh_token=%zz") is LoginLink.Uitkomst.Fout)
    }
}
